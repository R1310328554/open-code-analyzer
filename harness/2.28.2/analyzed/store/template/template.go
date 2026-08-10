// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// template 包为非 OSS 构建提供流水线模板（Template）的数据库存储实现。
package template

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// New 创建 core.TemplateStore 数据库实现。
func New(db *db.DB) core.TemplateStore {
	return &templateStore{
		db: db,
	}
}

// templateStore 封装 templates 表的 CRUD。
type templateStore struct {
	db *db.DB
}

// List 返回指定命名空间下的全部模板，按名称排序。
func (s *templateStore) List(ctx context.Context, namespace string) ([]*core.Template, error) {
	var out []*core.Template
	err := s.db.View(func(queryer db.Queryer, binder db.Binder) error {
		params := map[string]interface{}{"template_namespace": namespace}
		stmt, args, err := binder.BindNamed(queryNamespace, params)
		if err != nil {
			return err
		}
		rows, err := queryer.Query(stmt, args...)
		if err != nil {
			return err
		}
		out, err = scanRows(rows)
		return err
	})
	return out, err
}

// ListAll 返回系统中全部模板。
func (s *templateStore) ListAll(ctx context.Context) ([]*core.Template, error) {
	var out []*core.Template
	err := s.db.View(func(queryer db.Queryer, binder db.Binder) error {
		params := map[string]interface{}{}
		stmt, args, err := binder.BindNamed(queryAll, params)
		if err != nil {
			return err
		}
		rows, err := queryer.Query(stmt, args...)
		if err != nil {
			return err
		}
		out, err = scanRows(rows)
		return err
	})
	return out, err
}

// Find 按主键 ID 查询单条模板。
func (s *templateStore) Find(ctx context.Context, id int64) (*core.Template, error) {
	out := &core.Template{Id: id}
	err := s.db.View(func(queryer db.Queryer, binder db.Binder) error {
		params, err := toParams(out)
		if err != nil {
			return err
		}
		query, args, err := binder.BindNamed(queryKey, params)
		if err != nil {
			return err
		}
		row := queryer.QueryRow(query, args...)
		return scanRow(row, out)
	})
	return out, err
}

// FindName 在指定命名空间内按模板名称查询。
func (s *templateStore) FindName(ctx context.Context, name string, namespace string) (*core.Template, error) {
	out := &core.Template{Name: name, Namespace: namespace}
	err := s.db.View(func(queryer db.Queryer, binder db.Binder) error {
		params, err := toParams(out)
		if err != nil {
			return err
		}

		query, args, err := binder.BindNamed(queryName, params)

		if err != nil {
			return err
		}
		row := queryer.QueryRow(query, args...)
		return scanRow(row, out)
	})
	return out, err
}

// Create 插入新模板；Postgres 使用 RETURNING 获取 ID。
func (s *templateStore) Create(ctx context.Context, template *core.Template) error {
	if s.db.Driver() == db.Postgres {
		return s.createPostgres(ctx, template)
	}
	return s.create(ctx, template)
}

// create SQLite/MySQL 插入路径，通过 LastInsertId 获取自增 ID。
func (s *templateStore) create(ctx context.Context, template *core.Template) error {
	return s.db.Lock(func(execer db.Execer, binder db.Binder) error {
		params, err := toParams(template)
		if err != nil {
			return err
		}
		stmt, args, err := binder.BindNamed(stmtInsert, params)
		if err != nil {
			return err
		}
		res, err := execer.Exec(stmt, args...)
		if err != nil {
			return err
		}
		template.Id, err = res.LastInsertId()
		return err
	})
}

// createPostgres 使用 RETURNING template_id 获取新记录 ID。
func (s *templateStore) createPostgres(ctx context.Context, template *core.Template) error {
	return s.db.Lock(func(execer db.Execer, binder db.Binder) error {
		params, err := toParams(template)
		if err != nil {
			return err
		}
		stmt, args, err := binder.BindNamed(stmtInsertPostgres, params)
		if err != nil {
			return err
		}
		return execer.QueryRow(stmt, args...).Scan(&template.Id)
	})
}

// Update 更新已有模板的名称、命名空间与数据。
func (s *templateStore) Update(ctx context.Context, template *core.Template) error {
	return s.db.Lock(func(execer db.Execer, binder db.Binder) error {
		params, err := toParams(template)
		if err != nil {
			return err
		}
		stmt, args, err := binder.BindNamed(stmtUpdate, params)
		if err != nil {
			return err
		}
		_, err = execer.Exec(stmt, args...)
		return err
	})
}

// Delete 按主键删除模板。
func (s *templateStore) Delete(ctx context.Context, template *core.Template) error {
	return s.db.Lock(func(execer db.Execer, binder db.Binder) error {
		params, err := toParams(template)
		if err != nil {
			return err
		}
		stmt, args, err := binder.BindNamed(stmtDelete, params)
		if err != nil {
			return err
		}
		_, err = execer.Exec(stmt, args...)
		return err
	})
}

const queryKey = queryBase + `
FROM templates
WHERE template_id = :template_id
LIMIT 1
`

const queryBase = `
SELECT
 template_id
,template_name
,template_namespace
,template_data
,template_created
,template_updated
`

const queryAll = queryBase + `
FROM templates
ORDER BY template_name
`

const queryNamespace = queryBase + `
FROM templates
WHERE template_namespace = :template_namespace
ORDER BY template_name
`

const stmtInsert = `
INSERT INTO templates (
 template_name
,template_namespace
,template_data
,template_created
,template_updated
) VALUES (
 :template_name
,:template_namespace
,:template_data
,:template_created
,:template_updated
)
`

const stmtUpdate = `
UPDATE templates SET
template_name = :template_name
,template_namespace = :template_namespace
,template_data = :template_data
,template_updated = :template_updated
WHERE template_id = :template_id
`

const stmtDelete = `
DELETE FROM templates
WHERE template_id = :template_id
`
const queryName = queryBase + `
FROM templates
WHERE template_name = :template_name
AND template_namespace = :template_namespace
LIMIT 1
`

const stmtInsertPostgres = stmtInsert + `
RETURNING template_id
`
