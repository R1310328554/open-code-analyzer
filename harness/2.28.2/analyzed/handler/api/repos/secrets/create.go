// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// secrets 包提供仓库级密钥 CRUD 的 HTTP API 处理器（非 OSS 构建）。
package secrets

import (
	"encoding/json"
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// secretInput 是创建仓库密钥时请求体的 JSON 结构。
type secretInput struct {
	Type            string `json:"type"`
	Name            string `json:"name"`
	Data            string `json:"data"`
	PullRequest     bool   `json:"pull_request"`
	PullRequestPush bool   `json:"pull_request_push"`
}

// HandleCreate 返回 HTTP 处理器，在指定仓库下创建新密钥。
func HandleCreate(
	repos core.RepositoryStore,
	secrets core.SecretStore,
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
		in := new(secretInput)
		err = json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		s := &core.Secret{
			RepoID:          repo.ID,
			Name:            in.Name,
			Data:            in.Data,
			PullRequest:     in.PullRequest,
			PullRequestPush: in.PullRequestPush,
		}

		err = s.Validate()
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		err = secrets.Create(r.Context(), s)
		if err != nil {
			render.InternalError(w, err)
			return
		}

		// 返回副本，避免响应中泄露原始密钥值以外的敏感字段。
		s = s.Copy()
		render.JSON(w, s, 200)
	}
}
