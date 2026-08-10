package loghttp

// legacy 包定义实时 tail（WebSocket）查询的 HTTP JSON 响应结构。

import (
	"time"

	"github.com/grafana/loki/v3/pkg/logproto"
)

// DroppedEntry 记录 tail 过程中因背压或限流被丢弃的日志条目及其标签。
// DroppedEntry represents a dropped entry in a tail call
type DroppedEntry struct {
	Timestamp time.Time
	Labels    string
}

// TailResponse 包含推送的新日志流及被丢弃条目列表，供客户端增量合并。
// TailResponse represents the http json response to a tail query
type TailResponse struct {
	Streams        []logproto.Stream `json:"streams"`
	DroppedEntries []DroppedEntry    `json:"dropped_entries"`
}
// DroppedEntries 使客户端能感知丢失数据并在 UI 上提示用户。
