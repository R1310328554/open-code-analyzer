package utils //nolint:revive

// 测试用 Remote Write HTTP 服务端：解析 Snappy 压缩的 logproto.PushRequest，
// 附带 X-Scope-OrgID 租户 ID 写入 channel，供 Promtail 集成测试断言。

import (
	"math"
	"net/http"
	"net/http/httptest"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/util"
)

// RemoteWriteRequest wraps the received logs remote write request that is received.
// 封装一次 remote write 请求的租户 ID 与解码后的 PushRequest 体。
type RemoteWriteRequest struct {
	TenantID string
	Request  logproto.PushRequest
}

// 启动 httptest.Server，handler 解析 body 后写入 receivedChan 并返回指定状态码。
// NewRemoteWriteServer creates and starts a new httpserver.Server that can handle remote write request. When a request is handled,
// the received entries are written to receivedChan, and status is responded.
func NewRemoteWriteServer(receivedChan chan RemoteWriteRequest, status int) *httptest.Server {
	server := httptest.NewServer(createServerHandler(receivedChan, status))
	return server
}

// 返回 HandlerFunc：ParseProtoReader 解码 Snappy PushRequest 并转发到 channel。
func createServerHandler(receivedReqsChan chan RemoteWriteRequest, receivedOKStatus int) http.HandlerFunc {
	return func(rw http.ResponseWriter, req *http.Request) {
		// Parse the request
		var pushReq logproto.PushRequest
		if err := util.ParseProtoReader(req.Context(), req.Body, int(req.ContentLength), math.MaxInt32, &pushReq, util.RawSnappy); err != nil {
			rw.WriteHeader(500)
			return
		}

		receivedReqsChan <- RemoteWriteRequest{
			TenantID: req.Header.Get("X-Scope-OrgID"),
			Request:  pushReq,
		}

		rw.WriteHeader(receivedOKStatus)
	}
}
