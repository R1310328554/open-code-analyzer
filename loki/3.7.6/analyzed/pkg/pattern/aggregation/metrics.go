package aggregation

// aggregation 包 metrics 定义 pattern ingester 向 Loki 推送聚合指标与模式样本时的 Prometheus 观测项。

import (
	"sync"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

var (
	aggMetrics  *Metrics
	metricsOnce sync.Once
)

// Metrics 封装 push 错误、批次大小、活跃模式数等 Counter/Histogram/Gauge 向量。
type Metrics struct {
	reg prometheus.Registerer

// push 相关：推送失败计数与 payload 体积直方图。
	// push operation
	pushErrors  *prometheus.CounterVec
	payloadSize *prometheus.HistogramVec

// 批次指标：每次 push 的 stream/entry 数量与被跟踪 service 数。
	// Batch metrics
	streamsPerPush  *prometheus.HistogramVec
	entriesPerPush  *prometheus.HistogramVec
	servicesTracked *prometheus.GaugeVec

	writeTimeout *prometheus.CounterVec

// 模式写入：字节总量、写入次数与内存中活跃模式数。
	// Pattern writing metrics
	PatternBytesWrittenTotal *prometheus.CounterVec
	PatternWritesTotal       *prometheus.CounterVec
	PatternsActive           *prometheus.GaugeVec

// 聚合指标写入：汇总后的 metric 条目字节总量。
	// Aggregated metrics writing metrics
	AggregatedMetricBytesWrittenTotal *prometheus.CounterVec
}

// NewMetrics 以 sync.Once 注册并返回单例 Metrics，避免重复注册同名指标。
func NewMetrics(r prometheus.Registerer) *Metrics {
	metricsOnce.Do(func() {
		aggMetrics = &Metrics{
			pushErrors: promauto.With(r).NewCounterVec(prometheus.CounterOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "push_errors_total",
				Help:      "Total number of errors when pushing metrics to Loki.",
			}, []string{"tenant_id", "error_type"}),

			// Batch metrics
			payloadSize: promauto.With(r).NewHistogramVec(prometheus.HistogramOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "push_payload_bytes",
				Help:      "Size of push payloads in bytes.",
				Buckets:   []float64{1024, 4096, 16384, 65536, 262144, 1048576},
			}, []string{"tenant_id"}),
			streamsPerPush: promauto.With(r).NewHistogramVec(prometheus.HistogramOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "streams_per_push",
				Help:      "Number of streams in each push request.",
				Buckets:   []float64{1, 5, 10, 25, 50, 100, 250, 500, 1000},
			}, []string{"tenant_id"}),
			entriesPerPush: promauto.With(r).NewHistogramVec(prometheus.HistogramOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "entries_per_push",
				Help:      "Number of entries in each push request.",
				Buckets:   []float64{10, 50, 100, 500, 1000, 5000, 10000},
			}, []string{"tenant_id"}),
			servicesTracked: promauto.With(r).NewGaugeVec(prometheus.GaugeOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "services_tracked",
				Help:      "Number of unique services being tracked.",
			}, []string{"tenant_id"}),
			writeTimeout: promauto.With(r).NewCounterVec(prometheus.CounterOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "write_timeouts_total",
				Help:      "Total number of write timeouts.",
			}, []string{"tenant_id"}),

			// Pattern writing metrics
			PatternBytesWrittenTotal: promauto.With(r).NewCounterVec(prometheus.CounterOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "pattern_bytes_written_total",
				Help:      "Total bytes written for pattern payloads to Loki.",
			}, []string{"tenant_id"}),

			PatternWritesTotal: promauto.With(r).NewCounterVec(prometheus.CounterOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "pattern_writes_total",
				Help:      "Total number of pattern write operations to Loki.",
			}, []string{"tenant_id"}),

			PatternsActive: promauto.With(r).NewGaugeVec(prometheus.GaugeOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "patterns_active",
				Help:      "Number of active patterns currently tracked in memory.",
			}, []string{"tenant_id"}),

			// Aggregated metrics writing metrics
			AggregatedMetricBytesWrittenTotal: promauto.With(r).NewCounterVec(prometheus.CounterOpts{
				Namespace: constants.Loki,
				Subsystem: "pattern_ingester",
				Name:      "aggregated_metric_bytes_written_total",
				Help:      "Total bytes written for aggregated metric payloads to Loki.",
			}, []string{"tenant_id"}),
		}
	})

	return aggMetrics
}
// writeTimeout 统计 HTTP 客户端超时次数，便于排查 Loki 端慢响应。
