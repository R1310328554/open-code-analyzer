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

// collabs 包在 OSS 构建中提供协作者 API 的占位实现，所有端点均返回未实现。
package collabs

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// notImplemented 为 OSS 版统一返回 501 Not Implemented 的占位处理器。
var notImplemented = func(w http.ResponseWriter, r *http.Request) {
	render.NotImplemented(w, render.ErrNotImplemented)
}

// HandleDelete 删除协作者成员关系（OSS 版未实现）。
func HandleDelete(core.UserStore, core.RepositoryStore, core.PermStore) http.HandlerFunc {
	return notImplemented
}

// HandleFind 查询单个协作者详情（OSS 版未实现）。
func HandleFind(core.UserStore, core.RepositoryStore, core.PermStore) http.HandlerFunc {
	return notImplemented
}

// HandleList 列出仓库协作者（OSS 版未实现）。
func HandleList(core.RepositoryStore, core.PermStore) http.HandlerFunc {
	return notImplemented
}
