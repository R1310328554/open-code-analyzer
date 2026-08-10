package file

// 文件 target Prometheus 指标：按 path 的读字节/总行/行数/编码失败，以及 manager 级活跃文件与失败 target 计数。

import "github.com/prometheus/client_golang/prometheus"

// GaugeVec/CounterVec 带 path 标签，filesActive/targetsActive 为全局 Gauge。
// Metrics hold the set of file-based metrics.
type Metrics struct {
	// Registerer used. May be nil.
	reg prometheus.Registerer

	// File-specific metrics
	readBytes        *prometheus.GaugeVec
	totalBytes       *prometheus.GaugeVec
	readLines        *prometheus.CounterVec
	encodingFailures *prometheus.CounterVec
	filesActive      prometheus.Gauge

	// Manager metrics
	failedTargets *prometheus.CounterVec
	targetsActive prometheus.Gauge
}

// 注册 read_bytes_total、file_bytes_total、read_lines_total 等 promtail 指标。
// NewMetrics creates a new set of file metrics. If reg is non-nil, the metrics
// will be registered.
func NewMetrics(reg prometheus.Registerer) *Metrics {
	var m Metrics
	m.reg = reg

	m.readBytes = prometheus.NewGaugeVec(prometheus.GaugeOpts{
		Namespace: "promtail",
		Name:      "read_bytes_total",
		Help:      "Number of bytes read.",
	}, []string{"path"})
	m.totalBytes = prometheus.NewGaugeVec(prometheus.GaugeOpts{
		Namespace: "promtail",
		Name:      "file_bytes_total",
		Help:      "Number of bytes total.",
	}, []string{"path"})
	m.readLines = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "read_lines_total",
		Help:      "Number of lines read.",
	}, []string{"path"})
	m.filesActive = prometheus.NewGauge(prometheus.GaugeOpts{
		Namespace: "promtail",
		Name:      "files_active_total",
		Help:      "Number of active files.",
	})

// FileTarget 创建或 sync 失败时按 reason 标签计数。
	m.failedTargets = prometheus.NewCounterVec(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "targets_failed_total",
		Help:      "Number of failed targets.",
	}, []string{"reason"})
	m.targetsActive = prometheus.NewGauge(prometheus.GaugeOpts{
		Namespace: "promtail",
		Name:      "targets_active_total",
		Help:      "Number of active total.",
	})

	if reg != nil {
		reg.MustRegister(
			m.readBytes,
			m.totalBytes,
			m.readLines,
			m.filesActive,
			m.failedTargets,
			m.targetsActive,
		)
	}

	return &m
}
