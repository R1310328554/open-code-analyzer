package gcp

// instrumentation 为 Bigtable gRPC 与 GCS HTTP 请求注册 Prometheus 直方图，并封装 grpc.DialOption 到 google.api option 的转换。

import (
	"net/http"
	"strconv"
	"time"

	"github.com/grafana/dskit/middleware"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"google.golang.org/api/option"
	"google.golang.org/grpc"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

var (
	bigtableRequestDuration = promauto.NewHistogramVec(prometheus.HistogramOpts{
		Namespace: constants.Loki,
		Name:      "bigtable_request_duration_seconds",
		Help:      "Time spent doing Bigtable requests.",

		// Bigtable latency seems to range from a few ms to a several seconds and is
		// important.  So use 9 buckets from 1ms to just over 1 minute (65s).
		Buckets: prometheus.ExponentialBuckets(0.001, 4, 9),
	}, []string{"operation", "status_code"})

	gcsRequestDuration = promauto.NewHistogramVec(prometheus.HistogramOpts{
		Namespace: constants.Loki,
		Name:      "gcs_request_duration_seconds",
		Help:      "Time spent doing GCS requests.",

		// 6 buckets from 5ms to 20s.
		Buckets: prometheus.ExponentialBuckets(0.005, 4, 7),
	}, []string{"operation", "status_code"})
)

// bigtableInstrumentation 返回 dskit middleware 客户端拦截器，按 operation/status 标签计时。
func bigtableInstrumentation() ([]grpc.UnaryClientInterceptor, []grpc.StreamClientInterceptor) {
	return []grpc.UnaryClientInterceptor{
			middleware.UnaryClientInstrumentInterceptor(bigtableRequestDuration),
		},
		[]grpc.StreamClientInterceptor{
			middleware.StreamClientInstrumentInterceptor(bigtableRequestDuration),
		}
}

// gcsInstrumentation 用 instrumentedTransport 包装 RoundTripper 记录 GCS 请求耗时。
func gcsInstrumentation(transport http.RoundTripper) *http.Client {
	client := &http.Client{
		Transport: instrumentedTransport{
			observer: gcsRequestDuration,
			next:     transport,
		},
	}
	return client
}

// toOptions 将 dskit 生成的 grpc.DialOption 列表转为 Bigtable 客户端所需的 ClientOption。
func toOptions(opts []grpc.DialOption) []option.ClientOption {
	result := make([]option.ClientOption, 0, len(opts))
	for _, opt := range opts {
		result = append(result, option.WithGRPCDialOption(opt))
	}
	return result
}

// instrumentedTransport 在 RoundTrip 成功时按 HTTP 方法与状态码 Observe 延迟。
type instrumentedTransport struct {
	observer prometheus.ObserverVec
	next     http.RoundTripper
}

func (i instrumentedTransport) RoundTrip(req *http.Request) (*http.Response, error) {
	start := time.Now()
	resp, err := i.next.RoundTrip(req)
	if err == nil {
		i.observer.WithLabelValues(req.Method, strconv.Itoa(resp.StatusCode)).Observe(time.Since(start).Seconds())
	}
	return resp, err
}
// bigtable 桶覆盖约 1ms–65s；GCS 桶从 5ms 起指数扩展共 7 档，便于 SLO 与慢查询分析。
