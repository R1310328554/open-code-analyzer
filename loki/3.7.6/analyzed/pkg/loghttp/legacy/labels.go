package loghttp

// legacy 包提供旧版 Loki HTTP API 的 JSON 响应结构，供兼容客户端解析标签查询结果。

// LabelResponse 为标签查询 HTTP JSON 响应，Values 列出匹配时间范围内的标签值。
// LabelResponse represents the http json response to a label query
type LabelResponse struct {
	Values []string `json:"values,omitempty"`
}
// 旧版 /api/prom/label 端点返回此结构，与 Prometheus 标签 API 格式兼容。
