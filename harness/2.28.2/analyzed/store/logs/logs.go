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

// logs 包提供基于数据库的构建步骤日志存储实现。
package logs

import (
	"bytes"
	"context"
	"io"
	"io/ioutil"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// New 创建将日志存入 logs 表的 core.LogStore 实现。
func New(db *db.DB) core.LogStore {
	return &logStore{db}
}

// logStore 以步骤 ID 为键在 logs 表读写 JSONL 日志数据。
type logStore struct {
	db *db.DB
}

// Find 按步骤 ID 读取日志字节并包装为 ReadCloser。
func (s *logStore) Find(ctx context.Context, step int64) (io.ReadCloser, error) {
	out := &logs{ID: step}
	err := s.db.View(func(queryer db.Queryer, binder db.Binder) error {
		query, args, err := binder.BindNamed(queryKey, out)
		if err != nil {
			return err
		}
		row := queryer.QueryRow(query, args...)
		return scanRow(row, out)
	})
	return ioutil.NopCloser(
		bytes.NewBuffer(out.Data),
	), err
}

// Create 读取请求体并插入新日志记录。
func (s *logStore) Create(ctx context.Context, step int64, r io.Reader) error {
	data, err := ioutil.ReadAll(r)
	if err != nil {
		return err
	}
	return s.db.Lock(func(execer db.Execer, binder db.Binder) error {
		params := &logs{
			ID:   step,
			Data: data,
		}
		stmt, args, err := binder.BindNamed(stmtInsert, params)
		if err != nil {
			return err
		}
		_, err = execer.Exec(stmt, args...)
		return err
	})
}

// Update 覆盖指定步骤的 log_data 字段。
func (s *logStore) Update(ctx context.Context, step int64, r io.Reader) error {
	data, err := ioutil.ReadAll(r)
	if err != nil {
		return err
	}
	return s.db.Lock(func(execer db.Execer, binder db.Binder) error {
		params := &logs{
			ID:   step,
			Data: data,
		}
		stmt, args, err := binder.BindNamed(stmtUpdate, params)
		if err != nil {
			return err
		}
		_, err = execer.Exec(stmt, args...)
		return err
	})
}

// Delete 按步骤 ID 删除日志行。
func (s *logStore) Delete(ctx context.Context, step int64) error {
	return s.db.Lock(func(execer db.Execer, binder db.Binder) error {
		params := &logs{
			ID: step,
		}
		stmt, args, err := binder.BindNamed(stmtDelete, params)
		if err != nil {
			return err
		}
		_, err = execer.Exec(stmt, args...)
		return err
	})
}

// logs 对应 logs 表一行记录。
type logs struct {
	ID   int64  `db:"log_id"`
	Data []byte `db:"log_data"`
}

const queryKey = `
SELECT
 log_id
,log_data
FROM logs
WHERE log_id = :log_id
`

const stmtInsert = `
INSERT INTO logs (
 log_id
,log_data
) VALUES (
 :log_id
,:log_data
)
`

const stmtUpdate = `
UPDATE logs
SET log_data = :log_data
WHERE log_id = :log_id
`

const stmtDelete = `
DELETE FROM logs
WHERE log_id = :log_id
`
