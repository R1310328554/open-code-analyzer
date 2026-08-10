package channels

import (
	"ragflow/internal/harness/graph/errors"
)

// topic.go — 发布/订阅主题通道：可累积或逐步清空消息列表。

// Topic 是可配置的 PubSub 主题通道，存储一组消息。
type Topic struct {
	BaseChannel
	values     []interface{} // 当前消息列表
	accumulate bool          // false 时每步结束后清空；true 时跨步累积
}

// NewTopic 创建 Topic 通道。
// accumulate 为 false 时，每步执行后清空消息。
func NewTopic(typ interface{}, accumulate bool) *Topic {
	return &Topic{
		BaseChannel: BaseChannel{Typ: typ},
		values:      make([]interface{}, 0),
		accumulate:  accumulate,
	}
}

// Get 返回当前所有消息的副本。
func (c *Topic) Get() (interface{}, error) {
	if len(c.values) == 0 {
		return nil, &errors.EmptyChannelError{}
	}
	result := make([]interface{}, len(c.values))
	copy(result, c.values)
	return result, nil
}

// IsAvailable 通道是否持有消息。
func (c *Topic) IsAvailable() bool {
	return len(c.values) > 0
}

// flatten 展平可能嵌套列表的输入序列。
func flatten(values []interface{}) []interface{} {
	result := make([]interface{}, 0)
	for _, v := range values {
		if list, ok := v.([]interface{}); ok {
			result = append(result, list...)
		} else {
			result = append(result, v)
		}
	}
	return result
}

// Update 追加新消息；非累积模式下先清空旧消息。
func (c *Topic) Update(values []interface{}) (bool, error) {
	updated := false
	if !c.accumulate {
		if len(c.values) > 0 {
			updated = true
		}
		c.values = make([]interface{}, 0)
	}
	flatValues := flatten(values)
	if len(flatValues) > 0 {
		updated = true
		c.values = append(c.values, flatValues...)
	}
	return updated, nil
}

// Copy 返回通道拷贝。
func (c *Topic) Copy() Channel {
	newCh := NewTopic(c.Typ, c.accumulate)
	newCh.Key = c.Key
	newCh.values = make([]interface{}, len(c.values))
	copy(newCh.values, c.values)
	return newCh
}

// Checkpoint 返回当前消息列表；空时返回 Missing。
func (c *Topic) Checkpoint() interface{} {
	if len(c.values) == 0 {
		return Missing
	}
	result := make([]interface{}, len(c.values))
	copy(result, c.values)
	return result
}

// FromCheckpoint 从检查点恢复消息列表。
func (c *Topic) FromCheckpoint(checkpoint interface{}) Channel {
	newCh := NewTopic(c.Typ, c.accumulate)
	newCh.Key = c.Key
	if !IsMissing(checkpoint) {
		if v, ok := checkpoint.([]interface{}); ok {
			newCh.values = make([]interface{}, len(v))
			copy(newCh.values, v)
		}
	}
	return newCh
}
