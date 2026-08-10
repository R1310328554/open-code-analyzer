package index

// index 包内部 Prometheus 指标：按 operation 与 status_code 记录索引请求耗时直方图。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

type metrics struct {
	indexQueryLatency *prometheus.HistogramVec
}

// newMetrics 注册 Loki 命名空间下指数分桶的 index_request_duration_seconds 指标。
func newMetrics(reg prometheus.Registerer) *metrics {
	return &metrics{
		indexQueryLatency: promauto.With(reg).NewHistogramVec(prometheus.HistogramOpts{
			Namespace: constants.Loki,
			Name:      "index_request_duration_seconds",
			Help:      "Time (in seconds) spent in serving index query requests",
			Buckets:   prometheus.ExponentialBucketsRange(0.005, 100, 12),
		}, []string{"operation", "status_code"}),
	}
}
// buckets 从 5ms 到 100s 共 12 档，覆盖典型索引查询与慢查询尾部延迟。
