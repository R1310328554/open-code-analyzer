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

// user 包提供当前登录用户相关的 REST API 处理器。
package user

import (
	"net/http"

	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/handler/api/request"
)

// HandleFind 返回 HTTP 处理器，将请求上下文中当前登录用户的账户信息以 JSON 写入响应。
func HandleFind() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()
		viewer, _ := request.UserFrom(ctx)
		render.JSON(w, viewer, 200)
	}
}
