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

// HandleDelete 返回 HTTP 处理器，按命名空间与名称删除全局密钥。
func HandleDelete(secrets core.GlobalSecretStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			namespace = chi.URLParam(r, "namespace")
			name      = chi.URLParam(r, "name")
		)
		s, err := secrets.FindName(r.Context(), namespace, name)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		err = secrets.Delete(r.Context(), s)
		if err != nil {
			render.InternalError(w, err)
			return
		}
		w.WriteHeader(http.StatusNoContent)
	}
}
