// read.go — 通道读取抽象：选择器、变换器与触发器。
package pregel

import (
	"context"
	"fmt"
	"sync"

	"ragflow/internal/harness/graph/channels"
)

// ChannelRead 封装从多个通道读取状态的逻辑。
// 支持选择器（Selector）与变换器（Transformer）组合。
type ChannelRead struct {
	registry    *channels.Registry
	selector    ChannelSelector
	transformer ChannelTransformer
	mu          sync.RWMutex
}

// ChannelSelector 选择要读取的通道集合。
type ChannelSelector interface {
	Select(registry *channels.Registry) ([]string, error)
}

// ChannelTransformer 对读取的通道值做变换。
type ChannelTransformer interface {
	Transform(values map[string]any) (map[string]any, error)
}

// NewChannelRead 创建通道读取操作，默认全通道+恒等变换。
func NewChannelRead(registry *channels.Registry, opts ...ChannelReadOption) *ChannelRead {
	cr := &ChannelRead{
		registry:    registry,
		selector:    &AllChannelsSelector{},
		transformer: &IdentityTransformer{},
	}

	for _, opt := range opts {
		opt(cr)
	}

	return cr
}

// ChannelReadOption ChannelRead 配置选项。
type ChannelReadOption func(*ChannelRead)

// WithSelector 设置通道选择器。
func WithSelector(selector ChannelSelector) ChannelReadOption {
	return func(cr *ChannelRead) {
		cr.selector = selector
	}
}

// WithTransformer 设置通道变换器。
func WithTransformer(transformer ChannelTransformer) ChannelReadOption {
	return func(cr *ChannelRead) {
		cr.transformer = transformer
	}
}

// Read 执行读取：选择通道 → 取值 → 变换。
func (cr *ChannelRead) Read(ctx context.Context) (map[string]any, error) {
	cr.mu.RLock()
	defer cr.mu.RUnlock()

	// Select channels to read
	channelNames, err := cr.selector.Select(cr.registry)
	if err != nil {
		return nil, fmt.Errorf("channel selection failed: %w", err)
	}

	// Read values from channels
	values := make(map[string]any)
	for _, name := range channelNames {
		if ch, ok := cr.registry.Get(name); ok {
			val, err := ch.Get()
			if err == nil {
				values[name] = val
			}
		}
	}

	// Transform values
	if cr.transformer != nil {
		transformed, err := cr.transformer.Transform(values)
		if err != nil {
			return nil, fmt.Errorf("channel transformation failed: %w", err)
		}
		values = transformed
	}

	return values, nil
}

// ReadChannel 读取单个命名通道。
func (cr *ChannelRead) ReadChannel(ctx context.Context, name string) (any, error) {
	cr.mu.RLock()
	defer cr.mu.RUnlock()

	if ch, ok := cr.registry.Get(name); ok {
		return ch.Get()
	}

	return nil, fmt.Errorf("channel not found: %s", name)
}

// HasChannel 检查通道是否已注册。
func (cr *ChannelRead) HasChannel(name string) bool {
	cr.mu.RLock()
	defer cr.mu.RUnlock()
	_, ok := cr.registry.Get(name)
	return ok
}

// ListChannels 返回全部通道名称。
func (cr *ChannelRead) ListChannels() []string {
	cr.mu.RLock()
	defer cr.mu.RUnlock()
	return cr.registry.List()
}

// ==================== 通道选择器 ====================

// AllChannelsSelector 选择所有已注册通道。
type AllChannelsSelector struct{}

func (s *AllChannelsSelector) Select(registry *channels.Registry) ([]string, error) {
	return registry.List(), nil
}

// SpecificChannelsSelector 选择指定名称的通道。
type SpecificChannelsSelector struct {
	channels []string
}

// NewSpecificChannelsSelector 创建指定通道选择器。
func NewSpecificChannelsSelector(channels ...string) *SpecificChannelsSelector {
	return &SpecificChannelsSelector{channels: channels}
}

func (s *SpecificChannelsSelector) Select(registry *channels.Registry) ([]string, error) {
	result := make([]string, 0, len(s.channels))
	for _, name := range s.channels {
		if _, ok := registry.Get(name); ok {
			result = append(result, name)
		}
	}
	return result, nil
}

