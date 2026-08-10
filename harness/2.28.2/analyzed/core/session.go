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

import "net/http"

// Session 为已认证用户提供基于 Cookie 的会话管理。
type Session interface {
	// Create 创建新用户会话并将 Cookie 写入 HTTP 响应。
	Create(http.ResponseWriter, *User) error

	// Delete 从 HTTP 响应中清除用户会话 Cookie。
	Delete(http.ResponseWriter) error

	// Get 从 HTTP 请求中读取会话并返回关联用户。
	// 若无有效会话则返回 nil 用户；错误信息仅用于调试。
	Get(*http.Request) (*User, error)
}
