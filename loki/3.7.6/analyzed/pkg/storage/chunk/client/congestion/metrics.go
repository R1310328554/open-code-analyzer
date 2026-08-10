package congestion

// metrics 为 store_congestion_control 子系统注册 Prometheus 指标：当前限速、退避时长、请求/重试计数及不可重试与超限错误统计。

import (
	"github.com/grafana/loki/v3/pkg/util/constants"

	"github.com/prometheus/client_golang/prometheus"
)

type Metrics struct {
	currentLimit       prometheus.Gauge
	backoffSec         prometheus.Counter
	requests           prometheus.Counter
	retries            prometheus.Counter
	nonRetryableErrors prometheus.Counter
	retriesExceeded    prometheus.Counter
}

// Unregister 在 ObjectClient 生命周期结束时注销全部指标，避免泄漏注册表条目。
func (m Metrics) Unregister() {
	prometheus.Unregister(m.currentLimit)
	prometheus.Unregister(m.backoffSec)
	prometheus.Unregister(m.requests)
	prometheus.Unregister(m.retries)
	prometheus.Unregister(m.nonRetryableErrors)
	prometheus.Unregister(m.retriesExceeded)
}

// NewMetrics 按 strategy 与 name 常量标签注册指标；重复 name 会导致 MustRegister panic。
// NewMetrics creates metrics to be used for monitoring congestion control.
// It needs to accept a "name" because congestion control is used in object clients, and there can be many object clients
// creates for the same store (multiple period configs, etc). It is the responsibility of the caller to ensure uniqueness,
// otherwise a duplicate registration panic will occur.
func NewMetrics(name string, cfg Config) *Metrics {
	labels := map[string]string{
		"strategy": cfg.Controller.Strategy,
		"name":     name,
	}

	const namespace = constants.Loki
	const subsystem = "store_congestion_control"
	m := Metrics{
		currentLimit: prometheus.NewGauge(prometheus.GaugeOpts{
			Namespace:   namespace,
			Subsystem:   subsystem,
			Name:        "limit",
			Help:        "Current per-second request limit to control congestion",
			ConstLabels: labels,
		}),
		backoffSec: prometheus.NewCounter(prometheus.CounterOpts{
			Namespace:   namespace,
			Subsystem:   subsystem,
			Name:        "backoff_seconds_total",
			Help:        "How much time is spent backing off once throughput limit is encountered",
			ConstLabels: labels,
		}),
		requests: prometheus.NewCounter(prometheus.CounterOpts{
			Namespace:   namespace,
			Subsystem:   subsystem,
			Name:        "requests_total",
			Help:        "How many requests were issued to the store",
			ConstLabels: labels,
		}),
		retries: prometheus.NewCounter(prometheus.CounterOpts{
			Namespace:   namespace,
			Subsystem:   subsystem,
			Name:        "retries_total",
			Help:        "How many retries occurred",
			ConstLabels: labels,
		}),
		nonRetryableErrors: prometheus.NewCounter(prometheus.CounterOpts{
			Namespace:   namespace,
			Subsystem:   subsystem,
			Name:        "non_retryable_errors_total",
			Help:        "How many request errors occurred which could not be retried",
			ConstLabels: labels,
		}),
		retriesExceeded: prometheus.NewCounter(prometheus.CounterOpts{
			Namespace:   namespace,
			Subsystem:   subsystem,
			Name:        "retries_exceeded_total",
			Help:        "How many times the number of retries exceeded the configured limit.",
			ConstLabels: labels,
		}),
	}

	prometheus.MustRegister(m.currentLimit)
	prometheus.MustRegister(m.backoffSec)
	prometheus.MustRegister(m.requests)
	prometheus.MustRegister(m.retries)
	prometheus.MustRegister(m.nonRetryableErrors)
	prometheus.MustRegister(m.retriesExceeded)
	return &m
}
// limit 反映 AIMD 当前每秒请求上限；backoff_seconds_total 累计遇限流时的等待时间。
