package queryrangebase

// queryrangebase 包 instrumentation 为 middleware 链注入 Prometheus 直方图，记录各中间件处理 query_range 请求的耗时与状态码。

import (
	"context"
	"time"

	"github.com/grafana/dskit/instrument"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

// InstrumentMiddleware 用 dskit instrument.CollectedRequest 包裹 next.Do 并上报 duration。
// InstrumentMiddleware can be inserted into the middleware chain to expose timing information.
func InstrumentMiddleware(name string, metrics *InstrumentMiddlewareMetrics) Middleware {
	var durationCol instrument.Collector

	// Support the case metrics shouldn't be tracked (ie. unit tests).
	if metrics != nil {
		durationCol = instrument.NewHistogramCollector(metrics.duration)
	} else {
		durationCol = &NoopCollector{}
	}

	return MiddlewareFunc(func(next Handler) Handler {
		return HandlerFunc(func(ctx context.Context, req Request) (Response, error) {
			var resp Response
			err := instrument.CollectedRequest(ctx, name, durationCol, instrument.ErrorCode, func(ctx context.Context) error {
				var err error
				resp, err = next.Do(ctx, req)
				return err
			})
			return resp, err
		})
	})
}

// InstrumentMiddlewareMetrics 持有 frontend_query_range_duration_seconds 直方图向量。
// InstrumentMiddlewareMetrics holds the metrics tracked by InstrumentMiddleware.
type InstrumentMiddlewareMetrics struct {
	duration *prometheus.HistogramVec
}

// NewInstrumentMiddlewareMetrics 在指定 metricsNamespace 下注册 method/status_code 标签。
// NewInstrumentMiddlewareMetrics makes a new InstrumentMiddlewareMetrics.
func NewInstrumentMiddlewareMetrics(registerer prometheus.Registerer, metricsNamespace string) *InstrumentMiddlewareMetrics {
	return &InstrumentMiddlewareMetrics{
		duration: promauto.With(registerer).NewHistogramVec(prometheus.HistogramOpts{
			Namespace: metricsNamespace,
			Name:      "frontend_query_range_duration_seconds",
			Help:      "Total time spent in seconds doing query range requests.",
			Buckets:   prometheus.DefBuckets,
		}, []string{"method", "status_code"}),
	}
}

// NoopCollector 在单测等场景替代真实 collector，Register/Before/After 均为空操作。
// NoopCollector is a noop collector that can be used as placeholder when no metric
// should tracked by the instrumentation.
type NoopCollector struct{}

// Register implements instrument.Collector.
func (c *NoopCollector) Register() {}

// Before implements instrument.Collector.
func (c *NoopCollector) Before(_ context.Context, _ string, _ time.Time) {}

// After implements instrument.Collector.
func (c *NoopCollector) After(_ context.Context, _, _ string, _ time.Time) {}
// metrics 为 nil 时自动选用 NoopCollector，避免测试环境必须注册 Prometheus。
