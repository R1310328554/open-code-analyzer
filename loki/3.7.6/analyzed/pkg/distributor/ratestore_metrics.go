package distributor

// rateStore Prometheus 指标：刷新失败、流数量、分片分布、速率直方图与刷新耗时。

import (
	"github.com/grafana/dskit/instrument"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

// ratestoreMetrics 聚合 rate store 周期同步所需的 Counter/Gauge/Histogram 句柄。
type ratestoreMetrics struct {
	rateRefreshFailures *prometheus.CounterVec
	streamCount         prometheus.Gauge
	expiredCount        prometheus.Counter
	maxStreamShardCount prometheus.Gauge
	streamShardCount    prometheus.Histogram
	maxStreamRate       prometheus.Gauge
	streamRate          prometheus.Histogram
	maxUniqueStreamRate prometheus.Gauge
	refreshDuration     *instrument.HistogramCollector
}

// newRateStoreMetrics 在 loki 命名空间下注册 refresh、shard 与 stream rate 相关指标。
func newRateStoreMetrics(reg prometheus.Registerer) *ratestoreMetrics {
	return &ratestoreMetrics{
// rateRefreshFailures 按 source（ring 或 ingester 地址）统计刷新失败次数。
		rateRefreshFailures: promauto.With(reg).NewCounterVec(prometheus.CounterOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_refresh_failures_total",
			Help:      "The total number of failed attempts to refresh the distributor's view of stream rates",
		}, []string{"source"}),
		streamCount: promauto.With(reg).NewGauge(prometheus.GaugeOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_streams",
			Help:      "The number of unique streams reported by all ingesters. Sharded streams are combined",
		}),
		expiredCount: promauto.With(reg).NewCounter(prometheus.CounterOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_expired_streams_total",
			Help:      "The number of streams that have been expired by the ratestore",
		}),
		maxStreamShardCount: promauto.With(reg).NewGauge(prometheus.GaugeOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_max_stream_shards",
			Help:      "The number of shards for a single stream reported by ingesters during a sync operation.",
		}),
		streamShardCount: promauto.With(reg).NewHistogram(prometheus.HistogramOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_stream_shards",
			Help:      "The distribution of number of shards for a single stream reported by ingesters during a sync operation.",
			Buckets:   []float64{0, 1, 2, 4, 8, 16, 32, 64, 128},
		}),
		maxStreamRate: promauto.With(reg).NewGauge(prometheus.GaugeOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_max_stream_rate_bytes",
			Help:      "The maximum stream rate for any stream reported by ingesters during a sync operation. Sharded Streams are combined.",
		}),
		streamRate: promauto.With(reg).NewHistogram(prometheus.HistogramOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_stream_rate_bytes",
			Help:      "The distribution of stream rates for any stream reported by ingesters during a sync operation. Sharded Streams are combined.",
			Buckets:   prometheus.ExponentialBuckets(20000, 2, 14), // biggest bucket is 20000*2^(14-1) = 163,840,000 (~163.84MB)
		}),
		maxUniqueStreamRate: promauto.With(reg).NewGauge(prometheus.GaugeOpts{
			Namespace: constants.Loki,
			Name:      "rate_store_max_unique_stream_rate_bytes",
			Help:      "The maximum stream rate for any stream reported by ingesters during a sync operation. Sharded Streams are considered separate.",
		}),
		refreshDuration: instrument.NewHistogramCollector(
			promauto.With(reg).NewHistogramVec(
				prometheus.HistogramOpts{
					Namespace: constants.Loki,
					Name:      "rate_store_refresh_duration_seconds",
					Help:      "Time spent refreshing the rate store",
					Buckets:   prometheus.DefBuckets,
				}, instrument.HistogramCollectorBuckets,
			),
		),
	}
}
// maxStreamRate 合并分片后最大值，maxUniqueStreamRate 将各分片视为独立流统计。
