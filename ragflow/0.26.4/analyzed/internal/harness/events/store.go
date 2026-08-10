package events

import (
	"context"
	"slices"
	"time"
)

// store.go — 事件日志与存储接口：仅追加、过滤、快照与订阅。

// EventLog 仅追加事件日志接口，所有实现须并发安全。
type EventLog interface {
	// Append 追加一条或多条不可变事件。
	Append(ctx context.Context, events ...*Event) error

	// Stream 按逻辑时钟顺序返回匹配过滤器的事件迭代器。
	Stream(ctx context.Context, filter EventFilter) EventIterator

	// Get 按 ID 检索单个事件；未找到返回 nil, nil。
	Get(ctx context.Context, id EventID) (*Event, error)

	// Range 返回逻辑时钟在 [from, to] 内且匹配过滤器的事件。
	Range(ctx context.Context, from, to uint64, filter EventFilter) ([]*Event, error)

	// Seek 从指定逻辑时钟位置开始迭代。
	Seek(ctx context.Context, clock uint64) (EventIterator, error)

	// Length 返回日志中的事件总数。
	Length(ctx context.Context) (uint64, error)
}

// EventFilter 事件过滤条件。
type EventFilter struct {
	TraceID    string      // 按轨迹 ID 过滤
	ThreadID   string      // 按线程 ID 过滤
	Types      []EventType // 限定事件类型；空表示全部
	Node       string      // 限定图节点
	FromClock  uint64      // 最小逻辑时钟（含）
	ToClock    uint64      // 最大逻辑时钟（含）；0 表示无上限
	FromTime   time.Time   // 最小墙钟时间
	ToTime     time.Time   // 最大墙钟时间
	Limit      int         // 返回数量上限；0 表示不限
}

// Matches 判断事件是否满足本过滤器。
func (f EventFilter) Matches(e *Event) bool {
	if f.TraceID != "" && e.TraceID != f.TraceID {
		return false
	}
	if f.ThreadID != "" && e.ThreadID != f.ThreadID {
		return false
	}
	if len(f.Types) > 0 {
		if !slices.Contains(f.Types, e.Type) {
			return false
		}
	}
	if f.Node != "" && e.Node != f.Node {
		return false
	}
	if f.FromClock > 0 && e.Clock < f.FromClock {
		return false
	}
	if f.ToClock > 0 && e.Clock > f.ToClock {
		return false
	}
	if !f.FromTime.IsZero() && e.Timestamp.Before(f.FromTime) {
		return false
	}
	if !f.ToTime.IsZero() && e.Timestamp.After(f.ToTime) {
		return false
	}
	return true
}

// EventIterator 按序遍历事件的迭代器。
type EventIterator interface {
	// Next 返回下一条事件；耗尽时返回 nil, false。
	Next(ctx context.Context) (*Event, bool)
	// Close 释放迭代器持有的资源。
	Close() error
}

// Snapshot 某一时刻的事件状态快照，用于加速重放（避免从事件 0 重放）。
type Snapshot struct {
	ID        string    `json:"id"`
	TraceID   string    `json:"trace_id"`
	Clock     uint64    `json:"clock"`
	CreatedAt time.Time `json:"created_at"`
	Data      []byte    `json:"data,omitempty"`
}

// EventStore 扩展 EventLog，增加生命周期管理能力。
type EventStore interface {
	EventLog

	// CreateSnapshot 为指定轨迹创建快照。
	CreateSnapshot(ctx context.Context, traceID string) (*Snapshot, error)

	// RestoreSnapshot 加载快照并返回定位在快照时钟之后的迭代器。
	RestoreSnapshot(ctx context.Context, snapshotID string) (EventIterator, error)

	// Subscribe 返回接收匹配过滤器新事件的通道；ctx 取消时关闭。
	Subscribe(ctx context.Context, filter EventFilter) (<-chan *Event, error)

	// GC 删除早于给定保留期的事件。
	GC(ctx context.Context, retention time.Duration) error
}
