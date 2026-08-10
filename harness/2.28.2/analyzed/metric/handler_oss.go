// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// +build oss

package metric

import (
	"net/http"

	"github.com/drone/drone/core"
)

// Server 是 OSS 构建中的空操作指标 HTTP 服务，不暴露任何 Prometheus 数据。
type Server struct {
}

// NewServer 返回空操作指标服务实例，保留与非 OSS 版本相同的构造签名。
func NewServer(session core.Session, anonymous bool) *Server {
	return new(Server)
}

// ServeHTTP 为空操作处理器，OSS 构建不对外提供指标端点。
func (s *Server) ServeHTTP(w http.ResponseWriter, r *http.Request) {}
