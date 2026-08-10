package heroku

// Heroku Log Drain target Prometheus 指标：HTTP drain 成功条目与解析错误计数。

import "github.com/prometheus/client_golang/prometheus"

type Metrics struct {
	herokuEntries *prometheus.CounterVec
	herokuErrors  *prometheus.CounterVec
}

func NewMetrics(reg prometheus.Registerer) *Metrics {
	var m Metrics

// 每条成功解析的 Heroku drain 日志行递增。
	m.herokuEntries = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "heroku_drain_target_entries_total",
		Help:      "Number of successful entries received by the Heroku target",
	}, []string{})

	m.herokuErrors = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "heroku_drain_target_parsing_errors_total",
		Help:      "Number of parsing errors while receiving Heroku messages",
	}, []string{})

	reg.MustRegister(m.herokuEntries, m.herokuErrors)
	return &m
}
