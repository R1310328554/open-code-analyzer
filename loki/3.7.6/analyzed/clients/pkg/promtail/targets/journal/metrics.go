package journal

// systemd journal target 的 Prometheus 指标：成功读取行数与按 error 标签
// 分类的解析错误（无 MESSAGE、relabel 后标签为空等）。

import "github.com/prometheus/client_golang/prometheus"

// journalLines 计数成功条目，journalErrors 按 no_message/empty_labels 维度统计。
// Metrics holds a set of journal target metrics.
type Metrics struct {
	reg prometheus.Registerer

	journalErrors *prometheus.CounterVec
	journalLines  prometheus.Counter
}

const (
	noMessageError   = "no_message"
	emptyLabelsError = "empty_labels"
)

// 创建 promtail_journal_target_* 命名空间 Counter 并在 reg 非空时 MustRegister。
// NewMetrics creates a new set of journal target metrics. If reg is non-nil, the
// metrics will be registered.
func NewMetrics(reg prometheus.Registerer) *Metrics {
	var m Metrics
	m.reg = reg

	m.journalErrors = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "journal_target_parsing_errors_total",
		Help:      "Total number of parsing errors while reading journal messages",
	}, []string{"error"})
	m.journalLines = prometheus.NewCounter(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "journal_target_lines_total",
		Help:      "Total number of successful journal lines read",
	})

	if reg != nil {
		reg.MustRegister(
			m.journalErrors,
			m.journalLines,
		)
	}

	return &m
}
