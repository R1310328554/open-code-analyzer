package jobqueue

// JobQueue Prometheus 指标：统计 Main Compactor 侧任务入队、出队、
// 重试与丢弃，以及 Worker 侧处理结果与连接状态。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

type queueMetrics struct {
	jobsSent               prometheus.Counter
	jobsDeQueued           prometheus.Counter
	jobsProcessed          prometheus.Counter
	jobRetries             *prometheus.CounterVec
	jobsDropped            prometheus.Counter
	jobsProcessingDuration prometheus.Histogram
}

// newQueueMetrics 注册 compactor_jobs_* 系列指标并返回 queueMetrics 实例。
func newQueueMetrics(r prometheus.Registerer) *queueMetrics {
	m := queueMetrics{}

	m.jobsSent = promauto.With(r).NewCounter(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "compactor_jobs_queued_total",
		Help:      "Number of jobs sent to the worker for processing",
	})
	m.jobsProcessed = promauto.With(r).NewCounter(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "compactor_jobs_processed_total",
		Help:      "Total number of jobs successfully processed",
	})
	m.jobRetries = promauto.With(r).NewCounterVec(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "compactor_job_retries_total",
		Help:      "Total number of job retries due to various reasons",
	}, []string{"reason"})
	m.jobsDropped = promauto.With(r).NewCounter(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "compactor_jobs_dropped_total",
		Help:      "Total number of jobs dropped after running out of retry attempts",
	})
	m.jobsProcessingDuration = promauto.With(r).NewHistogram(prometheus.HistogramOpts{
		Namespace: constants.Loki,
		Name:      "compactor_jobs_processing_duration_seconds",
		Help:      "Duration of job processing in seconds",
	})

	return &m
}

// registerJobsLeftTrackerMetric 为各 JobType 注册剩余任务数 GaugeFunc。
func registerJobsLeftTrackerMetric(jobType string, jobsLeftFunc func() float64, r prometheus.Registerer) {
	promauto.With(r).NewGaugeFunc(prometheus.GaugeOpts{
		Namespace: constants.Loki,
		Name:      "compactor_jobs_left",
		Help:      "Number of jobs left to be processed for concluding ongoing unit of work",
		ConstLabels: prometheus.Labels{
			"job_type": jobType,
		},
	}, jobsLeftFunc)
}

// workerMetrics 跟踪 Worker 处理成功/失败计数及与 Compactor 连接状态。
type workerMetrics struct {
	jobsProcessed              *prometheus.CounterVec
	workerConnectedToCompactor prometheus.GaugeFunc
}

func newWorkerMetrics(r prometheus.Registerer, allWorkersConnectedToCompactor func() bool) *workerMetrics {
	m := workerMetrics{}

	m.jobsProcessed = promauto.With(r).NewCounterVec(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "compactor_worker_jobs_processed_total",
		Help:      "Number of jobs processed by worker with their processing status",
	}, []string{"status"})
	m.workerConnectedToCompactor = promauto.With(r).NewGaugeFunc(prometheus.GaugeOpts{
		Namespace: constants.Loki,
		Name:      "compactor_worker_connected_to_compactor",
		Help:      "Tracks whether all compactor workers are connected to the compactor",
	}, func() float64 {
		if allWorkersConnectedToCompactor() {
			return 1
		}
		return 0
	})

	return &m
}
