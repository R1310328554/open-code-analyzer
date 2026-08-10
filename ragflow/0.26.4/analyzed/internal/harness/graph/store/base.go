package store

// base.go — Store 抽象接口：命名空间 KV、搜索、Batch 与 TTL/索引扩展。


import (
	"context"
	"time"
)

// BaseStore 存储抽象接口，支持命名空间隔离的 get/put/search。
// 提供 Batch、GetItem/PutItem、SearchItems 与 ListNamespaces 等高级操作。
type BaseStore interface {
	// Get 按命名空间与 key 读取值。
	// Returns the value if found, nil if not found.
	Get(ctx context.Context, namespace []string, key string) (map[string]interface{}, error)

	// Put 写入 map 值。
	Put(ctx context.Context, namespace []string, key string, value map[string]interface{}) error

	// Delete 删除指定 key。
	Delete(ctx context.Context, namespace []string, key string) error

	// Search 在命名空间内按 query 搜索（实现相关）。
	// The query format is implementation-specific.
	Search(ctx context.Context, namespace []string, query string, limit int) ([]map[string]interface{}, error)

	// List 列出命名空间内 key。
	List(ctx context.Context, namespace []string, limit int) ([]string, error)

	// Batch 原子执行多条 Op。
	Batch(ctx context.Context, ops []Op) ([]Result, error)

	// GetItem 返回值及创建/更新时间元数据。
	GetItem(ctx context.Context, namespace []string, key string, refreshTTL *bool) (*Item, error)

	// PutItem 带 TTL 与索引选项写入。
	PutItem(ctx context.Context, namespace []string, key string, value map[string]interface{},
		index interface{}, ttl *time.Duration) error

	// SearchItems 高级过滤与自然语言查询搜索。
	SearchItems(ctx context.Context, namespace []string, query *string, filter map[string]interface{},
		limit, offset int, refreshTTL *bool) ([]*SearchItem, error)

	// ListNamespaces 按 MatchCondition 列出命名空间。
	ListNamespaces(ctx context.Context, conditions []MatchCondition, maxDepth *int,
		limit, offset int) ([][]string, error)
}

// Op 存储操作标记接口。
type Op interface{}

// GetOp 批量读操作。
type GetOp struct {
	Namespace  []string
	Key        string
	RefreshTTL bool
}

// PutOp 批量写操作。
type PutOp struct {
	Namespace []string
	Key       string
	Value     map[string]interface{}
	Index     interface{} // false, nil, or []string
	TTL       *time.Duration
}

// SearchOp 批量搜索操作。
type SearchOp struct {
	NamespacePrefix []string
	Filter          map[string]interface{}
	Limit           int
	Offset          int
	Query           *string // natural language query
	RefreshTTL      bool
}

// ListNamespacesOp 批量列命名空间操作。
type ListNamespacesOp struct {
	MatchConditions []MatchCondition
	MaxDepth        *int
	Limit           int
	Offset          int
}

// Result 单条 Batch 操作结果。
type Result struct {
	Value interface{}
	Error error
}

// Item 含 TTL 与时间的存储项。
type Item struct {
	Value     map[string]interface{}
	Key       string
	Namespace []string
	CreatedAt time.Time
	UpdatedAt time.Time
	ExpiresAt *time.Time
}

// SearchItem 带相似度分数的搜索结果。
type SearchItem struct {
	*Item
	Score *float64
}

// MatchCondition 命名空间前缀/后缀匹配条件。
type MatchCondition struct {
	MatchType string // "prefix" or "suffix"
	Path      []string
}

// TTLConfig TTL 刷新与清扫配置。
type TTLConfig struct {
	RefreshOnRead bool
	DefaultTTL    *time.Duration
	SweepInterval *time.Duration
}

// IndexConfig 向量维度与嵌入字段配置。
type IndexConfig struct {
	Dims   int
	Embed  interface{} // embedding function
	Fields []string
}

// PutOperation 已弃用，请使用 PutOp。
type PutOperation struct {
	Namespace []string
	Key       string
	Value     map[string]interface{}
}

// SearchOptions 已弃用搜索选项。
type SearchOptions struct {
	Limit    int
	Offset   int
	Filter   map[string]interface{}
	SortBy   string
	SortDesc bool
}

// Store 供 Agent 长期记忆与跨会话状态；与 checkpointer 互补而非替代。
