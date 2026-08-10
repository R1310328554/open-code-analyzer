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
	// Commit 表示 Git 提交元数据。
	Commit struct {
		Sha       string
		Ref       string
		Message   string
		Author    *Committer
		Committer *Committer
		Link      string
	}

	// Committer 表示提交作者或提交者信息。
	Committer struct {
		Name   string
		Email  string
		Date   int64
		Login  string
		Avatar string
	}

	// Change 表示某次提交中的单个文件变更。
	Change struct {
		Path    string
		Added   bool
		Renamed bool
		Deleted bool
	}

	// CommitService 从外部源代码管理服务（如 GitHub）
	// 读取提交历史与变更详情。
	CommitService interface {
		// Find 按提交 SHA 获取提交信息。
		Find(ctx context.Context, user *User, repo, sha string) (*Commit, error)

		// FindRef 按 Git 引用获取对应提交信息。
		FindRef(ctx context.Context, user *User, repo, ref string) (*Commit, error)

		// ListChanges 按 SHA 或引用列出该提交涉及的文件变更。
		ListChanges(ctx context.Context, user *User, repo, sha, ref string) ([]*Change, error)
	}
)
