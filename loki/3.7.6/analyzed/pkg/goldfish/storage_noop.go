package goldfish

// storage_noop 为禁用 Goldfish 功能时提供的空实现 Storage：写入方法静默成功，读取方法返回 feature disabled 错误。

import (
	"context"
	"errors"
)

// NoopStorage 满足 Storage 接口但不持久化任何数据。
// NoopStorage is a no-op implementation of the Storage interface
type NoopStorage struct{}

// NewNoopStorage 返回无状态的 NoopStorage 实例。
// NewNoopStorage creates a new no-op storage backend
func NewNoopStorage() *NoopStorage {
	return &NoopStorage{}
}

// StoreQuerySample 直接返回 nil，丢弃采样数据。
// StoreQuerySample is a no-op
func (n *NoopStorage) StoreQuerySample(_ context.Context, _ *QuerySample, _ *ComparisonResult) error {
	return nil
}

// StoreComparisonResult 直接返回 nil，不记录对比结果。
// StoreComparisonResult is a no-op
func (n *NoopStorage) StoreComparisonResult(_ context.Context, _ *ComparisonResult) error {
	return nil
}

// GetSampledQueries 返回 goldfish feature is disabled 错误。
// GetSampledQueries returns an error as goldfish is disabled
func (n *NoopStorage) GetSampledQueries(_ context.Context, _, _ int, _ QueryFilter) (*APIResponse, error) {
	return nil, errors.New("goldfish feature is disabled")
}

// GetQueryByCorrelationID 功能禁用时拒绝查询。
// GetQueryByCorrelationID returns an error as goldfish is disabled
func (n *NoopStorage) GetQueryByCorrelationID(_ context.Context, _ string) (*QuerySample, error) {
	return nil, errors.New("goldfish feature is disabled")
}

// GetStatistics 功能禁用时拒绝统计请求。
// GetStatistics returns an error as goldfish is disabled
func (n *NoopStorage) GetStatistics(_ context.Context, _ StatsFilter) (*Statistics, error) {
	return nil, errors.New("goldfish feature is disabled")
}

// Close 无资源需释放，始终返回 nil。
// Close is a no-op
func (n *NoopStorage) Close() error {
	return nil
}
// 生产环境可通过配置切换 MySQL 与 Noop 后端。
