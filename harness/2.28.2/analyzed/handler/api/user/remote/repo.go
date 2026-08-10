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

// remote 包提供从远程 SCM 查询仓库信息的 API 处理器。
package remote

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/handler/api/request"
	"github.com/drone/drone/logger"
	"github.com/drone/go-scm/scm"

	"github.com/go-chi/chi"
)

// HandleRepo 返回 HTTP 处理器，按 owner/name 从远程 SCM 查询单个仓库并以 JSON 写入响应。
func HandleRepo(repos core.RepositoryService) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			viewer, _ = request.UserFrom(r.Context())

			owner = chi.URLParam(r, "owner")
			name  = chi.URLParam(r, "name")
			slug  = scm.Join(owner, name)
		)

		repo, err := repos.Find(r.Context(), viewer, slug)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Debugln("api: cannot get remote repository")
			return
		}

		// 附加当前用户在远程仓库上的权限信息。
		perms, err := repos.FindPerm(r.Context(), viewer, slug)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Debugln("api: cannot get remote repository permissions")
		} else {
			repo.Perms = perms
		}

		render.JSON(w, repo, 200)
	}
}
