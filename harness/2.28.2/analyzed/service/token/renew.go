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

// token 包负责 OAuth 访问令牌的自动刷新与持久化。
package token

import (
	"context"
	"time"

	"github.com/drone/drone/core"

	"github.com/drone/go-scm/scm"
	"github.com/drone/go-scm/scm/transport/oauth2"
)

// expiryDelta 定义令牌提前视为过期的缓冲时间，缓解客户端与服务端时钟偏差。
const expiryDelta = time.Minute

// renewer 实现 core.Renewer，在令牌过期时调用 OAuth2 刷新并写回用户存储。
type renewer struct {
	refresh *oauth2.Refresher
	users   core.UserStore
}

// Renewer 创建 Renewer 实例。
func Renewer(refresh *oauth2.Refresher, store core.UserStore) core.Renewer {
	return &renewer{
		refresh: refresh,
		users:   store,
	}
}

// Renew 在令牌过期或 force 为 true 时刷新并更新用户记录。
func (r *renewer) Renew(ctx context.Context, user *core.User, force bool) error {
	if r.refresh == nil {
		return nil
	}
	t := &scm.Token{
		Token:   user.Token,
		Refresh: user.Refresh,
		Expires: time.Unix(user.Expiry, 0),
	}
	if expired(t) == false && force == false {
		return nil
	}
	err := r.refresh.Refresh(t)
	if err != nil {
		return err
	}
	user.Token = t.Token
	user.Refresh = t.Refresh
	user.Expiry = t.Expires.Unix()
	return r.users.Update(ctx, user)
}

// expired 判断 OAuth 令牌是否已过期（含 expiryDelta 提前量）。
func expired(token *scm.Token) bool {
	if len(token.Refresh) == 0 {
		return false
	}
	if token.Expires.IsZero() && len(token.Token) != 0 {
		return false
	}
	return token.Expires.Add(-expiryDelta).
		Before(time.Now())
}
