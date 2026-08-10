package cache

// snappy 包装 Cache 接口：Store 前压缩、Fetch 后解压，降低 Memcached/Redis 等远程缓存的网络与内存占用。

import (
	"context"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/golang/snappy"

	"github.com/grafana/loki/v3/pkg/logqlmodel/stats"
)

type snappyCache struct {
	next   Cache
	logger log.Logger
}

// NewSnappy 构造压缩层，GetCacheType 透传底层类型以便统计区分。
// NewSnappy makes a new snappy encoding cache wrapper.
func NewSnappy(next Cache, logger log.Logger) Cache {
	return &snappyCache{
		next:   next,
		logger: logger,
	}
}

func (s *snappyCache) Store(ctx context.Context, keys []string, bufs [][]byte) error {
	cs := make([][]byte, 0, len(bufs))
	for _, buf := range bufs {
		c := snappy.Encode(nil, buf)
		cs = append(cs, c)
	}
	return s.next.Store(ctx, keys, cs)
}

func (s *snappyCache) Fetch(ctx context.Context, keys []string) ([]string, [][]byte, []string, error) {
	found, bufs, missing, err := s.next.Fetch(ctx, keys)
	ds := make([][]byte, 0, len(bufs))
	for _, buf := range bufs {
		d, err := snappy.Decode(nil, buf)
		if err != nil {
			level.Error(s.logger).Log("msg", "failed to decode cache entry", "err", err)
			return nil, nil, keys, err
		}
		ds = append(ds, d)
	}
	return found, ds, missing, err
}

func (s *snappyCache) Stop() {
	s.next.Stop()
}

func (c *snappyCache) GetCacheType() stats.CacheType {
	return c.next.GetCacheType()
}
// Store 逐条 snappy.Encode；Fetch 逐条 Decode，失败时返回 keys 作为全部 missing。
