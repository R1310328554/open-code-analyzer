package server

// server 包 query tags 拦截器在 gRPC 链路透传 X-Query-Tags：context 值序列化到 metadata，跨 hop 后在服务端写回 QueryTagsHTTPHeader。

import (
	"context"

	"google.golang.org/grpc"
	"google.golang.org/grpc/metadata"

	"github.com/grafana/loki/v3/pkg/util/httpreq"
)

func getQueryTags(ctx context.Context) string {
	v, _ := ctx.Value(httpreq.QueryTagsHTTPHeader).(string)
	return v
}

// injectIntoGRPCRequest 将非空 query tags 写入出站 gRPC metadata 副本。
func injectIntoGRPCRequest(ctx context.Context) context.Context {
	queryTags := getQueryTags(ctx)
	if queryTags == "" {
		return ctx
	}

	// inject into GRPC metadata
	md, ok := metadata.FromOutgoingContext(ctx)
	if !ok {
		md = metadata.New(map[string]string{})
	}
	md = md.Copy()
	md.Set(string(httpreq.QueryTagsHTTPHeader), queryTags)

	return metadata.NewOutgoingContext(ctx, md)
}

func extractFromGRPCRequest(ctx context.Context) context.Context {
	md, ok := metadata.FromIncomingContext(ctx)
	if !ok {
		// No metadata, just return as is
		return ctx
	}

	headerValues := md.Get(string(httpreq.QueryTagsHTTPHeader))
	if len(headerValues) == 0 {
		return ctx
	}

	return context.WithValue(ctx, httpreq.QueryTagsHTTPHeader, headerValues[0])
}

// UnaryClientQueryTagsInterceptor 为一元客户端调用注入 tags，最终映射为 HTTP/2 头。
// UnaryClientQueryTagsInterceptor propagates the query tags from the context to gRPC metadata, which eventually ends up as a HTTP2 header.
// For unary gRPC requests.
func UnaryClientQueryTagsInterceptor(ctx context.Context, method string, req, reply interface{}, cc *grpc.ClientConn, invoker grpc.UnaryInvoker, opts ...grpc.CallOption) error {
	return invoker(injectIntoGRPCRequest(ctx), method, req, reply, cc, opts...)
}

// StreamClientQueryTagsInterceptor propagates the query tags from the context to gRPC metadata, which eventually ends up as a HTTP2 header.
// For streaming gRPC requests.
func StreamClientQueryTagsInterceptor(ctx context.Context, desc *grpc.StreamDesc, cc *grpc.ClientConn, method string, streamer grpc.Streamer, opts ...grpc.CallOption) (grpc.ClientStream, error) {
	return streamer(injectIntoGRPCRequest(ctx), desc, cc, method, opts...)
}

// UnaryServerQueryTagsInterceptor propagates the query tags from the gRPC metadata back to our context for unary gRPC requests.
func UnaryServerQueryTagsInterceptor(ctx context.Context, req interface{}, _ *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (interface{}, error) {
	return handler(extractFromGRPCRequest(ctx), req)
}

// StreamServerQueryTagsInterceptor 流式服务端从 metadata 还原 tags 到包装 stream 的 ctx。
// StreamServerQueryTagsInterceptor propagates the query tags from the gRPC metadata back to our context for streaming gRPC requests.
func StreamServerQueryTagsInterceptor(srv interface{}, ss grpc.ServerStream, _ *grpc.StreamServerInfo, handler grpc.StreamHandler) error {
	return handler(srv, serverStream{
		ctx:          extractFromGRPCRequest(ss.Context()),
		ServerStream: ss,
	})
}

// serverStream 嵌入 grpc.ServerStream，Context 返回注入 query tags 后的 ctx。
type serverStream struct {
	ctx context.Context
	grpc.ServerStream
}

func (ss serverStream) Context() context.Context {
	return ss.ctx
}
// query tags 用于限额审计与 blocked_queries 规则匹配，需与 HTTP 网关侧保持一致。
