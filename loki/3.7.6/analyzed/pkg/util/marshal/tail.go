package marshal

// marshal 包 tail 子模块将 legacy.DroppedEntry 转为 loghttp.DroppedStream，供 tail 响应中 dropped_entries 字段 JSON 序列化。

import (
	"github.com/grafana/loki/v3/pkg/loghttp"
	legacy "github.com/grafana/loki/v3/pkg/loghttp/legacy"
)

// NewDroppedStream 解析 Labels 字符串为 LabelSet 并携带丢弃时间戳。
// NewDroppedStream constructs a DroppedStream from a legacy.DroppedEntry
func NewDroppedStream(s *legacy.DroppedEntry) (loghttp.DroppedStream, error) {
	l, err := NewLabelSet(s.Labels)
	if err != nil {
		return loghttp.DroppedStream{}, err
	}

	return loghttp.DroppedStream{
		Timestamp: s.Timestamp,
		Labels:    l,
	}, nil
}
// 标签解析失败时返回零值 DroppedStream 与包装错误，encodeDroppedEntries 会中止编码。
