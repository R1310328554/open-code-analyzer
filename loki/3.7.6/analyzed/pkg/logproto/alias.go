package logproto

// alias 为 push 包类型提供 logproto 命名空间别名，避免大规模 import 路径迁移。

import (
	"google.golang.org/grpc"

	"github.com/grafana/loki/pkg/push"
)

// 以下类型别名将 github.com/grafana/loki/pkg/push 映射到 logproto 包名。
// Aliases to avoid renaming all the imports of logproto

type Entry = push.Entry
type Stream = push.Stream
type LabelAdapter = push.LabelAdapter
type PushRequest = push.PushRequest
type PushResponse = push.PushResponse
type PusherClient = push.PusherClient
type PusherServer = push.PusherServer

// NewPusherClient 创建 gRPC Pusher 客户端，供 distributor/ingester 推送日志。
func NewPusherClient(cc *grpc.ClientConn) PusherClient {
	return push.NewPusherClient(cc)
}

func RegisterPusherServer(s *grpc.Server, srv PusherServer) {
	push.RegisterPusherServer(s, srv)
}
// Entry/Stream/PushRequest 等核心推送类型通过 type alias 保持 API 稳定。
