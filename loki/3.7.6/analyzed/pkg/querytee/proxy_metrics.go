package querytee

// ProxyMetrics 注册 query-tee 的 Prometheus 指标：请求量、耗时、响应比对结果、Goldfish 采样与 race 胜出计数。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

const (
	comparisonMatch    = "match"
	comparisonMismatch = "mismatch"
	comparisonFailed   = "fail"
	comparisonSkipped  = "skipped"

	unknownIssuer = "unknown"
	canaryIssuer  = "loki-canary"
)

// ProxyMetrics 使用 cortex_querytee 与 loki_querytee 命名空间区分通用与 race 指标。
type ProxyMetrics struct {
	requestsTotal          *prometheus.CounterVec
	requestDuration        *prometheus.HistogramVec
	responsesTotal         *prometheus.CounterVec
	responsesComparedTotal *prometheus.CounterVec
	missingMetrics         *prometheus.HistogramVec

	// Sampling metrics
	queriesSampled    *prometheus.CounterVec
	samplingDecisions *prometheus.CounterVec

// raceWins 在 RoutingModeRace 下按 backend/route/issuer 统计竞速胜出次数。
	// Race metrics
	raceWins *prometheus.CounterVec
}

// NewProxyMetrics 用 promauto 注册 requests_total、request_duration_seconds 等向量。
func NewProxyMetrics(registerer prometheus.Registerer) *ProxyMetrics {
	m := &ProxyMetrics{
		requestsTotal: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: "cortex_querytee",
			Name:      "requests_total",
			Help:      "Total number of HTTP requests received by query-tee.",
		}, []string{"method", "route"}),
		requestDuration: promauto.With(registerer).NewHistogramVec(prometheus.HistogramOpts{
			Namespace: "cortex_querytee",
			Name:      "request_duration_seconds",
			Help:      "Time (in seconds) spent serving HTTP requests.",
			Buckets:   []float64{.005, .01, .025, .05, .1, .25, .5, 0.75, 1, 1.5, 2, 3, 4, 5, 10, 25, 50, 100},
		}, []string{"backend", "backend_alias", "method", "route", "status_code", "issuer"}),
		responsesTotal: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: "cortex_querytee",
			Name:      "responses_total",
			Help:      "Total number of responses sent back to the client by the selected backend.",
		}, []string{"backend", "backend_alias", "method", "route", "issuer"}),
		responsesComparedTotal: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: "cortex_querytee",
			Name:      "responses_compared_total",
			Help:      "Total number of responses compared per route and backend name by result.",
		}, []string{"backend", "backend_alias", "route", "result", "issuer", "tenant"}),
		missingMetrics: promauto.With(registerer).NewHistogramVec(prometheus.HistogramOpts{
			Namespace: "cortex_querytee",
			Name:      "missing_metrics_series",
			Help:      "Number of missing metrics (series) in a vector response.",
			Buckets:   []float64{.005, .01, .025, .05, .1, .25, .5, 0.75, 1, 1.5, 2, 3, 4, 5, 10, 25, 50, 100},
		}, []string{"backend", "backend_alias", "route", "status_code", "issuer"}),

		queriesSampled: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: "cortex_querytee",
			Name:      "queries_sampled_total",
			Help:      "Total number of queries that were sampled and sent to Kafka.",
		}, []string{"tenant", "route"}),

		samplingDecisions: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: "cortex_querytee",
			Name:      "sampling_decisions_total",
			Help:      "Total number of sampling decisions made.",
		}, []string{"tenant", "route", "decision"}),

		raceWins: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: "loki_querytee",
			Name:      "race_wins_total",
			Help:      "Total number of times each backend won the race (when racing is enabled).",
		}, []string{"backend", "backend_alias", "route", "issuer"}),
	}

	return m
}
// comparisonMatch/Mismatch/Failed/Skipped 常量用于 responses_compared_total 标签。
