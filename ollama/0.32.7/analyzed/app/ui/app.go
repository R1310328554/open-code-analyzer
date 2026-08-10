//go:build windows || darwin

// ui 包内嵌 React 构建产物，并为桌面应用提供 SPA 静态资源 HTTP 处理器。
package ui

import (
	"bytes"
	"embed"
	"errors"
	"io/fs"
	"net/http"
	"strings"
	"time"
)

//go:embed app/dist
var appFS embed.FS

// appHandler 返回服务 React SPA 的 HTTP 处理器：优先返回真实静态文件，未知路径回退 index.html 以支持 React Router。
//
// appHandler returns an HTTP handler that serves the React SPA.
// It tries to serve real files first, then falls back to index.html for React Router.
func (s *Server) appHandler() http.Handler {
	// 去掉 dist 前缀，使 URL 路径更简洁
	// Strip the dist prefix so URLs look clean
	fsys, _ := fs.Sub(appFS, "app/dist")
	fileServer := http.FileServer(http.FS(fsys))

	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		p := strings.TrimPrefix(r.URL.Path, "/")
		if _, err := fsys.Open(p); err == nil {
			// 直接返回匹配的静态文件
			// Serve the file directly
			fileServer.ServeHTTP(w, r)
			return
		}
		// 回退到 index.html，使客户端路由可处理未知路径
		// Fallback – serve index.html for unknown paths so React Router works
		data, err := fs.ReadFile(fsys, "index.html")
		if err != nil {
			if errors.Is(err, fs.ErrNotExist) {
				http.NotFound(w, r)
			} else {
				http.Error(w, "Internal Server Error", http.StatusInternalServerError)
			}
			return
		}
		http.ServeContent(w, r, "index.html", time.Time{}, bytes.NewReader(data))
	})
}
