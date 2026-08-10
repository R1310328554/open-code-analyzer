// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 跨平台文件锁入口：Flock 创建/打开锁文件并委托平台 newLock 实现互斥。

package fileutil

import (
	"os"
	"path/filepath"
)

// Releaser 定义释放文件锁的 Release 方法，由 Flock 返回的具体类型实现。
// Releaser provides the Release method to release a file lock.
type Releaser interface {
	Release() error
}

// Flock 对 fileName 加独占锁（不存在则创建）；existed 表示锁文件是否已存在，非 goroutine-safe。
// Flock locks the file with the provided name. If the file does not exist, it is
// created. The returned Releaser is used to release the lock. existed is true
// if the file to lock already existed. A non-nil error is returned if the
// locking has failed. Neither this function nor the returned Releaser is
// goroutine-safe.
func Flock(fileName string) (r Releaser, existed bool, err error) {
	if err = os.MkdirAll(filepath.Dir(fileName), 0o755); err != nil {
		return nil, false, err
	}

	_, err = os.Stat(fileName)
	existed = err == nil

	r, err = newLock(fileName)
	return r, existed, err
}
