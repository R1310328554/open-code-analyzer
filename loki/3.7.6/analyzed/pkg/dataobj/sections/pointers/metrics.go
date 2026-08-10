package pointers

// pointers 段 Prometheus 指标：编码耗时、记录总数与时间戳范围 Gauge。

import (
	"context"
	"errors"
	"time"

	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/dataobj/sections/internal/columnar"
)

// Metrics 组合 columnar 通用指标与 pointers 子系统专属采集器。
// Metrics instruments the streams section.
type Metrics struct {
	columnar *columnar.Metrics

	encodeSeconds prometheus.Histogram
	recordsTotal  prometheus.Counter
	minTimestamp  prometheus.Gauge
	maxTimestamp  prometheus.Gauge
}

// NewMetrics 创建 encode_seconds 直方图与 min/max_timestamp Gauge 等。
// NewMetrics creates a new set of metrics for the pointers section.
func NewMetrics() *Metrics {
	return &Metrics{
		columnar: columnar.NewMetrics(sectionType),

		encodeSeconds: prometheus.NewHistogram(prometheus.HistogramOpts{
			Namespace: "loki_dataobj",
			Subsystem: "pointers",
			Name:      "encode_seconds",

			Help: "Time taken encoding pointers section in seconds.",

			Buckets:                         prometheus.DefBuckets,
			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: time.Hour,
		}),

		recordsTotal: prometheus.NewCounter(prometheus.CounterOpts{
			Namespace: "loki_dataobj",
			Subsystem: "pointers",
			Name:      "records_total",

			Help: "Total number of records in the pointers section.",
		}),

		minTimestamp: prometheus.NewGauge(prometheus.GaugeOpts{
			Namespace: "loki_dataobj",
			Subsystem: "pointers",
			Name:      "min_timestamp",

			Help: "The minimum timestamp (in unix seconds) across all pointers; this resets after an encode.",
		}),

		maxTimestamp: prometheus.NewGauge(prometheus.GaugeOpts{
			Namespace: "loki_dataobj",
			Subsystem: "pointers",
			Name:      "max_timestamp",

			Help: "The maximum timestamp (in unix seconds) across all pointers; this resets after an encode.",
		}),
	}
}

// Register registers metrics to report to reg.
// Register 注册 columnar 与 pointers 全部采集器，错误合并返回。
func (m *Metrics) Register(reg prometheus.Registerer) error {
	var errs []error
	errs = append(errs, m.columnar.Register(reg))
	errs = append(errs, reg.Register(m.encodeSeconds))
	errs = append(errs, reg.Register(m.recordsTotal))
	errs = append(errs, reg.Register(m.minTimestamp))
	errs = append(errs, reg.Register(m.maxTimestamp))
	return errors.Join(errs...)
}

// Unregister unregisters metrics from the provided Registerer.
func (m *Metrics) Unregister(reg prometheus.Registerer) {
	m.columnar.Unregister(reg)

	reg.Unregister(m.encodeSeconds)
	reg.Unregister(m.recordsTotal)
	reg.Unregister(m.minTimestamp)
	reg.Unregister(m.maxTimestamp)
}

// Observe observes section statistics for a given section.
// Observe 委托 columnar.Metrics 观察已打开段的统计信息。
func (m *Metrics) Observe(ctx context.Context, section *Section) error {
	return m.columnar.Observe(ctx, section.inner)
}
// Builder Reset 后将 min/max_timestamp Gauge 归零。
// Builder Reset 后将 min/max_timestamp Gauge 归零。
