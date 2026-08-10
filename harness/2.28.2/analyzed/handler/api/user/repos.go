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

// user 包提供当前登录用户相关的 REST API 处理器。
package user

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/handler/api/request"
	"github.com/drone/drone/logger"
)

// HandleRepos 返回 HTTP 处理器，列出当前用户已激活的本地仓库并以 JSON 写入响应。
// 查询参数 latest=true 时仅返回最近访问的仓库。
func HandleRepos(repos core.RepositoryStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		viewer, _ := request.UserFrom(r.Context())

		var list []*core.Repository
		var err error
		if r.FormValue("latest") != "true" {
			list, err = repos.List(r.Context(), viewer.ID)
		} else {
			list, err = repos.ListLatest(r.Context(), viewer.ID)
		}
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Debugln("api: cannot list repositories")
		} else {
			render.JSON(w, list, 200)
		}
	}
}
