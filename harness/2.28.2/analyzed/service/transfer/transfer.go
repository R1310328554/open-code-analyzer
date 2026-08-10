// Copyright 2020 Drone IO, Inc.
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

// transfer 包在用户账号停用时转移其名下仓库所有权。
package transfer

import (
	"context"
	"runtime/debug"

	"github.com/drone/drone/core"

	"github.com/hashicorp/go-multierror"
	"github.com/sirupsen/logrus"
)

// Transferer 将停用用户拥有的仓库转交给具备管理员权限的其他成员。
type Transferer struct {
	Repos core.RepositoryStore
	Perms core.PermStore
}

// New 创建仓库转移服务。
func New(repos core.RepositoryStore, perms core.PermStore) core.Transferer {
	return &Transferer{
		Repos: repos,
		Perms: perms,
	}
}

// Transfer 遍历用户仓库，将 owner 重新指派给同仓库的其他 admin 成员。
func (t *Transferer) Transfer(ctx context.Context, user *core.User) error {
	defer func() {
		// 防御性 recover，避免 panic 中断批量转移流程。
		if r := recover(); r != nil {
			logrus.Errorf("transferer: unexpected panic: %s", r)
			debug.PrintStack()
		}
	}()

	repos, err := t.Repos.List(ctx, user.ID)
	if err != nil {
		return err
	}

	var result error
	for _, repo := range repos {
		// 仅转移停用用户本人拥有的仓库。
		if repo.UserID != user.ID {
			continue
		}

		members, err := t.Perms.List(ctx, repo.UID)
		if err != nil {
			result = multierror.Append(result, err)
			continue
		}

		var admin int64
		for _, member := range members {
			// 选择非停用用户的管理员作为新 owner。
			if repo.UserID == member.UserID {
				continue
			}
			if member.Admin {
				admin = member.UserID
				break
			}
		}

		if admin == 0 {
			logrus.
				WithField("repo.id", repo.ID).
				WithField("repo.namespace", repo.Namespace).
				WithField("repo.name", repo.Name).
				Traceln("repository disabled")
		} else {
			logrus.
				WithField("repo.id", repo.ID).
				WithField("repo.namespace", repo.Namespace).
				WithField("repo.name", repo.Name).
				WithField("old.user.id", repo.UserID).
				WithField("new.user.id", admin).
				Traceln("repository owner re-assigned")
		}

		// 未找到替代管理员时将 UserID 置零，表示仓库无 owner。
		repo.UserID = admin
		err = t.Repos.Update(ctx, repo)
		if err != nil {
			result = multierror.Append(result, err)
		}
	}

	return result
}
