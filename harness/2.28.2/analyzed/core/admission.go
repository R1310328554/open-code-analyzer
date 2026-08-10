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

// AdmissionService 控制系统准入权限。
// 可用于限制仅授权用户访问，例如仅允许源代码管理
// 系统中某组织的成员登录或使用系统。
type AdmissionService interface {
	// Admit 判定给定用户是否允许进入系统。
	Admit(context.Context, *User) error
}
