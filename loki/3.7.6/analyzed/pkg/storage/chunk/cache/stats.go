package cache

// stats 为 Cache 装饰器，将 Store/Fetch 操作写入 logqlmodel/stats 上下文，供查询路径观测缓存命中率、下载耗时与字节量。

import (
	"context"
	"time"

	"github.com/grafana/loki/v3/pkg/logqlmodel/stats"
)

type statsCollector struct {
	Cache
}

// CollectStats 包装任意 Cache 实现，不改变缓存语义仅增加可观测性。
// CollectStats returns a new Cache that keeps various statistics on cache usage.
func CollectStats(cache Cache) Cache {
	return &statsCollector{
		Cache: cache,
	}
}

func (s statsCollector) Store(ctx context.Context, keys []string, bufs [][]byte) error {
	st := stats.FromContext(ctx)
	st.AddCacheRequest(s.Cache.GetCacheType(), 1)

	// we blindly count the number of keys to be stored since we can't know if these will actually be written back to
	// the cache successfully if cache.backgroundCache is in use
	st.AddCacheEntriesStored(s.Cache.GetCacheType(), len(keys))

	return s.Cache.Store(ctx, keys, bufs)
}

func (s statsCollector) Fetch(ctx context.Context, keys []string) (found []string, bufs [][]byte, missing []string, err error) {
	st := stats.FromContext(ctx)
	st.AddCacheRequest(s.Cache.GetCacheType(), 1)

	start := time.Now()

	found, bufs, missing, err = s.Cache.Fetch(ctx, keys)

	st.AddCacheDownloadTime(s.Cache.GetCacheType(), time.Since(start))
	st.AddCacheEntriesFound(s.Cache.GetCacheType(), len(found))
	st.AddCacheEntriesRequested(s.Cache.GetCacheType(), len(keys))

	for j := range bufs {
		st.AddCacheBytesRetrieved(s.Cache.GetCacheType(), len(bufs[j]))
	}

	return found, bufs, missing, err
}

func (s statsCollector) Stop() {
	s.Cache.Stop()
}

func (s statsCollector) GetCacheType() stats.CacheType {
	return s.Cache.GetCacheType()
}
// Store 按 keys 数量计数 entries stored；Fetch 记录请求数、命中数、耗时与检索字节。
