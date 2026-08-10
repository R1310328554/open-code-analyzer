// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// secrets 包提供全局密钥（Global Secret）相关的 REST API 处理器。
package secrets

import (
	"encoding/json"
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// secretUpdate 为更新全局密钥请求体，字段均为可选指针以便部分更新。
type secretUpdate struct {
	Data            *string `json:"data"`
	PullRequest     *bool   `json:"pull_request"`
	PullRequestPush *bool   `json:"pull_request_push"`
}

// HandleUpdate 返回 HTTP 处理器，按命名空间与名称更新已有全局密钥并返回更新后的 JSON。
func HandleUpdate(secrets core.GlobalSecretStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			namespace = chi.URLParam(r, "namespace")
			name      = chi.URLParam(r, "name")
		)

		in := new(secretUpdate)
		err := json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		s, err := secrets.FindName(r.Context(), namespace, name)
		if err != nil {
			render.NotFound(w, err)
			return
		}

		// 仅合并请求体中显式提供的字段。
		if in.Data != nil {
			s.Data = *in.Data
		}
		if in.PullRequest != nil {
			s.PullRequest = *in.PullRequest
		}
		if in.PullRequestPush != nil {
			s.PullRequestPush = *in.PullRequestPush
		}

		err = s.Validate()
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		err = secrets.Update(r.Context(), s)
		if err != nil {
			render.InternalError(w, err)
			return
		}

		s = s.Copy()
		render.JSON(w, s, 200)
	}
}
