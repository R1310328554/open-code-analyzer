package logsobj

// builder_metrics 模块为 logsobj.Builder 提供 Prometheus 指标：
// 覆盖 append/build 耗时、大小估计、flush 失败及各 section 观测。

import (
	"errors"

	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/logs"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/streams"
)

// builderMetrics 聚合 logs/streams/dataobj 子指标与 builder 级计数器。
// builderMetrics provides instrumnetation for a [Builder].
type builderMetrics struct {
	logs    *logs.Metrics
	streams *streams.Metrics
	dataobj *dataobj.Metrics

	targetPageSize   prometheus.Gauge
	targetObjectSize prometheus.Gauge

	appends       prometheus.Counter
	appendTime    prometheus.Histogram
	buildTime     prometheus.Histogram
	flushFailures prometheus.Counter

	sizeEstimate prometheus.Gauge
	builtSize    prometheus.Histogram
}

// newBuilderMetrics 初始化全部 Gauge/Counter/Histogram 指标实例。
// newBuilderMetrics creates a new set of [builderMetrics] for instrumenting
// logs objects.
func newBuilderMetrics() *builderMetrics {
	return &builderMetrics{
		logs:    logs.NewMetrics(),
		streams: streams.NewMetrics(),
		dataobj: dataobj.NewMetrics(),
		targetPageSize: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_dataobj_config_target_page_size_bytes",
			Help: "Configured target page size in bytes.",
		}),
		targetObjectSize: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_dataobj_config_target_object_size_bytes",
			Help: "Configured target object size in bytes.",
		}),
		appends: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_dataobj_appends_total",
			Help: "Total number of appends.",
		}),
		appendTime: prometheus.NewHistogram(prometheus.HistogramOpts{
			Name: "loki_dataobj_append_time_seconds",
			Help: "Time taken appending a set of log lines in a stream to a data object.",

			Buckets:                         prometheus.DefBuckets,
			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: 0,
		}),
		buildTime: prometheus.NewHistogram(prometheus.HistogramOpts{
			Name: "loki_dataobj_build_time_seconds",
			Help: "Time taken building a data object to flush.",

			Buckets:                         prometheus.DefBuckets,
			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: 0,
		}),
		sizeEstimate: prometheus.NewGauge(prometheus.GaugeOpts{
			Name: "loki_dataobj_size_estimate_bytes",
			Help: "Current estimated size of the data object in bytes.",
		}),
		builtSize: prometheus.NewHistogram(prometheus.HistogramOpts{
			Name: "loki_dataobj_built_size_bytes",
			Help: "Distribution of constructed data object sizes in bytes.",

			NativeHistogramBucketFactor:     1.1,
			NativeHistogramMaxBucketNumber:  100,
			NativeHistogramMinResetDuration: 0,
		}),
		flushFailures: prometheus.NewCounter(prometheus.CounterOpts{
			Name: "loki_dataobj_flush_failures_total",
			Help: "Total number of flush failures.",
		}),
	}
}

// ObserveConfig 将 TargetPageSize 与 TargetObjectSize 写入配置 Gauge。
// ObserveConfig updates config metrics based on the provided [BuilderConfig].
func (m *builderMetrics) ObserveConfig(cfg BuilderConfig) {
	m.targetPageSize.Set(float64(cfg.TargetPageSize))
	m.targetObjectSize.Set(float64(cfg.TargetObjectSize))
}

// Register 向 Registerer 注册所有 builder 相关指标。
// Register registers metrics to report to reg.
func (m *builderMetrics) Register(reg prometheus.Registerer) error {
	var errs []error

	errs = append(errs, m.logs.Register(reg))
	errs = append(errs, m.streams.Register(reg))
	errs = append(errs, m.dataobj.Register(reg))

	errs = append(errs, reg.Register(m.targetPageSize))
	errs = append(errs, reg.Register(m.targetObjectSize))

	errs = append(errs, reg.Register(m.appends))
	errs = append(errs, reg.Register(m.appendTime))
	errs = append(errs, reg.Register(m.buildTime))

	errs = append(errs, reg.Register(m.sizeEstimate))
	errs = append(errs, reg.Register(m.builtSize))
	errs = append(errs, reg.Register(m.flushFailures))

	return errors.Join(errs...)
}

// Unregister 从 Registerer 注销全部已注册指标。
// Unregister unregisters metrics from the provided Registerer.
func (m *builderMetrics) Unregister(reg prometheus.Registerer) {
	m.logs.Unregister(reg)
	m.streams.Unregister(reg)
	m.dataobj.Unregister(reg)

	reg.Unregister(m.targetPageSize)
	reg.Unregister(m.targetObjectSize)

	reg.Unregister(m.appends)
	reg.Unregister(m.appendTime)
	reg.Unregister(m.buildTime)

	reg.Unregister(m.sizeEstimate)
	reg.Unregister(m.builtSize)
	reg.Unregister(m.flushFailures)
}
