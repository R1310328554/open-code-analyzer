package constants

// internal_streams 定义内部合成 stream 使用的保留标签名与应用标识：聚合指标、模式检测与 Logs Drilldown 请求来源标记。

const (
// AggregatedMetricLabel 标记由查询引擎生成的聚合指标专用 stream。
	// AggregatedMetricLabel is the label added to streams containing aggregated metrics
	AggregatedMetricLabel = "__aggregated_metric__"
// PatternLabel 标识 pattern 检测模块写入的内部 pattern stream。
	// PatternLabel is the label added to streams containing detected patterns
	PatternLabel = "__pattern__"
// LogsDrilldownAppName 用于识别 Grafana Logs Drilldown 应用的请求来源。
	// LogsDrilldownAppName is the app name used to identify requests from Logs Drilldown
	LogsDrilldownAppName = "grafana-lokiexplore-app"
)
// 这些标签以双下划线前缀表示系统保留，用户日志 stream 不应复用同名 label。
