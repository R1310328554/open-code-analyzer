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

// stages 包提供构建阶段审批、拒绝等人工审核相关的 HTTP API 处理器。
package stages

import (
	"context"
	"net/http"
	"strconv"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// noContext 供调度器使用，避免请求上下文取消影响后台调度。
var noContext = context.Background()

// HandleApprove 返回 HTTP 处理器，批准处于 blocked 状态、等待人工审核的阶段并提交调度。
func HandleApprove(
	repos core.RepositoryStore,
	builds core.BuildStore,
	stages core.StageStore,
	sched core.Scheduler,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			namespace = chi.URLParam(r, "owner")
			name      = chi.URLParam(r, "name")
		)
		buildNumber, err := strconv.ParseInt(chi.URLParam(r, "number"), 10, 64)
		if err != nil {
			render.BadRequestf(w, "Invalid build number")
			return
		}
		stageNumber, err := strconv.Atoi(chi.URLParam(r, "stage"))
		if err != nil {
			render.BadRequestf(w, "Invalid stage number")
			return
		}
		repo, err := repos.FindName(r.Context(), namespace, name)
		if err != nil {
			render.NotFoundf(w, "Repository not found")
			return
		}
		build, err := builds.FindNumber(r.Context(), repo.ID, buildNumber)
		if err != nil {
			render.NotFoundf(w, "Build not found")
			return
		}
		stage, err := stages.FindNumber(r.Context(), build.ID, stageNumber)
		if err != nil {
			render.NotFoundf(w, "Stage not found")
			return
		}
		if stage.Status != core.StatusBlocked {
			render.BadRequestf(w, "Cannot approve a Pipeline with Status %q", stage.Status)
			return
		}
		// 存在上游依赖时先进入 waiting，否则直接进入 pending 等待调度。
		if len(stage.DependsOn) > 0 {
			stage.Status = core.StatusWaiting
		} else {
			stage.Status = core.StatusPending
		}
		err = stages.Update(r.Context(), stage)
		if err != nil {
			render.InternalErrorf(w, "There was a problem approving the Pipeline")
			return
		}
		err = sched.Schedule(noContext, stage)
		if err != nil {
			render.InternalErrorf(w, "There was a problem scheduling the Pipeline")
			return
		}
		w.WriteHeader(http.StatusNoContent)
	}
}
