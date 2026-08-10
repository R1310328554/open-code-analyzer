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

// repos 包提供仓库及关联构建记录的 SQL 扫描辅助函数。
package repos

import (
	"database/sql"

	"github.com/drone/drone/core"
	"github.com/drone/drone/store/shared/db"
)

// ToParams 将 Repository 结构体转换为命名查询参数字典。
func ToParams(v *core.Repository) map[string]interface{} {
	return map[string]interface{}{
		"repo_id":             v.ID,
		"repo_uid":            v.UID,
		"repo_user_id":        v.UserID,
		"repo_namespace":      v.Namespace,
		"repo_name":           v.Name,
		"repo_slug":           v.Slug,
		"repo_scm":            v.SCM,
		"repo_clone_url":      v.HTTPURL,
		"repo_ssh_url":        v.SSHURL,
		"repo_html_url":       v.Link,
		"repo_branch":         v.Branch,
		"repo_private":        v.Private,
		"repo_visibility":     v.Visibility,
		"repo_active":         v.Active,
		"repo_config":         v.Config,
		"repo_trusted":        v.Trusted,
		"repo_protected":      v.Protected,
		"repo_no_forks":       v.IgnoreForks,
		"repo_no_pulls":       v.IgnorePulls,
		"repo_cancel_pulls":   v.CancelPulls,
		"repo_cancel_push":    v.CancelPush,
		"repo_cancel_running": v.CancelRunning,
		"repo_timeout":        v.Timeout,
		"repo_throttle":       v.Throttle,
		"repo_counter":        v.Counter,
		"repo_synced":         v.Synced,
		"repo_created":        v.Created,
		"repo_updated":        v.Updated,
		"repo_version":        v.Version,
		"repo_signer":         v.Signer,
		"repo_secret":         v.Secret,
	}
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
func scanRow(scanner db.Scanner, dest *core.Repository) error {
	return scanner.Scan(
		&dest.ID,
		&dest.UID,
		&dest.UserID,
		&dest.Namespace,
		&dest.Name,
		&dest.Slug,
		&dest.SCM,
		&dest.HTTPURL,
		&dest.SSHURL,
		&dest.Link,
		&dest.Active,
		&dest.Private,
		&dest.Visibility,
		&dest.Branch,
		&dest.Counter,
		&dest.Config,
		&dest.Timeout,
		&dest.Throttle,
		&dest.Trusted,
		&dest.Protected,
		&dest.IgnoreForks,
		&dest.IgnorePulls,
		&dest.CancelPulls,
		&dest.CancelPush,
		&dest.CancelRunning,
		&dest.Synced,
		&dest.Created,
		&dest.Updated,
		&dest.Version,
		&dest.Signer,
		&dest.Secret,
	)
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
// scanRows 迭代 sql.Rows，扫描全部仓库记录。
func scanRows(rows *sql.Rows) ([]*core.Repository, error) {
	defer rows.Close()

	repos := []*core.Repository{}
	for rows.Next() {
		repo := new(core.Repository)
		err := scanRow(rows, repo)
		if err != nil {
			return nil, err
		}
		repos = append(repos, repo)
	}
	return repos, nil
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
// scanRowBuild 扫描含 LEFT JOIN 构建字段的仓库行。
func scanRowBuild(scanner db.Scanner, dest *core.Repository) error {
	build := new(nullBuild)
	err := scanner.Scan(
		&dest.ID,
		&dest.UID,
		&dest.UserID,
		&dest.Namespace,
		&dest.Name,
		&dest.Slug,
		&dest.SCM,
		&dest.HTTPURL,
		&dest.SSHURL,
		&dest.Link,
		&dest.Active,
		&dest.Private,
		&dest.Visibility,
		&dest.Branch,
		&dest.Counter,
		&dest.Config,
		&dest.Timeout,
		&dest.Throttle,
		&dest.Trusted,
		&dest.Protected,
		&dest.IgnoreForks,
		&dest.IgnorePulls,
		&dest.CancelPulls,
		&dest.CancelPush,
		&dest.CancelRunning,
		&dest.Synced,
		&dest.Created,
		&dest.Updated,
		&dest.Version,
		&dest.Signer,
		&dest.Secret,
		// 构建（Build）字段
		&build.ID,
		&build.RepoID,
		&build.Trigger,
		&build.Number,
		&build.Parent,
		&build.Status,
		&build.Error,
		&build.Event,
		&build.Action,
		&build.Link,
		&build.Timestamp,
		&build.Title,
		&build.Message,
		&build.Before,
		&build.After,
		&build.Ref,
		&build.Fork,
		&build.Source,
		&build.Target,
		&build.Author,
		&build.AuthorName,
		&build.AuthorEmail,
		&build.AuthorAvatar,
		&build.Sender,
		&build.Params,
		&build.Cron,
		&build.Deploy,
		&build.DeployID,
		&build.Debug,
		&build.Started,
		&build.Finished,
		&build.Created,
		&build.Updated,
		&build.Version,
	)
	if build.ID.Int64 != 0 {
		dest.Build = build.value()
	}
	return err
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
// scanRowsBuild 批量扫描含构建信息的仓库行。
func scanRowsBuild(rows *sql.Rows) ([]*core.Repository, error) {
	defer rows.Close()

	repos := []*core.Repository{}
	for rows.Next() {
		repo := new(core.Repository)
		err := scanRowBuild(rows, repo)
		if err != nil {
			return nil, err
		}
		repos = append(repos, repo)
	}
	return repos, nil
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
// repoBuildStageRowBuild 扫描单条仓库-构建-阶段运行状态行。
func repoBuildStageRowBuild(scanner db.Scanner, dest *core.RepoBuildStage) error {
	err := scanner.Scan(
		&dest.RepoNamespace,
		&dest.RepoName,
		&dest.RepoSlug,
		&dest.BuildNumber,
		&dest.BuildAuthor,
		&dest.BuildAuthorName,
		&dest.BuildAuthorEmail,
		&dest.BuildAuthorAvatar,
		&dest.BuildSender,
		&dest.BuildStarted,
		&dest.BuildFinished,
		&dest.BuildCreated,
		&dest.BuildUpdated,
		&dest.StageName,
		&dest.StageKind,
		&dest.StageType,
		&dest.StageStatus,
		&dest.StageMachine,
		&dest.StageOS,
		&dest.StageArch,
		&dest.StageVariant,
		&dest.StageKernel,
		&dest.StageLimit,
		&dest.StageLimitRepo,
		&dest.StageStarted,
		&dest.StageStopped,
	)
	return err
}

// scanRow 扫描 sql.Row，将列值复制到目标对象。
// repoBuildStageRowsBuild 批量扫描仓库-构建-阶段运行状态。
func repoBuildStageRowsBuild(rows *sql.Rows) ([]*core.RepoBuildStage, error) {
	defer rows.Close()

	slices := []*core.RepoBuildStage{}
	for rows.Next() {
		row := new(core.RepoBuildStage)
		err := repoBuildStageRowBuild(rows, row)
		if err != nil {
			return nil, err
		}
		slices = append(slices, row)
	}
	return slices, nil
}
