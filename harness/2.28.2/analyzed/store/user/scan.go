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

// user 包扫描辅助函数，用于 users 表行与 core.User 映射（含 OAuth 令牌加解密）。
package user

import (
	"database/sql"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
	"github.com/drone/drone/store/shared/encrypt"
)

// toParams 将 core.User 转为命名查询参数字典，并加密 OAuth 令牌字段。
func toParams(encrypt encrypt.Encrypter, u *core.User) (map[string]interface{}, error) {
	token, err := encrypt.Encrypt(u.Token)
	if err != nil {
		return nil, err
	}
	refresh, err := encrypt.Encrypt(u.Refresh)
	if err != nil {
		return nil, err
	}
	return map[string]interface{}{
		"user_id":            u.ID,
		"user_login":         u.Login,
		"user_email":         u.Email,
		"user_admin":         u.Admin,
		"user_machine":       u.Machine,
		"user_active":        u.Active,
		"user_avatar":        u.Avatar,
		"user_syncing":       u.Syncing,
		"user_synced":        u.Synced,
		"user_created":       u.Created,
		"user_updated":       u.Updated,
		"user_last_login":    u.LastLogin,
		"user_oauth_token":   token,
		"user_oauth_refresh": refresh,
		"user_oauth_expiry":  u.Expiry,
		"user_hash":          u.Hash,
	}, nil
}

// scanRow 从 sql.Row 扫描列值写入 core.User，并解密 OAuth 令牌。
func scanRow(encrypt encrypt.Encrypter, scanner db.Scanner, dest *core.User) error {
	var token, refresh []byte
	err := scanner.Scan(
		&dest.ID,
		&dest.Login,
		&dest.Email,
		&dest.Admin,
		&dest.Machine,
		&dest.Active,
		&dest.Avatar,
		&dest.Syncing,
		&dest.Synced,
		&dest.Created,
		&dest.Updated,
		&dest.LastLogin,
		&token,
		&refresh,
		&dest.Expiry,
		&dest.Hash,
	)
	if err != nil {
		return err
	}
	dest.Token, err = encrypt.Decrypt(token)
	if err != nil {
		return err
	}
	dest.Refresh, err = encrypt.Decrypt(refresh)
	if err != nil {
		return err
	}
	return nil
}

// scanRows 遍历 sql.Rows 批量扫描为 []*core.User 切片。
func scanRows(encrypt encrypt.Encrypter, rows *sql.Rows) ([]*core.User, error) {
	defer rows.Close()

	users := []*core.User{}
	for rows.Next() {
		user := new(core.User)
		err := scanRow(encrypt, rows, user)
		if err != nil {
			return nil, err
		}
		users = append(users, user)
	}
	return users, nil
}
