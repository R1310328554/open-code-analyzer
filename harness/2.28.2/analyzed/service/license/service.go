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

// license 包提供许可证配额校验与过期检查服务。
package license

import (
	"context"
	"time"

	"github.com/drone/drone/core"
)

// NewService 构造 LicenseService，注入用户/仓库/构建存储与许可证对象。
func NewService(
	users core.UserStore,
	repos core.RepositoryStore,
	builds core.BuildStore,
	license *core.License,
) core.LicenseService {
	return &service{
		users:   users,
		repos:   repos,
		builds:  builds,
		license: license,
	}
}

// service 实现 core.LicenseService，对照许可证限制检查资源用量。
type service struct {
	users   core.UserStore
	repos   core.RepositoryStore
	builds  core.BuildStore
	license *core.License
}

// Exceeded 检查构建数、用户数或仓库数是否超出许可证上限。
func (s *service) Exceeded(ctx context.Context) (bool, error) {
	if limit := s.license.Builds; limit > 0 {
		count, _ := s.builds.Count(ctx)
		if count > limit {
			return true, core.ErrBuildLimit
		}
	}
	if limit := s.license.Users; limit > 0 {
		count, _ := s.users.Count(ctx)
		if count > limit {
			return true, core.ErrUserLimit
		}
	}
	if limit := s.license.Repos; limit > 0 {
		count, _ := s.repos.Count(ctx)
		if count > limit {
			return true, core.ErrRepoLimit
		}
	}
	return false, nil
}

// Expired 判断许可证是否已过期。
func (s *service) Expired(ctx context.Context) bool {
	return s.license.Expired()
}

// Expires 返回许可证到期时间。
func (s *service) Expires(ctx context.Context) time.Time {
	return s.license.Expires
}
