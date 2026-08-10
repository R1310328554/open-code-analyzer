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

// web 包中的登录处理器负责 OAuth 认证、用户准入与会话创建。
package web

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"net/http"
	"time"

	"github.com/drone/drone/core"
	"github.com/drone/drone/logger"
	"github.com/drone/go-login/login"

	"github.com/dchest/uniuri"
	"github.com/sirupsen/logrus"
)

// syncPeriod 定义与远程系统同步用户账户的周期，默认为每周一次。
var syncPeriod = time.Hour * 24 * 7

// syncTimeout 定义单次同步操作的最大超时时间。
var syncTimeout = time.Minute * 30

// HandleLogin 创建 HTTP 处理器，完成用户认证、账户创建/更新及会话初始化。
func HandleLogin(
	users core.UserStore,
	userz core.UserService,
	syncer core.Syncer,
	session core.Session,
	admission core.AdmissionService,
	sender core.WebhookSender,
) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		ctx := r.Context()
		err := login.ErrorFrom(ctx)
		if err != nil {
			writeLoginError(w, r, err)
			logrus.Debugf("cannot authenticate user: %s", err)
			return
		}

		// 授权令牌由登录中间件写入请求上下文。
		tok := login.TokenFrom(ctx)

		account, err := userz.Find(ctx, tok.Access, tok.Refresh)
		if err != nil {
			writeLoginError(w, r, err)
			logrus.Debugf("cannot find remote user: %s", err)
			return
		}

		logger := logrus.WithField("login", account.Login)
		logger.Debugf("attempting authentication")

		redirect := "/"
		user, err := users.FindLogin(ctx, account.Login)
		if err == sql.ErrNoRows {
			redirect = "/register"

			user = &core.User{
				Login:     account.Login,
				Avatar:    account.Avatar,
				Admin:     false,
				Machine:   false,
				Active:    true,
				Syncing:   true,
				Synced:    0,
				LastLogin: time.Now().Unix(),
				Created:   time.Now().Unix(),
				Updated:   time.Now().Unix(),
				Token:     tok.Access,
				Refresh:   tok.Refresh,
				Hash:      uniuri.NewLen(32),
			}
			if !tok.Expires.IsZero() {
				user.Expiry = tok.Expires.Unix()
			}

			err = admission.Admit(ctx, user)
			if err != nil {
				writeLoginError(w, r, err)
				logger.Errorf("cannot admit user: %s", err)
				return
			}

			err = users.Create(ctx, user)
			if err != nil {
				writeLoginError(w, r, err)
				logger.Errorf("cannot create user: %s", err)
				return
			}

			err = sender.Send(ctx, &core.WebhookData{
				Event:  core.WebhookEventUser,
				Action: core.WebhookActionCreated,
				User:   user,
			})
			if err != nil {
				logger.Errorf("cannot send webhook: %s", err)
			} else {
				logger.Debugf("successfully created user")
			}
		} else if err != nil {
			writeLoginError(w, r, err)
			logger.Errorf("cannot find user: %s", err)
			return
		} else {
			err = admission.Admit(ctx, user)
			if err != nil {
				writeLoginError(w, r, err)
				logger.Errorf("cannot admit user: %s", err)
				return
			}
		}

		if user.Machine {
			writeLoginErrorStr(w, r, "Machine account login is forbidden")
			return
		}

		if user.Active == false {
			writeLoginErrorStr(w, r, "Account is not active")
			return
		}

		user.Avatar = account.Avatar
		user.Token = tok.Access
		user.Refresh = tok.Refresh
		user.LastLogin = time.Now().Unix()
		if !tok.Expires.IsZero() {
			user.Expiry = tok.Expires.Unix()
		}

		// 若账户从未同步或已超过同步周期，则标记为待同步。
		if time.Unix(user.Synced, 0).Add(syncPeriod).Before(time.Now()) {
			user.Syncing = true
		}

		err = users.Update(ctx, user)
		if err != nil {
			// 账户更新失败仍继续创建会话，视为非致命错误。
			logger.Errorf("cannot update user: %s", err)
		}

		// 在 goroutine 中启动同步，避免阻塞登录流程。
		if user.Syncing {
			go synchronize(ctx, syncer, user)
		}

		// 未完成注册（无邮箱）的用户重定向到注册页。
		if len(user.Email) == 0 && user.Created > 1619841600 {
			redirect = "/register"
		}

		logger.Debugf("authentication successful")

		session.Create(w, user)
		http.Redirect(w, r, redirect, http.StatusSeeOther)
	}
}

// synchronize 在后台执行用户与远程 SCM 的数据同步。
func synchronize(ctx context.Context, syncer core.Syncer, user *core.User) {
	log := logrus.WithField("login", user.Login)
	log.Debugf("begin synchronization")

	timeout, cancel := context.WithTimeout(context.Background(), syncTimeout)
	timeout = logger.WithContext(timeout, log)
	defer cancel()
	_, err := syncer.Sync(timeout, user)
	if err != nil {
		log.Debugf("synchronization failed: %s", err)
	} else {
		log.Debugf("synchronization success")
	}
}

// writeLoginError 将登录错误重定向到错误展示页。
func writeLoginError(w http.ResponseWriter, r *http.Request, err error) {
	http.Redirect(w, r, "/login/error?message="+err.Error(), http.StatusSeeOther)
}

// writeLoginErrorStr 将字符串形式的登录错误写入错误页。
func writeLoginErrorStr(w http.ResponseWriter, r *http.Request, s string) {
	writeLoginError(w, r, errors.New(s))
}

// writeCookie 设置 Cookie 并附加 SameSite=lax 属性。
func writeCookie(w http.ResponseWriter, cookie *http.Cookie) {
	w.Header().Set("Set-Cookie", cookie.String()+"; SameSite=lax")
}

// HandleLoginForm 返回展示用户名/密码登录表单的 HTTP 处理器。
func HandleLoginForm() http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/html")
		fmt.Fprint(w, loginForm)
	}
}

// loginForm 为收集凭据的简单 HTML 登录表单。
var loginForm = `
<form method="POST" action="/login">
<input type="text" name="username" />
<input type="password" name="password" />
<input type="submit" />
</form>
`
