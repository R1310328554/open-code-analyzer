package pregel

// write.go — 通道批量写入：ChannelWrite、变换器、校验器与 WriteContext。


import (
	"context"
	"fmt"
	"slices"
	"sync"

	"ragflow/internal/harness/graph/channels"
	"ragflow/internal/harness/graph/types"
)

// ChannelWrite 封装多通道写入，支持变换与校验链。
// 节点/引擎通过它批量提交状态更新到 Registry。
type ChannelWrite struct {
	registry    *channels.Registry
	entries     []*ChannelWriteEntry
	transformer WriteTransformer
	validator   WriteValidator
	mu          sync.RWMutex
}

// ChannelWriteEntry 单条写入（通道、值、覆盖标志）。
type ChannelWriteEntry struct {
	Channel   string
	Value     any
	Overwrite bool
	Node      string
	Metadata  map[string]any
}

// WriteTransformer 写入前变换接口。
type WriteTransformer interface {
	Transform(entry *ChannelWriteEntry) (*ChannelWriteEntry, error)
}

// WriteValidator 写入前校验接口。
type WriteValidator interface {
	Validate(entry *ChannelWriteEntry) error
}

// NewChannelWrite 创建写入器，默认恒等变换与空校验。
func NewChannelWrite(registry *channels.Registry, opts ...ChannelWriteOption) *ChannelWrite {
	cw := &ChannelWrite{
		registry:    registry,
		entries:     make([]*ChannelWriteEntry, 0),
		transformer: &IdentityWriteTransformer{},
		validator:   &NoOpValidator{},
	}

	for _, opt := range opts {
		opt(cw)
	}

	return cw
}

// ChannelWriteOption 配置函数选项。
type ChannelWriteOption func(*ChannelWrite)

// WithWriteTransformer 设置写入变换器。
func WithWriteTransformer(transformer WriteTransformer) ChannelWriteOption {
	return func(cw *ChannelWrite) {
		cw.transformer = transformer
	}
}

// WithValidator 设置写入校验器。
func WithValidator(validator WriteValidator) ChannelWriteOption {
	return func(cw *ChannelWrite) {
		cw.validator = validator
	}
}

// AddEntry 追加待写条目。
func (cw *ChannelWrite) AddEntry(entry *ChannelWriteEntry) {
	cw.mu.Lock()
	defer cw.mu.Unlock()
	cw.entries = append(cw.entries, entry)
}

// AddEntries 批量追加条目。
func (cw *ChannelWrite) AddEntries(entries ...*ChannelWriteEntry) {
	cw.mu.Lock()
	defer cw.mu.Unlock()
	cw.entries = append(cw.entries, entries...)
}

// WriteTo 追加普通 reducer 写入。
func (cw *ChannelWrite) WriteTo(channel string, value any) {
	cw.AddEntry(&ChannelWriteEntry{
		Channel:   channel,
		Value:     value,
		Overwrite: false,
	})
}

// Overwrite 追加覆盖写入（绕过 reducer）。
func (cw *ChannelWrite) Overwrite(channel string, value any) {
	cw.AddEntry(&ChannelWriteEntry{
		Channel:   channel,
		Value:     value,
		Overwrite: true,
	})
}

// WriteNode 带来源节点名的写入。
func (cw *ChannelWrite) WriteNode(node string, channel string, value any) {
	cw.AddEntry(&ChannelWriteEntry{
		Channel:   channel,
		Value:     value,
		Overwrite: false,
		Node:      node,
	})
}

// Write 校验→变换→Update 全部条目并清空队列。
func (cw *ChannelWrite) Write(ctx context.Context) (map[string]bool, error) {
	cw.mu.Lock()
	defer cw.mu.Unlock()

	updated := make(map[string]bool)

	for _, entry := range cw.entries {
		// Validate
		if cw.validator != nil {
			if err := cw.validator.Validate(entry); err != nil {
				return nil, fmt.Errorf("validation failed for channel %s: %w", entry.Channel, err)
			}
		}

		// Transform
		transformed := entry
		if cw.transformer != nil {
			var err error
			transformed, err = cw.transformer.Transform(entry)
			if err != nil {
				return nil, fmt.Errorf("transformation failed for channel %s: %w", entry.Channel, err)
			}
		}

		// Apply write
		if ch, ok := cw.registry.Get(transformed.Channel); ok {
			// Check for Overwrite wrapper
			value := transformed.Value
			if transformed.Overwrite {
				value = &types.Overwrite{Value: value}
			}

			wasUpdated, err := ch.Update([]any{value})
			if err != nil {
				return nil, fmt.Errorf("failed to update channel %s: %w", transformed.Channel, err)
			}
			if wasUpdated {
				updated[transformed.Channel] = true
			}
		} else {
			return nil, fmt.Errorf("channel not found: %s", transformed.Channel)
		}
	}

	// Clear entries after write
	cw.entries = make([]*ChannelWriteEntry, 0)

	return updated, nil
}

// Clear 清空待写队列。
func (cw *ChannelWrite) Clear() {
	cw.mu.Lock()
	defer cw.mu.Unlock()
	cw.entries = make([]*ChannelWriteEntry, 0)
}

