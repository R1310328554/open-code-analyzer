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

// perm 包提供权限记录的 SQL 扫描与参数转换辅助函数。
package perm

import (
	"database/sql"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// toParams 将 Perm 结构体转换为命名查询参数字典。
func toParams(perm *core.Perm) map[string]interface{} {
	return map[string]interface{}{
		"perm_user_id":  perm.UserID,
		"perm_repo_uid": perm.RepoUID,
		"perm_read":     perm.Read,
		"perm_write":    perm.Write,
		"perm_admin":    perm.Admin,
		"perm_synced":   perm.Synced,
		"perm_created":  perm.Created,
		"perm_updated":  perm.Updated,
	}
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
func scanRow(scanner db.Scanner, dst *core.Perm) error {
	return scanner.Scan(
		&dst.UserID,
		&dst.RepoUID,
		&dst.Read,
		&dst.Write,
		&dst.Admin,
		&dst.Synced,
		&dst.Created,
		&dst.Updated,
	)
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
// scanCollabRow 扫描协作者（含登录名与头像）权限行。
func scanCollabRow(scanner db.Scanner, dst *core.Collaborator) error {
	return scanner.Scan(
		&dst.UserID,
		&dst.RepoUID,
		&dst.Login,
		&dst.Avatar,
		&dst.Read,
		&dst.Write,
		&dst.Admin,
		&dst.Synced,
		&dst.Created,
		&dst.Updated,
	)
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
// scanCollabRows 批量扫描协作者权限记录。
func scanCollabRows(rows *sql.Rows) ([]*core.Collaborator, error) {
	defer rows.Close()

	collabs := []*core.Collaborator{}
	for rows.Next() {
		collab := new(core.Collaborator)
		err := scanCollabRow(rows, collab)
		if err != nil {
			return nil, err
		}
		collabs = append(collabs, collab)
	}
	return collabs, nil
}
