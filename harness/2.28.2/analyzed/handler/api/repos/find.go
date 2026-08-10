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

// repos 包提供仓库生命周期与配置相关的 REST API 处理器。
package repos

import (
	"net/http"

	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/handler/api/request"
)

// HandleFind 返回 HTTP 处理器，从请求上下文中读取已鉴权的仓库及其权限并以 JSON 返回。
func HandleFind() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()
		repo, _ := request.RepoFrom(ctx)
		perm, _ := request.PermFrom(ctx)
		repo.Perms = perm
		render.JSON(w, repo, 200)
	}
}
