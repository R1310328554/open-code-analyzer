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

// secrets 包在 OSS 构建中提供全局密钥 API 的占位实现，所有端点均返回未实现。
package secrets

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// notImplemented 为 OSS 版统一返回 501 Not Implemented 的占位处理器。
var notImplemented = func(w http.ResponseWriter, r *http.Request) {
	render.NotImplemented(w, render.ErrNotImplemented)
}

// HandleCreate 创建全局密钥（OSS 版未实现）。
func HandleCreate(core.GlobalSecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleUpdate 更新全局密钥（OSS 版未实现）。
func HandleUpdate(core.GlobalSecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleDelete 删除全局密钥（OSS 版未实现）。
func HandleDelete(core.GlobalSecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleFind 按名称查询单个全局密钥（OSS 版未实现）。
func HandleFind(core.GlobalSecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleList 列出命名空间下的全局密钥（OSS 版未实现）。
func HandleList(core.GlobalSecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleAll 列出全部全局密钥（OSS 版未实现）。
func HandleAll(core.GlobalSecretStore) http.HandlerFunc {
	return notImplemented
}
