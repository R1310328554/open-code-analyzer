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

// request 包在 context.Context 中存取当前请求的用户、权限与仓库信息。
package request

// https://github.com/kubernetes/apiserver/blob/master/pkg/endpoints/request/context.go

import (
	"context"

	"github.com/drone/drone/core"
)

// key 是 context 值的私有键类型，避免与其他包冲突。
type key int

const (
	userKey key = iota
	permKey
	repoKey
)

// WithUser 返回携带指定用户的新 context。
func WithUser(parent context.Context, user *core.User) context.Context {
	return context.WithValue(parent, userKey, user)
}

// UserFrom 从 context 读取当前用户；第二个返回值表示是否存在。
func UserFrom(ctx context.Context) (*core.User, bool) {
	user, ok := ctx.Value(userKey).(*core.User)
	return user, ok
}

// WithPerm 返回携带指定权限对象的新 context。
func WithPerm(parent context.Context, perm *core.Perm) context.Context {
	return context.WithValue(parent, permKey, perm)
}

// PermFrom 从 context 读取权限对象；第二个返回值表示是否存在。
func PermFrom(ctx context.Context) (*core.Perm, bool) {
	perm, ok := ctx.Value(permKey).(*core.Perm)
	return perm, ok
}

// WithRepo 返回携带指定仓库的新 context。
func WithRepo(parent context.Context, repo *core.Repository) context.Context {
	return context.WithValue(parent, repoKey, repo)
}

// RepoFrom 从 context 读取当前仓库；第二个返回值表示是否存在。
func RepoFrom(ctx context.Context) (*core.Repository, bool) {
	repo, ok := ctx.Value(repoKey).(*core.Repository)
	return repo, ok
}
