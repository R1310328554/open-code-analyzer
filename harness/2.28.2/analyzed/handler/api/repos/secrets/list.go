// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package secrets

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// HandleList 返回 HTTP 处理器，列出指定仓库下的全部密钥（不含明文值）。
func HandleList(
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
		list, err := secrets.List(r.Context(), repo.ID)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		// 复制每条记录并剔除密钥明文后再返回。
		secrets := []*core.Secret{}
		for _, secret := range list {
			secrets = append(secrets, secret.Copy())
		}
		render.JSON(w, secrets, 200)
	}
}
