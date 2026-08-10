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

// render 包提供 API 响应的统一 JSON 渲染与标准 HTTP 错误封装。
package render

import (
	"encoding/json"
	"fmt"
	"net/http"
	"os"
	"strconv"

	"github.com/drone/drone/handler/api/errors"
)

// indent 控制 JSON 响应是否缩进输出，由环境变量 HTTP_JSON_INDENT 决定。
var indent bool

func init() {
	indent, _ = strconv.ParseBool(
		os.Getenv("HTTP_JSON_INDENT"),
	)
}

var (
	// ErrInvalidToken 表示 API 请求令牌无效或缺失。
	ErrInvalidToken = errors.New("Invalid or missing token")

	// ErrUnauthorized 表示用户未通过认证。
	ErrUnauthorized = errors.New("Unauthorized")

	// ErrForbidden 表示用户无权访问目标资源。
	ErrForbidden = errors.New("Forbidden")

	// ErrNotFound 表示请求的资源不存在。
	ErrNotFound = errors.New("Not Found")

	// ErrNotImplemented 表示接口尚未实现。
	ErrNotImplemented = errors.New("Not Implemented")
)

// ErrorCode 将错误信息编码为 JSON 并以指定 HTTP 状态码写入响应。
func ErrorCode(w http.ResponseWriter, err error, status int) {
	JSON(w, &errors.Error{Message: err.Error()}, status)
}

// InternalError 返回 500 内部服务器错误 JSON 响应。
func InternalError(w http.ResponseWriter, err error) {
	ErrorCode(w, err, 500)
}

// InternalErrorf 按格式化字符串构造错误并返回 500 响应。
func InternalErrorf(w http.ResponseWriter, format string, a ...interface{}) {
	ErrorCode(w, fmt.Errorf(format, a...), 500)
}

// NotImplemented 返回 501 未实现 JSON 响应。
func NotImplemented(w http.ResponseWriter, err error) {
	ErrorCode(w, err, 501)
}

// NotFound 返回 404 未找到 JSON 响应。
func NotFound(w http.ResponseWriter, err error) {
	ErrorCode(w, err, 404)
}

// NotFoundf 按格式化字符串构造错误并返回 404 响应。
func NotFoundf(w http.ResponseWriter, format string, a ...interface{}) {
	ErrorCode(w, fmt.Errorf(format, a...), 404)
}

// Unauthorized 返回 401 未授权 JSON 响应。
func Unauthorized(w http.ResponseWriter, err error) {
	ErrorCode(w, err, 401)
}

// Forbidden 返回 403 禁止访问 JSON 响应。
func Forbidden(w http.ResponseWriter, err error) {
	ErrorCode(w, err, 403)
}

// BadRequest 返回 400 错误请求 JSON 响应。
func BadRequest(w http.ResponseWriter, err error) {
	ErrorCode(w, err, 400)
}

// BadRequestf 按格式化字符串构造错误并返回 400 响应。
func BadRequestf(w http.ResponseWriter, format string, a ...interface{}) {
	ErrorCode(w, fmt.Errorf(format, a...), 400)
}

// JSON 将任意值编码为 JSON 并以指定状态码写入响应体。
func JSON(w http.ResponseWriter, v interface{}, status int) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	enc := json.NewEncoder(w)
	if indent {
		enc.SetIndent("", "  ")
	}
	enc.Encode(v)
}
