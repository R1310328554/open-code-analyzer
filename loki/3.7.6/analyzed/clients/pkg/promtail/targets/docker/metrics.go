package docker

// Docker target Prometheus 指标：成功解析推送的日志行数与解析失败计数。

import "github.com/prometheus/client_golang/prometheus"

// dockerEntries/dockerErrors 计数器，reg 非空时注册到 promtail 命名空间。
// Metrics holds a set of Docker target metrics.
type Metrics struct {
	reg prometheus.Registerer

	dockerEntries prometheus.Counter
	dockerErrors  prometheus.Counter
}

// 初始化 docker_target_entries_total 与 docker_target_parsing_errors_total。
// NewMetrics creates a new set of Docker target metrics. If reg is non-nil, the
// metrics will be registered.
func NewMetrics(reg prometheus.Registerer) *Metrics {
	var m Metrics
	m.reg = reg

// 每经 handleOutput 成功发送一条容器日志行即 Inc。
	m.dockerEntries = prometheus.NewCounter(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "docker_target_entries_total",
		Help:      "Total number of successful entries sent to the Docker target",
	})
// extractTs 或帧解析失败时递增，便于监控 Docker 日志格式异常。
	m.dockerErrors = prometheus.NewCounter(prometheus.CounterOpts{
		Namespace: "promtail",
		Name:      "docker_target_parsing_errors_total",
		Help:      "Total number of parsing errors while receiving Docker messages",
	})

	if reg != nil {
		reg.MustRegister(
			m.dockerEntries,
			m.dockerErrors,
		)
	}

	return &m
}
