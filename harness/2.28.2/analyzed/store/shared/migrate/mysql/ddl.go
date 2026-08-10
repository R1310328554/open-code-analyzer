// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// mysql 包定义 MySQL 数据库迁移的 go:generate 入口（非 OSS 构建）。
package mysql

// 由 togo 从 SQL 模板生成 MySQL DDL（见 ddl_gen.go）。
//go:generate togo ddl -package mysql -dialect mysql
