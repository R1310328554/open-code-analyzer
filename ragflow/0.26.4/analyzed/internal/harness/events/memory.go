package events

import (
	"context"
	"encoding/json"
	"fmt"
	"sync"
	"sync/atomic"
	"time"
)

// memory.go — 内存事件存储：适用于测试与单实例开发，进程重启后数据丢失。

// MemoryEventStore 内存实现的 EventStore，进程重启后事件全部丢失。
type MemoryEventStore struct {
	mu     sync.RWMutex
	events []*Event
	byID   map[EventID]*Event
	clock  atomic.Uint64
	subs   map[int64]chan *Event // 订阅者通道
	subID  atomic.Int64
}

// NewMemoryEventStore 创建空的内存事件存储。
func NewMemoryEventStore() *MemoryEventStore {
	return &MemoryEventStore{
		events: make([]*Event, 0, 1024),
		byID:   make(map[EventID]*Event),
		subs:   make(map[int64]chan *Event),
	}
}

// Append 追加事件并分发给订阅者。
func (s *MemoryEventStore) Append(ctx context.Context, events ...*Event) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	for _, ev := range events {
		if ev.Clock == 0 {
			ev.Clock = s.clock.Add(1)
		}
		ev.Seal()
		s.events = append(s.events, ev)
		s.byID[ev.ID] = ev

		// 非阻塞分发给订阅者；慢订阅者丢弃事件
		for id, ch := range s.subs {
			select {
			case ch <- ev:
			default:
			}
			_ = id
		}
	}
	return nil
}

// Stream 返回匹配过滤器的事件迭代器。
func (s *MemoryEventStore) Stream(ctx context.Context, filter EventFilter) EventIterator {
	s.mu.RLock()
	defer s.mu.RUnlock()

	filtered := make([]*Event, 0)
	for _, ev := range s.events {
		if filter.Matches(ev) {
			filtered = append(filtered, ev)
		}
	}
	return &sliceIterator{events: filtered, pos: 0}
}

// Get 按 ID 检索事件。
func (s *MemoryEventStore) Get(ctx context.Context, id EventID) (*Event, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	ev, ok := s.byID[id]
	if !ok {
		return nil, nil
	}
	return ev, nil
}

// Range 返回逻辑时钟区间内匹配过滤器的事件。
func (s *MemoryEventStore) Range(ctx context.Context, from, to uint64, filter EventFilter) ([]*Event, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	result := make([]*Event, 0)
	for _, ev := range s.events {
		if ev.Clock < from {
			continue
		}
		if to > 0 && ev.Clock > to {
			continue
		}
		if filter.Matches(ev) {
			result = append(result, ev)
		}
	}
	return result, nil
}

// Seek 从指定逻辑时钟位置开始迭代。
func (s *MemoryEventStore) Seek(ctx context.Context, clock uint64) (EventIterator, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	pos := 0
	for i, ev := range s.events {
		if ev.Clock >= clock {
			pos = i
			break
		}
		_ = i
	}
	return &sliceIterator{events: s.events[pos:], pos: 0}, nil
}

// Length 返回事件总数。
func (s *MemoryEventStore) Length(ctx context.Context) (uint64, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return uint64(len(s.events)), nil
}

// CreateSnapshot 序列化当前全部事件为快照。
func (s *MemoryEventStore) CreateSnapshot(ctx context.Context, traceID string) (*Snapshot, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	clock := s.clock.Load()
	data, err := json.Marshal(s.events)
	if err != nil {
		return nil, fmt.Errorf("marshal snapshot: %w", err)
	}
	return &Snapshot{
		ID:        fmt.Sprintf("snap-%s-%d", traceID, clock),
		TraceID:   traceID,
		Clock:     clock,
		CreatedAt: time.Now(),
		Data:      data,
	}, nil
}

// RestoreSnapshot 从快照恢复；内存存储中事件仍在，直接 Seek 到起点。
func (s *MemoryEventStore) RestoreSnapshot(ctx context.Context, snapshotID string) (EventIterator, error) {
	return s.Seek(ctx, 0)
}

// Subscribe 注册实时事件订阅；ctx 取消时清理并关闭通道。
func (s *MemoryEventStore) Subscribe(ctx context.Context, filter EventFilter) (<-chan *Event, error) {
	ch := make(chan *Event, 256)
	id := s.subID.Add(1)

	s.mu.Lock()
	s.subs[id] = ch
	s.mu.Unlock()

	go func() {
		<-ctx.Done()
		s.mu.Lock()
		delete(s.subs, id)
		s.mu.Unlock()
		close(ch)
	}()

	return ch, nil
}

// GC 删除早于保留期的事件。
func (s *MemoryEventStore) GC(ctx context.Context, retention time.Duration) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	cutoff := time.Now().Add(-retention)
	keep := make([]*Event, 0, len(s.events))
	for _, ev := range s.events {
		if ev.Timestamp.After(cutoff) {
			keep = append(keep, ev)
		} else {
			delete(s.byID, ev.ID)
		}
	}
	s.events = keep
	return nil
}

// ---- sliceIterator ----

// sliceIterator 基于切片的事件迭代器。
type sliceIterator struct {
	events []*Event
	pos    int
}

func (it *sliceIterator) Next(_ context.Context) (*Event, bool) {
	if it.pos >= len(it.events) {
		return nil, false
	}
	ev := it.events[it.pos]
	it.pos++
	return ev, true
}

func (it *sliceIterator) Close() error {
	it.events = nil
	return nil
}
