// This directory was copied and adapted from https://github.com/grafana/agent/tree/main/pkg/metrics.
// We cannot vendor the agent in since the agent vendors loki in, which would cause a cyclic dependency.
// NOTE: many changes have been made to the original code for our use-case.
package instance

// Metrics 跟踪 WAL 实例异常退出次数与当前 running_instances 数量。

import (
	"github.com/prometheus/client_golang/prometheus"
)

type Metrics struct {
	r prometheus.Registerer

	AbnormalExits    *prometheus.CounterVec
	RunningInstances prometheus.Gauge
}

// NewMetrics 注册 instance_abnormal_exits_total 与 running_instances 两项指标。
func NewMetrics(r prometheus.Registerer) *Metrics {
	m := &Metrics{r: r}

	m.AbnormalExits = prometheus.NewCounterVec(prometheus.CounterOpts{
		Name: "instance_abnormal_exits_total",
		Help: "Total number of times a WAL instance exited unexpectedly, causing it to be restarted.",
	}, []string{"tenant"})
	m.RunningInstances = prometheus.NewGauge(prometheus.GaugeOpts{
		Name: "running_instances",
		Help: "Current number of running WAL instances.",
	})

	if r != nil {
		r.MustRegister(
			m.AbnormalExits,
			m.RunningInstances,
		)
	}

	return m
}
// BasicManager.runProcess 在异常退出时 Inc AbnormalExits 并记录 backoff 日志。
