package channels

import (
	"fmt"
	"sync"

	"ragflow/internal/harness/graph/errors"
)

// barrier.go — 命名屏障通道：等待指定名称全部到达后才可用，对齐 Python LangGraph 语义。

// NamedBarrierValue 等待所有指定名称的值均收到后才变为可用。
type NamedBarrierValue struct {
	BaseChannel
	names map[string]bool // 期望收到的名称集合
	seen  map[string]bool // 已收到的名称集合
	mu    sync.RWMutex
}

// NewNamedBarrierValue 创建命名屏障通道；waitFor 为期望名称列表。
func NewNamedBarrierValue(typ interface{}, waitFor []string) *NamedBarrierValue {
	names := make(map[string]bool)
	for _, name := range waitFor {
		names[name] = true
	}
	return &NamedBarrierValue{
		BaseChannel: BaseChannel{Typ: typ},
		names:       names,
		seen:        make(map[string]bool),
	}
}

// Get 全部名称到齐后返回 nil；否则 EmptyChannelError。
func (c *NamedBarrierValue) Get() (interface{}, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	if !c.namesMatchSeen() {
		return nil, &errors.EmptyChannelError{}
	}
	return nil, nil
}

// IsAvailable 是否已收齐全部期望名称。
func (c *NamedBarrierValue) IsAvailable() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.namesMatchSeen()
}

// Update 接收字符串名称更新；名称须在期望集合内。
func (c *NamedBarrierValue) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}

	c.mu.Lock()
	defer c.mu.Unlock()

	updated := false
	for _, val := range values {
		name, ok := val.(string)
		if !ok {
			return false, &errors.InvalidUpdateError{
				Message: fmt.Sprintf("value must be a string, got %T", val),
			}
		}

		if _, exists := c.names[name]; exists {
			if !c.seen[name] {
				c.seen[name] = true
				updated = true
			}
		} else {
			return false, &errors.InvalidUpdateError{
				Message: fmt.Sprintf("value '%s' not in expected names %v", name, c.names),
			}
		}
	}

	return updated, nil
}

// Copy 返回通道拷贝。
func (c *NamedBarrierValue) Copy() Channel {
	c.mu.RLock()
	defer c.mu.RUnlock()

	newCh := NewNamedBarrierValue(c.Typ, nil)
	newCh.Key = c.Key

	newCh.names = make(map[string]bool, len(c.names))
	for k, v := range c.names {
		newCh.names[k] = v
	}

	newCh.seen = make(map[string]bool, len(c.seen))
	for k, v := range c.seen {
		newCh.seen[k] = v
	}

	return newCh
}

// Checkpoint 返回已收到名称的映射副本。
func (c *NamedBarrierValue) Checkpoint() interface{} {
	c.mu.RLock()
	defer c.mu.RUnlock()

	if len(c.seen) == 0 {
		return Missing
	}

	result := make(map[string]bool, len(c.seen))
	for k, v := range c.seen {
		result[k] = v
	}
	return result
}

// FromCheckpoint 从检查点恢复；JSON 往返后 map[string]bool 可能变为 map[string]interface{}。
func (c *NamedBarrierValue) FromCheckpoint(checkpoint interface{}) Channel {
	c.mu.Lock()
	defer c.mu.Unlock()

	newCh := NewNamedBarrierValue(c.Typ, nil)
	newCh.Key = c.Key
	newCh.names = make(map[string]bool, len(c.names))
	for k, v := range c.names {
		newCh.names[k] = v
	}

	if checkpoint != nil && !IsMissing(checkpoint) {
		newCh.seen = make(map[string]bool)
		switch v := checkpoint.(type) {
		case map[string]bool:
			for k, bv := range v {
				newCh.seen[k] = bv
			}
		case map[string]interface{}:
			for k, bv := range v {
				if b, ok := bv.(bool); ok {
					newCh.seen[k] = b
				}
			}
		}
	}

	return newCh
}

// Finish 全部到齐后清空 seen；返回是否有内容被清除。
func (c *NamedBarrierValue) Finish() bool {
	c.mu.Lock()
	defer c.mu.Unlock()

	if len(c.seen) > 0 {
		c.seen = make(map[string]bool)
		return true
	}
	return false
}

