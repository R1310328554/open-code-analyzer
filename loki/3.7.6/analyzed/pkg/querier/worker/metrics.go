package worker

// worker 包 Metrics 注册 querier worker 并发、在途查询、frontend 客户端请求耗时与已连接 frontend 数量等 Prometheus 指标。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

type Metrics struct {
	concurrentWorkers             prometheus.Gauge
	inflightRequests              prometheus.Gauge
	frontendClientRequestDuration *prometheus.HistogramVec
	frontendClientsGauge          prometheus.Gauge
}

// NewMetrics 注册 loki_querier_worker_* 与 query_frontend_request_duration 系列。
func NewMetrics(_ Config, r prometheus.Registerer) *Metrics {
	return &Metrics{
		concurrentWorkers: promauto.With(r).NewGauge(prometheus.GaugeOpts{
			Name: "loki_querier_worker_concurrency",
			Help: "Number of concurrent querier workers",
		}),
		inflightRequests: promauto.With(r).NewGauge(prometheus.GaugeOpts{
			Name: "loki_querier_worker_inflight_queries",
			Help: "Number of queries being processed by the querier workers",
		}),
		frontendClientRequestDuration: promauto.With(r).NewHistogramVec(prometheus.HistogramOpts{
			Name:    "loki_querier_query_frontend_request_duration_seconds",
			Help:    "Time spend doing requests to frontend.",
			Buckets: prometheus.ExponentialBuckets(0.001, 4, 6),
		}, []string{"operation", "status_code"}),
		frontendClientsGauge: promauto.With(r).NewGauge(prometheus.GaugeOpts{
			Name: "loki_querier_query_frontend_clients",
			Help: "The current number of clients connected to query-frontend.",
		}),
	}
}
// frontendClientsGauge 由 dskit client.Pool 维护，反映 querier 到 frontend 连接数。
