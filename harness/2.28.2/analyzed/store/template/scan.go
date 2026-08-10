// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// template 包扫描辅助函数，用于 templates 表行与 core.Template 映射。
package template

import (
	"database/sql"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// toParams 将 core.Template 转为命名查询参数字典。
func toParams(template *core.Template) (map[string]interface{}, error) {
	return map[string]interface{}{
		"template_id":        template.Id,
		"template_name":      template.Name,
		"template_namespace": template.Namespace,
		"template_data":      template.Data,
		"template_created":   template.Created,
		"template_updated":   template.Updated,
	}, nil
}

// scanRow 从 sql.Row 扫描列值写入 core.Template。
func scanRow(scanner db.Scanner, dst *core.Template) error {
	err := scanner.Scan(
		&dst.Id,
		&dst.Name,
		&dst.Namespace,
		&dst.Data,
		&dst.Created,
		&dst.Updated,
	)
	if err != nil {
		return err
	}
	return nil
}

// scanRows 遍历 sql.Rows 批量扫描为 []*core.Template 切片。
func scanRows(rows *sql.Rows) ([]*core.Template, error) {
	defer rows.Close()

	template := []*core.Template{}
	for rows.Next() {
		tem := new(core.Template)
		err := scanRow(rows, tem)
		if err != nil {
			return nil, err
		}
		template = append(template, tem)
	}
	return template, nil
}
