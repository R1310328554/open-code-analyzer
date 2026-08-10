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

// skip.go 提供流水线触发条件匹配与提交消息跳过指令检测。
package trigger

import (
	"strings"

	"github.com/drone/drone-yaml/yaml"
	"github.com/drone/drone/core"
)

// skipBranch 当分支不匹配触发规则时返回 true（应跳过）。
func skipBranch(document *yaml.Pipeline, branch string) bool {
	return !document.Trigger.Branch.Match(branch)
}

// skipRef 当 ref 不匹配触发规则时返回 true。
func skipRef(document *yaml.Pipeline, ref string) bool {
	return !document.Trigger.Ref.Match(ref)
}

// skipEvent 当事件类型不匹配触发规则时返回 true。
func skipEvent(document *yaml.Pipeline, event string) bool {
	return !document.Trigger.Event.Match(event)
}

// skipAction 当 PR 动作不匹配触发规则时返回 true。
func skipAction(document *yaml.Pipeline, action string) bool {
	return !document.Trigger.Action.Match(action)
}

// skipInstance 当实例名不匹配触发规则时返回 true。
func skipInstance(document *yaml.Pipeline, instance string) bool {
	return !document.Trigger.Instance.Match(instance)
}

// skipTarget 当部署目标不匹配触发规则时返回 true。
func skipTarget(document *yaml.Pipeline, env string) bool {
	return !document.Trigger.Target.Match(env)
}

// skipRepo 当仓库 slug 不匹配触发规则时返回 true。
func skipRepo(document *yaml.Pipeline, repo string) bool {
	return !document.Trigger.Repo.Match(repo)
}

// skipCron 当 Cron 任务名不匹配触发规则时返回 true。
func skipCron(document *yaml.Pipeline, cron string) bool {
	return !document.Trigger.Cron.Match(cron)
}

// skipMessage 检测提交消息或标题是否含 [ci skip] 等跳过指令。
func skipMessage(hook *core.Hook) bool {
	switch {
	case hook.Event == core.EventTag:
		return false
	case hook.Event == core.EventCron:
		return false
	case hook.Event == core.EventCustom:
		return false
	case hook.Event == core.EventPromote:
		return false
	case hook.Event == core.EventRollback:
		return false
	case skipMessageEval(hook.Message):
		return true
	case skipMessageEval(hook.Title):
		return true
	default:
		return false
	}
}

// skipMessageEval 在字符串中查找 CI 跳过关键字（不区分大小写）。
func skipMessageEval(str string) bool {
	lower := strings.ToLower(str)
	switch {
	case strings.Contains(lower, "[ci skip]"),
		strings.Contains(lower, "[skip ci]"),
		strings.Contains(lower, "***no_ci***"):
		return true
	default:
		return false
	}
}

// skipPaths（已注释）按变更路径过滤流水线；空列表或 300+ 文件时强制运行。
// func skipPaths(document *config.Config, paths []string) bool {
// 	switch {
// 	// 仅 push/PR 事件返回变更文件；列表为空则强制运行全部流水线。
// 	case len(paths) == 0:
// 		return false
// 	// GitHub API 最多返回 300 个变更文件；达到上限时强制运行全部流水线。
// 	case len(paths) >= 300:
// 		return false
// 	default:
// 		return !document.Trigger.Paths.MatchAny(paths)
// 	}
// }
