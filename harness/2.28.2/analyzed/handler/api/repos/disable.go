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

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/logger"

	"github.com/go-chi/chi"
)

// HandleDisable 返回 HTTP 处理器，停用指定仓库；可选参数 remove=true 时同时删除仓库记录。
func HandleDisable(
	repos core.RepositoryStore,
	sender core.WebhookSender,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			owner = chi.URLParam(r, "owner")
			name  = chi.URLParam(r, "name")
		)

		repo, err := repos.FindName(r.Context(), owner, name)
		if err != nil {
			render.NotFound(w, err)
			logger.FromRequest(r).
				WithError(err).
				WithField("namespace", owner).
				WithField("name", name).
				Debugln("api: repository not found")
			return
		}
		repo.Active = false
		err = repos.Update(r.Context(), repo)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).
				WithError(err).
				WithField("namespace", owner).
				WithField("name", name).
				Warnln("api: cannot update repository")
			return
		}

		action := core.WebhookActionDisabled
		if r.FormValue("remove") == "true" {
			action = core.WebhookActionDeleted
			err = repos.Delete(r.Context(), repo)
			if err != nil {
				render.InternalError(w, err)
				logger.FromRequest(r).
					WithError(err).
					WithField("namespace", owner).
					WithField("name", name).
					Warnln("api: cannot delete repository")
				return
			}
		}

		err = sender.Send(r.Context(), &core.WebhookData{
			Event:  core.WebhookEventRepo,
			Action: action,
			Repo:   repo,
		})
		if err != nil {
			logger.FromRequest(r).
				WithError(err).
				WithField("namespace", owner).
				WithField("name", name).
				Warnln("api: cannot send webhook")
		}

		render.JSON(w, repo, 200)
	}
}
