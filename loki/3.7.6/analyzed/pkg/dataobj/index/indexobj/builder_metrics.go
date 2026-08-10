package indexobj

// builderMetrics 为 indexobj.Builder 提供 Prometheus  instrumentation：
// 涵盖 append/flush 耗时、尺寸估计与各 section 子 metrics。

import (
	"errors"

	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/consumer/logsobj"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/indexpointers"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/pointers"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/streams"
)

// builderMetrics 聚合 pointers、streams、indexpointers 与 dataobj 子指标。
// builderMetrics provides instrumnetation for a [Builder].
type builderMetrics struct {
	pointers      *pointers.Metrics
	indexPointers *indexpointers.Metrics
	streams       *streams.Metrics
	dataobj       *dataobj.Metrics

	targetPageSize   prometheus.Gauge
	targetObjectSize prometheus.Gauge

	appendTime     prometheus.Histogram
	appendFailures prometheus.Counter
	appendsTotal   prometheus.Counter

	buildTime     prometheus.Histogram
	flushFailures prometheus.Counter
	flushTotal    prometheus.Counter

	sizeEstimate prometheus.Gauge
	builtSize    prometheus.Histogram
}

// newBuilderMetrics 初始化全部 Counter、Gauge 与 Histogram 指标描述符。
// newBuilderMetrics creates a new set of [builderMetrics] for instrumenting
// logs objects.
func newBuilderMetrics() *builderMetrics {
	return &builderMetrics{
		indexPointers: indexpointers.NewMetrics(),
		pointers:      pointers.NewMetrics(),
		streams:       streams.NewMetrics(),
		dataobj:       dataobj.NewMetrics(),
		targetPageSize: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_indexobj_config_target_page_size_bytes",

			Help: "Configured target page size in bytes.",
		}),

		targetObjectSize: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_indexobj_config_target_object_size_bytes",

			Help: "Configured target object size in bytes.",
		}),

		appendTime: prometheus.NewHistogram(prometheus.HistogramOpts{
			Name: "loki_indexobj_append_time_seconds",

			Help: "Time taken appending a set of log lines in a stream to a data object.",

			Buckets:                         prometheus.DefBuckets,
			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: 0,
		}),

		appendFailures: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_indexobj_append_failures_total",
			Help: "Total number of append failures",
		}),

		appendsTotal: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_indexobj_appends_total",
			Help: "Total number of appends",
		}),

		buildTime: prometheus.NewHistogram(prometheus.HistogramOpts{
			Name: "loki_indexobj_build_time_seconds",

			Help: "Time taken building a data object to flush.",

			Buckets:                         prometheus.DefBuckets,
			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: 0,
		}),

		sizeEstimate: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_indexobj_size_estimate_bytes",

			Help: "Current estimated size of the data object in bytes.",
		}),

		builtSize: prometheus.NewHistogram(prometheus.HistogramOpts{
			Name: "loki_indexobj_built_size_bytes",

			Help: "Distribution of constructed data object sizes in bytes.",

			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: 0,
		}),

		flushFailures: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_indexobj_flush_failures_total",

			Help: "Total number of flush failures.",
		}),

		flushTotal: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_indexobj_flush_total",

			Help: "Total number of flushes.",
		}),
	}
}

// ObserveConfig 将 TargetPageSize 与 TargetObjectSize 写入配置 Gauge。
// ObserveConfig updates config metrics based on the provided [BuilderConfig].
func (m *builderMetrics) ObserveConfig(cfg logsobj.BuilderBaseConfig) {
	m.targetPageSize.Set(float64(cfg.TargetPageSize))
	m.targetObjectSize.Set(float64(cfg.TargetObjectSize))
}

// Register 向 Registerer 注册本包与子 section 的全部 collectors。
// Register registers metrics to report to reg.
func (m *builderMetrics) Register(reg prometheus.Registerer) error {
	var errs []error

	errs = append(errs, m.indexPointers.Register(reg))
	errs = append(errs, m.pointers.Register(reg))
	errs = append(errs, m.streams.Register(reg))
	errs = append(errs, m.dataobj.Register(reg))

	errs = append(errs, reg.Register(m.targetPageSize))
	errs = append(errs, reg.Register(m.targetObjectSize))

	errs = append(errs, reg.Register(m.appendTime))
	errs = append(errs, reg.Register(m.appendFailures))
	errs = append(errs, reg.Register(m.appendsTotal))

	errs = append(errs, reg.Register(m.buildTime))

	errs = append(errs, reg.Register(m.sizeEstimate))
	errs = append(errs, reg.Register(m.builtSize))
	errs = append(errs, reg.Register(m.flushFailures))
	errs = append(errs, reg.Register(m.flushTotal))

	return errors.Join(errs...)
}

// Unregister 从 Registerer 移除已注册的 metrics，用于热重载或测试清理。
// Unregister unregisters metrics from the provided Registerer.
func (m *builderMetrics) Unregister(reg prometheus.Registerer) {
	m.indexPointers.Unregister(reg)
	m.pointers.Unregister(reg)
	m.streams.Unregister(reg)
	m.dataobj.Unregister(reg)

	reg.Unregister(m.targetPageSize)
	reg.Unregister(m.targetObjectSize)

	reg.Unregister(m.appendTime)
	reg.Unregister(m.appendFailures)
	reg.Unregister(m.appendsTotal)

	reg.Unregister(m.buildTime)

	reg.Unregister(m.sizeEstimate)
	reg.Unregister(m.builtSize)
	reg.Unregister(m.flushFailures)
	reg.Unregister(m.flushTotal)
}
// append 与 build 直方图启用原生桶因子一点一，便于细粒度延迟观测。
