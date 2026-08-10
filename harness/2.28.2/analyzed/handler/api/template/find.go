// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

//go:build !oss
// +build !oss

// template 包提供流水线模板（Template）相关的 REST API 处理器。
package template

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

// HandleFind 返回 HTTP 处理器，按命名空间与名称查询单个模板详情并以 JSON 返回。
func HandleFind(templateStore core.TemplateStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		var (
			name      = chi.URLParam(r, "name")
			namespace = chi.URLParam(r, "namespace")
		)
		template, err := templateStore.FindName(r.Context(), name, namespace)
		if err != nil {
			render.NotFound(w, err)
			return
		}
		render.JSON(w, template, 200)
	}
}