// PrefixChannelsSelector 按前缀筛选通道。
type PrefixChannelsSelector struct {
	prefix string
}

// NewPrefixChannelsSelector 创建前缀选择器。
func NewPrefixChannelsSelector(prefix string) *PrefixChannelsSelector {
	return &PrefixChannelsSelector{prefix: prefix}
}

func (s *PrefixChannelsSelector) Select(registry *channels.Registry) ([]string, error) {
	all := registry.List()
	result := make([]string, 0)
	for _, name := range all {
		if len(name) >= len(s.prefix) && name[:len(s.prefix)] == s.prefix {
			result = append(result, name)
		}
	}
	return result, nil
}

// AvailableChannelsSelector 仅选择有值的通道。
type AvailableChannelsSelector struct{}

func (s *AvailableChannelsSelector) Select(registry *channels.Registry) ([]string, error) {
	all := registry.List()
	result := make([]string, 0)
	for _, name := range all {
		if ch, ok := registry.Get(name); ok && ch.IsAvailable() {
			result = append(result, name)
		}
	}
	return result, nil
}

// ==================== 通道变换器 ====================

// IdentityTransformer 恒等变换，原样返回。
type IdentityTransformer struct{}

func (t *IdentityTransformer) Transform(values map[string]any) (map[string]any, error) {
	return values, nil
}

// MappingTransformer 重命名通道键。
type MappingTransformer struct {
	mappings map[string]string
}

// NewMappingTransformer 创建键名映射变换器。
func NewMappingTransformer(mappings map[string]string) *MappingTransformer {
	return &MappingTransformer{mappings: mappings}
}

func (t *MappingTransformer) Transform(values map[string]any) (map[string]any, error) {
	result := make(map[string]any)
	for oldName, value := range values {
		newName := oldName
		if mapped, ok := t.mappings[oldName]; ok {
			newName = mapped
		}
		result[newName] = value
	}
	return result, nil
}

// FilterTransformer 白名单过滤通道。
type FilterTransformer struct {
	filter map[string]bool
}

// NewFilterTransformer 创建过滤变换器。
func NewFilterTransformer(keep ...string) *FilterTransformer {
	filter := make(map[string]bool)
	for _, name := range keep {
		filter[name] = true
	}
	return &FilterTransformer{filter: filter}
}

func (t *FilterTransformer) Transform(values map[string]any) (map[string]any, error) {
	result := make(map[string]any)
	for name, value := range values {
		if t.filter[name] {
			result[name] = value
		}
	}
	return result, nil
}

// DefaultTransformer 为缺失通道填充默认值。
type DefaultTransformer struct {
	defaults map[string]any
}

// NewDefaultTransformer 创建带默认值的变换器。
func NewDefaultTransformer(defaults map[string]any) *DefaultTransformer {
	return &DefaultTransformer{defaults: defaults}
}

func (t *DefaultTransformer) Transform(values map[string]any) (map[string]any, error) {
	result := make(map[string]any)
	for name, defValue := range t.defaults {
		if value, ok := values[name]; ok {
			result[name] = value
		} else {
			result[name] = defValue
		}
	}
	// Add any extra values that don't have defaults
	for name, value := range values {
		if _, ok := result[name]; !ok {
			result[name] = value
		}
	}
	return result, nil
}

// MergingTransformer 将多通道值合并为单一值。
type MergingTransformer struct {
	target string
	merger func([]any) (any, error)
}

// NewMergingTransformer 创建合并变换器。
func NewMergingTransformer(target string, merger func([]any) (any, error)) *MergingTransformer {
	return &MergingTransformer{
		target: target,
		merger: merger,
	}
}

func (t *MergingTransformer) Transform(values map[string]any) (map[string]any, error) {
	// Collect all values
	items := make([]any, 0, len(values))
	for _, value := range values {
		items = append(items, value)
	}

	// Merge
	merged, err := t.merger(items)
	if err != nil {
		return nil, err
	}

	// Return merged value under target key
	return map[string]any{t.target: merged}, nil
}

