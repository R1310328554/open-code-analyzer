// Package channels 提供 LangGraph Go 的通道（Channel）实现。
package channels

import (
	"fmt"
	"sync"
	"sync/atomic"

	"ragflow/internal/harness/graph/errors"
)

// Missing 哨兵值，表示通道尚无有效值。
var Missing = &missingSentinel{}

type missingSentinel struct{}

func (m *missingSentinel) String() string {
	return "<MISSING>"
}

// IsMissing 判断值是否为 Missing 哨兵。
func IsMissing(val any) bool {
	if val == nil {
		return false
	}
	_, ok := val.(*missingSentinel)
	return ok
}

// Channel 所有通道的基础接口。
type Channel interface {
	GetKey() string
	SetKey(key string)
	// Get 返回当前值；空通道返回 EmptyChannelError。
	Get() (interface{}, error)
	// IsAvailable 通道是否可读（非空）。
	IsAvailable() bool
	// Update 用给定更新序列写入通道；返回是否实际更新。
	Update(values []interface{}) (bool, error)
	Copy() Channel
	// Checkpoint 返回可序列化的通道状态；空时返回 Missing。
	Checkpoint() interface{}
	FromCheckpoint(checkpoint interface{}) Channel
	// Consume 通知通道：订阅任务已执行；返回是否更新了通道。
	Consume() bool
	// Finish 通知通道：Pregel 运行即将结束。
	Finish() bool
	// GetVersion 返回通道版本号；-1 表示不支持版本追踪。
	GetVersion() int
}

// BaseChannel 通道的基础嵌入结构，提供 Key、版本号等公共字段。
type BaseChannel struct {
	Key     string
	Typ     interface{}
	Version int64 // 原子版本号，用于变更检测（线程安全）
}

// GetKey 返回通道键名。
func (c *BaseChannel) GetKey() string {
	return c.Key
}

// SetKey 设置通道键名。
func (c *BaseChannel) SetKey(key string) {
	c.Key = key
}

// Consume 默认无操作。
func (c *BaseChannel) Consume() bool {
	return false
}

// Finish 默认无操作。
func (c *BaseChannel) Finish() bool {
	return false
}

// GetVersion 原子读取通道版本号。
func (c *BaseChannel) GetVersion() int {
	return int(atomic.LoadInt64(&c.Version))
}

// SetVersion 原子设置版本号（引擎应用写入后调用）。
func (c *BaseChannel) SetVersion(v int) {
	atomic.StoreInt64(&c.Version, int64(v))
}

// Registry 通道类型注册表，管理命名通道实例。
type Registry struct {
	mu       sync.RWMutex
	channels map[string]Channel
}

// NewRegistry 创建空注册表。
func NewRegistry() *Registry {
	return &Registry{
		channels: make(map[string]Channel),
	}
}

// Register 注册命名通道。
func (r *Registry) Register(name string, channel Channel) {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.channels[name] = channel
}

// Get 按名称获取通道。
func (r *Registry) Get(name string) (Channel, bool) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	ch, ok := r.channels[name]
	return ch, ok
}

// Remove 移除通道。
func (r *Registry) Remove(name string) {
	r.mu.Lock()
	defer r.mu.Unlock()
	delete(r.channels, name)
}

// Len 返回已注册通道数量。
func (r *Registry) Len() int {
	r.mu.RLock()
	defer r.mu.RUnlock()
	return len(r.channels)
}

// Names 返回所有通道名称。
func (r *Registry) Names() []string {
	r.mu.RLock()
	defer r.mu.RUnlock()
	names := make([]string, 0, len(r.channels))
	for name := range r.channels {
		names = append(names, name)
	}
	return names
}

// List Names 的别名。
func (r *Registry) List() []string {
	return r.Names()
}

// CreateCheckpoint 为所有通道创建检查点快照。
func (r *Registry) CreateCheckpoint() map[string]interface{} {
	r.mu.RLock()
	defer r.mu.RUnlock()
	checkpoint := make(map[string]interface{})
	for name, channel := range r.channels {
		cp := channel.Checkpoint()
		if !IsMissing(cp) {
			checkpoint[name] = cp
		}
	}
	return checkpoint
}

// RestoreFromCheckpoint 从检查点恢复所有通道。
func (r *Registry) RestoreFromCheckpoint(checkpoint map[string]interface{}) error {
	r.mu.Lock()
	defer r.mu.Unlock()
	for name, cp := range checkpoint {
		channel, ok := r.channels[name]
		if !ok {
			return fmt.Errorf("channel %s not found in registry", name)
		}
		newChannel := channel.FromCheckpoint(cp)
		r.channels[name] = newChannel
	}
	return nil
}

// UpdateChannels 批量更新各通道的写入值。
func (r *Registry) UpdateChannels(writes map[string][]interface{}) error {
	r.mu.Lock()
	defer r.mu.Unlock()
	for name, values := range writes {
		channel, ok := r.channels[name]
		if !ok {
			return &errors.ChannelNotFoundError{ChannelName: name}
		}
		if _, err := channel.Update(values); err != nil {
			return fmt.Errorf("failed to update channel %s: %w", name, err)
		}
	}
	return nil
}

// GetValues 读取所有通道的当前值（空通道跳过）。
func (r *Registry) GetValues() (map[string]interface{}, error) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	values := make(map[string]interface{})
	for name, channel := range r.channels {
		val, err := channel.Get()
		if err != nil {
			if errors.IsEmptyChannelError(err) {
				continue
			}
			return nil, fmt.Errorf("failed to get value from channel %s: %w", name, err)
		}
		values[name] = val
	}
	return values, nil
}
