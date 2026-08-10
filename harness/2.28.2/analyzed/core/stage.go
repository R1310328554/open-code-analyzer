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

type (
	// Stage 表示构建流水线中的一个执行阶段（对应 YAML 中的 pipeline）。
	Stage struct {
		ID        int64             `json:"id"`
		RepoID    int64             `json:"repo_id"`
		BuildID   int64             `json:"build_id"`
		Number    int               `json:"number"`
		Name      string            `json:"name"`
		Kind      string            `json:"kind,omitempty"`
		Type      string            `json:"type,omitempty"`
		Status    string            `json:"status"`
		Error     string            `json:"error,omitempty"`
		ErrIgnore bool              `json:"errignore"`
		ExitCode  int               `json:"exit_code"`
		Machine   string            `json:"machine,omitempty"`
		OS        string            `json:"os"`
		Arch      string            `json:"arch"`
		Variant   string            `json:"variant,omitempty"`
		Kernel    string            `json:"kernel,omitempty"`
		Limit     int               `json:"limit,omitempty"`
		LimitRepo int               `json:"throttle,omitempty"`
		Started   int64             `json:"started"`
		Stopped   int64             `json:"stopped"`
		Created   int64             `json:"created"`
		Updated   int64             `json:"updated"`
		Version   int64             `json:"version"`
		OnSuccess bool              `json:"on_success"`
		OnFailure bool              `json:"on_failure"`
		DependsOn []string          `json:"depends_on,omitempty"`
		Labels    map[string]string `json:"labels,omitempty"`
		Steps     []*Step           `json:"steps,omitempty"`
	}

	// StageStore 持久化构建阶段信息到存储后端。
	StageStore interface {
		// List 从数据存储中返回指定构建的阶段列表。
		List(context.Context, int64) ([]*Stage, error)

		// ListIncomplete 从数据存储中返回未完成（挂起或运行中）的阶段列表。
		ListIncomplete(ctx context.Context) ([]*Stage, error)

		// ListSteps 从数据存储中返回阶段列表，并包含各阶段下的步骤。
		ListSteps(context.Context, int64) ([]*Stage, error)

		// ListState 跨所有仓库按状态查询阶段列表。
		ListState(context.Context, string) ([]*Stage, error)

		// Find 按 ID 从数据存储中查询阶段。
		Find(context.Context, int64) (*Stage, error)

		// FindNumber 按构建 ID 与阶段序号从数据存储中查询阶段。
		FindNumber(context.Context, int64, int) (*Stage, error)

		// Create 将新阶段持久化到数据存储。
		Create(context.Context, *Stage) error

		// Update 将更新后的阶段持久化到数据存储。
		Update(context.Context, *Stage) error
	}
)

// IsDone 若阶段已处于终态（非等待/挂起/运行/阻塞）则返回 true。
func (s *Stage) IsDone() bool {
	switch s.Status {
	case StatusWaiting,
		StatusPending,
		StatusRunning,
		StatusBlocked:
		return false
	default:
		return true
	}
}

// IsFailed 若阶段处于失败、被终止或错误状态则返回 true。
func (s *Stage) IsFailed() bool {
	switch s.Status {
	case StatusFailing,
		StatusKilled,
		StatusError:
		return true
	default:
		return false
	}
}
