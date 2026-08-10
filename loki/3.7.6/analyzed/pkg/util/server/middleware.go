package server

// server 包 HTTP 中间件：预解析表单体与默认 JSON Content-Type，配合 dskit middleware 与 httpgrpc 错误响应。

import (
	"net/http"

	"github.com/grafana/dskit/httpgrpc"
	"github.com/grafana/dskit/middleware"
)

// NewPrepopulateMiddleware 在 handler 前 ParseForm，支持 POST x-www-form-urlencoded 替代 GET 查询串。
// NewPrepopulateMiddleware creates a middleware which will parse incoming http forms.
// This is important because some endpoints can POST x-www-form-urlencoded bodies instead of GET w/ query strings.
func NewPrepopulateMiddleware() middleware.Interface {
	return middleware.Func(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
			err := req.ParseForm()
			if err != nil {
				WriteError(httpgrpc.Errorf(http.StatusBadRequest, "%s", err.Error()), w)
				return

			}
			next.ServeHTTP(w, req)
		})
	})
}

// ResponseJSONMiddleware 在响应后若未设置 Content-Type 则补 application/json; charset=UTF-8。
// ResponseJSONMiddleware sets the Content-Type header to JSON if it's not set.
func ResponseJSONMiddleware() middleware.Interface {
	return middleware.Func(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, req *http.Request) {
			next.ServeHTTP(w, req)
			if w.Header().Get("Content-Type") == "" {
				w.Header().Set("Content-Type", "application/json; charset=UTF-8")
			}
		})
	})
}
// ParseForm 失败时通过 WriteError 返回 400，避免下游 handler 读取未解析 body。
