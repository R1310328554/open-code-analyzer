package gelf

// GELF target Prometheus 指标：成功接收条目总数与 UDP 读/解析错误计数。

import "github.com/prometheus/client_golang/prometheus"

// gelfEntries/gelfErrors 两个 Counter，reg 非空时 MustRegister。
// Metrics holds a set of gelf metrics.
type Metrics struct {
	reg prometheus.Registerer

	gelfEntries prometheus.Counter
	gelfErrors  prometheus.Counter
}

// 创建 promtail_gelf_target_entries_total 与 parsing_errors_total。
// NewMetrics creates a new set of gelf metrics. If reg is non-nil, the
// metrics will be registered.
func NewMetrics(reg prometheus.Registerer) *Metrics {
	var m Metrics
	m.reg = reg

// 每成功 handle 一条 GELF 消息递增 entries。
	m.gelfEntries = prometheus.NewCounter(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "gelf_target_entries_total",
		Help:      "Total number of successful entries sent to the gelf target",
	})
// ReadMessage 或 JSON 序列化失败时递增 errors。
	m.gelfErrors = prometheus.NewCounter(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "gelf_target_parsing_errors_total",
		Help:      "Total number of parsing errors while receiving gelf messages",
	})

	if reg != nil {
		reg.MustRegister(
			m.gelfEntries,
			m.gelfErrors,
		)
	}

	return &m
}
