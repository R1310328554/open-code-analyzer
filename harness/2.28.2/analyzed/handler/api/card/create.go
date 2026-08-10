// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// Package card 提供流水线步骤卡片（Card）的 CRUD HTTP 处理器。
package card

import (
	"bytes"
	"encoding/json"
	"io/ioutil"
	"net/http"
	"strconv"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// HandleCreate 返回创建新卡片的 HTTP 处理器。
// 请求体为 JSON 格式的 core.CardInput，含卡片数据与 schema；
// 路径参数定位仓库、构建、阶段与步骤，成功后返回步骤 ID。
func HandleCreate(
	buildStore core.BuildStore,
	cardStore core.CardStore,
	stageStore core.StageStore,
	stepStore core.StepStore,
	repoStore core.RepositoryStore,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			namespace = chi.URLParam(r, "owner") // 仓库命名空间（所有者）
			name      = chi.URLParam(r, "name")   // 仓库名称
		)

		// 解析 URL 中的构建编号
		buildNumber, err := strconv.ParseInt(chi.URLParam(r, "build"), 10, 64)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		// 解析阶段序号
		stageNumber, err := strconv.Atoi(chi.URLParam(r, "stage"))
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		// 解析步骤序号
		stepNumber, err := strconv.Atoi(chi.URLParam(r, "step"))
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		// 解码请求体中的卡片输入
		in := new(core.CardInput)
		err = json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		// 逐级查找仓库、构建、阶段与步骤
		repo, err := repoStore.FindName(r.Context(), namespace, name)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		build, err := buildStore.FindNumber(r.Context(), repo.ID, buildNumber)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		stage, err := stageStore.FindNumber(r.Context(), build.ID, stageNumber)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		step, err := stepStore.FindNumber(r.Context(), stage.ID, stepNumber)
		if err != nil {
			render.NotFound(w, err)
			return
		}

		data := ioutil.NopCloser(
			bytes.NewBuffer(in.Data),
		)

		// 创建卡片并持久化
		err = cardStore.Create(r.Context(), step.ID, data)
		if err != nil {
			render.InternalError(w, err)
			return
		}

		// 将 schema 写入步骤并更新
		step.Schema = in.Schema
		err = stepStore.Update(r.Context(), step)
		if err != nil {
			render.InternalError(w, err)
			return
		}
		render.JSON(w, step.ID, 200)
	}
}
