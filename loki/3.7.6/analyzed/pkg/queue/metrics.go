package queue

// queue 包 Metrics 为 query-scheduler 请求队列注册 Prometheus 指标：每租户队列长度、丢弃请求数与按 level 的入队计数。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

type Metrics struct {
	queueLength       *prometheus.GaugeVec   // Per tenant
	discardedRequests *prometheus.CounterVec // Per tenant
	enqueueCount      *prometheus.CounterVec // Per tenant and level
}

// NewMetrics 注册 queue_length、discarded_requests_total、enqueue_count 三类向量。
func NewMetrics(registerer prometheus.Registerer, metricsNamespace, subsystem string) *Metrics {
	return &Metrics{
		queueLength: promauto.With(registerer).NewGaugeVec(prometheus.GaugeOpts{
			Namespace: metricsNamespace,
			Subsystem: subsystem,
			Name:      "queue_length",
			Help:      "Number of queries in the queue.",
		}, []string{"user"}),
		discardedRequests: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: metricsNamespace,
			Subsystem: subsystem,
			Name:      "discarded_requests_total",
			Help:      "Total number of query requests discarded.",
		}, []string{"user"}),
		enqueueCount: promauto.With(registerer).NewCounterVec(prometheus.CounterOpts{
			Namespace: metricsNamespace,
			Subsystem: subsystem,
			Name:      "enqueue_count",
			Help:      "Total number of enqueued (sub-)queries.",
		}, []string{"user", "level"}),
	}
}

// Cleanup 在租户队列移除时 Delete 该 user 的全部 metric 标签，避免泄漏。
func (m *Metrics) Cleanup(user string) {
	m.queueLength.DeleteLabelValues(user)
	m.discardedRequests.DeleteLabelValues(user)
	m.enqueueCount.DeletePartialMatch(prometheus.Labels{"user": user})
}
// Enqueue 成功/失败时分别 Inc/Dec queue_length 与 discarded_requests。
