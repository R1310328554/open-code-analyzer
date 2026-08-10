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

// users 包提供管理员级别的用户账户 CRUD API 处理器。
package users

import (
	"context"
	"encoding/json"
	"net/http"

	"github.com/drone/drone/core"
	"github.com/drone/drone/handler/api/render"
	"github.com/drone/drone/logger"

	"github.com/go-chi/chi"
)

// userInput 是更新用户时请求体中可修改的字段，指针类型表示可选更新。
type userInput struct {
	Admin  *bool `json:"admin"`
	Active *bool `json:"active"`
}

// HandleUpdate 返回 HTTP 处理器，更新指定用户的管理员与激活状态。
func HandleUpdate(users core.UserStore, transferer core.Transferer) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		login := chi.URLParam(r, "user")

		in := new(userInput)
		err := json.NewDecoder(r.Body).Decode(in)
		if err != nil {
			render.BadRequest(w, err)
			logger.FromRequest(r).WithError(err).
				Debugln("api: cannot unmarshal request body")
			return
		}

		user, err := users.FindLogin(r.Context(), login)
		if err != nil {
			render.NotFound(w, err)
			logger.FromRequest(r).WithError(err).
				Debugln("api: cannot find user")
			return
		}

		if in.Admin != nil {
			user.Admin = *in.Admin
		}
		if in.Active != nil {
			user.Active = *in.Active
			// 停用用户时同时撤销管理员权限，避免仍通过 API 访问。
			if user.Active == false {
				user.Admin = false
			}
		}
		err = users.Update(r.Context(), user)
		if err != nil {
			render.InternalError(w, err)
			logger.FromRequest(r).WithError(err).
				Warnln("api: cannot update user")
		} else {
			render.JSON(w, user, 200)
		}

		if user.Active {
			return
		}

		// 用户被停用后转移其名下仓库所有权。
		err = transferer.Transfer(context.Background(), user)
		if err != nil {
			logger.FromRequest(r).WithError(err).
				Warnln("api: cannot transfer repository ownership")
		}
	}
}
