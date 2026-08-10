package goldfish

// storage 定义 Goldfish 持久化抽象：querytee 写入采样与对比结果，UI 侧只读查询与统计。

import (
	"context"
)

// Storage 接口分离写路径（Store*）与读路径（Get*），Close 释放连接池等资源。
// Storage defines the interface for storing and retrieving query samples and comparison results
type Storage interface {
	// Write operations (used by querytee)
	StoreQuerySample(ctx context.Context, sample *QuerySample, comparison *ComparisonResult) error
	StoreComparisonResult(ctx context.Context, result *ComparisonResult) error

	// Read operations (used by UI)
// GetSampledQueries 分页返回采样列表，支持 QueryFilter 过滤。
	GetSampledQueries(ctx context.Context, page, pageSize int, filter QueryFilter) (*APIResponse, error)
	GetQueryByCorrelationID(ctx context.Context, correlationID string) (*QuerySample, error)
	GetStatistics(ctx context.Context, filter StatsFilter) (*Statistics, error)

	// Lifecycle
	Close() error
}

// APIResponse 含 queries 切片、HasMore 翻页标志及 page/pageSize 元数据。
// APIResponse represents the paginated API response for UI
type APIResponse struct {
	Queries  []QuerySample `json:"queries"`
	HasMore  bool          `json:"hasMore"`
	Page     int           `json:"page"`
	PageSize int           `json:"pageSize"`
}
// GetQueryByCorrelationID 按关联 ID 拉取单次采样详情，便于 UI 深链。
