package cache

// redis_cache 将 Cache 接口适配到 RedisClient：Fetch 用 MGet 按序拆分 hit/miss，Store 用 MSet 批量写入。

import (
	"context"
	"fmt"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"

	"github.com/grafana/loki/v3/pkg/logqlmodel/stats"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

// RedisCache 构造时 Ping 探测连通性并打实验特性警告日志。
// RedisCache type caches chunks in redis
type RedisCache struct {
	name      string
	cacheType stats.CacheType
	redis     *RedisClient
	logger    log.Logger
}

// NewRedisCache 包装已建好的 RedisClient，name 用于日志与指标标签。
// NewRedisCache creates a new RedisCache
func NewRedisCache(name string, redisClient *RedisClient, logger log.Logger, cacheType stats.CacheType) *RedisCache {
	util_log.WarnExperimentalUse(fmt.Sprintf("Redis cache - %s", name), logger)
	cache := &RedisCache{
		name:      name,
		redis:     redisClient,
		logger:    logger,
		cacheType: cacheType,
	}
	if err := cache.redis.Ping(context.Background()); err != nil {
		level.Error(logger).Log("msg", "error connecting to redis", "name", name, "err", err)
	}
	return cache
}

// Fetch 出错时全部键记入 missed；成功时按 keys 顺序对齐 MGet 结果槽位。
// Fetch gets keys from the cache. The keys that are found must be in the order of the keys requested.
func (c *RedisCache) Fetch(ctx context.Context, keys []string) (found []string, bufs [][]byte, missed []string, err error) {
	data, err := c.redis.MGet(ctx, keys)
	if err != nil {
		level.Error(c.logger).Log("msg", "failed to get from redis", "name", c.name, "err", err)
		missed = make([]string, len(keys))
		copy(missed, keys)
		return
	}
	for i, key := range keys {
		if data[i] != nil {
			found = append(found, key)
			bufs = append(bufs, data[i])
		} else {
			missed = append(missed, key)
		}
	}
	return
}

// Store stores the key in the cache.
func (c *RedisCache) Store(ctx context.Context, keys []string, bufs [][]byte) error {
	err := c.redis.MSet(ctx, keys, bufs)
	if err != nil {
		level.Error(c.logger).Log("msg", "failed to put to redis", "name", c.name, "err", err)
	}
	return err
}

// Stop stops the redis client.
func (c *RedisCache) Stop() {
	_ = c.redis.Close()
}

func (c *RedisCache) GetCacheType() stats.CacheType {
	return c.cacheType
}
// Stop 关闭底层 redis 连接池；GetCacheType 透传构造时指定的 stats 类型。
