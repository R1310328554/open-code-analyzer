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

// Batch 表示一次批量同步请求，用于将用户账户对应的
// 本地仓库列表与权限存储与外部源保持一致。
type Batch struct {
	Insert []*Repository `json:"insert"` // 待新增的仓库
	Update []*Repository `json:"update"` // 待更新的仓库
	Rename []*Repository `json:"rename"` // 待重命名的仓库
	Revoke []*Repository `json:"revoke"` // 待撤销权限的仓库
}

// Batcher 对用户账户执行批量仓库与权限更新。
type Batcher interface {
	// Batch 应用一批仓库变更到指定用户。
	Batch(context.Context, *User, *Batch) error
}
