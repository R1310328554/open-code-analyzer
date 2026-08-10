package gcplog

// GCP Cloud Logging target 的 Prometheus 指标：Pull 订阅计数/解析错误/最近成功拉取时间，
// Push 订阅独立计数与按 reason 分类的解析错误。

import "github.com/prometheus/client_golang/prometheus"

// 封装 Pull 与 Push 两套 Counter/Gauge，reg 用于 pipeline stages 注册。
// Metrics stores gcplog entry metrics.
type Metrics struct {
	// reg is the Registerer used to create this set of metrics.
	reg prometheus.Registerer

	gcplogEntries                 *prometheus.CounterVec
	gcplogErrors                  *prometheus.CounterVec
	gcplogTargetLastSuccessScrape *prometheus.GaugeVec

	gcpPushEntries *prometheus.CounterVec
	gcpPushErrors  *prometheus.CounterVec
}

// 创建 promtail_gcplog_target_* 与 promtail_gcp_push_target_* 命名空间指标并 MustRegister。
// NewMetrics creates a new set of metrics. Metrics will be registered to reg.
func NewMetrics(reg prometheus.Registerer) *Metrics {
	var m Metrics
	m.reg = reg

	// Pull subscription metrics
	m.gcplogEntries = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "gcplog_target_entries_total",
		Help:      "Help number of successful entries sent to the gcplog target",
	}, []string{"project"})

	m.gcplogErrors = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "gcplog_target_parsing_errors_total",
		Help:      "Total number of parsing errors while receiving gcplog messages",
	}, []string{"project"})

	m.gcplogTargetLastSuccessScrape = prometheus.NewGaugeVec(prometheus.GaugeOpts{
		Namespace: "promtail",
		Name:      "gcplog_target_last_success_scrape",
		Help:      "Timestamp of the specific target's last successful poll",
	}, []string{"project", "target"})

// Push 模式：HTTP 接收成功条目总数与按 reason 标签的解析失败计数。
	// Push subscription metrics
	m.gcpPushEntries = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "gcp_push_target_entries_total",
		Help:      "Number of successful entries received by the GCP Push target",
	}, []string{})

	m.gcpPushErrors = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "gcp_push_target_parsing_errors_total",
		Help:      "Number of parsing errors while receiving GCP Push messages",
	}, []string{"reason"})

	reg.MustRegister(
		m.gcplogEntries,
		m.gcplogErrors,
		m.gcplogTargetLastSuccessScrape,
		m.gcpPushEntries,
		m.gcpPushErrors,
	)
	return &m
}
