package writefailures

// writefailures 指标：按 org_id 统计成功输出与因限流丢弃的写入失败日志条数。

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/util/constants"
)

type metrics struct {
	loggedCount    *prometheus.CounterVec
	discardedCount *prometheus.CounterVec
}

// newMetrics 注册 loki_write_failures_logged_total 与 discarded_total 计数器。
func newMetrics(reg prometheus.Registerer, subsystem string) *metrics {
	return &metrics{
		loggedCount: promauto.With(reg).NewCounterVec(prometheus.CounterOpts{
			Namespace:   constants.Loki,
			Name:        "write_failures_logged_total",
			Help:        "The total number of write failures logs successfully emitted for a tenant.",
			ConstLabels: prometheus.Labels{"subsystem": subsystem},
		}, []string{"org_id"}),
		discardedCount: promauto.With(reg).NewCounterVec(prometheus.CounterOpts{
			Namespace:   constants.Loki,
			Name:        "write_failures_discarded_total",
			Help:        "The total number of write failures logs discarded for a tenant.",
			ConstLabels: prometheus.Labels{"subsystem": subsystem},
		}, []string{"org_id"}),
	}
}
// ConstLabels 携带 subsystem 以区分 distributor 等不同组件实例。
