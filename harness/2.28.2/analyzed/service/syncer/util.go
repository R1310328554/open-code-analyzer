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

package syncer

import (
	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

// merge 将源仓库的关键字段合并到目标仓库。
func merge(dst, src *core.Repository) {
	dst.Namespace = src.Namespace
	dst.Name = src.Name
	dst.HTTPURL = src.HTTPURL
	dst.SSHURL = src.SSHURL
	dst.Private = src.Private
	dst.Branch = src.Branch
	dst.Slug = scm.Join(src.Namespace, src.Name)

	// Gitea/Gogs 仓库 API 不返回 HTML URL，避免用空值覆盖已有链接。
	if src.Link != "" {
		dst.Link = src.Link
	}
}

// diff 比较两个仓库的关键字段，任一不同则返回 true。
func diff(a, b *core.Repository) bool {
	switch {
	case a.Namespace != b.Namespace:
		return true
	case a.Name != b.Name:
		return true
	case a.HTTPURL != b.HTTPURL:
		return true
	case a.SSHURL != b.SSHURL:
		return true
	case a.Private != b.Private:
		return true
	case a.Branch != b.Branch:
		return true
	case a.Link != b.Link:
		return true
	default:
		return false
	}
}
