// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// secrets 包提供全局（组织/命名空间级）密钥管理的 HTTP API 处理器。
package secrets

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// HandleAll 返回 HTTP 处理器，列出系统中全部全局密钥（不含明文值）。
func HandleAll(secrets core.GlobalSecretStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		list, err := secrets.ListAll(r.Context())
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
