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

// queue 包将调度队列与取消器组合为 core.Scheduler 实现。
package queue

import (
	"context"
	"errors"
)

// scheduler 组合 queue 与内存 canceller，提供完整调度能力。
type scheduler struct {
	*queue
	*canceller
}

// Stats 返回调度器统计信息（当前未实现）。
func (d scheduler) Stats(context.Context) (interface{}, error) {
	return nil, errors.New("not implemented")
}
