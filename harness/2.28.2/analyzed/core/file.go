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
	// File 表示远程版本控制系统中的原始文件内容。
	File struct {
		Data []byte // 文件字节内容
		Hash []byte // 内容哈希
	}

	// FileArgs 提供从远程 SCM 拉取文件所需的仓库与提交信息。
	FileArgs struct {
		Commit string // 提交 SHA
		Ref    string // Git 引用
	}

	// FileService 从远程源代码管理服务（如 GitHub）
	// 读取指定路径的文件内容。
	FileService interface {
		// Find 按提交/引用与路径获取文件。
		Find(ctx context.Context, user *User, repo, commit, ref, path string) (*File, error)
	}
)
