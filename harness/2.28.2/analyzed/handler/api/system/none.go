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

// system 包在 OSS 构建中提供系统 API 的占位实现，所有端点均返回未实现。
package system

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// notImplemented 为 OSS 版统一返回 501 Not Implemented 的占位处理器。
var notImplemented = func(w http.ResponseWriter, r *http.Request) {
	render.NotImplemented(w, render.ErrNotImplemented)
}

// HandleLicense 查询许可证信息（OSS 版未实现）。
func HandleLicense(license core.License) http.HandlerFunc {
	return notImplemented
}

// HandleStats 查询系统运行统计（OSS 版未实现）。
func HandleStats(
	core.BuildStore,
	core.StageStore,
	core.UserStore,
	core.RepositoryStore,
	core.Pubsub,
	core.LogStream,
) http.HandlerFunc {
	return notImplemented
}
