package engine

// metrics 定义 Thor（V2）查询引擎的 Prometheus 指标：子查询计数、逻辑/物理/工作流规划耗时及执行耗时直方图。

import (
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

var (
	status               = "status"
	statusSuccess        = "success"
	statusFailure        = "failure"
	statusNotImplemented = "notimplemented"
)

// 注意：指标名称与标签可能随引擎演进快速变更，接入方需关注 CHANGELOG。
// NOTE: Metrics are subject to rapid change!
type metrics struct {
	subqueries       *prometheus.CounterVec
	logicalPlanning  prometheus.Histogram
	physicalPlanning prometheus.Histogram
	workflowPlanning prometheus.Histogram
	execution        prometheus.Histogram
}

// newMetrics 注册 loki_engine_v2_* 系列指标；物理/工作流/执行直方图扩展 15s–60s 线性桶。
func newMetrics(r prometheus.Registerer) *metrics {
	return &metrics{
		subqueries: promauto.With(r).NewCounterVec(prometheus.CounterOpts{
			Name: "loki_engine_v2_subqueries_total",
			Help: "Total number of subqueries executed with the new engine",
		}, []string{status}),
		logicalPlanning: newNativeHistogram(r, prometheus.HistogramOpts{
			Name: "loki_engine_v2_logical_planning_duration_seconds",
			Help: "Duration of logical query planning in seconds",
		}),
		physicalPlanning: newNativeHistogram(r, prometheus.HistogramOpts{
			Name: "loki_engine_v2_physical_planning_duration_seconds",
			Help: "Duration of physical query planning in seconds",
			Buckets: append(
				prometheus.DefBuckets,                    // 0.005s -> 10s
				prometheus.LinearBuckets(15, 5.0, 10)..., // 15s -> 60s
			),
		}),
		workflowPlanning: newNativeHistogram(r, prometheus.HistogramOpts{
			Name: "loki_engine_v2_workflow_planning_duration_seconds",
			Help: "Duration of workflow query planning in seconds",
			Buckets: append(
				prometheus.DefBuckets,                    // 0.005s -> 10s
				prometheus.LinearBuckets(15, 5.0, 10)..., // 15s -> 60s
			),
		}),
		execution: newNativeHistogram(r, prometheus.HistogramOpts{
			Name: "loki_engine_v2_execution_duration_seconds",
			Help: "Duration of query execution in seconds",
			Buckets: append(
				prometheus.DefBuckets,                    // 0.005s -> 10s
				prometheus.LinearBuckets(15, 5.0, 10)..., // 15s -> 60s
			),
		}),
	}
}

// newNativeHistogram 启用原生直方图：桶因子 1.1、最多 100 桶、最小重置间隔 1 小时。
func newNativeHistogram(r prometheus.Registerer, opts prometheus.HistogramOpts) prometheus.Histogram {
	opts.NativeHistogramBucketFactor = 1.1
	opts.NativeHistogramMaxBucketNumber = 100
	opts.NativeHistogramMinResetDuration = time.Hour

	return promauto.With(r).NewHistogram(opts)
}
// status 标签区分 success、failure 与 notimplemented 子查询结果。
