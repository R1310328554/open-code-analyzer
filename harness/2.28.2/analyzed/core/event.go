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

// 钩子事件类型常量，标识触发构建的 SCM 事件种类。
const (
	EventCron        = "cron"         // 定时任务触发
	EventCustom      = "custom"       // 自定义事件
	EventPush        = "push"         // 推送
	EventPullRequest = "pull_request" // Pull Request
	EventTag         = "tag"          // 标签
	EventPromote     = "promote"      // 晋升/晋级部署
	EventRollback    = "rollback"     // 回滚
)
