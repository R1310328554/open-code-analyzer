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

package main

import (
	"github.com/drone/drone/cmd/drone-server/config"
	"github.com/drone/drone/service/redisdb"

	"github.com/google/wire"
)

// wire set for loading the external services.
// externalSet 定义 Redis 等外部服务的 Wire 提供者集合。
var externalSet = wire.NewSet(
	provideRedisClient,
)

// provideRedisClient 根据配置创建并返回 Redis 客户端连接。
func provideRedisClient(config config.Config) (rdb redisdb.RedisDB, err error) {
	return redisdb.New(config)
}
