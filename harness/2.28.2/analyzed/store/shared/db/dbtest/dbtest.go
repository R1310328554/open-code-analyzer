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

// dbtest 包为 store 层单元测试提供数据库连接与重置工具。
package dbtest

import (
	"os"
	"strconv"

	"github.com/drone/drone/store/shared/db"

	// blank imports are used to load database drivers
	// for unit tests. Only unit tests should be importing
	// this package.
	_ "github.com/go-sql-driver/mysql"
	_ "github.com/lib/pq"
	_ "github.com/mattn/go-sqlite3"
)

// Connect 打开测试用数据库连接（默认内存 SQLite）。
func Connect() (*db.DB, error) {
	var (
		driver         = "sqlite3"
		config         = ":memory:?_foreign_keys=1"
		maxConnections = 0
	)
	if os.Getenv("DRONE_DATABASE_DRIVER") != "" {
		driver = os.Getenv("DRONE_DATABASE_DRIVER")
		config = os.Getenv("DRONE_DATABASE_DATASOURCE")
		maxConnectionsString := os.Getenv("DRONE_DATABASE_MAX_CONNECTIONS")
		maxConnections, _ = strconv.Atoi(maxConnectionsString)
	}
	return db.Connect(driver, config, maxConnections)
}

// Reset 清空各业务表，重置测试数据库状态。
func Reset(d *db.DB) {
	d.Lock(func(tx db.Execer, _ db.Binder) error {
		tx.Exec("DELETE FROM cron")
		tx.Exec("DELETE FROM cards")
		tx.Exec("DELETE FROM logs")
		tx.Exec("DELETE FROM steps")
		tx.Exec("DELETE FROM stages")
		tx.Exec("DELETE FROM latest")
		tx.Exec("DELETE FROM builds")
		tx.Exec("DELETE FROM perms")
		tx.Exec("DELETE FROM repos")
		tx.Exec("DELETE FROM users")
		tx.Exec("DELETE FROM templates")
		tx.Exec("DELETE FROM orgsecrets")
		return nil
	})
}

// Disconnect 关闭数据库连接。
func Disconnect(d *db.DB) error {
	return d.Close()
}
