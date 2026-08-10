package events

import (
	"context"
	"encoding/json"
	"fmt"
	"sync"
	"sync/atomic"
	"time"

	"github.com/nats-io/nats.go"
	"github.com/nats-io/nats.go/jetstream"
)

// nats.go — NATS JetStream 分布式事件存储，适用于生产多实例部署。

const (
	defaultNATSPrefix    = "harness_events"
	natsEventSubject     = "events.event"
	natsSnapshotSubject  = "events.snapshot"
	defaultMaxCacheAge   = 10 * time.Minute
	defaultMaxCacheItems = 10000
)

// cachedEvent 带 TTL 过期时间的缓存事件包装。
type cachedEvent struct {
	ev        *Event
	expiresAt time.Time
}

// NATSEventStore 将事件持久化到 NATS JetStream，适合分布式生产环境。
type NATSEventStore struct {
	conn        *nats.Conn
	js          jetstream.JetStream
	stream      string // JetStream 流名称
	prefix      string // 主题前缀
	mu          sync.RWMutex
	cache       map[string]*cachedEvent // ID → 带 TTL 的本地缓存
	maxCacheAge time.Duration
	clock       atomic.Uint64
	subs        map[int64]*nats.Subscription
	subID       atomic.Int64
}

// NewNATSEventStore 创建 NATSEventStore，自动确保 JetStream 流存在。
func NewNATSEventStore(conn *nats.Conn, stream string) (*NATSEventStore, error) {
	js, err := jetstream.New(conn)
	if err != nil {
		return nil, fmt.Errorf("jetstream init: %w", err)
	}

	_, err = js.Stream(ctxForInit(), stream)
	if err != nil {
		_, err = js.CreateStream(ctxForInit(), jetstream.StreamConfig{
			Name:      stream,
			Subjects:  []string{fmt.Sprintf("%s.>", defaultNATSPrefix)},
			MaxAge:    7 * 24 * time.Hour, // 保留 7 天
			Storage:   jetstream.FileStorage,
			Retention: jetstream.LimitsPolicy,
		})
		if err != nil {
			return nil, fmt.Errorf("create jetstream stream: %w", err)
		}
	}

	return &NATSEventStore{
		conn:   conn,
		js:     js,
		stream: stream,
		prefix: defaultNATSPrefix,
		cache:  make(map[string]*cachedEvent),
		subs:   make(map[int64]*nats.Subscription),
	}, nil
}

// ctxForInit 返回 NATS 流初始化用的带超时背景上下文。
func ctxForInit() context.Context {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	_ = cancel
	return ctx
}

// Append 发布事件到 JetStream 并更新本地 TTL 缓存。
func (s *NATSEventStore) Append(ctx context.Context, events ...*Event) error {
	for _, ev := range events {
		if ev.Clock == 0 {
			ev.Clock = s.clock.Add(1)
		}
		ev.Seal()

		data, err := json.Marshal(ev)
		if err != nil {
			return fmt.Errorf("marshal event: %w", err)
		}

		subject := fmt.Sprintf("%s.%s", s.prefix, natsEventSubject)
		if _, err := s.js.Publish(ctx, subject, data); err != nil {
			return fmt.Errorf("publish event: %w", err)
		}

		s.mu.Lock()
		maxAge := s.maxCacheAge
		if maxAge == 0 {
			maxAge = defaultMaxCacheAge
		}
		s.cache[string(ev.ID)] = &cachedEvent{ev: ev, expiresAt: time.Now().Add(maxAge)}
		if len(s.cache) > defaultMaxCacheItems {
			s.evictExpiredLocked()
		}
		s.mu.Unlock()
	}
	return nil
}

// Stream 从 JetStream 重放事件并返回迭代器。
func (s *NATSEventStore) Stream(ctx context.Context, filter EventFilter) EventIterator {
	subject := fmt.Sprintf("%s.%s", s.prefix, natsEventSubject)
	consumer, err := s.js.OrderedConsumer(ctx, s.stream, jetstream.OrderedConsumerConfig{
		FilterSubjects: []string{subject},
	})
	if err != nil {
		return &natsErrorIterator{err: fmt.Errorf("create consumer: %w", err)}
	}

	return &natsEventIterator{
		consumer: consumer,
		ctx:      ctx,
		filter:   filter,
		buffer:   make([]*Event, 0),
	}
}

// Get 优先从本地缓存读取；缓存未命中则扫描流（慢路径）。
func (s *NATSEventStore) Get(ctx context.Context, id EventID) (*Event, error) {
	s.mu.RLock()
	ce, ok := s.cache[string(id)]
	s.mu.RUnlock()
	if ok && ce.expiresAt.After(time.Now()) {
		return ce.ev, nil
	}
	if ok {
		s.mu.Lock()
		delete(s.cache, string(id))
		s.mu.Unlock()
	}

	iter := s.Stream(ctx, EventFilter{Limit: 1})
	defer iter.Close()
	for {
		e, ok := iter.Next(ctx)
		if !ok {
			break
		}
		if e.ID == id {
			return e, nil
		}
	}
	return nil, nil
}

// Range 返回逻辑时钟区间内匹配过滤器的事件。
func (s *NATSEventStore) Range(ctx context.Context, from, to uint64, filter EventFilter) ([]*Event, error) {
	iter := s.Stream(ctx, filter)
	defer iter.Close()

	result := make([]*Event, 0)
	for {
		ev, ok := iter.Next(ctx)
		if !ok {
			break
		}
		if ev.Clock < from {
			continue
		}
		if to > 0 && ev.Clock > to {
			continue
		}
		result = append(result, ev)
	}
	return result, nil
}

