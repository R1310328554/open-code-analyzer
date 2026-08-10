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

// Build 表示一次 CI/CD 构建执行记录。
type Build struct {
	ID           int64             `db:"build_id"             json:"id"`
	RepoID       int64             `db:"build_repo_id"        json:"repo_id"`
	Trigger      string            `db:"build_trigger"        json:"trigger"`
	Number       int64             `db:"build_number"         json:"number"`
	Parent       int64             `db:"build_parent"         json:"parent,omitempty"`
	Status       string            `db:"build_status"         json:"status"`
	Error        string            `db:"build_error"          json:"error,omitempty"`
	Event        string            `db:"build_event"          json:"event"`
	Action       string            `db:"build_action"         json:"action"`
	Link         string            `db:"build_link"           json:"link"`
	Timestamp    int64             `db:"build_timestamp"      json:"timestamp"`
	Title        string            `db:"build_title"          json:"title,omitempty"`
	Message      string            `db:"build_message"        json:"message"`
	Before       string            `db:"build_before"         json:"before"`
	After        string            `db:"build_after"          json:"after"`
	Ref          string            `db:"build_ref"            json:"ref"`
	Fork         string            `db:"build_source_repo"    json:"source_repo"`
	Source       string            `db:"build_source"         json:"source"`
	Target       string            `db:"build_target"         json:"target"`
	Author       string            `db:"build_author"         json:"author_login"`
	AuthorName   string            `db:"build_author_name"    json:"author_name"`
	AuthorEmail  string            `db:"build_author_email"   json:"author_email"`
	AuthorAvatar string            `db:"build_author_avatar"  json:"author_avatar"`
	Sender       string            `db:"build_sender"         json:"sender"`
	Params       map[string]string `db:"build_params"         json:"params,omitempty"`
	Cron         string            `db:"build_cron"           json:"cron,omitempty"`
	Deploy       string            `db:"build_deploy"         json:"deploy_to,omitempty"`
	DeployID     int64             `db:"build_deploy_id"      json:"deploy_id,omitempty"`
	Debug        bool              `db:"build_debug"          json:"debug,omitempty"`
	Started      int64             `db:"build_started"        json:"started"`
	Finished     int64             `db:"build_finished"       json:"finished"`
	Created      int64             `db:"build_created"        json:"created"`
	Updated      int64             `db:"build_updated"        json:"updated"`
	Version      int64             `db:"build_version"        json:"version"`
	Stages       []*Stage          `db:"-"                    json:"stages,omitempty"`
}

// BuildStore 定义构建记录的持久化与查询操作。
type BuildStore interface {
	// Find 按主键从数据存储中查找构建。
	Find(context.Context, int64) (*Build, error)

	// FindNumber 按仓库 ID 与构建编号查找构建。
	FindNumber(context.Context, int64, int64) (*Build, error)

	// FindRef 按仓库 ID 与 Git 引用查找最近一次构建。
	FindRef(context.Context, int64, string) (*Build, error)

	// List 按仓库 ID 分页列出构建记录。
	List(context.Context, int64, int, int) ([]*Build, error)

	// ListRef 按仓库 ID 与引用分页列出构建记录。
	ListRef(context.Context, int64, string, int, int) ([]*Build, error)

	// LatestBranches 返回各分支在数据存储中的最新构建。
	LatestBranches(context.Context, int64) ([]*Build, error)

	// LatestPulls 返回各 Pull Request 在数据存储中的最新构建。
	LatestPulls(context.Context, int64) ([]*Build, error)

	// LatestDeploys 返回各部署目标在数据存储中的最新构建。
	LatestDeploys(context.Context, int64) ([]*Build, error)

	// Pending 列出所有待处理构建（已弃用）。
	Pending(context.Context) ([]*Build, error)

	// Running 列出所有运行中构建（已弃用）。
	Running(context.Context) ([]*Build, error)

	// Create 将新构建及其阶段持久化到数据存储。
	Create(context.Context, *Build, []*Stage) error

	// Update 更新数据存储中的构建记录。
	Update(context.Context, *Build) error

	// Delete 从数据存储中删除构建。
	Delete(context.Context, *Build) error

	// DeletePull 删除 Pull Request 索引。
	DeletePull(context.Context, int64, int) error

	// DeleteBranch 删除分支索引。
	DeleteBranch(context.Context, int64, string) error

	// DeleteDeploy 删除部署目标索引。
	DeleteDeploy(context.Context, int64, string) error

	// Purge 删除构建编号小于 n 的历史构建。
	Purge(context.Context, int64, int64) error

	// Count 返回构建总数。
	Count(context.Context) (int64, error)
}

// IsDone 若构建已处于终态（非等待/排队/运行/阻塞）则返回 true。
func (b *Build) IsDone() bool {
	switch b.Status {
	case StatusWaiting,
		StatusPending,
		StatusRunning,
		StatusBlocked:
		return false
	default:
		return true
	}
}

// IsFailed 若构建已失败、被终止或出错则返回 true。
func (b *Build) IsFailed() bool {
	switch b.Status {
	case StatusFailing,
		StatusKilled,
		StatusError:
		return true
	default:
		return false
	}
}
