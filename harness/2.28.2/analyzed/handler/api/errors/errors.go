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

// Package errors 定义 API 层统一的 JSON 错误类型与常用错误常量。
package errors

var (
	// ErrInvalidToken 表示请求令牌无效或缺失。
	ErrInvalidToken = New("Invalid or missing token")

	// ErrUnauthorized 表示用户未通过身份认证。
	ErrUnauthorized = New("Unauthorized")

	// ErrForbidden 表示用户无权访问目标资源。
	ErrForbidden = New("Forbidden")

	// ErrNotFound 表示请求的资源不存在。
	ErrNotFound = New("Not Found")
)

// Error 表示可 JSON 序列化的 API 错误响应体。
type Error struct {
	Message string `json:"message"` // 面向客户端的错误描述
}

// Error 实现 error 接口，返回错误消息文本。
func (e *Error) Error() string {
	return e.Message
}

// New 构造带指定消息文本的 API 错误。
func New(text string) error {
	return &Error{Message: text}
}
