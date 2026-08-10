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

// user 包通过 SCM 客户端查询远程用户信息。
package user

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

// service 实现 core.UserService。
type service struct {
	client *scm.Client
	renew  core.Renewer
}

// New 创建 User 服务，提供对 SCM 用户资料的访问。
func New(client *scm.Client, renew core.Renewer) core.UserService {
	return &service{client: client, renew: renew}
}

// Find 使用 access/refresh token 查询当前 OAuth 用户。
func (s *service) Find(ctx context.Context, access, refresh string) (*core.User, error) {
	ctx = context.WithValue(ctx, scm.TokenKey{}, &scm.Token{
		Token:   access,
		Refresh: refresh,
	})
	src, _, err := s.client.Users.Find(ctx)
	if err != nil {
		return nil, err
	}
	return convert(src), nil
}

// FindLogin 按登录名查询 SCM 用户，调用前会刷新令牌。
func (s *service) FindLogin(ctx context.Context, user *core.User, login string) (*core.User, error) {
	err := s.renew.Renew(ctx, user, false)
	if err != nil {
		return nil, err
	}

	ctx = context.WithValue(ctx, scm.TokenKey{}, &scm.Token{
		Token:   user.Token,
		Refresh: user.Refresh,
	})
	src, _, err := s.client.Users.FindLogin(ctx, login)
	if err != nil {
		return nil, err
	}
	return convert(src), nil
}

// convert 将 scm.User 映射为 core.User。
func convert(src *scm.User) *core.User {
	dst := &core.User{
		Login:  src.Login,
		Email:  src.Email,
		Avatar: src.Avatar,
	}
	if !src.Created.IsZero() {
		dst.Created = src.Created.Unix()
	}
	if !src.Updated.IsZero() {
		dst.Updated = src.Updated.Unix()
	}
	return dst
}
