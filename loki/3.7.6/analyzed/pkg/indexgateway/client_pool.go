package indexgateway

// client_pool 封装到单个 Index Gateway 实例的 gRPC 连接：组合 Health 与 IndexGateway 客户端，供 ring 模式连接池工厂使用。

import (
	"io"

	"github.com/pkg/errors"
	"google.golang.org/grpc"
	"google.golang.org/grpc/health/grpc_health_v1"

	"github.com/grafana/loki/v3/pkg/logproto"
)

// ClientPool 嵌入 HealthClient、IndexGatewayClient 与 Closer，表示一条活跃 gRPC 连接。
// ClientPool represents a pool of gRPC connections to different index gateway instances.
//
// Only used when Index Gateway is configured to run in ring mode.
type ClientPool struct {
	grpc_health_v1.HealthClient
	logproto.IndexGatewayClient
	io.Closer
}

// NewClientPool 对给定地址 Dial gRPC 并创建健康检查与索引网关 protobuf 客户端。
// NewClientPool instantiates a new pool of IndexGateway GRPC connections.
//
// Internally, it also instantiates a protobuf index gateway client and a health client.
func NewClientPool(address string, opts []grpc.DialOption) (*ClientPool, error) {
	// nolint:staticcheck // grpc.Dial() has been deprecated; we'll address it before upgrading to gRPC 2.
	conn, err := grpc.Dial(address, opts...)
	if err != nil {
		return nil, errors.Wrap(err, "shipper new grpc pool dial")
	}

	return &ClientPool{
		Closer:             conn,
		HealthClient:       grpc_health_v1.NewHealthClient(conn),
		IndexGatewayClient: logproto.NewIndexGatewayClient(conn),
	}, nil
}
// 仅 ring 模式下由 clientpool 工厂为每个 ring 实例创建 ClientPool。
