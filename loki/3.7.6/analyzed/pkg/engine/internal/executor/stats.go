package executor

// stats 定义 executor 包内 xcap 统计项：通用 pipeline 指标与 ColumnCompat 碰撞标记。

import "github.com/grafana/loki/v3/pkg/xcap"

// xcap statistics used in executor pkg.
var (
	// 通用统计覆盖输出行数、Read 调用次数与累计读耗时（纳秒）。
// Common statistics tracked for all pipeline nodes.
	statRowsOut      = xcap.NewStatisticInt64("rows_out", xcap.AggregationTypeSum)
	statReadCalls    = xcap.NewStatisticInt64("read_calls", xcap.AggregationTypeSum)
	statReadDuration = xcap.NewStatisticInt64("read_duration_ns", xcap.AggregationTypeSum)

	// statCompatCollisionFound 在列名冲突检测命中时置位，供兼容层诊断。
// [ColumnCompat] statistics.
	statCompatCollisionFound = xcap.NewStatisticFlag("collision_found")
)
// xcap region 在 pipeline 执行结束时上报 StatPipelineExecDuration 等衍生指标。
