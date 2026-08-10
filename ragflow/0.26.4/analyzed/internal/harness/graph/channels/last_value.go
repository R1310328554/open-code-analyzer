package channels

import (
	"fmt"

	"ragflow/internal/harness/graph/errors"
)

// last_value.go — 末值通道：每步最多接收一个值，保留最后一次写入。

// LastValue 存储最后接收到的值，每步最多接受一个更新。
type LastValue struct {
	BaseChannel
	value interface{} // 当前末值
}

// NewLastValue 创建 LastValue 通道。
func NewLastValue(typ interface{}) *LastValue {
	return &LastValue{
		BaseChannel: BaseChannel{Typ: typ},
		value:       Missing,
	}
}

// Get 返回当前值；空通道返回 EmptyChannelError。
func (c *LastValue) Get() (interface{}, error) {
	if IsMissing(c.value) {
		return nil, &errors.EmptyChannelError{}
	}
	return c.value, nil
}

// IsAvailable 通道是否已有值。
func (c *LastValue) IsAvailable() bool {
	return !IsMissing(c.value)
}

// Update 用单个值更新通道；同一步骤传入多个值会报错。
func (c *LastValue) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}
	if len(values) != 1 {
		return false, &errors.InvalidUpdateError{
			Message: fmt.Sprintf("At key '%s': Can receive only one value per step. Use a reducer to handle multiple values.", c.Key),
		}
	}
	c.value = values[len(values)-1]
	return true, nil
}

// Copy 返回通道拷贝。
func (c *LastValue) Copy() Channel {
	newCh := NewLastValue(c.Typ)
	newCh.Key = c.Key
	newCh.value = c.value
	return newCh
}

// Checkpoint 返回当前值。
func (c *LastValue) Checkpoint() interface{} {
	return c.value
}

// FromCheckpoint 从检查点恢复。
func (c *LastValue) FromCheckpoint(checkpoint interface{}) Channel {
	newCh := NewLastValue(c.Typ)
	newCh.Key = c.Key
	if !IsMissing(checkpoint) {
		newCh.value = checkpoint
	}
	return newCh
}
