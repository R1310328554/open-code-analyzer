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

package web

import (
	"encoding/json"
	"errors"
	"net/http"
	"os"
	"strconv"
)

// indent 控制 JSON 响应是否缩进格式化，由环境变量 HTTP_JSON_INDENT 决定。
var indent bool

func init() {
	indent, _ = strconv.ParseBool(
		os.Getenv("HTTP_JSON_INDENT"),
	)
}

var (
	// errInvalidToken 表示 API 请求令牌无效或缺失。
	errInvalidToken = errors.New("Invalid or missing token")

	// errUnauthorized 表示用户未通过认证。
	errUnauthorized = errors.New("Unauthorized")

	// errForbidden 表示用户无权访问资源。
	errForbidden = errors.New("Forbidden")

	// errNotFound 表示请求的资源不存在。
	errNotFound = errors.New("Not Found")
)

// Error 为 JSON 编码的 API 错误响应体。
type Error struct {
	Message string `json:"message"`
}

// writeErrorCode 按指定 HTTP 状态码写入 JSON 错误消息。
func writeErrorCode(w http.ResponseWriter, err error, status int) {
	writeJSON(w, &Error{Message: err.Error()}, status)
}

// writeError 以 500 内部错误状态写入 JSON 错误消息。
func writeError(w http.ResponseWriter, err error) {
	writeErrorCode(w, err, 500)
}

// writeNotFound 以 404 状态写入 JSON 错误消息。
func writeNotFound(w http.ResponseWriter, err error) {
	writeErrorCode(w, err, 404)
}

// writeUnauthorized 以 401 未授权状态写入 JSON 错误消息。
func writeUnauthorized(w http.ResponseWriter, err error) {
	writeErrorCode(w, err, 401)
}

// writeForbidden 以 403 禁止访问状态写入 JSON 错误消息。
func writeForbidden(w http.ResponseWriter, err error) {
	writeErrorCode(w, err, 403)
}

// writeBadRequest 以 400 错误请求状态写入 JSON 错误消息。
func writeBadRequest(w http.ResponseWriter, err error) {
	writeErrorCode(w, err, 400)
}

// writeJSON 将任意值序列化为 JSON 并写入响应，Content-Type 为 application/json。
func writeJSON(w http.ResponseWriter, v interface{}, status int) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	enc := json.NewEncoder(w)
	if indent {
		enc.SetIndent("", "  ")
	}
	enc.Encode(v)
}
