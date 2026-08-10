package cache

// cache_gen 通过 context 注入缓存代数，在键前缀附加 gen 号实现保留策略变更时的缓存隔离，对外 Fetch 仍返回原始键名。

import (
	"context"

	"github.com/grafana/loki/v3/pkg/logqlmodel/stats"
)

type contextKey int

// cacheGenContextKey 为私有 context 键，避免与其他值类型冲突。
// cacheGenContextKey is used for setting a Cache Generation number in context.
const cacheGenContextKey contextKey = 0

// GenNumMiddleware 装饰下游 Cache：Store/Fetch 前加前缀，Fetch 返回前从 found/missing 剥离 gen 前缀。
// GenNumMiddleware adds gen number to keys from context. Expected size of gen numbers is upto 2 digits.
// If we start seeing problems with keys exceeding length limit, we need to look into resetting gen numbers.
type GenNumMiddleware struct {
	downstreamCache Cache
}

// NewCacheGenNumMiddleware creates a new GenNumMiddleware.
func NewCacheGenNumMiddleware(downstreamCache Cache) Cache {
	return &GenNumMiddleware{downstreamCache}
}

// Store adds cache gen number to keys before calling Store method of downstream cache.
func (c GenNumMiddleware) Store(ctx context.Context, keys []string, buf [][]byte) error {
	keys = addCacheGenNumToCacheKeys(ctx, keys)
	return c.downstreamCache.Store(ctx, keys, buf)
}

// Fetch adds cache gen number to keys before calling Fetch method of downstream cache.
// It also removes gen number before responding back with found and missing keys to make sure consumer of response gets to see same keys.
func (c GenNumMiddleware) Fetch(ctx context.Context, keys []string) (found []string, bufs [][]byte, missing []string, err error) {
	keys = addCacheGenNumToCacheKeys(ctx, keys)

	found, bufs, missing, err = c.downstreamCache.Fetch(ctx, keys)

	found = removeCacheGenNumFromKeys(ctx, found)
	missing = removeCacheGenNumFromKeys(ctx, missing)

	return
}

// Stop calls Stop method of downstream cache.
func (c GenNumMiddleware) Stop() {
	c.downstreamCache.Stop()
}

func (c GenNumMiddleware) GetCacheType() stats.CacheType {
	return c.downstreamCache.GetCacheType()
}

// InjectCacheGenNumber/ExtractCacheGenNumber 供上游在 retention 等场景写入与读取 gen。
// InjectCacheGenNumber returns a derived context containing the cache gen.
func InjectCacheGenNumber(ctx context.Context, cacheGen string) context.Context {
	return context.WithValue(ctx, interface{}(cacheGenContextKey), cacheGen)
}

// ExtractCacheGenNumber gets the cache gen from the context.
func ExtractCacheGenNumber(ctx context.Context) string {
	cacheGenNumber, ok := ctx.Value(cacheGenContextKey).(string)
	if !ok {
		return ""
	}
	return cacheGenNumber
}

// addCacheGenNumToCacheKeys 无 gen 时原样返回；removeCacheGenNumFromKeys 按前缀长度截断。
// addCacheGenNumToCacheKeys adds gen number to keys as prefix.
func addCacheGenNumToCacheKeys(ctx context.Context, keys []string) []string {
	cacheGen := ExtractCacheGenNumber(ctx)
	if cacheGen == "" {
		return keys
	}

	prefixedKeys := make([]string, len(keys))

	for i := range keys {
		prefixedKeys[i] = cacheGen + keys[i]
	}

	return prefixedKeys
}

// removeCacheGenNumFromKeys removes prefixed gen number from keys.
func removeCacheGenNumFromKeys(ctx context.Context, keys []string) []string {
	cacheGen := ExtractCacheGenNumber(ctx)
	if cacheGen == "" {
		return keys
	}

	unprefixedKeys := make([]string, len(keys))
	cacheGenPrefixLen := len(cacheGen)

	for i := range keys {
		unprefixedKeys[i] = keys[i][cacheGenPrefixLen:]
	}

	return unprefixedKeys
}
// gen 号预期不超过两位；键过长时需考虑重置 gen 以免 memcache 键长度超限。
