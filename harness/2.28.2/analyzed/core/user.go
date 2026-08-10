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

package core

import (
	"context"
	"errors"

	"github.com/asaskevich/govalidator"
)

var (
	errUsernameLen  = errors.New("Invalid username length")
	errUsernameChar = errors.New("Invalid character in username")
)

type (
	// User 表示系统用户账户。
	User struct {
		ID        int64  `json:"id"`
		Login     string `json:"login"`
		Email     string `json:"email"`
		Machine   bool   `json:"machine"`    // 是否为机器账户
		Admin     bool   `json:"admin"`      // 是否为系统管理员
		Active    bool   `json:"active"`     // 账户是否已激活
		Avatar    string `json:"avatar"`
		Syncing   bool   `json:"syncing"`    // 是否正在同步仓库
		Synced    int64  `json:"synced"`     // 上次同步完成时间戳
		Created   int64  `json:"created"`
		Updated   int64  `json:"updated"`
		LastLogin int64  `json:"last_login"`
		Token     string `json:"-"` // API 访问令牌（不对外暴露）
		Refresh   string `json:"-"` // OAuth 刷新令牌
		Expiry    int64  `json:"-"` // 令牌过期时间
		Hash      string `json:"-"` // 令牌哈希值
	}

	// UserParams 定义用户列表查询参数。
	UserParams struct {
		// Sort 为 true 时按 Login 排序，否则按主键排序。
		Sort bool

		Page int64 // 页码
		Size int64 // 每页条数
	}

	// UserStore 定义用户账户的持久化与查询操作。
	UserStore interface {
		// Find 按主键从数据存储查找用户。
		Find(context.Context, int64) (*User, error)

		// FindLogin 按用户名从数据存储查找用户。
		FindLogin(context.Context, string) (*User, error)

		// FindToken 按 API 令牌从数据存储查找用户。
		FindToken(context.Context, string) (*User, error)

		// List 返回数据存储中的全部用户列表。
		List(context.Context) ([]*User, error)

		// ListRange 按分页参数返回用户列表。
		ListRange(context.Context, UserParams) ([]*User, error)

		// Create 将新用户持久化到数据存储。
		Create(context.Context, *User) error

		// Update 将更新后的用户持久化到数据存储。
		Update(context.Context, *User) error

		// Delete 从数据存储删除用户。
		Delete(context.Context, *User) error

		// Count 返回人类用户与机器用户的总数。
		Count(context.Context) (int64, error)

		// CountHuman 返回人类用户数量。
		CountHuman(context.Context) (int64, error)
	}

	// UserService 提供对远程系统（如 GitHub）用户账户资源的访问。
	UserService interface {
		// Find 返回当前已认证用户的信息。
		Find(ctx context.Context, access, refresh string) (*User, error)

		// FindLogin 按用户名查找远程用户。
		FindLogin(ctx context.Context, user *User, login string) (*User, error)
	}
)

// Validate 校验用户名字段长度与字符规则，失败时返回错误。
func (u *User) Validate() error {
	switch {
	case !govalidator.IsByteLength(u.Login, 1, 50):
		return errUsernameLen
	case !govalidator.Matches(u.Login, "^[.a-zA-Z0-9_-]+$"):
		return errUsernameChar
	default:
		return nil
	}
}
