// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// template 包提供流水线模板（Template）相关的 REST API 处理器。
package template

import (
	"encoding/json"
	"net/http"
	"path/filepath"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/errors"
	"github.com/drone/drone/handler/api/render"

	"github.com/go-chi/chi"
)

var (
	// errTemplateExtensionInvalid 表示模板文件名扩展名不在允许列表中。
	errTemplateExtensionInvalid = errors.New("Template extension invalid. Must be yaml, starlark or jsonnet")
)

// templateInput 为创建模板请求体，包含文件名与模板内容。
type templateInput struct {
	Name string `json:"name"`
	Data string `json:"data"`
}

// HandleCreate 返回 HTTP 处理器，在指定命名空间下创建新模板并返回 JSON。
func HandleCreate(templateStore core.TemplateStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		namespace := chi.URLParam(r, "namespace")
		in := new(templateInput)
		err := json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		// 校验模板文件扩展名必须为 yaml、starlark 或 jsonnet 之一。
		switch filepath.Ext(in.Name) {
		case ".yml", ".yaml":
		case ".star", ".starlark", ".script":
		case ".jsonnet":
		default:
			render.BadRequest(w, errTemplateExtensionInvalid)
			return
		}

		t := &core.Template{
			Name:      in.Name,
			Data:      in.Data,
			Namespace: namespace,
		}

		err = t.Validate()
		if err != nil {
			render.BadRequest(w, err)
			return
		}

		err = templateStore.Create(r.Context(), t)
		if err != nil {
			render.InternalError(w, err)
			return
		}

		render.JSON(w, t, 200)
	}
}
