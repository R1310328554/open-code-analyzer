// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// crons 包提供仓库定时任务（Cron Job）相关的 REST API 处理器。
package crons

import (
	"encoding/json"
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// HandleCreate 返回 HTTP 处理器，为指定仓库创建新的定时任务。
func HandleCreate(
	repos core.RepositoryStore,
	crons core.CronStore,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			namespace = chi.URLParam(r, "owner")
			name      = chi.URLParam(r, "name")
		)
		repo, err := repos.FindName(r.Context(), namespace, name)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		in := new(core.Cron)
		err = json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			return
		}
		// 组装 Cron 实体：默认触发 push 事件，绑定仓库并校验表达式。
		cronjob := new(core.Cron)
		cronjob.Event = core.EventPush
		cronjob.Branch = in.Branch
		cronjob.RepoID = repo.ID
		cronjob.SetName(in.Name)
		err = cronjob.SetExpr(in.Expr)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		err = cronjob.Validate()
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		err = crons.Create(r.Context(), cronjob)
		if err != nil {
			render.InternalError(w, err)
			return
		}
		render.JSON(w, cronjob, 200)
	}
}
