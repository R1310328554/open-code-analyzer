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

// linker 包根据 SCM 客户端生成提交、分支等资源的可浏览链接。
package linker

import (
	"context"

	"github.com/drone/drone/core"
	"github.com/drone/go-scm/scm"
)

// New 构造 Linker 服务，委托 SCM 客户端生成资源 URL。
func New(client *scm.Client) core.Linker {
	return &service{
		client: client,
	}
}

// service 实现 core.Linker 接口。
type service struct {
	client *scm.Client
}

// Link 返回仓库指定 ref/sha 在 SCM 上的浏览链接。
func (s *service) Link(ctx context.Context, repo, ref, sha string) (string, error) {
	return s.client.Linker.Resource(ctx, repo, scm.Reference{
		Path: ref,
		Sha:  sha,
	})
}
