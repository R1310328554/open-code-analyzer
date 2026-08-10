package events

import (
	"sync/atomic"
)

// clock.go — 进程内单调逻辑时钟，为事件提供全序排序。

// LogicalClock 是单调递增的逻辑时钟，为进程内事件提供全序关系，并发安全。
type LogicalClock struct {
	value atomic.Uint64 // 当前时钟值，原子读写
}

// NewLogicalClock 创建从 0 开始的逻辑时钟。
func NewLogicalClock() *LogicalClock {
	return &LogicalClock{}
}

// Tick 原子递增时钟并返回新值。
func (c *LogicalClock) Tick() uint64 {
	return c.value.Add(1)
}

// Now 返回当前时钟值，不递增。
func (c *LogicalClock) Now() uint64 {
	return c.value.Load()
}

// Reset 将时钟重置为零。仅在启动完全独立的执行上下文时使用。
func (c *LogicalClock) Reset() {
	c.value.Store(0)
}
