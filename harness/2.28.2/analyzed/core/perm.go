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

import "context"

type (
	// Perm 表示用户对仓库的读、写、管理权限记录。
	Perm struct {
		UserID  int64  `db:"perm_user_id"  json:"-"`
		RepoUID string `db:"perm_repo_uid" json:"-"`
		Read    bool   `db:"perm_read"     json:"read"`
		Write   bool   `db:"perm_write"    json:"write"`
		Admin   bool   `db:"perm_admin"    json:"admin"`
		Synced  int64  `db:"perm_synced"   json:"-"`
		Created int64  `db:"perm_created"  json:"-"`
		Updated int64  `db:"perm_updated"  json:"-"`
	}

	// Collaborator 表示项目协作者，包含账户信息与仓库权限详情。
	Collaborator struct {
		UserID  int64  `db:"perm_user_id"  json:"user_id"`
		RepoUID string `db:"perm_repo_uid" json:"repo_id"`
		Login   string `db:"user_login"    json:"login"`
		Avatar  string `db:"user_avatar"   json:"avatar"`
		Read    bool   `db:"perm_read"     json:"read"`
		Write   bool   `db:"perm_write"    json:"write"`
		Admin   bool   `db:"perm_admin"    json:"admin"`
		Synced  int64  `db:"perm_synced"   json:"synced"`
		Created int64  `db:"perm_created"  json:"created"`
		Updated int64  `db:"perm_updated"  json:"updated"`
	}

	// PermStore 定义仓库权限的持久化存储操作。
	PermStore interface {
		// Find 从数据存储中按仓库 UID 与用户 ID 查询权限记录。
		Find(ctx context.Context, repoUID string, userID int64) (*Perm, error)

		// List 从数据存储中返回指定仓库的全部协作者列表。
		List(ctx context.Context, repoUID string) ([]*Collaborator, error)

		// Update 将更新后的权限记录持久化到数据存储。
		Update(context.Context, *Perm) error

		// Delete 从数据存储中删除指定权限记录。
		Delete(context.Context, *Perm) error
	}
)
