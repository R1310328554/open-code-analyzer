package cloudflare

// Cloudflare target Prometheus 指标：成功推送条目计数与最近一次拉取窗口结束时间戳。

import "github.com/prometheus/client_golang/prometheus"

// 封装 Entries 计数器与 LastEnd 时间戳 Gauge，可选注册到 registerer。
// Metrics holds a set of cloudflare metrics.
type Metrics struct {
	reg prometheus.Registerer

	Entries prometheus.Counter
	LastEnd prometheus.Gauge
}

// 创建 promtail_cloudflare_target_* 命名空间指标并在 reg 非空时 MustRegister。
// NewMetrics creates a new set of cloudflare metrics. If reg is non-nil, the
// metrics will be registered.
func NewMetrics(reg prometheus.Registerer) *Metrics {
	var m Metrics
	m.reg = reg

// Entries 统计经 handler 成功发送的 Cloudflare 日志行数。
	m.Entries = prometheus.NewCounter(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "cloudflare_target_entries_total",
		Help:      "Total number of successful entries sent via the cloudflare target",
	})
// LastEnd 记录上次成功拉取的 end 时间，用于观测 target 相对实时数据的滞后。
	m.LastEnd = prometheus.NewGauge(prometheus.GaugeOpts{
		Namespace: "promtail",
		Name:      "cloudflare_target_last_requested_end_timestamp",
		Help:      "The last cloudflare request end timestamp fetched. This allows to calculate how far the target is behind.",
	})

	if reg != nil {
		reg.MustRegister(
			m.Entries,
			m.LastEnd,
		)
	}

	return &m
}
