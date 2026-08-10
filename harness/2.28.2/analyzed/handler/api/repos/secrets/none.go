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

// secrets 包在 OSS 构建中提供未实现的仓库密钥 API 占位处理器。
package secrets

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// notImplemented 是统一的 501 未实现响应处理器。
var notImplemented = func(w http.ResponseWriter, r *http.Request) {
	render.NotImplemented(w, render.ErrNotImplemented)
}

// HandleCreate 在 OSS 版本中返回未实现响应。
func HandleCreate(core.RepositoryStore, core.SecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleUpdate 在 OSS 版本中返回未实现响应。
func HandleUpdate(core.RepositoryStore, core.SecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleDelete 在 OSS 版本中返回未实现响应。
func HandleDelete(core.RepositoryStore, core.SecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleFind 在 OSS 版本中返回未实现响应。
func HandleFind(core.RepositoryStore, core.SecretStore) http.HandlerFunc {
	return notImplemented
}

// HandleList 在 OSS 版本中返回未实现响应。
func HandleList(core.RepositoryStore, core.SecretStore) http.HandlerFunc {
	return notImplemented
}
