package loghttp

// legacy 包定义旧版即时/范围 LogQL 查询的 HTTP JSON 响应类型。

import (
	"time"
)

// QueryResponse 封装查询结果，Streams 字段承载各标签组合下的日志条目。
// QueryResponse represents the http json response to a label query
type QueryResponse struct {
	Streams []*Stream `json:"streams,omitempty"`
}

// Stream 表示单条日志流：Labels 为 Prometheus 风格标签串，Entries 为时间有序日志行。
// Stream represents a log stream.  It includes a set of log entries and their labels.
type Stream struct {
	Labels  string  `json:"labels"`
	Entries []Entry `json:"entries"`
}

// Entry 单条日志：Timestamp 为 RFC3339 可序列化时间戳，Line 为原始日志文本。
// Entry represents a log entry.  It includes a log message and the time it occurred at.
type Entry struct {
	Timestamp time.Time `json:"ts"`
	Line      string    `json:"line"`
}
// legacy 查询响应与新版 loghttp.Stream 结构类似，但标签字段为字符串而非 LabelSet。