// ==================== 读取触发器 ====================

// Trigger 决定通道读取是否应触发。
type Trigger interface {
	ShouldTrigger(registry *channels.Registry) bool
}

// AlwaysTrigger 始终触发。
type AlwaysTrigger struct{}

func (t *AlwaysTrigger) ShouldTrigger(registry *channels.Registry) bool {
	return true
}

// AnyAvailableTrigger 任一选定通道有值时触发。
type AnyAvailableTrigger struct {
	channels []string
}

// NewAnyAvailableTrigger 创建任一可用触发器。
func NewAnyAvailableTrigger(channels ...string) *AnyAvailableTrigger {
	return &AnyAvailableTrigger{channels: channels}
}

func (t *AnyAvailableTrigger) ShouldTrigger(registry *channels.Registry) bool {
	for _, name := range t.channels {
		if ch, ok := registry.Get(name); ok && ch.IsAvailable() {
			return true
		}
	}
	return false
}

// AllAvailableTrigger 全部选定通道有值时触发。
type AllAvailableTrigger struct {
	channels []string
}

// NewAllAvailableTrigger 创建全部可用触发器。
func NewAllAvailableTrigger(channels ...string) *AllAvailableTrigger {
	return &AllAvailableTrigger{channels: channels}
}

func (t *AllAvailableTrigger) ShouldTrigger(registry *channels.Registry) bool {
	for _, name := range t.channels {
		if ch, ok := registry.Get(name); !ok || !ch.IsAvailable() {
			return false
		}
	}
	return true
}

// ChannelChangedTrigger 指定通道版本变更时触发。
type ChannelChangedTrigger struct {
	channel     string
	lastVersion int64
}

// NewChannelChangedTrigger 创建通道变更触发器。
func NewChannelChangedTrigger(channel string) *ChannelChangedTrigger {
	return &ChannelChangedTrigger{
		channel:     channel,
		lastVersion: -1,
	}
}

func (t *ChannelChangedTrigger) ShouldTrigger(registry *channels.Registry) bool {
	if ch, ok := registry.Get(t.channel); ok {
		// Track the channel version to detect actual changes (not just availability).
		// GetVersion returns -1 if the channel does not support versioning, in which
		// case we fall back to IsAvailable() for backward compatibility.
		version := int64(ch.GetVersion())
		if version >= 0 && version != t.lastVersion {
			t.lastVersion = version
			return true
		}
		// Fallback for channels without version tracking.
		return version < 0 && ch.IsAvailable()
	}
	return false
}

// ==================== 工具类型 ====================

// ReadContext 节点级通道读取上下文（含触发器与多读取器）。
type ReadContext struct {
	Node     string
	Step     int
	Triggers []Trigger
	Readers  map[string]*ChannelRead
}

// NewReadContext 创建读取上下文。
func NewReadContext(node string, step int) *ReadContext {
	return &ReadContext{
		Node:     node,
		Step:     step,
		Triggers: make([]Trigger, 0),
		Readers:  make(map[string]*ChannelRead),
	}
}

// AddReader 注册命名读取器。
func (rc *ReadContext) AddReader(name string, reader *ChannelRead) {
	rc.Readers[name] = reader
}

// GetReader 按名称获取读取器。
func (rc *ReadContext) GetReader(name string) *ChannelRead {
	return rc.Readers[name]
}

// ShouldExecute 检查是否有触发器满足条件。
func (rc *ReadContext) ShouldExecute(registry *channels.Registry) bool {
	if len(rc.Triggers) == 0 {
		return true
	}

	for _, trigger := range rc.Triggers {
		if trigger.ShouldTrigger(registry) {
			return true
		}
	}

	return false
}

// ReadAll 执行全部读取器并合并结果（键前缀为读取器名）。
func (rc *ReadContext) ReadAll(ctx context.Context) (map[string]any, error) {
	combined := make(map[string]any)
	for name, reader := range rc.Readers {
		values, err := reader.Read(ctx)
		if err != nil {
			return nil, fmt.Errorf("reader %s failed: %w", name, err)
		}
		for k, v := range values {
			combined[name+"."+k] = v
		}
	}
	return combined, nil
}
