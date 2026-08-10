package stats

// WallTimeMiddleware 为 HTTP 查询路径测量 handler 墙钟耗时并写入 context Stats。

import (
	"net/http"
	"time"
)

// WallTimeMiddleware 实现 dskit middleware.Interface，Wrap 下游 HTTP Handler。
// WallTimeMiddleware tracks the wall time.
type WallTimeMiddleware struct{}

// NewWallTimeMiddleware 构造零值中间件，无额外配置。
// NewWallTimeMiddleware makes a new WallTimeMiddleware.
func NewWallTimeMiddleware() WallTimeMiddleware {
	return WallTimeMiddleware{}
}

// Wrap 在 IsEnabled 时记录 start 至 next.ServeHTTP 结束的 duration 到 Stats。
// Wrap implements middleware.Interface.
func (m WallTimeMiddleware) Wrap(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if !IsEnabled(r.Context()) {
			next.ServeHTTP(w, r)
			return
		}

		startTime := time.Now()
		next.ServeHTTP(w, r)

		stats := FromContext(r.Context())
		stats.AddWallTime(time.Since(startTime))
	})
}
// 未启用 stats 时直接透传，避免无 Stats 的 context 产生空指针。
