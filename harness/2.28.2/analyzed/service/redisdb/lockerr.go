// Copyright 2021 Drone IO, Inc.
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

// redisdb 包定义 Redis 分布式锁的错误感知接口与空实现。
package redisdb

import (
	"context"
)

// LockErr 定义带错误返回的加锁/解锁接口，方法名与 redsync.Mutex 兼容。
type LockErr interface {
	LockContext(context.Context) error
	UnlockContext(context.Context) (bool, error)
}

// LockErrNoOp 空操作锁实现，用于未启用 Redis 锁的场景。
type LockErrNoOp struct{}

// LockContext 空实现，始终成功。
func (l LockErrNoOp) LockContext(context.Context) error           { return nil }
// UnlockContext 空实现，返回 false 且无错误。
func (l LockErrNoOp) UnlockContext(context.Context) (bool, error) { return false, nil }
