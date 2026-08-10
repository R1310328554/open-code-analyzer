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

// Package builds 提供构建相关 HTTP 处理器；本文件为 OSS 构建变体的占位实现。
package builds

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// notImplemented 是 OSS 版统一返回「未实现」的占位处理器。
var notImplemented = func(w http.ResponseWriter, r *http.Request) {
	render.NotImplemented(w, render.ErrNotImplemented)
}

// HandleIncomplete 返回查询未完成构建的 HTTP 处理器；OSS 版未实现。
func HandleIncomplete(repos core.RepositoryStore) http.HandlerFunc {
	return notImplemented
}

// HandleRunningStatus 返回查询运行中构建状态的 HTTP 处理器；OSS 版未实现。
func HandleRunningStatus(repos core.RepositoryStore) http.HandlerFunc {
	return notImplemented
}
