package transport

// transport 定义 query-frontend 与 querier/scheduler 之间的传输抽象：GrpcRoundTripper 处理 httpgrpc 消息，Codec 扩展 queryrangebase 编解码。

import (
	"context"

	"github.com/grafana/dskit/httpgrpc"

	"github.com/grafana/loki/v3/pkg/querier/queryrange"
	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
)

// GrpcRoundTripper 接口以 httpgrpc.HTTPRequest/Response 替代原生 net/http 往返。
// GrpcRoundTripper is similar to http.RoundTripper, but works with HTTP requests converted to protobuf messages.
type GrpcRoundTripper interface {
	RoundTripGRPC(context.Context, *httpgrpc.HTTPRequest) (*httpgrpc.HTTPResponse, error)
}

// Codec 在 queryrangebase.Codec 基础上增加 HTTP-gRPC 响应解码与 QueryRequestWrap。
type Codec interface {
	queryrangebase.Codec
	DecodeHTTPGrpcResponse(r *httpgrpc.HTTPResponse, req queryrangebase.Request) (queryrangebase.Response, error)
	QueryRequestWrap(context.Context, queryrangebase.Request) (*queryrange.QueryRequest, error)
}
// QueryRequestWrap 支持 protobuf 编码路径下将 queryrange 请求包装为 QueryRequest。
