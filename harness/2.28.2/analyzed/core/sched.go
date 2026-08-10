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

package core

import "context"

// Filter 提供调度器请求阶段时的过滤条件，用于匹配 Runner 能力。
type Filter struct {
	Kind    string
	Type    string
	OS      string
	Arch    string
	Kernel  string
	Variant string
	Labels  map[string]string
}

// Scheduler 负责将构建阶段（Stage）调度到 Runner 执行。
type Scheduler interface {
	// Schedule 将阶段加入调度队列等待执行。
	Schedule(context.Context, *Stage) error

	// Request 按过滤条件请求下一个待执行的阶段。
	Request(context.Context, Filter) (*Stage, error)

	// Cancel 取消与指定构建 ID 关联的已调度或运行中的任务。
	Cancel(context.Context, int64) error

	// Cancelled 阻塞监听取消事件，若构建已被取消则返回 true。
	Cancelled(context.Context, int64) (bool, error)

	// Pause 暂停调度器，阻止新流水线被调度执行。
	Pause(context.Context) error

	// Resume 恢复调度器，允许新流水线被调度执行。
	Resume(context.Context) error

	// Stats 返回底层调度器的统计信息，数据格式因实现而异。
	Stats(context.Context) (interface{}, error)
}
