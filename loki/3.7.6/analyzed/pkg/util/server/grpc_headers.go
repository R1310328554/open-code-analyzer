package server

// server 包 grpc 拦截器将 HTTP 上下文头透传至 gRPC metadata：客户端注入 LokiDisablePipelineWrappers 头，服务端还原到 context。

import (
	"context"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"

	"github.com/grafana/loki/v3/pkg/util/httpreq"
)

func injectHTTPHeadersIntoGRPCRequest(ctx context.Context) context.Context {
	header := httpreq.ExtractHeader(ctx, httpreq.LokiDisablePipelineWrappersHeader)
	if header == "" {
		return ctx
	}

	// inject into GRPC metadata
	md, ok := metadata.FromOutgoingContext(ctx)
	if !ok {
		md = metadata.New(map[string]string{})
	}
	md = md.Copy()
	md.Set(httpreq.LokiDisablePipelineWrappersHeader, header)

	return metadata.NewOutgoingContext(ctx, md)
}

// extractHTTPHeadersFromGRPCRequest 从入站 metadata 取首值并 InjectHeader 回 context。
func extractHTTPHeadersFromGRPCRequest(ctx context.Context) context.Context {
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		// No metadata, just return as is
		return ctx
	}

	headerValues := md.Get(httpreq.LokiDisablePipelineWrappersHeader)
	if len(headerValues) == 0 {
		return ctx
	}

	return httpreq.InjectHeader(ctx, httpreq.LokiDisablePipelineWrappersHeader, headerValues[0])
}

// UnaryClientHTTPHeadersInterceptor 为一元 RPC 在 invoker 前注入 HTTP 头。
func UnaryClientHTTPHeadersInterceptor(ctx context.Context, method string, req, reply interface{}, cc *grpc.ClientConn, invoker grpc.UnaryInvoker, opts ...grpc.CallOption) error {
	return invoker(injectHTTPHeadersIntoGRPCRequest(ctx), method, req, reply, cc, opts...)
}

func StreamClientHTTPHeadersInterceptor(ctx context.Context, desc *grpc.StreamDesc, cc *grpc.ClientConn, method string, streamer grpc.Streamer, opts ...grpc.CallOption) (grpc.ClientStream, error) {
	return streamer(injectHTTPHeadersIntoGRPCRequest(ctx), desc, cc, method, opts...)
}

func UnaryServerHTTPHeadersnIterceptor(ctx context.Context, req interface{}, _ *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (interface{}, error) {
	return handler(extractHTTPHeadersFromGRPCRequest(ctx), req)
}

// StreamServerHTTPHeadersInterceptor 包装 serverStream 使流式 handler 可见提取后的 ctx。
func StreamServerHTTPHeadersInterceptor(srv interface{}, ss grpc.ServerStream, _ *grpc.StreamServerInfo, handler grpc.StreamHandler) error {
	return handler(srv, serverStream{
		ctx:          extractHTTPHeadersFromGRPCRequest(ss.Context()),
		ServerStream: ss,
	})
}
// 无对应 metadata 时 inject/extract 均为 no-op，避免覆盖已有出站上下文。
