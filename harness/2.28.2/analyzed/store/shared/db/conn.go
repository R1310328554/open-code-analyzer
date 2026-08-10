// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

// db 包提供非 OSS 构建的数据库连接、Ping 与迁移入口。
package db

import (
	"database/sql"
	"sync"
	"time"

	"github.com/jmoiron/sqlx"

	"github.com/drone/drone/store/shared/migrate/mysql"
	"github.com/drone/drone/store/shared/migrate/postgres"
	"github.com/drone/drone/store/shared/migrate/sqlite"
)

// Connect 连接数据库并通过 Ping 验证可用性，随后执行自动迁移。
func Connect(driver, datasource string, maxOpenConnections int) (*DB, error) {
	db, err := sql.Open(driver, datasource)
	if err != nil {
		return nil, err
	}
	switch driver {
	case "mysql":
		db.SetMaxIdleConns(0)
	}
	if err := pingDatabase(db); err != nil {
		return nil, err
	}
	if err := setupDatabase(db, driver); err != nil {
		return nil, err
	}
	// 通常为 0；大型部署可由用户配置最大连接数
	db.SetMaxOpenConns(maxOpenConnections)

	var engine Driver
	var locker Locker
	switch driver {
	case "mysql":
		engine = Mysql
		locker = &nopLocker{}
	case "postgres":
		engine = Postgres
		locker = &nopLocker{}
	default:
		engine = Sqlite
		locker = &sync.RWMutex{}
	}

	return &DB{
		conn:   sqlx.NewDb(db, driver),
		lock:   locker,
		driver: engine,
	}, nil
}

// pingDatabase 以退避重试方式 Ping 数据库，确保迁移前连接可用。
func pingDatabase(db *sql.DB) (err error) {
	for i := 0; i < 30; i++ {
		err = db.Ping()
		if err == nil {
			return
		}
		time.Sleep(time.Second)
	}
	return
}

// setupDatabase 根据驱动类型执行自动数据库迁移。
func setupDatabase(db *sql.DB, driver string) error {
	switch driver {
	case "mysql":
		return mysql.Migrate(db)
	case "postgres":
		return postgres.Migrate(db)
	default:
		return sqlite.Migrate(db)
	}
}