// Consume 全部到齐后重置 seen；返回是否消费成功。
func (c *NamedBarrierValue) Consume() bool {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.namesMatchSeen() && len(c.seen) > 0 {
		c.seen = make(map[string]bool)
		return true
	}
	return false
}

// namesMatchSeen 检查是否所有期望名称均已收到。
func (c *NamedBarrierValue) namesMatchSeen() bool {
	if len(c.names) != len(c.seen) {
		return false
	}
	for name := range c.names {
		if !c.seen[name] {
			return false
		}
	}
	return true
}

// NamedBarrierValueAfterFinish 等待全部名称到齐，且须调用 Finish 后才可用。
type NamedBarrierValueAfterFinish struct {
	BaseChannel
	names    map[string]bool
	seen     map[string]bool
	finished bool
	mu       sync.RWMutex
}

// NewNamedBarrierValueAfterFinish 创建须 Finish 后才可用的命名屏障通道。
func NewNamedBarrierValueAfterFinish(typ interface{}, waitFor []string) *NamedBarrierValueAfterFinish {
	names := make(map[string]bool)
	for _, name := range waitFor {
		names[name] = true
	}
	return &NamedBarrierValueAfterFinish{
		BaseChannel: BaseChannel{Typ: typ},
		names:       names,
		seen:        make(map[string]bool),
		finished:    false,
	}
}

// Get Finish 且全部到齐后返回 nil。
func (c *NamedBarrierValueAfterFinish) Get() (interface{}, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	if !c.finished || !c.namesMatchSeen() {
		return nil, &errors.EmptyChannelError{}
	}
	return nil, nil
}

// IsAvailable 是否已 Finish 且全部名称到齐。
func (c *NamedBarrierValueAfterFinish) IsAvailable() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.finished && c.namesMatchSeen()
}

// Update 接收字符串名称更新。
func (c *NamedBarrierValueAfterFinish) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}

	c.mu.Lock()
	defer c.mu.Unlock()

	updated := false
	for _, val := range values {
		name, ok := val.(string)
		if !ok {
			return false, &errors.InvalidUpdateError{
				Message: fmt.Sprintf("value must be a string, got %T", val),
			}
		}

		if _, exists := c.names[name]; exists {
			if !c.seen[name] {
				c.seen[name] = true
				updated = true
			}
		} else {
			return false, &errors.InvalidUpdateError{
				Message: fmt.Sprintf("value '%s' not in expected names %v", name, c.names),
			}
		}
	}

	return updated, nil
}

// Copy 返回通道拷贝。
func (c *NamedBarrierValueAfterFinish) Copy() Channel {
	c.mu.RLock()
	defer c.mu.RUnlock()

	newCh := NewNamedBarrierValueAfterFinish(c.Typ, nil)
	newCh.Key = c.Key

	newCh.names = make(map[string]bool, len(c.names))
	for k, v := range c.names {
		newCh.names[k] = v
	}

	newCh.seen = make(map[string]bool, len(c.seen))
	for k, v := range c.seen {
		newCh.seen[k] = v
	}

	newCh.finished = c.finished

	return newCh
}

// Checkpoint 返回 (seen, finished) 状态映射。
func (c *NamedBarrierValueAfterFinish) Checkpoint() interface{} {
	c.mu.RLock()
	defer c.mu.RUnlock()

	if len(c.seen) == 0 && !c.finished {
		return Missing
	}

	result := map[string]interface{}{
		"seen":     c.seen,
		"finished": c.finished,
	}
	return result
}

