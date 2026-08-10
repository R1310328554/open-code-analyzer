package resultscache

// resultscache 定义 Request/Response/Handler 等扩展点：KeyGenerator、Extractor、ResponseMerger 供不同查询类型 plug-in 实现。

import (
	"context"
	"time"

	"github.com/gogo/protobuf/proto"
)

type Request interface {
	proto.Message
	// GetStart returns the start timestamp of the request in milliseconds.
	GetStart() time.Time
	// GetEnd returns the end timestamp of the request in milliseconds.
	GetEnd() time.Time
	// GetStep returns the step of the request in milliseconds.
	GetStep() int64
	// GetQuery returns the query of the request.
	GetQuery() string
	// GetCachingOptions returns the caching options.
	GetCachingOptions() CachingOptions
	// WithStartEndForCache clone the current request with different start and end timestamp.
	WithStartEndForCache(start time.Time, end time.Time) Request
}

type Response interface {
	proto.Message
}

// ResponseMerger 将 partition 产生的多段子响应合并为单一 Response。
// ResponseMerger is used by middlewares making multiple requests to merge back all responses into a single one.
type ResponseMerger interface {
	// MergeResponse merges responses from multiple requests into a single Response
	MergeResponse(...Response) (Response, error)
}

type Handler interface {
	Do(ctx context.Context, req Request) (Response, error)
}

// Extractor 从缓存 extent 中按毫秒时间窗裁剪 overlap 片段供 merge 使用。
// Extractor is used by the cache to extract a subset of a response from a cache entry.
type Extractor interface {
	// Extract extracts a subset of a response from the `start` and `end` timestamps in milliseconds
	// in the `res` response which spans from `resStart` to `resEnd`.
	Extract(start, end int64, res Response, resStart, resEnd int64) Response
}

// KeyGenerator 允许自定义缓存键策略；CacheGenNumberLoader 提供租户级 gen 号。
// KeyGenerator generates cache keys. This is a useful interface for downstream
// consumers who wish to implement their own strategies.
type KeyGenerator interface {
	GenerateCacheKey(ctx context.Context, userID string, r Request) string
}

type CacheGenNumberLoader interface {
	GetResultsCacheGenNumber(tenantIDs []string) string
	Stop()
}
// Handler.Do 为中间件链下游；CachingOptions 由具体 Request 实现暴露缓存行为选项。
