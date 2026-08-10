package channels

import (
	"ragflow/internal/harness/graph/errors"
)

// any_value.go — 任意值通道：接收任意更新并保留最后一个值。

// AnyValue 存储接收到的任意值，新值会覆盖旧值。
type AnyValue struct {
	BaseChannel
	value interface{} // 当前存储的值，Missing 表示尚未写入
}

// NewAnyValue 创建 AnyValue 通道。
func NewAnyValue(typ interface{}) *AnyValue {
	return &AnyValue{
		BaseChannel: BaseChannel{Typ: typ},
		value:       Missing,
	}
}

// Get 返回通道当前值；空通道返回 EmptyChannelError。
func (c *AnyValue) Get() (interface{}, error) {
	if IsMissing(c.value) {
		return nil, &errors.EmptyChannelError{}
	}
	return c.value, nil
}

// IsAvailable 通道是否已有可读值。
func (c *AnyValue) IsAvailable() bool {
	return !IsMissing(c.value)
}

// Update 用 values 更新通道；可接收多个值，仅保留最后一个。
func (c *AnyValue) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}
	c.value = values[len(values)-1]
	return true, nil
}

// Copy 返回通道的浅拷贝。
func (c *AnyValue) Copy() Channel {
	newCh := NewAnyValue(c.Typ)
	newCh.Key = c.Key
	newCh.value = c.value
	return newCh
}

// Checkpoint 返回当前值供序列化检查点。
func (c *AnyValue) Checkpoint() interface{} {
	return c.value
}

// FromCheckpoint 从检查点恢复通道状态。
func (c *AnyValue) FromCheckpoint(checkpoint interface{}) Channel {
	newCh := NewAnyValue(c.Typ)
	newCh.Key = c.Key
	if !IsMissing(checkpoint) {
		newCh.value = checkpoint
	}
	return newCh
}