// FromCheckpoint 从检查点恢复 seen 与 finished 状态。
func (c *NamedBarrierValueAfterFinish) FromCheckpoint(checkpoint interface{}) Channel {
	c.mu.Lock()
	defer c.mu.Unlock()

	newCh := NewNamedBarrierValueAfterFinish(c.Typ, nil)
	newCh.Key = c.Key
	newCh.names = make(map[string]bool, len(c.names))
	for k, v := range c.names {
		newCh.names[k] = v
	}

	if checkpoint != nil && !IsMissing(checkpoint) {
		if cp, ok := checkpoint.(map[string]interface{}); ok {
			newCh.seen = make(map[string]bool)
			switch seen := cp["seen"].(type) {
			case map[string]bool:
				for k, bv := range seen {
					newCh.seen[k] = bv
				}
			case map[string]interface{}:
				for k, bv := range seen {
					if b, ok := bv.(bool); ok {
						newCh.seen[k] = b
					}
				}
			}
			if finished, ok := cp["finished"].(bool); ok {
				newCh.finished = finished
			}
		}
	}

	return newCh
}

// Finish 全部到齐后标记为 finished。
func (c *NamedBarrierValueAfterFinish) Finish() bool {
	c.mu.Lock()
	defer c.mu.Unlock()

	if !c.finished && c.namesMatchSeen() {
		c.finished = true
		return true
	}
	return false
}

// Consume Finish 且全部到齐后重置 finished 与 seen。
func (c *NamedBarrierValueAfterFinish) Consume() bool {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.finished && c.namesMatchSeen() && len(c.seen) > 0 {
		c.finished = false
		c.seen = make(map[string]bool)
		return true
	}
	return false
}

// namesMatchSeen 检查是否所有期望名称均已收到。
func (c *NamedBarrierValueAfterFinish) namesMatchSeen() bool {
	if len(c.names) != len(c.seen) {
		return false
	}
	for name := range c.names {
		if !c.seen[name] {
			return false
		}
	}
	return true
}

// LastValueAfterFinish 存储末值，但须 Finish 后才可读。
type LastValueAfterFinish struct {
	BaseChannel
	value    interface{}
	finished bool
	mu       sync.RWMutex
}

// NewLastValueAfterFinish 创建须 Finish 后才可用的末值通道。
func NewLastValueAfterFinish(typ interface{}) *LastValueAfterFinish {
	return &LastValueAfterFinish{
		BaseChannel: BaseChannel{Typ: typ},
		value:       Missing,
		finished:    false,
	}
}

// Get Finish 后返回最后写入的值。
func (c *LastValueAfterFinish) Get() (interface{}, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	if !c.finished {
		return nil, &errors.EmptyChannelError{}
	}

	if IsMissing(c.value) {
		return nil, &errors.EmptyChannelError{}
	}

	return c.value, nil
}

// IsAvailable 是否已 Finish 且持有值。
func (c *LastValueAfterFinish) IsAvailable() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.finished && !IsMissing(c.value)
}

// Update 每步最多接收一个值。
func (c *LastValueAfterFinish) Update(values []interface{}) (bool, error) {
	if len(values) == 0 {
		return false, nil
	}

	c.mu.Lock()
	defer c.mu.Unlock()

	if len(values) > 1 {
		return false, &errors.InvalidUpdateError{
			Message: "Can receive only one value per step. Use a reducer to handle multiple values.",
		}
	}

	c.value = values[0]
	return true, nil
}

// Copy 返回通道拷贝。
func (c *LastValueAfterFinish) Copy() Channel {
	c.mu.RLock()
	defer c.mu.RUnlock()

	newCh := NewLastValueAfterFinish(c.Typ)
	newCh.Key = c.Key
	newCh.value = c.value
	newCh.finished = c.finished
	return newCh
}

// Checkpoint 返回当前值。
func (c *LastValueAfterFinish) Checkpoint() interface{} {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.value
}

// FromCheckpoint 从检查点恢复。
func (c *LastValueAfterFinish) FromCheckpoint(checkpoint interface{}) Channel {
	c.mu.Lock()
	defer c.mu.Unlock()

	newCh := NewLastValueAfterFinish(c.Typ)
	newCh.Key = c.Key

	if !IsMissing(checkpoint) {
		newCh.value = checkpoint
	}

	return newCh
}

// Finish 标记通道为 finished。
func (c *LastValueAfterFinish) Finish() bool {
	c.mu.Lock()
	defer c.mu.Unlock()

	if !c.finished {
		c.finished = true
		return true
	}
	return false
}

// Consume 本通道始终返回 false。
func (c *LastValueAfterFinish) Consume() bool {
	return false
}
