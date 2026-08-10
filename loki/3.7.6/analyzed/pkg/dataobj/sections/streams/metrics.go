package streams

// Metrics 为 streams 区段编码与 builder 状态暴露 Prometheus 指标。

import (
	"context"
	"errors"
	"time"

	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/dataobj/sections/internal/columnar"
)

// Metrics 聚合 columnar 通用指标与 streams 特有的编码/计数 gauge。
// Metrics instruments the streams section.
type Metrics struct {
	columnar *columnar.Metrics

	encodeSeconds prometheus.Histogram
	recordsTotal  prometheus.Counter
	streamCount   prometheus.Gauge
	minTimestamp  prometheus.Gauge
	maxTimestamp  prometheus.Gauge
}

// NewMetrics 注册 encode_seconds、records_total、stream_count 等指标。
// NewMetrics creates a new set of metrics for the streams section.
func NewMetrics() *Metrics {
	return &Metrics{
		columnar: columnar.NewMetrics(sectionType),

		encodeSeconds: prometheus.NewHistogram(prometheus.HistogramOpts{
			Namespace: "loki_dataobj",
			Subsystem: "streams",
			Name:      "encode_seconds",

			Help: "Time taken encoding streams section in seconds.",

			Buckets:                         prometheus.DefBuckets,
			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: time.Hour,
		}),

		recordsTotal: prometheus.NewCounter(prometheus.CounterOpts{
			Namespace: "loki_dataobj",
			Subsystem: "streams",
			Name:      "records_total",

			Help: "The total number of stream records appended.",
		}),

		streamCount: prometheus.NewGauge(prometheus.GaugeOpts{
			Namespace: "loki_dataobj",
			Subsystem: "streams",
			Name:      "stream_count",

			Help: "The current number of tracked streams; this resets after an encode.",
		}),

		minTimestamp: prometheus.NewGauge(prometheus.GaugeOpts{
			Namespace: "loki_dataobj",
			Subsystem: "streams",
			Name:      "min_timestamp",

			Help: "The minimum timestamp (in unix seconds) across all streams; this resets after an encode.",
		}),

		maxTimestamp: prometheus.NewGauge(prometheus.GaugeOpts{
			Namespace: "loki_dataobj",
			Subsystem: "streams",
			Name:      "max_timestamp",

			Help: "The maximum timestamp (in unix seconds) across all streams; this resets after an encode.",
		}),
	}
}

// Observe 委托 columnar.Metrics 采集已编码区段的列级统计。
// Observe observes section statistics for a given section.
func (m *Metrics) Observe(ctx context.Context, section *Section) error {
	return m.columnar.Observe(ctx, section.inner)
}

// Register 将 streams 与 columnar 子指标一并注册到给定 Registerer。
// Register registers metrics to report to reg.
func (m *Metrics) Register(reg prometheus.Registerer) error {
	var errs []error
	errs = append(errs, m.columnar.Register(reg))
	errs = append(errs, reg.Register(m.encodeSeconds))
	errs = append(errs, reg.Register(m.recordsTotal))
	errs = append(errs, reg.Register(m.streamCount))
	errs = append(errs, reg.Register(m.minTimestamp))
	errs = append(errs, reg.Register(m.maxTimestamp))
	return errors.Join(errs...)
}

// Unregister 从 Registerer 移除所有 streams 相关 collector。
// Unregister unregisters metrics from the provided Registerer.
func (m *Metrics) Unregister(reg prometheus.Registerer) {
	m.columnar.Unregister(reg)

	reg.Unregister(m.encodeSeconds)
	reg.Unregister(m.recordsTotal)
	reg.Unregister(m.streamCount)
	reg.Unregister(m.minTimestamp)
	reg.Unregister(m.maxTimestamp)
}
// min/max_timestamp gauge 在 Flush 后随 builder Reset 归零。
