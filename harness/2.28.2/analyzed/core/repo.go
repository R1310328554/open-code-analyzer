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

// 仓库可见性常量。
const (
	VisibilityPublic   = "public"
	VisibilityPrivate  = "private"
	VisibilityInternal = "internal"
)

// 版本控制系统类型常量。
const (
	VersionControlGit       = "git"
	VersionControlMercurial = "hg"
)

type (
	// Repository 表示源代码仓库及其 Drone 配置与元数据。
	Repository struct {
		ID            int64  `json:"id"`
		UID           string `json:"uid"`
		UserID        int64  `json:"user_id"`
		Namespace     string `json:"namespace"`
		Name          string `json:"name"`
		Slug          string `json:"slug"`
		SCM           string `json:"scm"`
		HTTPURL       string `json:"git_http_url"`
		SSHURL        string `json:"git_ssh_url"`
		Link          string `json:"link"`
		Branch        string `json:"default_branch"`
		Private       bool   `json:"private"`
		Visibility    string `json:"visibility"`
		Active        bool   `json:"active"`
		Config        string `json:"config_path"`
		Trusted       bool   `json:"trusted"`
		Protected     bool   `json:"protected"`
		IgnoreForks   bool   `json:"ignore_forks"`
		IgnorePulls   bool   `json:"ignore_pull_requests"`
		CancelPulls   bool   `json:"auto_cancel_pull_requests"`
		CancelPush    bool   `json:"auto_cancel_pushes"`
		CancelRunning bool   `json:"auto_cancel_running"`
		Timeout       int64  `json:"timeout"`
		Throttle      int64  `json:"throttle,omitempty"`
		Counter       int64  `json:"counter"`
		Synced        int64  `json:"synced"`
		Created       int64  `json:"created"`
		Updated       int64  `json:"updated"`
		Version       int64  `json:"version"`
		Signer        string `json:"-"`
		Secret        string `json:"-"`
		Build         *Build `json:"build,omitempty"`
		Perms         *Perm  `json:"permissions,omitempty"`
		Archived      bool   `json:"archived"`
	}

	// RepoBuildStage 聚合仓库、构建与阶段的运行状态信息，
	// 用于查询未完成构建的详情。
	RepoBuildStage struct {
		RepoNamespace     string `json:"repo_namespace"`
		RepoName          string `json:"repo_name"`
		RepoSlug          string `json:"repo_slug"`
		BuildNumber       int64  `json:"build_number"`
		BuildAuthor       string `json:"build_author"`
		BuildAuthorName   string `json:"build_author_name"`
		BuildAuthorEmail  string `json:"build_author_email"`
		BuildAuthorAvatar string `json:"build_author_avatar"`
		BuildSender       string `json:"build_sender"`
		BuildStarted      int64  `json:"build_started"`
		BuildFinished     int64  `json:"build_finished"`
		BuildCreated      int64  `json:"build_created"`
		BuildUpdated      int64  `json:"build_updated"`
		StageName         string `json:"stage_name"`
		StageKind         string `json:"stage_kind"`
		StageType         string `json:"stage_type"`
		StageStatus       string `json:"stage_status"`
		StageMachine      string `json:"stage_machine"`
		StageOS           string `json:"stage_os"`
		StageArch         string `json:"stage_arch"`
		StageVariant      string `json:"stage_variant"`
		StageKernel       string `json:"stage_kernel"`
		StageLimit        string `json:"stage_limit"`
		StageLimitRepo    string `json:"stage_limit_repo"`
		StageStarted      int64  `json:"stage_started"`
		StageStopped      int64  `json:"stage_stopped"`
	}

	// RepositoryStore 定义仓库数据的持久化存储操作。
	RepositoryStore interface {
		// List 从数据存储中返回指定用户的仓库列表。
		List(context.Context, int64) ([]*Repository, error)

		// ListLatest 返回去重后的仓库列表，每个仓库附带最近一次构建。
		ListLatest(context.Context, int64) ([]*Repository, error)

		// ListRecent 返回非去重的仓库列表，包含最近多次构建记录。
		ListRecent(context.Context, int64) ([]*Repository, error)

		// ListIncomplete 返回存在未完成构建的仓库列表。
		ListIncomplete(context.Context) ([]*Repository, error)

		// ListRunningStatus 返回所有未完成构建的仓库/构建/阶段状态信息。
		ListRunningStatus(context.Context) ([]*RepoBuildStage, error)

		// ListAll 分页返回数据库中全部仓库，包括已禁用的仓库。
		ListAll(ctx context.Context, limit, offset int) ([]*Repository, error)

		// Find 按 ID 从数据存储中查询仓库。
		Find(context.Context, int64) (*Repository, error)

		// FindName 按命名空间与名称从数据存储中查询仓库。
		FindName(context.Context, string, string) (*Repository, error)

		// Create 将新仓库持久化到数据存储。
		Create(context.Context, *Repository) error

		// Activate 将仓库的激活状态持久化到数据存储。
		Activate(context.Context, *Repository) error

		// Update 将仓库变更持久化到数据存储。
		Update(context.Context, *Repository) error

		// Delete 从数据存储中删除仓库。
		Delete(context.Context, *Repository) error

		// Count 返回已激活仓库的总数。
		Count(context.Context) (int64, error)

		// Increment 递增并返回仓库的构建编号。
		Increment(context.Context, *Repository) (*Repository, error)
	}

	// RepositoryService 提供对外部源代码管理系统（如 GitHub）
	// 中仓库信息与权限的查询能力。
	RepositoryService interface {
		// List 返回用户可访问的仓库列表。
		List(ctx context.Context, user *User) ([]*Repository, error)

		// Find 返回指定名称的仓库详情。
		Find(ctx context.Context, user *User, repo string) (*Repository, error)

		// FindPerm 返回指定仓库的用户权限。
		FindPerm(ctx context.Context, user *User, repo string) (*Perm, error)
	}
)
