package loghttp

// loghttp 包中 DetectedFields 相关类型，映射 Loki 检测字段/标签 API 的 JSON 响应。

import "github.com/grafana/loki/v3/pkg/logproto"

// DetectedFieldsResponse 表示 detected fields/labels 查询的 HTTP JSON 响应体。
// LabelResponse represents the http json response to a label query
type DetectedFieldsResponse struct {
	Fields []DetectedField `json:"fields,omitempty"`
	Values []string        `json:"values,omitempty"`
}

// DetectedField 描述单个被检测字段的标签名、类型、基数及可用 parser 列表。
type DetectedField struct {
	Label       string                     `json:"label,omitempty"`
	Type        logproto.DetectedFieldType `json:"type,omitempty"`
	Cardinality uint64                     `json:"cardinality,omitempty"`
	Parsers     []string                   `json:"parsers,omitempty"`
}
// Values 字段在 values 端点返回字符串列表；Fields 在 fields 端点返回结构化详情。
