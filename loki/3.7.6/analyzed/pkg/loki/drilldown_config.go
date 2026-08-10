package loki

// drilldown_config 定义 drilldown-limits API 的 JSON 响应结构，供 Grafana Explore drilldown 获取过滤后的租户限额与特性开关。

// DrilldownConfigResponse 包含 limits 映射、pattern ingester 开关及 Loki 版本字符串。
// DrilldownConfigResponse represents the structure for the drilldown config endpoint
// This endpoint returns the filtered tenant limits in a JSON-optimized format
type DrilldownConfigResponse struct {
	Limits                 map[string]any `json:"limits"`
	PatternIngesterEnabled bool           `json:"pattern_ingester_enabled"`
	Version                string         `json:"version"`
}
// 该端点由 tenantLimitsHandler(forDrilldown=true) 挂载于 /loki/api/v1/drilldown-limits。
