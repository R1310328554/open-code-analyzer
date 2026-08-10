// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Manager 是 Prometheus 告警通知总控：维护多个 alertmanagerSet、消费 SD 更新并将规则告警分发至 Alertmanager。

package notifier

import (
	"context"
	"fmt"
	"log/slog"
	"net/http"
	"net/url"
	"sync"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/common/model"
	"github.com/prometheus/common/promslog"
	"github.com/prometheus/common/version"

	"github.com/prometheus/prometheus/config"
	"github.com/prometheus/prometheus/discovery/targetgroup"
	"github.com/prometheus/prometheus/model/labels"
	"github.com/prometheus/prometheus/model/relabel"
)

const (
// DefaultMaxBatchSize 为单次 HTTP POST 告警条数上限（默认 256）。
	// DefaultMaxBatchSize is the default maximum number of alerts to send in a single request to the alertmanager.
	DefaultMaxBatchSize = 256

	contentTypeJSON = "application/json"
)

// namespace/subsystem 常量供 notifications 指标命名。
// String constants for instrumentation.
const (
	namespace         = "prometheus"
	subsystem         = "notifications"
	alertmanagerLabel = "alertmanager"
)

var userAgent = version.PrometheusUserAgent()

// Manager 协调配置应用、目标同步与 Send 入队。
// Manager is responsible for dispatching alert notifications to an
// alert manager service.
type Manager struct {
	opts *Options

	metrics *alertMetrics

	mtx sync.RWMutex

	stopOnce      *sync.Once
	stopRequested chan struct{}

	alertmanagers map[string]*alertmanagerSet
	logger        *slog.Logger
}

// Options 定义队列容量、shutdown 排水、external labels 与 HTTP Do 注入等。
// Options are the configurable parameters of a Handler.
type Options struct {
	QueueCapacity   int
	DrainOnShutdown bool
	ExternalLabels  labels.Labels
	RelabelConfigs  []*relabel.Config
	// Used for sending HTTP requests to the Alertmanager.
	Do func(ctx context.Context, client *http.Client, req *http.Request) (*http.Response, error)

	Registerer prometheus.Registerer

	// MaxBatchSize determines the maximum number of alerts to send in a single request to the alertmanager.
	// MaxBatchSize 限制 sendLoop 每批 POST 的告警数量。
	MaxBatchSize int
}

func do(ctx context.Context, client *http.Client, req *http.Request) (*http.Response, error) {
	if client == nil {
		client = http.DefaultClient
	}
	return client.Do(req.WithContext(ctx))
}

// NewManager 补全默认 Do/MaxBatchSize/logger 并注册告警指标。
// NewManager is the manager constructor.
func NewManager(o *Options, nameValidationScheme model.ValidationScheme, logger *slog.Logger) *Manager {
	if o.Do == nil {
		o.Do = do
	}
	// Set default MaxBatchSize if not provided.
	if o.MaxBatchSize <= 0 {
		o.MaxBatchSize = DefaultMaxBatchSize
	}
	if logger == nil {
		logger = promslog.NewNopLogger()
	}

	for _, rc := range o.RelabelConfigs {
		switch rc.NameValidationScheme {
		case model.LegacyValidation, model.UTF8Validation:
		default:
			rc.NameValidationScheme = nameValidationScheme
		}
	}

	n := &Manager{
		stopRequested: make(chan struct{}),
		stopOnce:      &sync.Once{},
		opts:          o,
		logger:        logger,
	}

	alertmanagersDiscoveredFunc := func() float64 { return float64(len(n.Alertmanagers())) }

	n.metrics = newAlertMetrics(o.Registerer, alertmanagersDiscoveredFunc)
	n.metrics.queueCapacity.Set(float64(o.QueueCapacity))

	return n
}

// ApplyConfig 刷新 external/relabel 配置并按 hash 复用或重建 alertmanagerSet。
// ApplyConfig updates the status state as the new config requires.
func (n *Manager) ApplyConfig(conf *config.Config) error {
	n.mtx.Lock()
	defer n.mtx.Unlock()

	n.opts.ExternalLabels = conf.GlobalConfig.ExternalLabels
	n.opts.RelabelConfigs = conf.AlertingConfig.AlertRelabelConfigs
	for i, rc := range n.opts.RelabelConfigs {
		switch rc.NameValidationScheme {
		case model.LegacyValidation, model.UTF8Validation:
		default:
			n.opts.RelabelConfigs[i].NameValidationScheme = conf.GlobalConfig.MetricNameValidationScheme
		}
	}

	amSets := make(map[string]*alertmanagerSet)
// 按配置哈希映射旧 set，避免 SD 未更新时丢弃已知端点与 sendLoop。
	// configToAlertmanagers maps alertmanager sets for each unique AlertmanagerConfig,
	// helping to avoid dropping known alertmanagers and re-use them without waiting for SD updates when applying the config.
	configToAlertmanagers := make(map[string]*alertmanagerSet, len(n.alertmanagers))
	for _, oldAmSet := range n.alertmanagers {
		hash, err := oldAmSet.configHash()
		if err != nil {
			return err
		}
		configToAlertmanagers[hash] = oldAmSet
	}

	for k, cfg := range conf.AlertingConfig.AlertmanagerConfigs.ToMap() {
		ams, err := newAlertmanagerSet(cfg, n.opts, n.logger, n.metrics)
		if err != nil {
			return err
		}

		hash, err := ams.configHash()
		if err != nil {
			return err
		}

		if oldAmSet, ok := configToAlertmanagers[hash]; ok {
			ams.ams = oldAmSet.ams
			ams.droppedAms = oldAmSet.droppedAms
// 相同 hash 仅首个新 set 接管 sendLoops，防止多 set 共享可变 map。
			// Only transfer sendLoops to the first new config with this hash.
			// Subsequent configs with the same hash should not share the sendLoops
			// map reference, as that would cause shared mutable state between
			// alertmanagerSets (cleanup in one would affect the other).
			oldAmSet.mtx.Lock()
			if oldAmSet.sendLoops != nil {
				ams.mtx.Lock()
				ams.sendLoops = oldAmSet.sendLoops
				oldAmSet.sendLoops = nil
				ams.mtx.Unlock()
			}
			oldAmSet.mtx.Unlock()
		}

		amSets[k] = ams
	}

// 清理未被接管的旧 sendLoop（配置删除或 hash 变更）。
	// Clean up sendLoops that weren't transferred to new config.
	// This happens when: (1) key was removed, or (2) key exists but hash changed.
	// After the transfer loop above, any oldAmSet with non-nil sendLoops
	// had its sendLoops NOT transferred (since we set it to nil on transfer).
	for _, oldAmSet := range n.alertmanagers {
		oldAmSet.mtx.Lock()
		if oldAmSet.sendLoops != nil {
			oldAmSet.cleanSendLoops(oldAmSet.ams...)
		}
		oldAmSet.mtx.Unlock()
	}

	n.alertmanagers = amSets

	return nil
}

