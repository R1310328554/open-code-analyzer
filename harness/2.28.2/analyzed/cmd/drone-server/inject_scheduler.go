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

package main

import (
	"github.com/drone/drone/core"
	"github.com/drone/drone/scheduler/queue"
	"github.com/drone/drone/service/redisdb"

	"github.com/google/wire"
)

// wire set for loading the scheduler.
// schedulerSet 定义构建阶段调度器的 Wire 提供者集合。
var schedulerSet = wire.NewSet(
	provideScheduler,
)

// provideScheduler 根据环境配置返回基于 Redis 队列的构建阶段调度器。
func provideScheduler(store core.StageStore, r redisdb.RedisDB) core.Scheduler {
	return queue.New(store, r)
}
