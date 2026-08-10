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

// db 包封装 sqlx 连接池，提供只读 View、写锁 Lock 与事务 Update。
package db

import (
	"database/sql"
	"runtime/debug"

	"github.com/jmoiron/sqlx"
)

// Driver 表示数据库驱动类型。
type Driver int

// 数据库驱动枚举常量。
const (
	Sqlite = iota + 1
	Mysql
	Postgres
)

type (
	// A Scanner represents an object that can be scanned
	// for values.
	// Scanner 表示可从 SQL 结果集 Scan 值的对象。
	Scanner interface {
		Scan(dest ...interface{}) error
	}

	// A Locker represents an object that can be locked and unlocked.
	// Locker 表示可加读/写锁的对象（SQLite 并发控制）。
	Locker interface {
		Lock()
		Unlock()
		RLock()
		RUnlock()
	}

	// Binder interface defines database field bindings.
	// Binder 定义命名参数绑定接口。
	Binder interface {
		BindNamed(query string, arg interface{}) (string, []interface{}, error)
	}

	// Queryer interface defines a set of methods for
	// querying the database.
	// Queryer 定义数据库只读查询方法集。
	Queryer interface {
		Query(query string, args ...interface{}) (*sql.Rows, error)
		QueryRow(query string, args ...interface{}) *sql.Row
	}

	// Execer interface defines a set of methods for executing
	// read and write commands against the database.
	// Execer 定义数据库读写执行方法集。
	Execer interface {
		Queryer
		Exec(query string, args ...interface{}) (sql.Result, error)
	}

	// DB is a pool of zero or more underlying connections to
	// the drone database.
	// DB 封装 Drone 数据库连接池及驱动类型。
	DB struct {
		conn   *sqlx.DB
		lock   Locker
		driver Driver
	}
)

// View executes a function within the context of a managed read-only
// transaction. Any error that is returned from the function is returned
// from the View() method.
// View 在共享读锁下执行只读查询回调。
func (db *DB) View(fn func(Queryer, Binder) error) error {
	db.lock.RLock()
	err := fn(db.conn, db.conn)
	db.lock.RUnlock()
	return err
}

// Lock obtains a write lock to the database (sqlite only) and executes
// a function. Any error that is returned from the function is returned
// from the Lock() method.
// Lock 获取写锁（SQLite）后执行写操作回调。
func (db *DB) Lock(fn func(Execer, Binder) error) error {
	db.lock.Lock()
	err := fn(db.conn, db.conn)
	db.lock.Unlock()
	return err
}

// Update executes a function within the context of a read-write managed
// transaction. If no error is returned from the function then the
// transaction is committed. If an error is returned then the entire
// transaction is rolled back. Any error that is returned from the function
// or returned from the commit is returned from the Update() method.
// Update 在托管读写事务中执行回调；出错则回滚。
func (db *DB) Update(fn func(Execer, Binder) error) (err error) {
	db.lock.Lock()
	defer db.lock.Unlock()

	tx, err := db.conn.Begin()
	if err != nil {
		return err
	}

	defer func() {
		if p := recover(); p != nil {
			err = tx.Rollback()
			debug.PrintStack()
		} else if err != nil {
			tx.Rollback()
		} else {
			err = tx.Commit()
		}
	}()

	err = fn(tx, db.conn)
	return err
}

// Driver returns the name of the SQL driver.
// Driver 返回当前 SQL 驱动类型枚举。
func (db *DB) Driver() Driver {
	return db.driver
}

// Close closes the database connection.
// Close 关闭底层数据库连接。
func (db *DB) Close() error {
	return db.conn.Close()
}