// EntryCount 返回待写条目数。
func (cw *ChannelWrite) EntryCount() int {
	cw.mu.RLock()
	defer cw.mu.RUnlock()
	return len(cw.entries)
}

// GetEntries 返回条目副本。
func (cw *ChannelWrite) GetEntries() []*ChannelWriteEntry {
	cw.mu.RLock()
	defer cw.mu.RUnlock()

	entries := make([]*ChannelWriteEntry, len(cw.entries))
	copy(entries, cw.entries)
	return entries
}

// ==================== 写入变换器 ====================

// IdentityWriteTransformer 恒等变换。
type IdentityWriteTransformer struct{}

func (t *IdentityWriteTransformer) Transform(entry *ChannelWriteEntry) (*ChannelWriteEntry, error) {
	return entry, nil
}

// MappingWriteTransformer 通道名映射。
type MappingWriteTransformer struct {
	mappings map[string]string
}

// NewMappingWriteTransformer 按 map 重命名目标通道。
func NewMappingWriteTransformer(mappings map[string]string) *MappingWriteTransformer {
	return &MappingWriteTransformer{mappings: mappings}
}

func (t *MappingWriteTransformer) Transform(entry *ChannelWriteEntry) (*ChannelWriteEntry, error) {
	if newName, ok := t.mappings[entry.Channel]; ok {
		transformed := *entry
		transformed.Channel = newName
		return &transformed, nil
	}
	return entry, nil
}

// PrefixWriteTransformer 为通道名加前缀。
type PrefixWriteTransformer struct {
	prefix string
}

// NewPrefixWriteTransformer 创建前缀变换器。
func NewPrefixWriteTransformer(prefix string) *PrefixWriteTransformer {
	return &PrefixWriteTransformer{prefix: prefix}
}

func (t *PrefixWriteTransformer) Transform(entry *ChannelWriteEntry) (*ChannelWriteEntry, error) {
	transformed := *entry
	transformed.Channel = t.prefix + entry.Channel
	return &transformed, nil
}

// MetadataWriteTransformer 合并元数据到条目。
type MetadataWriteTransformer struct {
	metadata map[string]any
}

// NewMetadataWriteTransformer 创建元数据注入变换器。
func NewMetadataWriteTransformer(metadata map[string]any) *MetadataWriteTransformer {
	return &MetadataWriteTransformer{metadata: metadata}
}

func (t *MetadataWriteTransformer) Transform(entry *ChannelWriteEntry) (*ChannelWriteEntry, error) {
	transformed := *entry
	if transformed.Metadata == nil {
		transformed.Metadata = make(map[string]any)
	}
	for k, v := range t.metadata {
		transformed.Metadata[k] = v
	}
	return &transformed, nil
}

// NodeWriteTransformer 补全空 Node 字段。
type NodeWriteTransformer struct {
	node string
}

// NewNodeWriteTransformer 创建节点信息变换器。
func NewNodeWriteTransformer(node string) *NodeWriteTransformer {
	return &NodeWriteTransformer{node: node}
}

func (t *NodeWriteTransformer) Transform(entry *ChannelWriteEntry) (*ChannelWriteEntry, error) {
	if entry.Node == "" {
		transformed := *entry
		transformed.Node = t.node
		return &transformed, nil
	}
	return entry, nil
}

// FilterWriteTransformer 谓词过滤，不匹配则 WriteSkipError。
type FilterWriteTransformer struct {
	predicate func(*ChannelWriteEntry) bool
}

// NewFilterWriteTransformer 创建过滤变换器。
func NewFilterWriteTransformer(predicate func(*ChannelWriteEntry) bool) *FilterWriteTransformer {
	return &FilterWriteTransformer{predicate: predicate}
}

func (t *FilterWriteTransformer) Transform(entry *ChannelWriteEntry) (*ChannelWriteEntry, error) {
	if t.predicate != nil && !t.predicate(entry) {
		return nil, &WriteSkipError{Channel: entry.Channel}
	}
	return entry, nil
}

// ==================== 写入校验器 ====================

// NoOpValidator 空校验。
type NoOpValidator struct{}

func (v *NoOpValidator) Validate(entry *ChannelWriteEntry) error {
	return nil
}

// TypeWriteValidator 按通道期望类型校验。
type TypeWriteValidator struct {
	types map[string]any
}

// NewTypeWriteValidator 创建类型校验器。
func NewTypeWriteValidator(types map[string]any) *TypeWriteValidator {
	return &TypeWriteValidator{types: types}
}

func (v *TypeWriteValidator) Validate(entry *ChannelWriteEntry) error {
	if expectedType, ok := v.types[entry.Channel]; ok {
		if entry.Value != nil && fmt.Sprintf("%T", entry.Value) != fmt.Sprintf("%T", expectedType) {
			return &WriteValidationError{
				Channel: entry.Channel,
				Message: fmt.Sprintf("expected type %T, got %T", expectedType, entry.Value),
			}
		}
	}
	return nil
}

// NonNullWriteValidator 拒绝 nil（白名单通道除外）。
type NonNullWriteValidator struct {
	whitelist []string
}

