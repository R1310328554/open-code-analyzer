package logql

// mapper_metrics 为 LogQL AST 映射器（分片与范围拆分）注册 Prometheus 指标。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

// StreamsKey/MetricsKey 区分下游查询类型：日志流选择器与指标样本表达式。
// expression type used in metrics
const (
	StreamsKey = "streams"
	MetricsKey = "metrics"
)

// SuccessKey/FailureKey/NoopKey 标记 Parse 结果：成功改写、失败或未改写。
// parsing evaluation result used in metrics
const (
	SuccessKey = "success"
	FailureKey = "failure"
	NoopKey    = "noop"
)

// MapperMetrics 汇总下游查询数、解析计数与单次请求的下游因子直方图。
// MapperMetrics is the metrics wrapper used in logql mapping (shard and range)
type MapperMetrics struct {
	DownstreamQueries *prometheus.CounterVec // downstream queries total, partitioned by streams/metrics
	ParsedQueries     *prometheus.CounterVec // parsed ASTs total, partitioned by success/failure/noop
	DownstreamFactor  prometheus.Histogram   // per request downstream factor
}

// newMapperMetrics 注册带 mapper 标签的 counter 与 histogram，供 range/shard 映射器共用。
func newMapperMetrics(registerer prometheus.Registerer, mapper string) *MapperMetrics {
	return &MapperMetrics{
		DownstreamQueries: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace:   constants.Loki,
			Name:        "query_frontend_shards_total",
			Help:        "Number of downstream queries by expression type",
			ConstLabels: prometheus.Labels{"mapper": mapper},
		}, []string{"type"}),
		ParsedQueries: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace:   constants.Loki,
			Name:        "query_frontend_sharding_parsed_queries_total",
			Help:        "Number of parsed queries by evaluation type",
			ConstLabels: prometheus.Labels{"mapper": mapper},
		}, []string{"type"}),
		DownstreamFactor: promauto.With(registerer).NewHistogram(prometheus.HistogramOpts{
			Namespace: constants.Loki,
			Name:      "query_frontend_shard_factor",
			Help:      "Number of downstream queries per request",
			// 1 -> 65k shards
			Buckets:     prometheus.ExponentialBuckets(1, 4, 8),
			ConstLabels: prometheus.Labels{"mapper": mapper},
		}),
	}
}

// downstreamRecorder 在单次 AST 映射过程中累加下游数，Finish 时写入 shard factor 直方图。
// downstreamRecorder wraps a vector & histogram, providing an easy way to increment downstream counts.
// and unify them into histogram entries.
// NOT SAFE FOR CONCURRENT USE! We avoid introducing mutex locking here
// because AST mapping is single threaded.
type downstreamRecorder struct {
	done  bool
	total int
	*MapperMetrics
}

// downstreamRecorder constructs a recorder using the underlying metrics.
func (m *MapperMetrics) downstreamRecorder() *downstreamRecorder {
	return &downstreamRecorder{
		MapperMetrics: m,
	}
}

// Add increments both the downstream count and tracks it for the eventual histogram entry.
// Add 累加下游查询数并按 type 标签递增 DownstreamQueries 计数器。
func (r *downstreamRecorder) Add(x int, key string) {
	r.total += x
	r.DownstreamQueries.WithLabelValues(key).Add(float64(x))
}

// Finish idemptotently records a histogram entry with the total downstream factor.
// Finish 幂等地记录本次映射的总下游因子到 DownstreamFactor 直方图。
func (r *downstreamRecorder) Finish() {
	if !r.done {
		r.done = true
		r.DownstreamFactor.Observe(float64(r.total))
	}
}
// AST 映射为单线程，downstreamRecorder 无需加锁。
