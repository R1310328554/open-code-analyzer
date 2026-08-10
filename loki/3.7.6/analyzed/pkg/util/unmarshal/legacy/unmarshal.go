package unmarshal

// unmarshal legacy 子包保留 json-iterator 直解码 logproto.PushRequest 路径：与新版 loghttp 中间层解码并存，供兼容旧 push API 客户端。

import (
	"io"

	json "github.com/json-iterator/go"

	"github.com/grafana/loki/v3/pkg/logproto"
)

// DecodePushRequest 直接将 JSON 流解码到 logproto.PushRequest，无 loghttp 转换层。
// DecodePushRequest directly decodes json to a logproto.PushRequest
func DecodePushRequest(b io.Reader, r *logproto.PushRequest) error {
	return json.NewDecoder(b).Decode(r)
}
// 新代码应优先使用 pkg/util/unmarshal 以减少分配；legacy 仅作向后兼容。
