package queryrange

// instrument 提供 queryrange 中间件的 Prometheus 指标与 OpenTelemetry 追踪：记录 inflight、延迟分位数及按路由 span。

import (
	"context"
	"strconv"
	"time"

	"github.com/grafana/dskit/httpgrpc"
	"github.com/grafana/dskit/instrument"
	"github.com/grafana/dskit/middleware"

	"github.com/grafana/dskit/server"

	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
)

const (
	method = "GET"
)

// Instrument 包装 server.Metrics，在 Wrap 中按 DefaultCodec.Path 打标签。
type Instrument struct {
	*server.Metrics
}

var _ queryrangebase.Middleware = Instrument{}

// Instrument.Wrap 递增 inflight 计数并在 observe 中区分 httpgrpc 与 500 状态码。
// Wrap implements the queryrangebase.Middleware
func (i Instrument) Wrap(next queryrangebase.Handler) queryrangebase.Handler {
	return queryrangebase.HandlerFunc(func(ctx context.Context, r queryrangebase.Request) (queryrangebase.Response, error) {
		route := DefaultCodec.Path(r)
		route = middleware.MakeLabelValue(route)
		inflight := i.InflightRequests.WithLabelValues(method, route)
		inflight.Inc()
		defer inflight.Dec()

		begin := time.Now()
		result, err := next.Do(ctx, r)
		i.observe(ctx, route, err, time.Since(begin))

		return result, err
	})
}

func (i Instrument) observe(ctx context.Context, route string, err error, duration time.Duration) {
	respStatus := "200"
	if err != nil {
		if errResp, ok := httpgrpc.HTTPResponseFromError(err); ok {
			respStatus = strconv.Itoa(int(errResp.Code))
		} else {
			respStatus = "500"
		}
	}
	instrument.ObserveWithExemplar(ctx, i.RequestDuration.WithLabelValues(method, route, respStatus, "false"), duration.Seconds())
}

// Tracer 为每个 queryrange 请求创建以 API 路径命名的 span。
type Tracer struct{}

var _ queryrangebase.Middleware = Tracer{}

// Wrap implements the queryrangebase.Middleware
func (t Tracer) Wrap(next queryrangebase.Handler) queryrangebase.Handler {
	return queryrangebase.HandlerFunc(func(ctx context.Context, r queryrangebase.Request) (queryrangebase.Response, error) {
		route := DefaultCodec.Path(r)
		route = middleware.MakeLabelValue(route)
		ctx, span := tracer.Start(ctx, route)
		defer span.End()

		return next.Do(ctx, r)
	})
}
// observe 使用 instrument.ObserveWithExemplar 支持 exemplar 关联 trace 上下文。
