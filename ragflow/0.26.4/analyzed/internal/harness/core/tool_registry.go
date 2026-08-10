package core

// tool_registry.go — 集中式工具注册表：别名、分类、合并与过滤。


import (
	"fmt"
	"sync"

	"ragflow/internal/harness/core/schema"
)

// ToolRegistry 集中管理工具，支持别名、分类与过滤查询。
// and filtering. It replaces raw []Tool slices for more flexible tool discovery.
type ToolRegistry struct {
	mu       sync.RWMutex
	tools    map[string]Tool     // name -> tool
	aliases  map[string]string   // alias -> canonical name
	category map[string][]string // category -> tool names
}

// NewToolRegistry 创建空注册表。
func NewToolRegistry() *ToolRegistry {
	return &ToolRegistry{
		tools:    make(map[string]Tool),
		aliases:  make(map[string]string),
		category: make(map[string][]string),
	}
}

// Register 注册工具并可附加别名与分类。
func (r *ToolRegistry) Register(tool Tool, opts ...RegistryOption) {
	r.mu.Lock()
	defer r.mu.Unlock()
	name := tool.Name()
	r.tools[name] = tool
	for _, opt := range opts {
		opt(name, r)
	}
}

// RegistryOption 注册时的可选配置。
type RegistryOption func(name string, r *ToolRegistry)

// WithAlias 为工具注册别名。
func WithAlias(alias string) RegistryOption {
	return func(name string, r *ToolRegistry) {
		r.aliases[alias] = name
	}
}

// WithCategory 将工具归入一个或多个分类。
func WithCategory(categories ...string) RegistryOption {
	return func(name string, r *ToolRegistry) {
		for _, cat := range categories {
			r.category[cat] = append(r.category[cat], name)
		}
	}
}

// Lookup 按名称或别名查找工具。
func (r *ToolRegistry) Lookup(name string) (Tool, bool) {
	r.mu.RLock()
	defer r.mu.RUnlock()
	if t, ok := r.tools[name]; ok {
		return t, true
	}
	if canonical, ok := r.aliases[name]; ok {
		t, ok := r.tools[canonical]
		return t, ok
	}
	return nil, false
}

// LookupByCategory 返回指定分类下的全部工具。
func (r *ToolRegistry) LookupByCategory(category string) []Tool {
	r.mu.RLock()
	defer r.mu.RUnlock()
	names := r.category[category]
	result := make([]Tool, 0, len(names))
	for _, n := range names {
		if t, ok := r.tools[n]; ok {
			result = append(result, t)
		}
	}
	return result
}

// AllTools 返回所有已注册工具的切片。
func (r *ToolRegistry) AllTools() []Tool {
	r.mu.RLock()
	defer r.mu.RUnlock()
	result := make([]Tool, 0, len(r.tools))
	for _, t := range r.tools {
		result = append(result, t)
	}
	return result
}

// ToSlice 转为 []Tool 以兼容现有 API。
func (r *ToolRegistry) ToSlice() []Tool {
	return r.AllTools()
}

// Merge 合并另一注册表，冲突时以源覆盖；自合并为 no-op。
// Uses a snapshot-then-apply pattern to avoid deadlock: other's data is read under
// RLock before locking r. Self-merge (r.Merge(r)) is handled as a no-op.
func (r *ToolRegistry) Merge(other *ToolRegistry) {
	if r == other {
		return
	}

	// Snapshot other's data under read lock.
	other.mu.RLock()
	tools := make(map[string]Tool, len(other.tools))
	for k, v := range other.tools {
		tools[k] = v
	}
	aliases := make(map[string]string, len(other.aliases))
	for k, v := range other.aliases {
		aliases[k] = v
	}
	categories := make(map[string][]string, len(other.category))
	for k, v := range other.category {
		categories[k] = append([]string{}, v...)
	}
	other.mu.RUnlock()

	// Apply snapshot under our write lock.
	r.mu.Lock()
	defer r.mu.Unlock()
	for name, tool := range tools {
		r.tools[name] = tool
	}
	for alias, canonical := range aliases {
		r.aliases[alias] = canonical
	}
	for cat, names := range categories {
		r.category[cat] = append(r.category[cat], names...)
	}
}

// Filter 按谓词筛选并返回新注册表。
func (r *ToolRegistry) Filter(fn func(Tool) bool) *ToolRegistry {
	r.mu.RLock()
	defer r.mu.RUnlock()
	result := NewToolRegistry()
	for _, t := range r.tools {
		if fn(t) {
			result.Register(t)
		}
	}
	return result
}

// MustLookup 查找工具，未找到则 panic（适用于 init）。
func (r *ToolRegistry) MustLookup(name string) Tool {
	t, ok := r.Lookup(name)
	if !ok {
		panic(fmt.Sprintf("tool '%s' not found in registry", name))
	}
	return t
}

// Unregister 按名称移除工具及其别名/分类引用。
func (r *ToolRegistry) Unregister(name string) {
	r.mu.Lock()
	defer r.mu.Unlock()
	delete(r.tools, name)
	for alias, canonical := range r.aliases {
		if canonical == name {
			delete(r.aliases, alias)
		}
	}
	for cat, names := range r.category {
		filtered := names[:0]
		for _, n := range names {
			if n != name {
				filtered = append(filtered, n)
			}
		}
		if len(filtered) == 0 {
			delete(r.category, cat)
		} else {
			r.category[cat] = filtered
		}
	}
}

// ToolInfos 返回全部工具的元数据；实现 ToolInfoProvider 时使用完整 schema。
// ToolInfoProvider, its full structured info is used; otherwise a minimal
// Name+Description info is created.
func (r *ToolRegistry) ToolInfos() []*schema.ToolInfo {
	r.mu.RLock()
	defer r.mu.RUnlock()
	infos := make([]*schema.ToolInfo, 0, len(r.tools))
	for _, t := range r.tools {
		if p, ok := t.(ToolInfoProvider); ok {
			infos = append(infos, p.ToolInfo())
		} else {
			infos = append(infos, &schema.ToolInfo{Name: t.Name(), Description: t.Description()})
		}
	}
	return infos
}
