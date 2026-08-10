package client

// Promtail 远程写入管理器：实例化多 client、可选 WAL watcher，fan-out 或 WAL 回放路径。
// WriterEventsNotifier 将 WAL 段清理/写入事件订阅到 clientWriteTo 与 watcher。

import (
	"fmt"
	"strings"
	"sync"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/limit"
	"github.com/grafana/loki/v3/clients/pkg/promtail/wal"
)

// WAL 写入/清理事件通知接口，Manager 构造时向 watcher 注册订阅者。
// WriterEventsNotifier implements a notifier that's received by the Manager, to which wal.Watcher can subscribe for
// writer events.
type WriterEventsNotifier interface {
	SubscribeCleanup(subscriber wal.CleanupEventSubscriber)
	SubscribeWrite(subscriber wal.WriteEventSubscriber)
}

var (
// 空实现 Notifier，WAL 未启用时避免 nil 判断。
	// NilNotifier is a no-op WriterEventsNotifier.
	NilNotifier = nilNotifier{}
)

// SubscribeCleanup/SubscribeWrite 均为空操作。
// nilNotifier implements WriterEventsNotifier with no-ops callbacks.
type nilNotifier struct{}

func (n nilNotifier) SubscribeCleanup(_ wal.CleanupEventSubscriber) {}

func (n nilNotifier) SubscribeWrite(_ wal.WriteEventSubscriber) {}

// 仅需 Stop 的可停止组件，WAL watcher 等统一生命周期管理。
type Stoppable interface {
	Stop()
}

// 管理多 Client 与 WAL watcher：entries channel 入口，WAL 模式消费丢弃或 fan-out。
// Manager manages remote write client instantiation, and connects the related components to orchestrate the flow of api.Entry
// from the scrape targets, to the remote write clients themselves.
//
// Right now it just supports instantiating the WAL writer side of the future-to-be WAL enabled client. In follow-up
// work, tracked in https://github.com/grafana/loki/issues/8197, this Manager will be responsible for instantiating all client
// types: Logger, Multi and WAL.
type Manager struct {
	name        string
	clients     []Client
	walWatchers []Stoppable

	entries chan api.Entry
	once    sync.Once

	wg sync.WaitGroup
}

// 按配置创建各 client、可选 WAL watcher 并启动 consume 或 forward 例程。
// NewManager creates a new Manager
func NewManager(metrics *Metrics, logger log.Logger, limits limit.Config, reg prometheus.Registerer, walCfg wal.Config, notifier WriterEventsNotifier, clientCfgs ...Config) (*Manager, error) {
	var fake struct{}

	watcherMetrics := wal.NewWatcherMetrics(reg)

	if len(clientCfgs) == 0 {
		return nil, fmt.Errorf("at least one client config must be provided")
	}

	clientsCheck := make(map[string]struct{})
	clients := make([]Client, 0, len(clientCfgs))
	watchers := make([]Stoppable, 0, len(clientCfgs))
	for _, cfg := range clientCfgs {
		client, err := New(metrics, cfg, limits.MaxStreams, limits.MaxLineSize.Val(), limits.MaxLineSizeTruncate, logger)
		if err != nil {
			return nil, err
		}

		// Don't allow duplicate clients, we have client specific metrics that need at least one unique label value (name).
		if _, ok := clientsCheck[client.Name()]; ok {
			return nil, fmt.Errorf("duplicate client configs are not allowed, found duplicate for name: %s", cfg.Name)
		}

		clientsCheck[client.Name()] = fake
		clients = append(clients, client)

		if walCfg.Enabled {
			// Create and launch wal watcher for this client

			// add some context information for the logger the watcher uses
			wlog := log.With(logger, "client", client.Name())

			writeTo := newClientWriteTo(client.Chan(), wlog)
			// subscribe watcher's wal.WriteTo to writer events. This will make the writer trigger the cleanup of the wal.WriteTo
			// series cache whenever a segment is deleted.
			notifier.SubscribeCleanup(writeTo)

			watcher := wal.NewWatcher(walCfg.Dir, client.Name(), watcherMetrics, writeTo, wlog, walCfg.WatchConfig)
			// subscribe watcher to wal write events
			notifier.SubscribeWrite(watcher)

			level.Debug(logger).Log("msg", "starting WAL watcher for client", "client", client.Name())
			watcher.Start()

			watchers = append(watchers, watcher)
		}
	}
	manager := &Manager{
		clients:     clients,
		walWatchers: watchers,
		entries:     make(chan api.Entry),
	}
	if walCfg.Enabled {
		manager.name = "wal"
		manager.startWithConsume()
	} else {
		manager.name = "multi"
		manager.startWithForward()
	}
	return manager, nil
}

// WAL 启用时丢弃 pipeline 直送条目，实际数据由 WAL 回放经 clientWriteTo 注入。
// startWithConsume starts the main manager routine, which reads and discards entries from the exposed channel.
// This is necessary since to treat the WAL-enabled manager the same way as the WAL-disabled one, the processing pipeline
// send entries both to the WAL writer, and the channel exposed by the manager. In the case the WAL is enabled, these entries
// are not used since they are read from the WAL, so we need a routine to just read the entries received through the channel
// and discarding them, to not block the sending side.
func (m *Manager) startWithConsume() {
	m.wg.Add(1)
	go func() {
		defer m.wg.Done()
		// discard read entries
		//nolint:revive
		for range m.entries {
		}
	}()
}

// 无 WAL 时将 Manager channel 条目 fan-out 到每个 client 的 Chan。
// startWithForward starts the main manager routine, which reads entries from the exposed channel, and forwards them
// doing a fan-out across all inner clients.
func (m *Manager) startWithForward() {
	m.wg.Add(1)
	go func() {
		defer m.wg.Done()
		for e := range m.entries {
			for _, c := range m.clients {
				c.Chan() <- e
			}
		}
	}()
}

func (m *Manager) StopNow() {
	for _, c := range m.clients {
		c.StopNow()
	}
}

func (m *Manager) Name() string {
	var sb strings.Builder
	sb.WriteString(m.name)
	sb.WriteString(":")
	for i, c := range m.clients {
		sb.WriteString(c.Name())
		if i != len(m.clients)-1 {
			sb.WriteString(",")
		}
	}
	return sb.String()
}

func (m *Manager) Chan() chan<- api.Entry {
	return m.entries
}

// 关闭 entries、等待 forward/consume、停止 watcher 再 Stop 各 client。
func (m *Manager) Stop() {
	// first stop the receiving channel
	m.once.Do(func() { close(m.entries) })
	m.wg.Wait()
	// close wal watchers
	for _, walWatcher := range m.walWatchers {
		walWatcher.Stop()
	}
	// close clients
	for _, c := range m.clients {
		c.Stop()
	}
}
