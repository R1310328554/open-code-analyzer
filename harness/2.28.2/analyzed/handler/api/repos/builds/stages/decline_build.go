// Copyright 2024 Drone IO, Inc.
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

package stages

import (
	"fmt"
	"net/http"
	"strconv"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// HandleDeclineBuild 返回 HTTP 处理器，拒绝整次构建中所有处于 blocked 状态的阶段。
func HandleDeclineBuild(
	repos core.RepositoryStore,
	builds core.BuildStore,
	stages core.StageStore,
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

		stageList, err := stages.List(r.Context(), build.ID)
		if err != nil {
			render.NotFoundf(w, "Stages not found")
			return
		}

		// 任一阶段非 blocked 则拒绝整次 decline 操作。
		for _, stage := range stageList {
			if stage.Status != core.StatusBlocked {
				err := fmt.Errorf("Cannot decline build with status %q", stage.Status)
				render.BadRequest(w, err)
				return
			}

			stage.Status = core.StatusDeclined
			err = stages.Update(r.Context(), stage)
			if err != nil {
				render.InternalError(w, err)
				return
			}
		}

		build.Status = core.StatusDeclined
		err = builds.Update(r.Context(), build)
		if err != nil {
			render.InternalError(w, err)
			return
		}
		// TODO: 从构建队列中删除所有 pending 阶段
		// TODO: 将数据库中 pending 阶段更新为 skipped
		// TODO: 在源码管理系统中将构建状态更新为 error
		w.WriteHeader(http.StatusNoContent)
	}
}
