package logqlmodel

// logqlmodel 定义 LogQL 查询结果容器：Streams 实现 promql.Value，Result 聚合数据、统计、响应头与警告。

import (
	"github.com/prometheus/prometheus/promql/parser"

	"github.com/grafana/loki/pkg/push"

	"github.com/grafana/loki/v3/pkg/logqlmodel/stats"
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase/definitions"
)

// ValueTypeStreams 为日志流结果的 promql/parser ValueType 字符串常量。
// ValueTypeStreams promql.ValueType for log streams
const ValueTypeStreams = "streams"

// PackedEntryKey 标识 pack stage 写入的嵌套 JSON 日志字段键 _entry。
// PackedEntryKey is a special JSON key used by the pack promtail stage and unpack parser
const PackedEntryKey = "_entry"

// Result 封装 parser.Value 数据面、stats.Result 统计、Prometheus 响应头与警告列表。
// Result is the result of a query execution.
type Result struct {
	Data       parser.Value
	Statistics stats.Result
	Headers    []*definitions.PrometheusResponseHeader
	Warnings   []string
}

// Streams is promql.Value
// Streams 为 push.Stream 切片，实现 sort.Interface 按 Labels 字符串排序。
type Streams []push.Stream

func (streams Streams) Len() int      { return len(streams) }
func (streams Streams) Swap(i, j int) { streams[i], streams[j] = streams[j], streams[i] }
func (streams Streams) Less(i, j int) bool {
	return streams[i].Labels <= streams[j].Labels
}

// Type 返回 ValueTypeStreams，使引擎与 HTTP 层识别日志流结果类型。
// Type implements `promql.Value` and `parser.Value`
func (Streams) Type() parser.ValueType { return ValueTypeStreams }

// String implements `promql.Value` and `parser.Value`
func (Streams) String() string {
	return ""
}

// Lines 统计所有流中日志条目总数，供限制与统计摘要使用。
func (streams Streams) Lines() int64 {
	var res int64
	for _, s := range streams {
		res += int64(len(s.Entries))
	}
	return res
}
// Streams.String 为空实现，日志流的人类可读展示由上层 API 格式化负责。
