package frontend

// frontend 客户端装饰层：acceptedStreamsCache 过滤已接受流，降低 limits 后端 RPC 压力。

import (
	"context"
	"math/rand"
	"sync"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/limits/proto"
)

// limitsClient 抽象 ExceedsLimits 与 UpdateRates，便于 ring 与 cache 装饰组合。
type limitsClient interface {
	// ExceedsLimits checks if any streams in the request have exceeded their
	// limits. It returns a response containing any rejected streams and the
	// reason each stream was rejected. If the response is empty, all streams
	// were accepted.
	ExceedsLimits(context.Context, *proto.ExceedsLimitsRequest) (*proto.ExceedsLimitsResponse, error)

	// UpdateRates updates the per-second rates for the streams.
	UpdateRates(context.Context, *proto.UpdateRatesRequest) (*proto.UpdateRatesResponse, error)
}

// cacheLimitsClient 在 miss 时委托 onMiss，命中缓存的流不再向后端查询。
// A cacheLimitsClient uses caches to reduce the load on limits backends.
type cacheLimitsClient struct {
	acceptedStreamsCache *acceptedStreamsCache
	onMiss               limitsClient
}

// newCacheLimitsClient returns a new cache limits client.
func newCacheLimitsClient(acceptedStreamsCache *acceptedStreamsCache, onMiss limitsClient) *cacheLimitsClient {
	return &cacheLimitsClient{
		acceptedStreamsCache: acceptedStreamsCache,
		onMiss:               onMiss,
	}
}

// ExceedsLimits implements the [limitsClient] interface.
// ExceedsLimits 先 ExpireTTL 与 FilterInPlace，仅将未见过的流转发后端。
func (c *cacheLimitsClient) ExceedsLimits(ctx context.Context, req *proto.ExceedsLimitsRequest) (*proto.ExceedsLimitsResponse, error) {
	c.acceptedStreamsCache.ExpireTTL()
	// Remove streams that have been accepted from the request. This means
	// we just check streams we haven't seen before, which reduces the
	// number of requests we need to make to the limits backends.
	c.acceptedStreamsCache.FilterInPlace(req)
	if len(req.Streams) == 0 {
		return &proto.ExceedsLimitsResponse{}, nil
	}
	// Need to check remaining streams with the limits service.
	resp, err := c.onMiss.ExceedsLimits(ctx, req)
	if err != nil {
		return nil, err
	}
	// Fast path, all streams rejected.
	if len(resp.Results) == len(req.Streams) {
		return resp, nil
	}
	// There are some accepted streams we haven't seen before, so add them
	// to the cache. We do not cache rejected streams at this time, so
	// rejections must be filtered out before updating the cache.
// 仅缓存被接受的流；拒绝结果不写入 cache，避免误跳过后续限流检查。
	rejected := make(map[uint64]struct{})
	for _, res := range resp.Results {
		rejected[res.StreamHash] = struct{}{}
	}
	accepted := make([]*proto.StreamMetadata, 0, len(req.Streams))
	for _, s := range req.Streams {
		if _, ok := rejected[s.StreamHash]; !ok {
			accepted = append(accepted, s)
		}
	}
	c.acceptedStreamsCache.Update(req.Tenant, accepted)
	return resp, nil
}

// UpdateRates implements the [limitsClient] interface.
func (c *cacheLimitsClient) UpdateRates(ctx context.Context, req *proto.UpdateRatesRequest) (*proto.UpdateRatesResponse, error) {
	return c.onMiss.UpdateRates(ctx, req)
}

// acceptedStreamsCache 按 tenant 维护 streamHash 集合，带 TTL 全量重置。
type acceptedStreamsCache struct {
	ttl time.Duration

	// The fields below MUST NOT be used without mtx.
	mtx         sync.RWMutex
	entries     map[string]map[uint64]struct{}
	entriesSize int
	lastExpired time.Time

	// Metrics.
	cacheSize prometheus.GaugeFunc
}

func newAcceptedStreamsCache(ttl, maxJitter time.Duration, r prometheus.Registerer) *acceptedStreamsCache {
	c := &acceptedStreamsCache{
		ttl:         ttl,
		entries:     make(map[string]map[uint64]struct{}, 4096),
		lastExpired: time.Now().Add(randDuration(maxJitter)),
	}
	c.cacheSize = promauto.With(r).NewGaugeFunc(prometheus.GaugeOpts{
		Name: "loki_ingest_limits_frontend_accepted_streams_cache_size",
		Help: "Current size of the accepted streams cache.",
	}, func() float64 {
		c.mtx.RLock()
		defer c.mtx.RUnlock()
		return float64(c.entriesSize)
	})
	return c
}

// ExpireTTL 双重检查锁：快路径读锁判断，过期后写锁清空 entries。
// ExpireTTL expires the caches if the TTL has been exceeded.
func (c *acceptedStreamsCache) ExpireTTL() {
	// Fast path, first check the TTL with a read lock.
	c.mtx.RLock()
	lastExpired := c.lastExpired
	c.mtx.RUnlock()
	if time.Since(lastExpired) <= c.ttl {
		return
	}
	// If we have reached here we need to reset the cache. However, before
	// we can do that we need to check the TTL a second time with an exclusive
	// lock as we could be in a data race.
	c.mtx.Lock()
	defer c.mtx.Unlock()
	if time.Since(c.lastExpired) > c.ttl {
		clear(c.entries)
		c.entriesSize = 0
		c.lastExpired = time.Now()
	}
}

// FilterInPlace 原地压缩 req.Streams 切片，移除已在缓存中的 streamHash。
// FilterInPlace removes streams that are present in the cache.
func (c *acceptedStreamsCache) FilterInPlace(req *proto.ExceedsLimitsRequest) {
	c.mtx.RLock()
	defer c.mtx.RUnlock()
	tenantEntries, ok := c.entries[req.Tenant]
	if !ok {
		return
	}
	// See https://go.dev/wiki/SliceTricks.
	filtered := req.Streams[:0]
	for _, s := range req.Streams {
		if _, found := tenantEntries[s.StreamHash]; !found {
			filtered = append(filtered, s)
		}
	}
	req.Streams = filtered
}

func (c *acceptedStreamsCache) Update(tenant string, streams []*proto.StreamMetadata) {
	c.mtx.Lock()
	defer c.mtx.Unlock()
	tenantEntries, ok := c.entries[tenant]
	if !ok {
		tenantEntries = make(map[uint64]struct{}, 64)
	}
	for _, s := range streams {
		if _, ok := tenantEntries[s.StreamHash]; !ok {
			tenantEntries[s.StreamHash] = struct{}{}
			c.entriesSize++
		}
	}
	c.entries[tenant] = tenantEntries
}

// randDuration returns a random duration between [0, d].
// randDuration 为 acceptedStreamsCache 初始过期时间注入抖动，避免齐刷刷失效。
func randDuration(d time.Duration) time.Duration {
	return time.Duration(rand.Int63n(d.Nanoseconds()))
}
// cacheSize 指标通过 GaugeFunc 实时暴露当前缓存条目总数。
