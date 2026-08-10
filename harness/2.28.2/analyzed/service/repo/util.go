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

package repo

import (
	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

// convertRepository 将 SCM 仓库对象映射为 Drone 内部 core.Repository。
func convertRepository(src *scm.Repository, visibility string, trusted bool) *core.Repository {
	return &core.Repository{
		UID:        src.ID,
		Namespace:  src.Namespace,
		Name:       src.Name,
		Slug:       scm.Join(src.Namespace, src.Name),
		HTTPURL:    src.Clone,
		SSHURL:     src.CloneSSH,
		Link:       src.Link,
		Private:    src.Private,
		Visibility: convertVisibility(src, visibility),
		Branch:     src.Branch,
		Trusted:    trusted,
		Archived:   src.Archived,
	}
}

// convertVisibility 根据 SCM 隐私标志与全局默认可见性推导仓库可见性级别。
func convertVisibility(src *scm.Repository, visibility string) string {
	// GitHub Enterprise / GitLab 的 internal 可见性：全局未配置时自动设为 internal。
	if visibility == "" && src.Visibility == scm.VisibilityInternal {
		return core.VisibilityInternal
	}

	switch {
	case src.Private == true:
		return core.VisibilityPrivate
	case visibility == core.VisibilityInternal:
		return core.VisibilityInternal
	default:
		return core.VisibilityPublic
	}
}
