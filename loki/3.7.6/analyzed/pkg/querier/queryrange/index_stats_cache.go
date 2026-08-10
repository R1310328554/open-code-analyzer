package queryrange

// index_stats_cache 为 IndexStats 查询提供 results cache 中间件：按时间比例 Extract 子区间统计并尊重 max_stats_cache_freshness。

import (
	"context"
	"flag"
	"fmt"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/tenant"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
	"github.com/grafana/loki/v3/pkg/storage/chunk/cache"
	"github.com/grafana/loki/v3/pkg/storage/chunk/cache/resultscache"
	"github.com/grafana/loki/v3/pkg/util"
	"github.com/grafana/loki/v3/pkg/util/validation"
)

// IndexStatsSplitter 在通用 cache key 前加 indexStats: 前缀区分统计缓存。
type IndexStatsSplitter struct {
	cacheKeyLimits
}

// GenerateCacheKey generates a cache key based on the userID, Request and interval.
func (i IndexStatsSplitter) GenerateCacheKey(ctx context.Context, userID string, r resultscache.Request) string {
	cacheKey := i.cacheKeyLimits.GenerateCacheKey(ctx, userID, r)
	return fmt.Sprintf("indexStats:%s", cacheKey)
}

type IndexStatsExtractor struct{}

// IndexStatsExtractor 假设日志量均匀分布，按时间因子比例缩放 bytes/entries 等字段。
// Extract favors the ability to cache over exactness of results. It assumes a constant distribution
// of log volumes over a range and will extract subsets proportionally.
func (p IndexStatsExtractor) Extract(start, end int64, res resultscache.Response, resStart, resEnd int64) resultscache.Response {
	factor := util.GetFactorOfTime(start, end, resStart, resEnd)

	statsRes := res.(*IndexStatsResponse)
	return &IndexStatsResponse{
		Response: &logproto.IndexStatsResponse{
			Streams: statsRes.Response.GetStreams(),
			Chunks:  statsRes.Response.GetChunks(),
			Bytes:   uint64(float64(statsRes.Response.GetBytes()) * factor),
			Entries: uint64(float64(statsRes.Response.GetEntries()) * factor),
		},
	}
}

func (p IndexStatsExtractor) ResponseWithoutHeaders(resp queryrangebase.Response) queryrangebase.Response {
	statsRes := resp.(*IndexStatsResponse)
	return &IndexStatsResponse{
		Response: statsRes.Response,
	}
}

// IndexStatsCacheConfig 嵌入 ResultsCacheConfig，flag 前缀为 frontend.index-stats-results-cache。
type IndexStatsCacheConfig struct {
	queryrangebase.ResultsCacheConfig `yaml:",inline"`
}

// RegisterFlags registers flags.
func (cfg *IndexStatsCacheConfig) RegisterFlags(f *flag.FlagSet) {
	cfg.RegisterFlagsWithPrefix(f, "frontend.index-stats-results-cache.")
}

func (cfg *IndexStatsCacheConfig) Validate() error {
	return cfg.ResultsCacheConfig.Validate()
}

// statsCacheMiddlewareNowTimeFunc is a function that returns the current time.
// It is used to allow tests to override the current time.
var statsCacheMiddlewareNowTimeFunc = model.Now

// shouldCacheStats 在请求结束时间落入 max_stats_cache_freshness 窗口内时跳过缓存。
// shouldCacheStats returns true if the request should be cached.
// It returns false if:
// - The request end time falls within the max_stats_cache_freshness duration.
func shouldCacheStats(ctx context.Context, req queryrangebase.Request, lim Limits) (bool, error) {
	tenantIDs, err := tenant.TenantIDs(ctx)
	if err != nil {
		return false, err
	}

	cacheFreshnessCapture := func(id string) time.Duration { return lim.MaxStatsCacheFreshness(ctx, id) }
	maxCacheFreshness := validation.MaxDurationPerTenant(tenantIDs, cacheFreshnessCapture)

	now := statsCacheMiddlewareNowTimeFunc()
	return maxCacheFreshness == 0 || model.Time(req.GetEnd().UnixMilli()).Before(now.Add(-maxCacheFreshness)), nil
}

func NewIndexStatsCacheMiddleware(
	log log.Logger,
	limits Limits,
	merger queryrangebase.Merger,
	c cache.Cache,
	cacheGenNumberLoader queryrangebase.CacheGenNumberLoader,
	iqo util.IngesterQueryOptions,
	shouldCache queryrangebase.ShouldCacheFn,
	parallelismForReq queryrangebase.ParallelismForReqFn,
	retentionEnabled bool,
	transformer UserIDTransformer,
	metrics *queryrangebase.ResultsCacheMetrics,
) (queryrangebase.Middleware, error) {
	return queryrangebase.NewResultsCacheMiddleware(
		log,
		c,
		IndexStatsSplitter{cacheKeyLimits{limits, transformer, iqo}},
		limits,
		merger,
		IndexStatsExtractor{},
		cacheGenNumberLoader,
		func(ctx context.Context, r queryrangebase.Request) bool {
			if shouldCache != nil && !shouldCache(ctx, r) {
				return false
			}

			cacheStats, err := shouldCacheStats(ctx, r, limits)
			if err != nil {
				level.Error(log).Log("msg", "failed to determine if stats should be cached. Won't cache", "err", err)
				return false
			}

			return cacheStats
		},
		parallelismForReq,
		retentionEnabled,
		false,
		metrics,
	)
}
// NewIndexStatsCacheMiddleware 组合 Splitter、Extractor 与租户 freshness 判定注入缓存链。