// Run 阻塞于 target 更新循环，退出前清理全部 sendLoop。
// Run dispatches notifications continuously, returning once Stop has been called and all
// pending notifications have been drained from the queue (if draining is enabled).
//
// Dispatching of notifications occurs in parallel to processing target updates to avoid one starving the other.
// Refer to https://github.com/prometheus/prometheus/issues/13676 for more details.
func (n *Manager) Run(tsets <-chan map[string][]*targetgroup.Group) {
	n.targetUpdateLoop(tsets)

	n.mtx.Lock()
	defer n.mtx.Unlock()
	for _, ams := range n.alertmanagers {
		ams.mtx.Lock()
		ams.cleanSendLoops(ams.ams...)
		ams.mtx.Unlock()
	}
}

// targetUpdateLoop 监听 SD 通道，stop 优先于继续处理更新。
// targetUpdateLoop receives updates of target groups and triggers a reload.
func (n *Manager) targetUpdateLoop(tsets <-chan map[string][]*targetgroup.Group) {
	for {
		// If we've been asked to stop, that takes priority over processing any further target group updates.
		select {
		case <-n.stopRequested:
			return
		default:
			select {
			case <-n.stopRequested:
				return
			case ts, ok := <-tsets:
				if !ok {
					break
				}
				n.reload(ts)
			}
		}
	}
}

// reload 按 alerting 配置 id 将 target group 同步到对应 alertmanagerSet。
func (n *Manager) reload(tgs map[string][]*targetgroup.Group) {
	n.mtx.Lock()
	defer n.mtx.Unlock()

	for id, tgroup := range tgs {
		am, ok := n.alertmanagers[id]
		if !ok {
			n.logger.Error("couldn't sync alert manager set", "err", fmt.Sprintf("invalid id:%v", id))
			continue
		}
		am.sync(tgroup)
	}
}

// Send 对告警做全局 relabel 后广播到全部 alertmanagerSet。
// Send queues the given notification requests for processing.
// Panics if called on a handler that is not running.
func (n *Manager) Send(alerts ...*Alert) {
	// If we've been asked to stop, that takes priority over accepting new alerts.
	select {
	case <-n.stopRequested:
		return
	default:
	}

	n.mtx.RLock()
	defer n.mtx.RUnlock()

	alerts = relabelAlerts(n.opts.RelabelConfigs, n.opts.ExternalLabels, alerts)
	if len(alerts) == 0 {
		return
	}

	for _, ams := range n.alertmanagers {
		ams.send(alerts...)
	}
}

// Alertmanagers 返回当前活跃 AM 端点 URL 列表（供 UI/API）。
// Alertmanagers returns a slice of Alertmanager URLs.
func (n *Manager) Alertmanagers() []*url.URL {
	n.mtx.RLock()
	amSets := n.alertmanagers
	n.mtx.RUnlock()

	var res []*url.URL

	for _, ams := range amSets {
		ams.mtx.RLock()
		for _, am := range ams.ams {
			res = append(res, am.url())
		}
		ams.mtx.RUnlock()
	}

	return res
}

// DroppedAlertmanagers 返回 relabel 丢弃的 AM 端点 URL。
// DroppedAlertmanagers returns a slice of Alertmanager URLs.
func (n *Manager) DroppedAlertmanagers() []*url.URL {
	n.mtx.RLock()
	amSets := n.alertmanagers
	n.mtx.RUnlock()

	var res []*url.URL

	for _, ams := range amSets {
		ams.mtx.RLock()
		for _, dam := range ams.droppedAms {
			res = append(res, dam.url())
		}
		ams.mtx.RUnlock()
	}

	return res
}

// Stop 关闭 stopRequested，Run 侧循环随后退出；可多次调用。
// Stop signals the notification manager to shut down and immediately returns.
//
// Run will return once the notification manager has successfully shut down.
//
// The manager will optionally drain send loops before shutting down.
//
// Stop is safe to call multiple times.
func (n *Manager) Stop() {
	n.logger.Info("Stopping notification manager...")

	n.stopOnce.Do(func() {
		close(n.stopRequested)
	})
}
