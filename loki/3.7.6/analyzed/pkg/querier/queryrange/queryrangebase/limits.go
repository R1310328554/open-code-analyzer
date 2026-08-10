package queryrangebase

// queryrangebase 包 limits 定义 query-frontend 运行时按租户读取的限制接口：查询回溯、长度、并行度与缓存新鲜度。

import (
	"context"
	"time"
)

// Limits 由 limits 子系统实现，middleware 在 split/cache 前查询租户策略。
// Limits allows us to specify per-tenant runtime limits on the behavior of
// the query handling code.
type Limits interface {
// MaxQueryLookback 限制查询可回溯的最长时间窗口。
	// MaxQueryLookback returns the max lookback period of queries.
	MaxQueryLookback(context.Context, string) time.Duration

	// MaxQueryLength returns the limit of the length (in time) of a query.
	MaxQueryLength(context.Context, string) time.Duration

// MaxQueryParallelism 约束 frontend 同时处理的 split 子查询数量。
	// MaxQueryParallelism returns the limit to the number of split queries the
	// frontend will process in parallel.
	MaxQueryParallelism(context.Context, string) int

// MaxCacheFreshness 定义多近期内的结果不可缓存，防止缓存刚写入的数据。
	// MaxCacheFreshness returns the period after which results are cacheable,
	// to prevent caching of very recent results.
	MaxCacheFreshness(context.Context, string) time.Duration
}
// MaxQueryLength 与 validation 层配合拒绝超长区间，保护 querier 与对象存储。