// NewNonNullWriteValidator 创建非空校验器。
func NewNonNullWriteValidator(whitelist ...string) *NonNullWriteValidator {
	return &NonNullWriteValidator{whitelist: whitelist}
}

func (v *NonNullWriteValidator) Validate(entry *ChannelWriteEntry) error {
	if slices.Contains(v.whitelist, entry.Channel) {
		return nil
	}

	if entry.Value == nil {
		return &WriteValidationError{
			Channel: entry.Channel,
			Message: "value cannot be nil",
		}
	}
	return nil
}

// LengthWriteValidator 校验 slice/string/map 长度上下界。
type LengthWriteValidator struct {
	minLengths map[string]int
	maxLengths map[string]int
}

// NewLengthWriteValidator 创建长度校验器。
func NewLengthWriteValidator(minLengths, maxLengths map[string]int) *LengthWriteValidator {
	return &LengthWriteValidator{
		minLengths: minLengths,
		maxLengths: maxLengths,
	}
}

func (v *LengthWriteValidator) Validate(entry *ChannelWriteEntry) error {
	var length int

	switch val := entry.Value.(type) {
	case []any:
		length = len(val)
	case string:
		length = len(val)
	case map[string]any:
		length = len(val)
	default:
		return nil
	}

	if min, ok := v.minLengths[entry.Channel]; ok && length < min {
		return &WriteValidationError{
			Channel: entry.Channel,
			Message: fmt.Sprintf("length %d is less than minimum %d", length, min),
		}
	}

	if max, ok := v.maxLengths[entry.Channel]; ok && length > max {
		return &WriteValidationError{
			Channel: entry.Channel,
			Message: fmt.Sprintf("length %d exceeds maximum %d", length, max),
		}
	}

	return nil
}

// ==================== 写入批次 ====================

// WriteBatch 命名批次，便于分组 flush。
type WriteBatch struct {
	entries []*ChannelWriteEntry
}

// NewWriteBatch 创建空批次。
func NewWriteBatch() *WriteBatch {
	return &WriteBatch{
		entries: make([]*ChannelWriteEntry, 0),
	}
}

// Add 向批次追加条目。
func (b *WriteBatch) Add(entry *ChannelWriteEntry) {
	b.entries = append(b.entries, entry)
}

// WriteTo 批次内普通写入。
func (b *WriteBatch) WriteTo(channel string, value any) {
	b.Add(&ChannelWriteEntry{
		Channel:   channel,
		Value:     value,
		Overwrite: false,
	})
}

// Overwrite 批次内覆盖写入。
func (b *WriteBatch) Overwrite(channel string, value any) {
	b.Add(&ChannelWriteEntry{
		Channel:   channel,
		Value:     value,
		Overwrite: true,
	})
}

// Entries 返回批次全部条目。
func (b *WriteBatch) Entries() []*ChannelWriteEntry {
	return b.entries
}

// Size 返回批次大小。
func (b *WriteBatch) Size() int {
	return len(b.entries)
}

// Clear 清空批次。
func (b *WriteBatch) Clear() {
	b.entries = make([]*ChannelWriteEntry, 0)
}

// ==================== 写入上下文 ====================

// WriteContext 节点级写入上下文，管理多命名批次。
type WriteContext struct {
	Node    string
	Step    int
	Writer  *ChannelWrite
	Batches map[string]*WriteBatch
}

// NewWriteContext 绑定节点名、步号与主 Writer。
func NewWriteContext(node string, step int, writer *ChannelWrite) *WriteContext {
	return &WriteContext{
		Node:    node,
		Step:    step,
		Writer:  writer,
		Batches: make(map[string]*WriteBatch),
	}
}

// CreateBatch 创建并注册命名批次。
func (wc *WriteContext) CreateBatch(name string) *WriteBatch {
	batch := NewWriteBatch()
	wc.Batches[name] = batch
	return batch
}

// GetBatch 获取已有批次。
func (wc *WriteContext) GetBatch(name string) *WriteBatch {
	return wc.Batches[name]
}

// Flush 合并所有批次并执行 Write。
func (wc *WriteContext) Flush(ctx context.Context) (map[string]bool, error) {
	for _, batch := range wc.Batches {
		wc.Writer.AddEntries(batch.Entries()...)
	}
	return wc.Writer.Write(ctx)
}

// ==================== 错误类型 ====================

// WriteValidationError 校验失败错误。
type WriteValidationError struct {
	Channel string
	Message string
}

func (e *WriteValidationError) Error() string {
	return fmt.Sprintf("write validation error for channel %s: %s", e.Channel, e.Message)
}

// WriteSkipError 表示条目被过滤跳过。
type WriteSkipError struct {
	Channel string
}

func (e *WriteSkipError) Error() string {
	return fmt.Sprintf("write skipped for channel %s", e.Channel)
}

// IsWriteSkipError 判断是否为 WriteSkipError。
func IsWriteSkipError(err error) bool {
	_, ok := err.(*WriteSkipError)
	return ok
}

// Overwrite 写入包装为 types.Overwrite，由通道 Update 识别并绕过 reducer。
