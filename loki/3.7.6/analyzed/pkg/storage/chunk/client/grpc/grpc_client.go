package grpc

// grpc_client 配置远程 gRPC store 地址并建立连接：keepalive 保活、非 TLS（insecure）Dial，返回 GrpcStoreClient 与可关闭的 ClientConn。

import (
	"flag"
	"time"

	"github.com/pkg/errors"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/keepalive"
)

// Config 仅含 server_address，对应 grpc-store.server-address 标志。
// Config for a StorageClient
type Config struct {
	Address string `yaml:"server_address,omitempty"`
}

// RegisterFlags adds the flags required to config this to the given FlagSet
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	f.StringVar(&cfg.Address, "grpc-store.server-address", "", "Hostname or IP of the gRPC store instance.")
}

// connectToGrpcServer 设置 20s ping/10s 超时 keepalive，Dial 后包装 NewGrpcStoreClient。
func connectToGrpcServer(serverAddress string) (GrpcStoreClient, *grpc.ClientConn, error) {
	params := keepalive.ClientParameters{
		Time:                time.Second * 20,
		Timeout:             time.Second * 10,
		PermitWithoutStream: true,
	}
	param := grpc.WithKeepaliveParams(params)

	// nolint:staticcheck // grpc.Dial() has been deprecated; we'll address it before upgrading to gRPC 2.
	cc, err := grpc.Dial(serverAddress, param, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		return nil, nil, errors.Wrapf(err, "failed to dial grpc-store %s", serverAddress)
	}
	return NewGrpcStoreClient(cc), cc, nil
}
// 生产环境通常在前置代理终止 TLS；此处 insecure _credentials 适用于内网或测试拓扑。
