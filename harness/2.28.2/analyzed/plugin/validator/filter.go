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

package validator

import (
	"context"
	"path/filepath"

	"github.com/drone/drone/core"
)

// Filter 返回按仓库 slug 白名单/黑名单过滤的校验服务，不匹配则跳过流水线。
func Filter(include, exclude []string) core.ValidateService {
	return &filter{
		include: include,
		exclude: exclude,
	}
}

// filter 根据 include/exclude 通配符决定是否对当前仓库执行校验。
type filter struct {
	include []string
	exclude []string
}

// Validate 匹配 include 列表或排除 exclude 列表中的仓库，返回 ErrValidatorSkip 跳过。
func (f *filter) Validate(ctx context.Context, in *core.ValidateArgs) error {
	if len(f.include) > 0 {
		for _, pattern := range f.include {
			ok, _ := filepath.Match(pattern, in.Repo.Slug)
			if ok {
				return nil
			}
		}

		// 指定了 include 列表但仓库未匹配任何模式，应跳过校验。
		return core.ErrValidatorSkip
	}

	if len(f.exclude) > 0 {
		for _, pattern := range f.exclude {
			ok, _ := filepath.Match(pattern, in.Repo.Slug)
			if ok {
				// 仓库命中 exclude 列表中的模式，应跳过校验。
				return core.ErrValidatorSkip
			}
		}
	}

	return nil
}
