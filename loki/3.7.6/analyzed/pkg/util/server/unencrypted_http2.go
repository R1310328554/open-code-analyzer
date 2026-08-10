package server

// server 包 EnableUnencryptedHTTP2 为 http.Server 启用 cleartext HTTP/2（h2c），同时保留 HTTP/1.x，供内网或 sidecar 无 TLS 场景。

import "net/http"

// EnableUnencryptedHTTP2 懒创建 srv.Protocols 并 SetHTTP1/SetUnencryptedHTTP2 为 true。
// EnableUnencryptedHTTP2 configures srv to accept HTTP/1.x and cleartext HTTP/2 (h2c).
func EnableUnencryptedHTTP2(srv *http.Server) {
	protocols := srv.Protocols
	if protocols == nil {
		protocols = new(http.Protocols)
		srv.Protocols = protocols
	}
	protocols.SetHTTP1(true)
	protocols.SetUnencryptedHTTP2(true)
}
// Go 1.24+ 通过 http.Protocols 显式声明协议，未配置时默认可能拒绝 h2c 升级。
