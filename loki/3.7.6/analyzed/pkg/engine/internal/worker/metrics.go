package worker

// metrics 定义 worker 级别的 Prometheus 指标：已分配任务总数与任务执行耗时直方图。

import (
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

// metrics 持有独立 Registry，便于与 wire 指标分开注册到全局采集器。
// metrics is a container of metrics for a worker.
type metrics struct {
	// registry to collect metrics as a unit.
	reg *prometheus.Registry

	tasksAssignedTotal prometheus.Counter
	taskExecSeconds    prometheus.Histogram
}

func newMetrics() *metrics {
	reg := prometheus.NewRegistry()

	return &metrics{
		reg: reg,

		tasksAssignedTotal: promauto.With(reg).NewCounter(prometheus.CounterOpts{
			Name: "loki_engine_worker_tasks_assigned_total",
			Help: "Total number of tasks assigned to the worker",
		}),
		taskExecSeconds: promauto.With(reg).NewHistogram(prometheus.HistogramOpts{
			Name: "loki_engine_worker_task_exec_seconds",
			Help: "Number of seconds a task took to complete successfully",

			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: time.Hour,
		}),
	}
}

// Register 将 worker 私有 Registry 挂载到外部 Registerer。
// Register registers metrics to report to reg.
func (m *metrics) Register(reg prometheus.Registerer) error { return reg.Register(m.reg) }

// Unregister 从采集器移除 worker 指标，用于优雅关闭或热重载。
// Unregister unregisters metrics from the provided Registerer.
func (m *metrics) Unregister(reg prometheus.Registerer) { reg.Unregister(m.reg) }
// taskExecSeconds 使用原生直方图，桶因子 1.1，最小重置间隔为一小时。
