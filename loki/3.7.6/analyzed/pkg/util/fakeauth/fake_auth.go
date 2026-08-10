// fakeauth 包注入固定 org ID fake，使单租户测试环境无需完整认证栈即可走多租户代码路径。
// Package fakeauth provides middlewares thats injects a fake userID, so the rest of the code
// 启用真实认证时使用 dskit AuthenticateUser；否则 HTTP/gRPC 均注入 fake 用户头。
// can continue to be multitenant.
package fakeauth

import (
	"context"
	"net/http"

	"github.com/grafana/dskit/middleware"
	"github.com/grafana/dskit/server"
	"github.com/grafana/dskit/user"
	"google.golang.org/grpc"
)

// SetupAuthMiddleware 根据 enabled 挂载 gRPC/HTTP 中间件，noGRPCAuthOn 可豁免指定 RPC。
// SetupAuthMiddleware for the given server config.
func SetupAuthMiddleware(config *server.Config, enabled bool, noGRPCAuthOn []string) middleware.Interface {
	if enabled {
		ignoredMethods := map[string]bool{}
		for _, m := range noGRPCAuthOn {
			ignoredMethods[m] = true
		}

		config.GRPCMiddleware = append(config.GRPCMiddleware, func(ctx context.Context, req interface{}, info *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (resp interface{}, err error) {
			if ignoredMethods[info.FullMethod] {
				return handler(ctx, req)
			}
			return middleware.ServerUserHeaderInterceptor(ctx, req, info, handler)
		})

		config.GRPCStreamMiddleware = append(config.GRPCStreamMiddleware,
			func(srv interface{}, ss grpc.ServerStream, info *grpc.StreamServerInfo, handler grpc.StreamHandler) error {
				if ignoredMethods[info.FullMethod] {
					return handler(srv, ss)
				}
				return middleware.StreamServerUserHeaderInterceptor(srv, ss, info, handler)
			},
		)

		return middleware.AuthenticateUser
	}

	config.GRPCMiddleware = append(config.GRPCMiddleware,
		fakeGRPCAuthUniaryMiddleware,
	)
	config.GRPCStreamMiddleware = append(config.GRPCStreamMiddleware,
		fakeGRPCAuthStreamMiddleware,
	)
	return fakeHTTPAuthMiddleware
}

// fakeHTTPAuthMiddleware 在请求上下文注入 org fake，下游 handler 可读取租户 ID。
var fakeHTTPAuthMiddleware = middleware.Func(func(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		ctx := user.InjectOrgID(r.Context(), "fake")
		next.ServeHTTP(w, r.WithContext(ctx))
	})
})

var fakeGRPCAuthUniaryMiddleware = func(ctx context.Context, req interface{}, _ *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (interface{}, error) {
	ctx = user.InjectOrgID(ctx, "fake")
	return handler(ctx, req)
}

var fakeGRPCAuthStreamMiddleware = func(srv interface{}, ss grpc.ServerStream, _ *grpc.StreamServerInfo, handler grpc.StreamHandler) error {
	ctx := user.InjectOrgID(ss.Context(), "fake")
	return handler(srv, serverStream{
		ctx:          ctx,
		ServerStream: ss,
	})
}

// serverStream 包装 grpc.ServerStream，用注入 org 的 context 覆盖 Context() 返回值。
type serverStream struct {
	ctx context.Context
	grpc.ServerStream
}

func (ss serverStream) Context() context.Context {
	return ss.ctx
}
// fakeGRPCAuthStreamMiddleware 对流式 RPC 同样注入 org，保证流上下文与一元调用一致。
