package stats

// stats 包在 context 中累积 querier 查询统计：墙钟时间、拉取 series 数与 chunk 字节数，供 query-frontend 与 API 响应返回。

import (
	"context"
	"sync/atomic" //lint:ignore faillint we can't use go.uber.org/atomic with a protobuf struct without wrapping it.
	"time"

	"github.com/gogo/googleapis/google/rpc"
	"github.com/grafana/dskit/httpgrpc"
)

type contextKey int

var ctxKey = contextKey(0)

// ContextWithEmptyStats 在 context 中挂载空 Stats，启用后续中间件与 store 累加。
// ContextWithEmptyStats returns a context with empty stats.
func ContextWithEmptyStats(ctx context.Context) (*Stats, context.Context) {
	stats := &Stats{}
	ctx = context.WithValue(ctx, ctxKey, stats)
	return stats, ctx
}

// FromContext 读取 context 中的 Stats，未初始化时返回 nil。
// FromContext gets the Stats out of the Context. Returns nil if stats have not
// been initialised in the context.
func FromContext(ctx context.Context) *Stats {
	o := ctx.Value(ctxKey)
	if o == nil {
		return nil
	}
	return o.(*Stats)
}

// IsEnabled 通过 FromContext 非 nil 判断当前请求是否追踪统计。
// IsEnabled returns whether stats tracking is enabled in the context.
func IsEnabled(ctx context.Context) bool {
	// When query statistics are enabled, the stats object is already initialised
	// within the context, so we can just check it.
	return FromContext(ctx) != nil
}

// AddWallTime/Merge 使用 atomic 累加，支持并行子查询合并统计。
// AddWallTime adds some time to the counter.
func (s *Stats) AddWallTime(t time.Duration) {
	if s == nil {
		return
	}

	atomic.AddInt64((*int64)(&s.WallTime), int64(t))
}

// LoadWallTime returns current wall time.
func (s *Stats) LoadWallTime() time.Duration {
	if s == nil {
		return 0
	}

	return time.Duration(atomic.LoadInt64((*int64)(&s.WallTime)))
}

func (s *Stats) AddFetchedSeries(series uint64) {
	if s == nil {
		return
	}

	atomic.AddUint64(&s.FetchedSeriesCount, series)
}

func (s *Stats) LoadFetchedSeries() uint64 {
	if s == nil {
		return 0
	}

	return atomic.LoadUint64(&s.FetchedSeriesCount)
}

func (s *Stats) AddFetchedChunkBytes(bytes uint64) {
	if s == nil {
		return
	}

	atomic.AddUint64(&s.FetchedChunkBytes, bytes)
}

func (s *Stats) LoadFetchedChunkBytes() uint64 {
	if s == nil {
		return 0
	}

	return atomic.LoadUint64(&s.FetchedChunkBytes)
}

// Merge the provide Stats into this one.
func (s *Stats) Merge(other *Stats) {
	if s == nil || other == nil {
		return
	}

	s.AddWallTime(other.LoadWallTime())
	s.AddFetchedSeries(other.LoadFetchedSeries())
	s.AddFetchedChunkBytes(other.LoadFetchedChunkBytes())
}

// ShouldTrackHTTPGRPCResponse 对 HTTP 5xx 不计入统计，避免失败请求污染指标。
func ShouldTrackHTTPGRPCResponse(r *httpgrpc.HTTPResponse) bool {
	// Do no track statistics for requests failed because of a server error.
	return r.Code < 500
}

func ShouldTrackQueryResponse(s *rpc.Status) bool {
	// Do no track statistics for requests failed because of a server error.
	// See HTTP mappings in
	// https://github.com/gogo/googleapis/blob/master/google/rpc/code.proto.
	return s.Code == int32(rpc.UNKNOWN) || s.Code == int32(rpc.DEADLINE_EXCEEDED) ||
		s.Code == int32(rpc.UNIMPLEMENTED) || s.Code == int32(rpc.INTERNAL) ||
		s.Code == int32(rpc.UNAVAILABLE) || s.Code == int32(rpc.DATA_LOSS) ||
		(s.Code > 200 && s.Code < 500)
}
// Stats 字段与 stats.proto 一致，JSON 序列化供 query_range 响应 stats 块使用。
