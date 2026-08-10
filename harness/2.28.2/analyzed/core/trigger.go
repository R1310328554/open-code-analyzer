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

// 构建触发来源类型常量。
const (
	TriggerHook = "@hook" // Webhook 事件触发
	TriggerCron = "@cron" // 定时任务触发
)

// Triggerer 负责根据入站 Hook 触发一次 CI/CD 构建。
// 若构建被跳过则返回 nil。
type Triggerer interface {
	Trigger(context.Context, *Repository, *Hook) (*Build, error)
}
