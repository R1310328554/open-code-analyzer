package limiter

// QueryLimiter 在单次查询 context 内累计唯一 series、chunk 字节数与 chunk 数量，超限即报错。

import (
	"context"
	"fmt"
	"sync"

	"github.com/prometheus/common/model"
	"go.uber.org/atomic"

	"github.com/grafana/loki/v3/pkg/ingester/client"
	"github.com/grafana/loki/v3/pkg/logproto"
)

type queryLimiterCtxKey struct{}

var (
	ctxKey                    = &queryLimiterCtxKey{}
	ErrMaxSeriesHit           = "the query hit the max number of series limit (limit: %d series)"
	ErrMaxChunkBytesHit       = "the query hit the aggregated chunks size limit (limit: %d bytes)"
	ErrMaxChunksPerQueryLimit = "the query hit the max number of chunks limit (limit: %d chunks)"
)

// uniqueSeries 用 fingerprint 去重；chunkBytesCount/chunkCount 为原子计数，支持并发 Add。
type QueryLimiter struct {
	uniqueSeriesMx sync.Mutex
	uniqueSeries   map[model.Fingerprint]struct{}

	chunkBytesCount atomic.Int64
	chunkCount      atomic.Int64

	maxSeriesPerQuery     int
	maxChunkBytesPerQuery int
	maxChunksPerQuery     int
}

// NewQueryLimiter 接受三项上限，0 表示该维度不限制（no-op）。
// NewQueryLimiter makes a new per-query limiter. Each query limiter
// is configured using the `maxSeriesPerQuery` limit.
func NewQueryLimiter(maxSeriesPerQuery, maxChunkBytesPerQuery int, maxChunksPerQuery int) *QueryLimiter {
	return &QueryLimiter{
		uniqueSeriesMx: sync.Mutex{},
		uniqueSeries:   map[model.Fingerprint]struct{}{},

		maxSeriesPerQuery:     maxSeriesPerQuery,
		maxChunkBytesPerQuery: maxChunkBytesPerQuery,
		maxChunksPerQuery:     maxChunksPerQuery,
	}
}

// AddQueryLimiterToContext 将 limiter 绑定到 ctx，供 ingester/querier 链路共享同一计数器。
func AddQueryLimiterToContext(ctx context.Context, limiter *QueryLimiter) context.Context {
	return context.WithValue(ctx, ctxKey, limiter)
}

// QueryLimiterFromContextWithFallback 无 limiter 时返回 NewQueryLimiter(0,0,0) 无限流 fallback。
// QueryLimiterFromContextWithFallback returns a QueryLimiter from the current context.
// If there is not a QueryLimiter on the context it will return a new no-op limiter.
func QueryLimiterFromContextWithFallback(ctx context.Context) *QueryLimiter {
	ql, ok := ctx.Value(ctxKey).(*QueryLimiter)
	if !ok {
		// If there's no limiter return a new unlimited limiter as a fallback
		ql = NewQueryLimiter(0, 0, 0)
	}
	return ql
}

// AddSeries 用 client.FastFingerprint 去重计数，超过 maxSeriesPerQuery 返回格式化错误。
// AddSeries adds the input series and returns an error if the limit is reached.
func (ql *QueryLimiter) AddSeries(seriesLabels []logproto.LabelAdapter) error {
	// If the max series is unlimited just return without managing map
	if ql.maxSeriesPerQuery == 0 {
		return nil
	}
	fingerprint := client.FastFingerprint(seriesLabels)

	ql.uniqueSeriesMx.Lock()
	defer ql.uniqueSeriesMx.Unlock()

	ql.uniqueSeries[fingerprint] = struct{}{}
	if len(ql.uniqueSeries) > ql.maxSeriesPerQuery {
		// Format error with max limit
		return fmt.Errorf(ErrMaxSeriesHit, ql.maxSeriesPerQuery)
	}
	return nil
}

// AddChunkBytes 原子累加解压后 chunk 字节，超过 maxChunkBytesPerQuery 则拒绝继续读取。
// AddChunkBytes adds the input chunk size in bytes and returns an error if the limit is reached.
func (ql *QueryLimiter) AddChunkBytes(chunkSizeInBytes int) error {
	if ql.maxChunkBytesPerQuery == 0 {
		return nil
	}
	if ql.chunkBytesCount.Add(int64(chunkSizeInBytes)) > int64(ql.maxChunkBytesPerQuery) {
		return fmt.Errorf(ErrMaxChunkBytesHit, ql.maxChunkBytesPerQuery)
	}
	return nil
}

// AddChunks 原子累加 chunk 条数，超过 maxChunksPerQuery 时返回 ErrMaxChunksPerQueryLimit。
func (ql *QueryLimiter) AddChunks(count int) error {
	if ql.maxChunksPerQuery == 0 {
		return nil
	}

	if ql.chunkCount.Add(int64(count)) > int64(ql.maxChunksPerQuery) {
		return fmt.Errorf("%s %d", ErrMaxChunksPerQueryLimit, ql.maxChunksPerQuery)
	}
	return nil
}
// ErrMaxSeriesHit 等常量供 fmt.Errorf 格式化，向用户展示明确的查询资源上限信息。
