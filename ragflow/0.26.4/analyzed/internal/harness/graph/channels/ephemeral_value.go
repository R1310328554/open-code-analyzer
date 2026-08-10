package channels

import (
	"ragflow/internal/harness/graph/errors"
)

// ephemeral_value.go — 瞬态值通道：读取一次后自动清空。

// EphemeralValue 存储读取后即清除的瞬态值。
type EphemeralValue struct {
	BaseChannel
	value interface{} // 当前值，Get 后重置为 Missing
	guard bool        // 为 true 时，未设值调用 Get 返回 EmptyChannelError
}

// NewEphemeralValue 创建 EphemeralValue 通道。
// guard 为 true 时，未设值调用 Get 会返回 EmptyChannelError。
func NewEphemeralValue(typ interface{}, guard bool) *EphemeralValue {
	return &EphemeralValue{
		BaseChannel: BaseChannel{Typ: typ},
		value:       Missing,
		guard:       guard,
	}
}

// Get 返回当前值并清空通道（一次性读取语义）。
func (c *EphemeralValue) Get() (interface{}, error) {
	if IsMissing(c.value) {
		if c.guard {
			return nil, &errors.EmptyChannelError{}
		}
		return nil, nil
	}
	val := c.value
	c.value = Missing
	return val, nil
}

// IsAvailable 通道是否持有未读值。
func (c *EphemeralValue) IsAvailable() bool {
	return !IsMissing(c.value)
}

// Update 更新通道，仅保留最后一个值。
func (c *EphemeralValue) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}
	c.value = values[len(values)-1]
	return true, nil
}

// Copy 返回通道拷贝。
func (c *EphemeralValue) Copy() Channel {
	newCh := NewEphemeralValue(c.Typ, c.guard)
	newCh.Key = c.Key
	newCh.value = c.value
	return newCh
}

// Checkpoint 返回当前值。
func (c *EphemeralValue) Checkpoint() interface{} {
	return c.value
}

// FromCheckpoint 从检查点恢复。
func (c *EphemeralValue) FromCheckpoint(checkpoint interface{}) Channel {
	newCh := NewEphemeralValue(c.Typ, c.guard)
	newCh.Key = c.Key
	if !IsMissing(checkpoint) {
		newCh.value = checkpoint
	}
	return newCh
}
