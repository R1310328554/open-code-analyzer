package unmarshal

// unmarshal 包优化 push/tail JSON 解码：先解到 loghttp.PushRequest 再 unsafe 复用 Streams 切片避免二次分配。

import (
	"io"
	"unsafe"

	jsoniter "github.com/json-iterator/go"

	"github.com/grafana/loki/v3/pkg/loghttp"
	"github.com/grafana/loki/v3/pkg/logproto"
)

// DecodePushRequest 经 loghttp 中间结构解码，Streams 字段与 logproto 布局兼容故可指针复用。
// DecodePushRequest directly decodes json to a logproto.PushRequest
func DecodePushRequest(b io.Reader, r *logproto.PushRequest) error {
	var request loghttp.PushRequest

	if err := jsoniter.NewDecoder(b).Decode(&request); err != nil {
		return err
	}

	*r = logproto.PushRequest{
		Streams: *(*[]logproto.Stream)(unsafe.Pointer(&request.Streams)), //#nosec G103 -- Just preventing an allocation, safe, there's no chance of an incorrect type cast here. -- nosemgrep: use-of-unsafe-block
	}

	return nil
}

// WebsocketReader 抽象 tail 订阅的 ReadMessage，便于单测 mock websocket 帧。
// WebsocketReader knows how to read message to a websocket connection.
type WebsocketReader interface {
	ReadMessage() (int, []byte, error)
}

// ReadTailResponseJSON 读一帧 JSON 并 jsoniter.Unmarshal 到 TailResponse。
// ReadTailResponseJSON unmarshals the loghttp.TailResponse from a websocket reader.
func ReadTailResponseJSON(r *loghttp.TailResponse, reader WebsocketReader) error {
	_, data, err := reader.ReadMessage()
	if err != nil {
		return err
	}
	return jsoniter.Unmarshal(data, r)
}
// unsafe.Pointer 转换仅用于同构 slice 类型别名，字段布局变更时需同步审查安全性。
