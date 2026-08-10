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

package acl

import (
	"net/http"

	"github.com/drone/drone/handler/api/errors"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/handler/api/request"
	"github.com/drone/drone/logger"
)

// AuthorizeUser 返回 HTTP 中间件，仅允许已认证用户继续处理请求链。
// 访客用户将被拒绝并返回 401 未授权错误。
func AuthorizeUser(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_, ok := request.UserFrom(r.Context())
		if !ok {
			render.Unauthorized(w, errors.ErrUnauthorized)
			logger.FromRequest(r).
				Debugln("api: authentication required")
		} else {
			next.ServeHTTP(w, r)
		}
	})
}

// AuthorizeAdmin 返回 HTTP 中间件，仅允许系统管理员继续处理请求链。
func AuthorizeAdmin(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		user, ok := request.UserFrom(r.Context())
		if !ok {
			render.Unauthorized(w, errors.ErrUnauthorized)
			logger.FromRequest(r).
				Debugln("api: authentication required")
		} else if !user.Admin {
			render.Forbidden(w, errors.ErrForbidden)
			logger.FromRequest(r).
				Debugln("api: administrative access required")
		} else {
			next.ServeHTTP(w, r)
		}
	})
}
