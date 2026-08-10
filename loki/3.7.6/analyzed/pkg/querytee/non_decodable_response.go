package querytee

// NonDecodableResponse 为 query-tee 在无法解码 queryrange 响应时使用的占位类型，实现 queryrangebase.Response 以便错误仍可被中间件捕获。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
)

// NonDecodableResponse 保存 HTTP 状态码与原始 body，供 writeResponse 回写客户端。
// NonDecodableResponse is a minimal response type used when returning errors.
// It satisfies the queryrangebase.Response interface, and allows the querytee
// to capture responses that would otherwise be lost.
// StatusCode 与 Body 分别对应下游错误响应的状态与正文。
type NonDecodableResponse struct {
	StatusCode int
	Body       []byte
}

// Reset implements proto.Message
func (e *NonDecodableResponse) Reset() {}

// String implements proto.Message
func (e *NonDecodableResponse) String() string {
	return fmt.Sprintf("ErrorResponse{StatusCode: %d}", e.StatusCode)
}

// ProtoMessage implements proto.Message
func (e *NonDecodableResponse) ProtoMessage() {}

// GetHeaders/WithHeaders 返回空 Prometheus 头，满足 Response 接口。
// GetHeaders implements queryrangebase.Response
func (e *NonDecodableResponse) GetHeaders() []*queryrangebase.PrometheusResponseHeader {
	return []*queryrangebase.PrometheusResponseHeader{}
}

// WithHeaders implements queryrangebase.Response
func (e *NonDecodableResponse) WithHeaders(_ []queryrangebase.PrometheusResponseHeader) queryrangebase.Response {
	return &NonDecodableResponse{
		StatusCode: e.StatusCode,
		Body:       e.Body,
	}
}

// SetHeader implements queryrangebase.Response
func (e *NonDecodableResponse) SetHeader(_, _ string) {}
// ProtoMessage 方法为 protobuf 兼容桩，不参与实际序列化。
