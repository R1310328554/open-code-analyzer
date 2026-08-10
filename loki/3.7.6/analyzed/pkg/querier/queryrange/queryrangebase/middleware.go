package queryrangebase

// queryrangebase 包 middleware 提供 Results-Cache-Gen-Number 头/上下文注入，使 frontend 与 querier 对 results cache 版本保持一致。

import (
	"context"
	"net/http"

	"github.com/grafana/dskit/middleware"
	"github.com/grafana/dskit/tenant"

	"github.com/grafana/loki/v3/pkg/storage/chunk/cache/resultscache"
)

const (
// ResultsCacheGenNumberHeaderName 为 HTTP 响应头名，标识当前缓存世代。
	// ResultsCacheGenNumberHeaderName holds name of the header we want to set in http response
	ResultsCacheGenNumberHeaderName = "Results-Cache-Gen-Number"
)

func CacheGenNumberHeaderSetterMiddleware(cacheGenNumbersLoader resultscache.CacheGenNumberLoader) middleware.Interface {
	return middleware.Func(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			userIDs, err := tenant.TenantIDs(r.Context())
			if err != nil {
				http.Error(w, err.Error(), http.StatusUnauthorized)
				return
			}

			cacheGenNumber := cacheGenNumbersLoader.GetResultsCacheGenNumber(userIDs)

			w.Header().Set(ResultsCacheGenNumberHeaderName, cacheGenNumber)
			next.ServeHTTP(w, r)
		})
	})
}

// CacheGenNumberContextSetterMiddleware 在 gRPC Handler 响应 SetHeader 注入世代号。
func CacheGenNumberContextSetterMiddleware(cacheGenNumbersLoader resultscache.CacheGenNumberLoader) Middleware {
	return MiddlewareFunc(func(next Handler) Handler {
		return HandlerFunc(func(ctx context.Context, req Request) (Response, error) {
			userIDs, err := tenant.TenantIDs(ctx)
			if err != nil {
				return nil, err
			}

			cacheGenNumber := cacheGenNumbersLoader.GetResultsCacheGenNumber(userIDs)

			res, err := next.Do(ctx, req)
			if err != nil {
				return nil, err
			}

			res.SetHeader(ResultsCacheGenNumberHeaderName, cacheGenNumber)
			return res, nil
		})
	})
}
// tenant.TenantIDs 解析失败时 HTTP 路径返回 401，Handler 路径直接返回错误。
