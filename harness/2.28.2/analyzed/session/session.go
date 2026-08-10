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

// session 包提供基于 Cookie 与 Bearer Token 的用户会话管理。
package session

import (
	"net/http"
	"strings"
	"time"

	"github.com/drone/drone/core"

	"github.com/dchest/authcookie"
)

// New 创建 Cookie 会话管理器。
func New(users core.UserStore, config Config) core.Session {
	return &session{
		secret:  []byte(config.Secret),
		secure:  config.Secure,
		timeout: config.Timeout,
		users:   users,
	}
}

// session 实现 core.Session，支持 Cookie 与 Authorization 两种鉴权方式。
type session struct {
	users   core.UserStore
	secret  []byte
	secure  bool
	timeout time.Duration

	administrator string // 管理员账号
	prometheus    string // Prometheus 账号
	autoscaler    string // Autoscaler 账号
}

// Create 为已登录用户签发 _session_ Cookie。
func (s *session) Create(w http.ResponseWriter, user *core.User) error {
	cookie := &http.Cookie{
		Name:     "_session_",
		Path:     "/",
		MaxAge:   2147483647,
		HttpOnly: true,
		Secure:   s.secure,
		Value: authcookie.NewSinceNow(
			user.Login,
			s.timeout,
			s.secret,
		),
	}
	w.Header().Add("Set-Cookie", cookie.String()+"; SameSite=lax")
	return nil
}

// Delete 清除客户端 _session_ Cookie。
func (s *session) Delete(w http.ResponseWriter) error {
	w.Header().Add("Set-Cookie", "_session_=deleted; Path=/; Max-Age=0")
	return nil
}

// Get 从请求中解析当前用户：优先 Bearer/Query Token，否则读取 Cookie。
func (s *session) Get(r *http.Request) (*core.User, error) {
	switch {
	case isAuthorizationToken(r):
		return s.fromToken(r)
	case isAuthorizationParameter(r):
		return s.fromToken(r)
	default:
		return s.fromSession(r)
	}
}

// fromSession 从 _session_ Cookie 解析登录名并加载用户。
func (s *session) fromSession(r *http.Request) (*core.User, error) {
	cookie, err := r.Cookie("_session_")
	if err != nil {
		return nil, nil
	}
	login := authcookie.Login(cookie.Value, s.secret)
	if login == "" {
		return nil, nil
	}
	return s.users.FindLogin(r.Context(), login)
}

// fromToken 通过 access token 查找用户。
func (s *session) fromToken(r *http.Request) (*core.User, error) {
	return s.users.FindToken(r.Context(),
		extractToken(r),
	)
}

// isAuthorizationToken 判断请求头是否携带 Authorization。
func isAuthorizationToken(r *http.Request) bool {
	return r.Header.Get("Authorization") != ""
}

// isAuthorizationParameter 判断 query/form 是否携带 access_token。
func isAuthorizationParameter(r *http.Request) bool {
	return r.FormValue("access_token") != ""
}

// extractToken 从 Header 或表单参数提取 Bearer token。
func extractToken(r *http.Request) string {
	bearer := r.Header.Get("Authorization")
	if bearer == "" {
		bearer = r.FormValue("access_token")
	}
	return strings.TrimPrefix(bearer, "Bearer ")
}
