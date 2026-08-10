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

// Organization 表示源代码管理系统（如 GitHub）中的组织实体。
type Organization struct {
	Name   string
	Avatar string
}

// OrganizationService 提供对外部源代码管理系统（如 GitHub）
// 中组织与团队访问权限的查询能力。
type OrganizationService interface {
	// List 返回用户所属的组织列表。
	List(context.Context, *User) ([]*Organization, error)

	// Membership 判定用户是否为组织成员，并返回是否为组织管理员。
	Membership(context.Context, *User, string) (bool, bool, error)
}
