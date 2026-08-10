package client

// compactor client 定义 Compactor 客户端抽象接口：
// 查询删除请求、缓存世代号，并暴露 JobQueue gRPC 客户端。

import (
	"context"

	"github.com/grafana/loki/v3/pkg/compactor/client/grpc"
	"github.com/grafana/loki/v3/pkg/compactor/deletion"
	"github.com/grafana/loki/v3/pkg/compactor/deletion/deletionproto"
)

// CompactorClient 封装与 Compactor 服务交互的核心 RPC 能力。
type CompactorClient interface {
	GetAllDeleteRequestsForUser(ctx context.Context, userID string, forQuerytimeFiltering bool, timeRange *deletion.TimeRange) ([]deletionproto.DeleteRequest, error)
	GetCacheGenerationNumber(ctx context.Context, userID string) (string, error)

// JobQueueClient 返回压缩任务队列的 gRPC 客户端。
	JobQueueClient() grpc.JobQueueClient

	Name() string
	Stop()
}
