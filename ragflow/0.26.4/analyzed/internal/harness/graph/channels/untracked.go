package channels

// untracked.go — 非检查点通道：存储值但不参与 checkpoint 持久化，恢复后丢失。


import (
	"ragflow/internal/harness/graph/errors"
)

// UntrackedValue 存储值但不纳入检查点追踪；
// 从检查点恢复图时该值会丢失。
type UntrackedValue struct {
	BaseChannel
	value interface{}
}

// NewUntrackedValue 创建非追踪值通道，初始为 Missing。
func NewUntrackedValue(typ interface{}) *UntrackedValue {
	return &UntrackedValue{
		BaseChannel: BaseChannel{Typ: typ},
		value:       Missing,
	}
}

// Get 返回当前值；空通道返回 EmptyChannelError。
func (c *UntrackedValue) Get() (interface{}, error) {
	if IsMissing(c.value) {
		return nil, &errors.EmptyChannelError{}
	}
	return c.value, nil
}

// IsAvailable 判断通道是否已有有效值。
func (c *UntrackedValue) IsAvailable() bool {
	return !IsMissing(c.value)
}

// Update 取 values 最后一项写入通道。
func (c *UntrackedValue) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}
	c.value = values[len(values)-1]
	return true, nil
}

// Copy 复制通道结构，值不复制（新通道为空）。
func (c *UntrackedValue) Copy() Channel {
	newCh := NewUntrackedValue(c.Typ)
	newCh.Key = c.Key
	return newCh
}

// Checkpoint 始终返回 Missing，不持久化值。
func (c *UntrackedValue) Checkpoint() interface{} {
	return Missing
}

// FromCheckpoint 从检查点恢复时始终创建空通道。
func (c *UntrackedValue) FromCheckpoint(checkpoint interface{}) Channel {
	return NewUntrackedValue(c.Typ)
}

// 适用于临时中间状态，无需跨中断/恢复保留的场景。
