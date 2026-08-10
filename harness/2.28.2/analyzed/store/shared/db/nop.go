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

// db 包提供非 SQLite 驱动下的空操作锁实现（Locker 接口）。
package db

// nopLocker 是 Locker 接口的空实现，用于非 SQLite 驱动（无需读写锁）。
type nopLocker struct{}

// Lock 获取写锁（空操作）。
func (nopLocker) Lock()    {}
// Unlock 释放写锁（空操作）。
func (nopLocker) Unlock()  {}
// RLock 获取读锁（空操作）。
func (nopLocker) RLock()   {}
// RUnlock 释放读锁（空操作）。
func (nopLocker) RUnlock() {}
