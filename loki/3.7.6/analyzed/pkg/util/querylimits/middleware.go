package querylimits

// querylimits 包 middleware 从 HTTP 请求头提取查询限制策略并注入 context，供下游查询路径读取 per-request 覆盖的全局限额。

import (
	"net/http"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/middleware"

	util_log "github.com/grafana/loki/v3/pkg/util/log"
)

type queryLimitsMiddleware struct {
	logger log.Logger
}

// NewQueryLimitsMiddleware 返回 dskit middleware.Interface，解析失败时记录 warn 并继续。
// NewQueryLimitsMiddleware creates a middleware that extracts the query limits
// policy from the HTTP header and injects it into the context of the request.
func NewQueryLimitsMiddleware(logger log.Logger) middleware.Interface {
	return &queryLimitsMiddleware{
		logger: logger,
	}
}

// Wrap 依次注入 QueryLimits 与 QueryLimitsContext，再调用 next 处理请求。
// Wrap implements the middleware interface
func (l *queryLimitsMiddleware) Wrap(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		limits, err := ExtractQueryLimitsHTTP(r)
		if err != nil {
			level.Warn(util_log.Logger).Log("msg", "could not extract query limits from header", "err", err)
			limits = nil
		}

		if limits != nil {
			r = r.Clone(InjectQueryLimitsIntoContext(r.Context(), *limits))
		}

		limitsCtx, err := ExtractQueryLimitsContextHTTP(r)
		if err != nil {
			level.Warn(util_log.Logger).Log("msg", "could not extract query limits context from header", "err", err)
			limitsCtx = nil
		}

		if limitsCtx != nil {
			r = r.Clone(InjectQueryLimitsContextIntoContext(r.Context(), *limitsCtx))
		}

		next.ServeHTTP(w, r)
	})
}
// 解析错误不阻断请求，limits 为 nil 时下游使用租户默认配置。
