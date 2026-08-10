package client

// Compactor gRPC 客户端：通过 dskit grpcclient 连接 Compactor，
// 拉取删除请求、缓存世代号，并暴露 JobQueue 客户端供水平扩展压缩。

import (
	"context"
	"flag"
	"strings"
	"time"

	"github.com/grafana/dskit/grpcclient"
	"github.com/grafana/dskit/instrument"
	"github.com/grafana/dskit/middleware"
	"github.com/grafana/dskit/user"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"google.golang.org/grpc"

	compactor_grpc "github.com/grafana/loki/v3/pkg/compactor/client/grpc"
	"github.com/grafana/loki/v3/pkg/compactor/deletion"
	"github.com/grafana/loki/v3/pkg/compactor/deletion/deletionproto"
)

// GRPCConfig 封装 compactor.grpc-client 前缀的 gRPC 客户端配置。
type GRPCConfig struct {
	GRPCClientConfig grpcclient.Config `yaml:",inline"`
}

// RegisterFlags registers flags.
func (cfg *GRPCConfig) RegisterFlags(f *flag.FlagSet) {
	cfg.GRPCClientConfig.RegisterFlagsWithPrefix("compactor.grpc-client", f)
}

// compactorGRPCClient 持有连接及 Compactor/JobQueue 两个 gRPC 桩客户端。
type compactorGRPCClient struct {
	cfg GRPCConfig

	grpcClientRequestDuration *prometheus.HistogramVec
	conn                      *grpc.ClientConn
	grpcClient                compactor_grpc.CompactorClient
	jobQueueClient            compactor_grpc.JobQueueClient
}

// NewGRPCClient 建立 gRPC 连接，注入租户头并注册请求耗时直方图。
// NewGRPCClient supports only methods which are used for internal communication of Loki like
// loading delete requests, cache gen numbers for query time filtering and interacting with job queue for horizontal scaling of compactor.
func NewGRPCClient(addr string, cfg GRPCConfig, r prometheus.Registerer) (CompactorClient, error) {
	client := &compactorGRPCClient{
		cfg: cfg,
		grpcClientRequestDuration: promauto.With(r).NewHistogramVec(prometheus.HistogramOpts{
			Namespace: "loki_compactor",
			Name:      "grpc_request_duration_seconds",
			Help:      "Time (in seconds) spent serving requests when using compactor GRPC client",
			Buckets:   instrument.DefBuckets,
		}, []string{"operation", "status_code"}),
	}

	dialOpts, err := cfg.GRPCClientConfig.DialOption(
		[]grpc.UnaryClientInterceptor{
			func(ctx context.Context, method string, req, reply any, cc *grpc.ClientConn, invoker grpc.UnaryInvoker, opts ...grpc.CallOption) error {
				// JobQueue has no auth methods so do not try to set the auth header
				if !strings.HasPrefix(method, "/grpc.JobQueue/") {
					var err error
					ctx, err = user.InjectIntoGRPCRequest(ctx)
					if err != nil {
						return err
					}
				}
				return invoker(ctx, method, req, reply, cc, opts...)
			},
			middleware.UnaryClientInstrumentInterceptor(client.grpcClientRequestDuration),
		}, []grpc.StreamClientInterceptor{
			func(ctx context.Context, desc *grpc.StreamDesc, cc *grpc.ClientConn, method string, streamer grpc.Streamer, opts ...grpc.CallOption) (grpc.ClientStream, error) {
				// JobQueue has no auth methods so do not try to set the auth header
				if !strings.HasPrefix(method, "/grpc.JobQueue/") {
					var err error
					ctx, err = user.InjectIntoGRPCRequest(ctx)
					if err != nil {
						return nil, err
					}
				}
				return streamer(ctx, desc, cc, method, opts...)
			},
			middleware.StreamClientInstrumentInterceptor(client.grpcClientRequestDuration),
		},
		middleware.NoOpInvalidClusterValidationReporter,
	)
	if err != nil {
		return nil, err
	}

	// nolint:staticcheck // grpc.Dial() has been deprecated; we'll address it before upgrading to gRPC 2.
	client.conn, err = grpc.Dial(addr, dialOpts...)
	if err != nil {
		return nil, err
	}

	client.grpcClient = compactor_grpc.NewCompactorClient(client.conn)
	client.jobQueueClient = compactor_grpc.NewJobQueueClient(client.conn)
	return client, nil
}

func (s *compactorGRPCClient) Stop() {
	s.conn.Close()
}

// GetAllDeleteRequestsForUser 注入 OrgID 后调用 GetDeleteRequests RPC 并转换响应。
func (s *compactorGRPCClient) GetAllDeleteRequestsForUser(ctx context.Context, userID string, forQuerytimeFiltering bool, timeRange *deletion.TimeRange) ([]deletionproto.DeleteRequest, error) {
	ctx = user.InjectOrgID(ctx, userID)

	req := &compactor_grpc.GetDeleteRequestsRequest{
		ForQuerytimeFiltering: forQuerytimeFiltering,
	}

	if timeRange != nil {
		req.StartTime = time.UnixMilli(int64(timeRange.Start))
		req.EndTime = time.UnixMilli(int64(timeRange.End))
	}

	grpcResp, err := s.grpcClient.GetDeleteRequests(ctx, req)
	if err != nil {
		return nil, err
	}

	deleteRequests := make([]deletionproto.DeleteRequest, len(grpcResp.DeleteRequests))
	for i, dr := range grpcResp.DeleteRequests {
		deleteRequests[i] = deletionproto.DeleteRequest{
			RequestID: dr.RequestID,
			StartTime: dr.StartTime,
			EndTime:   dr.EndTime,
			Query:     dr.Query,
			Status:    dr.Status,
			CreatedAt: dr.CreatedAt,
		}
	}

	return deleteRequests, nil
}

// GetCacheGenerationNumber 查询租户 results cache 世代号用于查询侧缓存失效。
func (s *compactorGRPCClient) GetCacheGenerationNumber(ctx context.Context, userID string) (string, error) {
	ctx = user.InjectOrgID(ctx, userID)
	grpcResp, err := s.grpcClient.GetCacheGenNumbers(ctx, &compactor_grpc.GetCacheGenNumbersRequest{})
	if err != nil {
		return "", err
	}

	return grpcResp.ResultsCacheGen, nil
}

func (s *compactorGRPCClient) JobQueueClient() compactor_grpc.JobQueueClient {
	return compactor_grpc.NewJobQueueClient(s.conn)
}

func (s *compactorGRPCClient) Name() string {
	return "grpc_client"
}
