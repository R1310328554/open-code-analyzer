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
	// Step 表示阶段内的单个构建步骤（对应 YAML pipeline 中的一个 step）。
	Step struct {
		ID        int64    `json:"id"`
		StageID   int64    `json:"step_id"` // this is a typo, fixing it has far reaching ramifications. It should only be attempted in a major version change
		Number    int      `json:"number"`
		Name      string   `json:"name"`
		Status    string   `json:"status"`
		Error     string   `json:"error,omitempty"`
		ErrIgnore bool     `json:"errignore,omitempty"`
		ExitCode  int      `json:"exit_code"`
		Started   int64    `json:"started,omitempty"`
		Stopped   int64    `json:"stopped,omitempty"`
		Version   int64    `json:"version"`
		DependsOn []string `json:"depends_on,omitempty"`
		Image     string   `json:"image,omitempty"`
		Detached  bool     `json:"detached,omitempty"`
		Schema    string   `json:"schema,omitempty"`
	}

	// StepStore 持久化构建步骤信息到存储后端。
	StepStore interface {
		// List 从数据存储中返回指定阶段的步骤列表。
		List(context.Context, int64) ([]*Step, error)

		// Find 按 ID 从数据存储中查询步骤。
		Find(context.Context, int64) (*Step, error)

		// FindNumber 按阶段 ID 与步骤序号从数据存储中查询步骤。
		FindNumber(context.Context, int64, int) (*Step, error)

		// Create 将新步骤持久化到数据存储。
		Create(context.Context, *Step) error

		// Update 将更新后的步骤持久化到数据存储。
		Update(context.Context, *Step) error
	}
)

// IsDone 若步骤已处于终态（非等待/挂起/运行/阻塞）则返回 true。
func (s *Step) IsDone() bool {
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
