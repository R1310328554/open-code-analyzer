// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package builds

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/logger"
)

// HandleIncomplete 返回 HTTP 处理器，将未完成构建列表以 JSON 写入响应体。
func HandleIncomplete(repos core.RepositoryStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		list, err := repos.ListIncomplete(r.Context())
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Debugln("api: cannot list incomplete builds")
		} else {
			render.JSON(w, list, 200)
		}
	}
}

// HandleRunningStatus 返回 HTTP 处理器，将正在运行的构建状态列表以 JSON 写入响应体。
func HandleRunningStatus(repos core.RepositoryStore) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		list, err := repos.ListRunningStatus(r.Context())
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Debugln("api: cannot list incomplete builds")
		} else {
			render.JSON(w, list, 200)
		}
	}
}
