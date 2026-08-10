package queryrange

// instant_metric_cache 为即时 metric 查询提供 results cache：cache key 含 currentInterval 与 split duration 防止 interval 变更复用旧条目。

import (
	"context"
	"flag"
	"fmt"
	"time"

	"github.com/go-kit/log"

	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
	"github.com/grafana/loki/v3/pkg/storage/chunk/cache"
	"github.com/grafana/loki/v3/pkg/storage/chunk/cache/resultscache"
)

// InstantMetricSplitter 按 InstantMetricQuerySplitDuration 划分时间桶生成缓存键。
type InstantMetricSplitter struct {
	Limits
	transformer UserIDTransformer
}

// GenerateCacheKey generates a cache key based on the userID, Request and interval.
func (i InstantMetricSplitter) GenerateCacheKey(ctx context.Context, userID string, r resultscache.Request) string {
	split := i.InstantMetricQuerySplitDuration(userID)

	var currentInterval int64
	if denominator := int64(split / time.Millisecond); denominator > 0 {
		currentInterval = r.GetStart().UnixMilli() / denominator
	}

	if i.transformer != nil {
		userID = i.transformer(ctx, userID)
	}

	// include both the currentInterval and the split duration in key to ensure
	// a cache key can't be reused when an interval changes
	return fmt.Sprintf("instant-metric:%s:%s:%d:%d", userID, r.GetQuery(), currentInterval, split)
}

// InstantMetricCacheConfig flag 前缀为 frontend.instant-metric-results-cache。
type InstantMetricCacheConfig struct {
	queryrangebase.ResultsCacheConfig `yaml:",inline"`
}

// RegisterFlags registers flags.
func (cfg *InstantMetricCacheConfig) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix(f, "frontend.instant-metric-results-cache.")
}

func (cfg *InstantMetricCacheConfig) Validate() error {
	return cfg.ResultsCacheConfig.Validate()
}

// NewInstantMetricCacheMiddleware 复用 PrometheusExtractor 解码 Prom 响应缓存。
func NewInstantMetricCacheMiddleware(
	log log.Logger,
	limits Limits,
	merger queryrangebase.Merger,
	c cache.Cache,
	cacheGenNumberLoader queryrangebase.CacheGenNumberLoader,
	shouldCache queryrangebase.ShouldCacheFn,
	parallelismForReq queryrangebase.ParallelismForReqFn,
	retentionEnabled bool,
	transformer UserIDTransformer,
	metrics *queryrangebase.ResultsCacheMetrics,
) (queryrangebase.Middleware, error) {
	return queryrangebase.NewResultsCacheMiddleware(
		log,
		c,
		InstantMetricSplitter{limits, transformer},
		limits,
		merger,
		PrometheusExtractor{},
		cacheGenNumberLoader,
		func(ctx context.Context, r queryrangebase.Request) bool {
			if shouldCache != nil && !shouldCache(ctx, r) {
				return false
			}
			return true
		},
		parallelismForReq,
		retentionEnabled,
		false,
		metrics,
	)
}
// UserIDTransformer 可在生成 cache key 前改写 tenant ID 以支持多租户映射场景。
