package util //nolint:revive

// util 包 EvictingQueue 实现带容量上限的 FIFO 队列：满时驱逐最旧元素并触发 onEvict 回调，适用于有界内存缓存。

import (
	"errors"
	"sync"
)

type EvictingQueue struct {
	sync.RWMutex

	capacity int
	entries  []interface{}
	onEvict  func()
}

func NewEvictingQueue(capacity int, onEvict func()) (*EvictingQueue, error) {
	if err := validateCapacity(capacity); err != nil {
		return nil, err
	}

	queue := &EvictingQueue{
		onEvict: onEvict,
		entries: make([]interface{}, 0, capacity),
	}

	err := queue.SetCapacity(capacity)
	if err != nil {
		return nil, err
	}

	return queue, nil
}

// Append 在已达容量时先 evictOldest 再追加，保证长度不超过 capacity。
func (q *EvictingQueue) Append(entry interface{}) {
	q.Lock()
	defer q.Unlock()

	if len(q.entries) >= q.capacity {
		q.evictOldest()
	}

	q.entries = append(q.entries, entry)
}

func (q *EvictingQueue) evictOldest() {
	q.onEvict()

	start := (len(q.entries) - q.Capacity()) + 1
	q.entries = append(q.entries[:0], q.entries[start:]...)
}

func (q *EvictingQueue) Entries() []interface{} {
	q.RLock()
	defer q.RUnlock()

	return q.entries
}

func (q *EvictingQueue) Length() int {
	q.RLock()
	defer q.RUnlock()

	return len(q.entries)
}

func (q *EvictingQueue) Capacity() int {
	return q.capacity
}

func (q *EvictingQueue) SetCapacity(capacity int) error {
	if err := validateCapacity(capacity); err != nil {
		return err
	}

	q.capacity = capacity
	return nil
}

func (q *EvictingQueue) Clear() {
	q.Lock()
	defer q.Unlock()

	q.entries = q.entries[:0]
}

// validateCapacity 拒绝零或负容量，防止无界或语义不明的队列配置。
func validateCapacity(capacity int) error {
	if capacity <= 0 {
		// a queue of 0 (or smaller) capacity is invalid
		return errors.New("queue cannot have a zero or negative capacity")
	}

	return nil
}
// Entries 返回内部 slice 引用，调用方应在锁保护下只读访问或自行拷贝。
