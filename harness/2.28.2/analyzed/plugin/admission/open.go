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

package admission

import (
	"context"
	"errors"

	"github.com/drone/drone/core"
)

// ErrClosed 在尝试注册新用户但系统已关闭注册时返回。
var ErrClosed = errors.New("User registration is disabled")

// Open 创建开放准入策略：默认允许新用户注册，可通过 disabled 标志关闭注册。
func Open(disabled bool) core.AdmissionService {
	return &closed{disabled: disabled}
}

// closed 根据 disabled 标志决定是否拒绝新用户注册。
type closed struct {
	disabled bool
}

// Admit 对新用户检查注册是否被禁用；已有用户始终放行。
func (s *closed) Admit(ctx context.Context, user *core.User) error {
	// 准入策略仅对新注册用户生效，已有用户始终允许通过。
	if user.ID != 0 {
		return nil
	}

	if s.disabled {
		return ErrClosed
	}
	return nil
}
