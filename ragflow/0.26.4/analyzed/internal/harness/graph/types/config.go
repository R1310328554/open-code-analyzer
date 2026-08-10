package types

// config.go — RunnableConfig：可配置项、递归限制、耐久模式与合并补丁。


import "ragflow/internal/harness/graph/constants"

// RunnableConfig 图/Runnable 单次执行的配置载体。
type RunnableConfig struct {
	// Configurable 节点与 checkpointer 共享的可配置 map
	Configurable map[string]interface{}

	// RecursionLimit 超步上限，触达则 GraphRecursionError
	RecursionLimit int

	// Tags 执行标签（追踪/过滤）
	Tags []string

	// Metadata 执行元数据
	Metadata map[string]interface{}

	// RunID 本次 run 唯一 ID
	RunID string

	// ThreadID 检查点线程 ID
	ThreadID string

	// Durability 检查点持久化时机（sync/async/exit）
	Durability Durability
}

// NewRunnableConfig 默认值含 DefaultRecursionLimit 与 DurabilitySync。
func NewRunnableConfig() *RunnableConfig {
	return &RunnableConfig{
		Configurable:   make(map[string]interface{}),
		RecursionLimit: constants.DefaultRecursionLimit,
		Tags:           make([]string, 0),
		Metadata:       make(map[string]interface{}),
		Durability:     DurabilitySync,
	}
}

// Get 读取 Configurable 键。
func (c *RunnableConfig) Get(key string) (interface{}, bool) {
	if c.Configurable == nil {
		return nil, false
	}
	val, ok := c.Configurable[key]
	return val, ok
}

// Set 写入 Configurable 键。
func (c *RunnableConfig) Set(key string, value interface{}) {
	if c.Configurable == nil {
		c.Configurable = make(map[string]interface{})
	}
	c.Configurable[key] = value
}

// Merge 合并另一配置的各字段。
func (c *RunnableConfig) Merge(other *RunnableConfig) *RunnableConfig {
	if other == nil {
		return c
	}

	// Merge configurable
	for k, v := range other.Configurable {
		c.Set(k, v)
	}

	// Use other's recursion limit if set
	if other.RecursionLimit > 0 {
		c.RecursionLimit = other.RecursionLimit
	}

	// Merge tags
	c.Tags = append(c.Tags, other.Tags...)

	// Merge metadata
	for k, v := range other.Metadata {
		c.Metadata[k] = v
	}

	// Use other's RunID if set
	if other.RunID != "" {
		c.RunID = other.RunID
	}

	// Use other's ThreadID if set
	if other.ThreadID != "" {
		c.ThreadID = other.ThreadID
	}

	// Use other's Durability if set (not default)
	if other.Durability != "" && other.Durability != DurabilitySync {
		c.Durability = other.Durability
	}

	return c
}

// WithConfigurable 替换 Configurable map。
func (c *RunnableConfig) WithConfigurable(configurable map[string]interface{}) *RunnableConfig {
	c.Configurable = configurable
	return c
}

// WithRecursionLimit 设置递归/超步限制。
func (c *RunnableConfig) WithRecursionLimit(limit int) *RunnableConfig {
	c.RecursionLimit = limit
	return c
}

// WithTags 设置标签。
func (c *RunnableConfig) WithTags(tags ...string) *RunnableConfig {
	c.Tags = tags
	return c
}

// WithMetadata 设置元数据。
func (c *RunnableConfig) WithMetadata(metadata map[string]interface{}) *RunnableConfig {
	c.Metadata = metadata
	return c
}

// WithRunID 设置 RunID。
func (c *RunnableConfig) WithRunID(runID string) *RunnableConfig {
	c.RunID = runID
	return c
}

// WithThreadID 设置 ThreadID。
func (c *RunnableConfig) WithThreadID(threadID string) *RunnableConfig {
	c.ThreadID = threadID
	return c
}

// WithDurability 设置耐久模式。
func (c *RunnableConfig) WithDurability(durability Durability) *RunnableConfig {
	c.Durability = durability
	return c
}

// GetOrEmpty 安全取 string 配置，缺失返回空串。
// 便于构造 checkpointer 使用的 flat config map。
func (c *RunnableConfig) GetOrEmpty(key string) string {
	if c.Configurable == nil {
		return ""
	}
	v, ok := c.Configurable[key]
	if !ok {
		return ""
	}
	s, _ := v.(string)
	return s
}

// ConfigPatcher 配置补丁函数类型。
type ConfigPatcher func(*RunnableConfig) *RunnableConfig

// PatchConfig 顺序应用多个 ConfigPatcher。
func PatchConfig(config *RunnableConfig, patchers ...ConfigPatcher) *RunnableConfig {
	if config == nil {
		config = NewRunnableConfig()
	}

	for _, patcher := range patchers {
		if patcher != nil {
			config = patcher(config)
		}
	}

	return config
}

// EnsureConfig nil 时返回 NewRunnableConfig。
func EnsureConfig(config *RunnableConfig) *RunnableConfig {
	if config == nil {
		return NewRunnableConfig()
	}
	return config
}

// MergeConfigs 将多个配置合并为一个。
func MergeConfigs(configs ...*RunnableConfig) *RunnableConfig {
	result := NewRunnableConfig()

	for _, config := range configs {
		result.Merge(config)
	}

	return result
}

// Configurable 中保留键见 constants 包（thread_id、checkpoint_ns 等）。
