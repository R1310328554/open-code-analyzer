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

// +build !oss

package queue

import (
	"context"
	"errors"
)

// schedulerRedis 组合 Redis 版队列与 Redis 取消器。
type schedulerRedis struct {
	*queue
	*cancellerRedis
}

// Stats 返回调度器统计信息（当前未实现）。
func (d schedulerRedis) Stats(context.Context) (interface{}, error) {
	return nil, errors.New("not implemented")
}
