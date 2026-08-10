package loki

// status 提供 /services 调试端点：列出当前 target 已启动模块及其 dskit 服务状态。

import (
	"fmt"
	"net/http"
)

func (t *Loki) servicesHandler(w http.ResponseWriter, _ *http.Request) {
	w.WriteHeader(200)
	w.Header().Set("Content-Type", "text/plain")

	// 未来可扩展为递归打印子服务状态；当前仅展示顶层模块服务。
// TODO: this could be extended to also print sub-services, if given service has any
	for mod, s := range t.serviceMap {
		if s != nil {
			fmt.Fprintf(w, "%v => %v\n", mod, s.State())
		}
	}
}
// 该处理器在 Run 中注册于 HTTP /services，仅用于运维诊断而非公开 API。
