// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// cron 包扫描辅助函数，用于 cron 表行与 core.Cron 映射。
package cron

import (
	"database/sql"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// toParams 将 core.Cron 转为命名查询参数字典。
func toParams(cron *core.Cron) map[string]interface{} {
	return map[string]interface{}{
		"cron_id":       cron.ID,
		"cron_repo_id":  cron.RepoID,
		"cron_name":     cron.Name,
		"cron_expr":     cron.Expr,
		"cron_next":     cron.Next,
		"cron_prev":     cron.Prev,
		"cron_event":    cron.Event,
		"cron_branch":   cron.Branch,
		"cron_target":   cron.Target,
		"cron_disabled": cron.Disabled,
		"cron_created":  cron.Created,
		"cron_updated":  cron.Updated,
		"cron_version":  cron.Version,
	}
}

// scanRow 从 sql.Row 扫描列值写入 core.Cron。
func scanRow(scanner db.Scanner, dst *core.Cron) error {
	return scanner.Scan(
		&dst.ID,
		&dst.RepoID,
		&dst.Name,
		&dst.Expr,
		&dst.Next,
		&dst.Prev,
		&dst.Event,
		&dst.Branch,
		&dst.Target,
		&dst.Disabled,
		&dst.Created,
		&dst.Updated,
		&dst.Version,
	)
}

// scanRows 遍历 sql.Rows 批量扫描为 []*core.Cron 切片。
func scanRows(rows *sql.Rows) ([]*core.Cron, error) {
	defer rows.Close()

	crons := []*core.Cron{}
	for rows.Next() {
		cron := new(core.Cron)
		err := scanRow(rows, cron)
		if err != nil {
			return nil, err
		}
		crons = append(crons, cron)
	}
	return crons, nil
}
