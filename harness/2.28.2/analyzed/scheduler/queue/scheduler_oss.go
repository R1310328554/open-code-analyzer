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

//go:build oss
// +build oss

// queue 包（OSS 构建）提供单机内存调度器，不使用 Redis。
package queue

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/drone/service/redisdb"
)

// New 创建 OSS 版调度器，始终使用内存队列与内存取消器。
func New(store core.StageStore, r redisdb.RedisDB) core.Scheduler {
	return scheduler{
		queue:     newQueue(context.Background(), store),
		canceller: newCanceller(),
	}
}
