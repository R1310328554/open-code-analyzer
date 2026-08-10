package tail

// tail 包 Metrics 注册 querier tail 相关 Prometheus 指标：活跃 tailer 数、被 tail 的 stream 数与累计 tail 字节。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

type Metrics struct {
	tailsActive         prometheus.Gauge
	tailedStreamsActive prometheus.Gauge
	tailedBytesTotal    prometheus.Counter
}

// NewMetrics 用 promauto 注册 loki_querier_tail_* 系列 gauge/counter。
func NewMetrics(r prometheus.Registerer) *Metrics {
	return &Metrics{
		tailsActive: promauto.With(r).NewGauge(prometheus.GaugeOpts{
			Name: "loki_querier_tail_active",
			Help: "Number of active tailers",
		}),
		tailedStreamsActive: promauto.With(r).NewGauge(prometheus.GaugeOpts{
			Name: "loki_querier_tail_active_streams",
			Help: "Number of active streams being tailed",
		}),
		tailedBytesTotal: promauto.With(r).NewCounter(prometheus.CounterOpts{
			Name: "loki_querier_tail_bytes_total",
			Help: "total bytes tailed",
		}),
	}
}
// Tailer 创建/关闭时增减 gauge，loop 发送响应时累加 tailedBytesTotal。
