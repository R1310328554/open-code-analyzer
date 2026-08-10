// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package metric

import (
	"errors"
	"net/http"

	"github.com/drone/drone/core"

	"github.com/prometheus/client_golang/prometheus/promhttp"
)

// errInvalidToken 在 Prometheus 访问令牌缺失或无效时返回。
var errInvalidToken = errors.New("Invalid or missing prometheus token")

// errAccessDenied 在已认证用户无权访问指标端点时返回。
var errAccessDenied = errors.New("Access denied")

// Server 是暴露 Prometheus 指标的 HTTP 服务处理器。
type Server struct {
	metrics   http.Handler
	session   core.Session
	anonymous bool
}

// NewServer 创建指标 HTTP 服务；anonymous 为 true 时跳过身份校验。
func NewServer(session core.Session, anonymous bool) *Server {
	return &Server{
		metrics:   promhttp.Handler(),
		session:   session,
		anonymous: anonymous,
	}
}

// ServeHTTP 处理指标请求：校验会话权限后以纯文本格式输出 Prometheus 指标。
func (s *Server) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	user, _ := s.session.Get(r)
	switch {
	case !s.anonymous && user == nil:
		http.Error(w, errInvalidToken.Error(), http.StatusUnauthorized)
	case !s.anonymous && !user.Admin && !user.Machine:
		http.Error(w, errAccessDenied.Error(), http.StatusForbidden)
	default:
		s.metrics.ServeHTTP(w, r)
	}
}
