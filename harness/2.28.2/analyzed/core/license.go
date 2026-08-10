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

import (
	"context"
	"errors"
	"time"
)

// 许可证类型常量。
const (
	LicenseFoss     = "foss"     // 开源版
	LicenseFree     = "free"     // 免费版
	LicensePersonal = "personal" // 个人版
	LicenseStandard = "standard" // 标准商业版
	LicenseTrial    = "trial"    // 试用版
)

// ErrUserLimit 在创建用户时超出许可证允许的用户数上限时返回。
var ErrUserLimit = errors.New("User limit exceeded")

// ErrRepoLimit 在创建仓库时超出许可证允许的仓库数上限时返回。
var ErrRepoLimit = errors.New("Repository limit exceeded")

// ErrBuildLimit 在创建构建时超出许可证允许的构建数上限时返回。
var ErrBuildLimit = errors.New("Build limit exceeded")

type (
	// License 定义软件许可证的配额与有效期等参数。
	License struct {
		Licensor     string    `json:"-"`
		Subscription string    `json:"-"`
		Expires      time.Time `json:"expires_at,omitempty"`
		Kind         string    `json:"kind,omitempty"`
		Repos        int64     `json:"repos,omitempty"`
		Users        int64     `json:"users,omitempty"`
		Builds       int64     `json:"builds,omitempty"`
		Nodes        int64     `json:"nodes,omitempty"`
	}

	// LicenseService 提供许可证校验，用于检测配额超限与过期。
	LicenseService interface {
		// Exceeded 若当前用量已超过许可证定义的上限则返回 true。
		Exceeded(context.Context) (bool, error)

		// Expired 若许可证已过期则返回 true。
		Expired(context.Context) bool
	}
)

// Expired 若许可证设置了过期时间且当前时间已过则返回 true。
func (l *License) Expired() bool {
	return l.Expires.IsZero() == false && time.Now().After(l.Expires)
}
