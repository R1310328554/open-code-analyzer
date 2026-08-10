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

package queue

import (
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
)

// notImplemented 是 OSS 版队列 API 的统一占位处理器。
var notImplemented = func(w http.ResponseWriter, r *http.Request) {
	render.NotImplemented(w, render.ErrNotImplemented)
}

// HandleItems 返回列出队列项的 HTTP 处理器；OSS 版未实现。
func HandleItems(store core.StageStore) http.HandlerFunc {
	return notImplemented
}

// HandlePause 返回暂停调度器的 HTTP 处理器；OSS 版未实现。
func HandlePause(core.Scheduler) http.HandlerFunc {
	return notImplemented
}

// HandleResume 返回恢复调度器的 HTTP 处理器；OSS 版未实现。
func HandleResume(core.Scheduler) http.HandlerFunc {
	return notImplemented
}
