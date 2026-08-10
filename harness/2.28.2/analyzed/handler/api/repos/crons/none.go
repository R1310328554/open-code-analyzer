// Copyright 2019 Drone IO, Inc.
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

// +build oss

// crons 包在 OSS 构建中提供定时任务 API 的占位实现，所有端点均返回未实现。
package crons

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// notImplemented 为 OSS 版统一返回 501 Not Implemented 的占位处理器。
var notImplemented = func(w http.ResponseWriter, r *http.Request) {
	render.NotImplemented(w, render.ErrNotImplemented)
}

// HandleCreate 创建定时任务（OSS 版未实现）。
func HandleCreate(core.RepositoryStore, core.CronStore) http.HandlerFunc {
	return notImplemented
}

// HandleUpdate 更新定时任务配置（OSS 版未实现）。
func HandleUpdate(core.RepositoryStore, core.CronStore) http.HandlerFunc {
	return notImplemented
}

// HandleDelete 删除定时任务（OSS 版未实现）。
func HandleDelete(core.RepositoryStore, core.CronStore) http.HandlerFunc {
	return notImplemented
}

// HandleFind 查询单个定时任务（OSS 版未实现）。
func HandleFind(core.RepositoryStore, core.CronStore) http.HandlerFunc {
	return notImplemented
}

// HandleList 列出仓库定时任务（OSS 版未实现）。
func HandleList(core.RepositoryStore, core.CronStore) http.HandlerFunc {
	return notImplemented
}

// HandleExec 手动触发定时任务（OSS 版未实现）。
func HandleExec(core.UserStore, core.RepositoryStore, core.CronStore,
	core.CommitService, core.Triggerer) http.HandlerFunc {
	return notImplemented
}