// Seek 从指定逻辑时钟位置开始迭代。
func (s *NATSEventStore) Seek(ctx context.Context, clock uint64) (EventIterator, error) {
	return s.Stream(ctx, EventFilter{FromClock: clock}), nil
}

// Length 返回 JetStream 流中的消息总数。
func (s *NATSEventStore) Length(ctx context.Context) (uint64, error) {
	streamInfo, err := s.js.Stream(ctx, s.stream)
	if err != nil {
		return 0, fmt.Errorf("get stream info: %w", err)
	}
	return streamInfo.CachedInfo().State.Msgs, nil
}

// CreateSnapshot 收集指定轨迹的全部事件并发布快照到 JetStream。
func (s *NATSEventStore) CreateSnapshot(ctx context.Context, traceID string) (*Snapshot, error) {
	clock := s.clock.Load()

	iter := s.Stream(ctx, EventFilter{TraceID: traceID})
	defer iter.Close()

	var traceEvents []*Event
	for {
		ev, ok := iter.Next(ctx)
		if !ok {
			break
		}
		traceEvents = append(traceEvents, ev)
	}

	data, err := json.Marshal(traceEvents)
	if err != nil {
		return nil, fmt.Errorf("marshal snapshot: %w", err)
	}

	snap := &Snapshot{
		ID:        fmt.Sprintf("snap-%s-%d", traceID, clock),
		TraceID:   traceID,
		Clock:     clock,
		CreatedAt: time.Now(),
		Data:      data,
	}

	snapData, _ := json.Marshal(snap)
	subject := fmt.Sprintf("%s.%s.%s", s.prefix, natsSnapshotSubject, traceID)
	s.js.Publish(ctx, subject, snapData)

	return snap, nil
}

// RestoreSnapshot 从快照恢复；直接 Seek 到起点。
func (s *NATSEventStore) RestoreSnapshot(ctx context.Context, snapshotID string) (EventIterator, error) {
	return s.Seek(ctx, 0)
}

// Subscribe 通过 JetStream 有序消费者推送新事件。
func (s *NATSEventStore) Subscribe(ctx context.Context, filter EventFilter) (<-chan *Event, error) {
	subject := fmt.Sprintf("%s.%s", s.prefix, natsEventSubject)
	ch := make(chan *Event, 256)

	consumer, err := s.js.OrderedConsumer(ctx, s.stream, jetstream.OrderedConsumerConfig{
		FilterSubjects: []string{subject},
		DeliverPolicy:  jetstream.DeliverNewPolicy,
	})
	if err != nil {
		close(ch)
		return ch, fmt.Errorf("create consumer: %w", err)
	}

	go func() {
		defer close(ch)
		for {
			msg, err := consumer.Next()
			if err != nil {
				return
			}
			var ev Event
			if err := json.Unmarshal(msg.Data(), &ev); err != nil {
				continue
			}
			if filter.Matches(&ev) {
				select {
				case ch <- &ev:
				case <-ctx.Done():
					return
				}
			}
		}
	}()

	return ch, nil
}

// GC 更新 JetStream 流的 MaxAge 保留策略（流级 TTL 由 JetStream 管理）。
func (s *NATSEventStore) GC(ctx context.Context, retention time.Duration) error {
	info, err := s.js.Stream(ctx, s.stream)
	if err != nil {
		return err
	}
	cfg := info.CachedInfo().Config
	cfg.MaxAge = retention
	_, err = s.js.UpdateStream(ctx, cfg)
	return err
}

// evictExpiredLocked 清除已过 TTL 的缓存条目；调用方须持有 s.mu（写锁）。
func (s *NATSEventStore) evictExpiredLocked() {
	now := time.Now()
	for k, ce := range s.cache {
		if now.After(ce.expiresAt) {
			delete(s.cache, k)
		}
	}
}

// ---- natsEventIterator ----

// natsEventIterator JetStream 有序消费者事件迭代器，带批缓冲。
type natsEventIterator struct {
	consumer jetstream.Consumer
	ctx      context.Context
	filter   EventFilter
	buffer   []*Event
	bufPos   int
}

func (it *natsEventIterator) Next(_ context.Context) (*Event, bool) {
	if it.bufPos < len(it.buffer) {
		ev := it.buffer[it.bufPos]
		it.bufPos++
		return ev, true
	}
	it.buffer = it.buffer[:0]
	it.bufPos = 0

	for i := 0; i < 100; i++ {
		msg, err := it.consumer.Next()
		if err != nil {
			return nil, false
		}
		var ev Event
		if err := json.Unmarshal(msg.Data(), &ev); err != nil {
			continue
		}
		if it.filter.Matches(&ev) {
			it.buffer = append(it.buffer, &ev)
		}
	}
	if len(it.buffer) == 0 {
		return nil, false
	}
	ev := it.buffer[0]
	it.bufPos = 1
	return ev, true
}

func (it *natsEventIterator) Close() error {
	return nil
}

// ---- natsErrorIterator ----

// natsErrorIterator 创建消费者失败时的空迭代器占位。
type natsErrorIterator struct {
	err     error
	emitted bool
}

func (it *natsErrorIterator) Next(_ context.Context) (*Event, bool) {
	return nil, false
}

func (it *natsErrorIterator) Close() error {
	return nil
}
