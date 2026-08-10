package limits

// limits 子包定义 queryrange Limits 接口扩展：从主包抽出以避免与 logql、queryrangebase 之间的 import cycle。

import (
	"context"
	"time"

	"github.com/grafana/loki/v3/pkg/logql"
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
)

// Limits 组合 queryrangebase.Limits 与 logql.Limits，并增加 split、并行度、分片与缓存 freshness 等方法。
// Limits extends the cortex limits interface with support for per tenant splitby parameters
// They've been extracted to avoid import cycles.
type Limits interface {
	queryrangebase.Limits
	logql.Limits
	QuerySplitDuration(string) time.Duration
	InstantMetricQuerySplitDuration(string) time.Duration
	EngineResultsCacheTimeBucketInterval(string) time.Duration
	MetadataQuerySplitDuration(string) time.Duration
	RecentMetadataQuerySplitDuration(string) time.Duration
	RecentMetadataQueryWindow(string) time.Duration
	IngesterQuerySplitDuration(string) time.Duration
	MaxQuerySeries(context.Context, string) int
	MaxEntriesLimitPerQuery(context.Context, string) int
	MinShardingLookback(string) time.Duration
// TSDBMaxQueryParallelism 限制 frontend 对 TSDB 索引 period 并行处理的 split 数量。
	// TSDBMaxQueryParallelism returns the limit to the number of split queries the
	// frontend will process in parallel for TSDB queries.
	TSDBMaxQueryParallelism(context.Context, string) int
// TSDBMaxBytesPerShard 与 TSDBShardingStrategy 控制 TSDB 查询分片大小与策略。
	// TSDBMaxBytesPerShard returns the limit to the number of bytes a single shard
	TSDBMaxBytesPerShard(string) int
	TSDBShardingStrategy(userID string) string

	RequiredLabels(context.Context, string) []string
	RequiredNumberLabels(context.Context, string) int
	MaxQueryBytesRead(context.Context, string) int
	MaxQuerierBytesRead(context.Context, string) int
	MaxStatsCacheFreshness(context.Context, string) time.Duration
	MaxMetadataCacheFreshness(context.Context, string) time.Duration
	VolumeEnabled(string) bool

	ShardAggregations(string) []string
}
// VolumeEnabled、ShardAggregations 等字段供 volume 查询与聚合分片中间件读取租户配置。
