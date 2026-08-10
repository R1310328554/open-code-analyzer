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

// Solaris 平台文件锁：与 AIX 相同使用 fcntl FcntlFlock 设置写锁。

//go:build solaris

package fileutil

import (
	"os"
	"syscall"
)

type unixLock struct {
	f *os.File
}

func (l *unixLock) Release() error {
	if err := l.set(false); err != nil {
		return err
	}
	return l.f.Close()
}

// set 通过 F_SETLK 对整文件范围加 F_WRLCK 或 F_UNLCK。
func (l *unixLock) set(lock bool) error {
	flock := syscall.Flock_t{
		Type:   syscall.F_UNLCK,
		Start:  0,
		Len:    0,
		Whence: 1,
	}
	if lock {
		flock.Type = syscall.F_WRLCK
	}
	return syscall.FcntlFlock(l.f.Fd(), syscall.F_SETLK, &flock)
}

// newLock 创建/打开锁文件并尝试非阻塞写锁。
func newLock(fileName string) (Releaser, error) {
	f, err := os.OpenFile(fileName, os.O_RDWR|os.O_CREATE, 0o666)
	if err != nil {
		return nil, err
	}
	l := &unixLock{f}
	err = l.set(true)
	if err != nil {
		f.Close()
		return nil, err
	}
	return l, nil
}
