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

// cronUpdate 表示 PATCH 请求体中可部分更新的定时任务字段。
type cronUpdate struct {
	Branch   *string `json:"branch"`
	Target   *string `json:"target"`
	Disabled *bool   `json:"disabled"`
}

// HandleUpdate 返回 HTTP 处理器，部分更新定时任务的分支、目标或启用状态。
func HandleUpdate(
	repos core.RepositoryStore,
	crons core.CronStore,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			namespace = chi.URLParam(r, "owner")
			name      = chi.URLParam(r, "name")
			cron      = chi.URLParam(r, "cron")
		)
		repo, err := repos.FindName(r.Context(), namespace, name)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		cronjob, err := crons.FindName(r.Context(), repo.ID, cron)
		if err != nil {
			render.NotFound(w, err)
			return
		}

		in := new(cronUpdate)
		json.NewDecoder(r.Body).Decode(in)
		// 仅合并请求体中显式提供的非 nil 字段。
		if in.Branch != nil {
			cronjob.Branch = *in.Branch
		}
		if in.Target != nil {
			cronjob.Target = *in.Target
		}
		if in.Disabled != nil {
			cronjob.Disabled = *in.Disabled
		}

		err = crons.Update(r.Context(), cronjob)
		if err != nil {
			render.InternalError(w, err)
			return
		}
		render.JSON(w, cronjob, 200)
	}
}
