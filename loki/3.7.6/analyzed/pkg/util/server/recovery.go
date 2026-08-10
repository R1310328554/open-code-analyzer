package server

// server 包 recovery 中间件统一捕获 HTTP/gRPC/queryrange panic：记录堆栈、递增 panic_total 并返回 500 httpgrpc 错误。

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"runtime"

	"github.com/grafana/dskit/httpgrpc"
	"github.com/grafana/dskit/middleware"
	grpc_recovery "github.com/grpc-ecosystem/go-grpc-middleware/v2/interceptors/recovery"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/querier/queryrange/queryrangebase"
	"github.com/grafana/loki/v3/pkg/util/constants"
)

// maxStacksize 限制 runtime.Stack 缓冲，防止超大栈输出阻塞 stderr。
const maxStacksize = 8 * 1024

var (
	panicTotal = promauto.NewCounter(prometheus.CounterOpts{
		Namespace: constants.Loki,
		Name:      "panic_total",
		Help:      "The total number of panic triggered",
	})

	RecoveryHTTPMiddleware = middleware.Func(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
			defer func() {
				if p := recover(); p != nil {
					WriteError(onPanic(p), w)
				}
			}()
			next.ServeHTTP(w, req)
		})
	})
	RecoveryGRPCStreamInterceptor = grpc_recovery.StreamServerInterceptor(grpc_recovery.WithRecoveryHandler(onPanic))
	RecoveryGRPCUnaryInterceptor  = grpc_recovery.UnaryServerInterceptor(grpc_recovery.WithRecoveryHandler(onPanic))

	RecoveryMiddleware queryrangebase.Middleware = queryrangebase.MiddlewareFunc(func(next queryrangebase.Handler) queryrangebase.Handler {
		return queryrangebase.HandlerFunc(func(ctx context.Context, req queryrangebase.Request) (res queryrangebase.Response, err error) {
			defer func() {
				if p := recover(); p != nil {
					err = onPanic(p)
				}
			}()
			res, err = next.Do(ctx, req)
			return
		})
	})
)

// onPanic 打印 multiline 栈到 stderr，Inc panic_total，包装 InternalServerError。
func onPanic(p interface{}) error {
	stack := make([]byte, maxStacksize)
	stack = stack[:runtime.Stack(stack, true)]
	// keep a multiline stack
	fmt.Fprintf(os.Stderr, "panic: %v\n%s", p, stack)
	panicTotal.Inc()
	return httpgrpc.Errorf(http.StatusInternalServerError, "error while processing request: %v", p)
}
// queryrange 路径 panic 转为 error 返回，避免整个 querier 进程因单请求崩溃退出。
