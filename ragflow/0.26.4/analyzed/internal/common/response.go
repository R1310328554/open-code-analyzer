//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
// response.go — Gin HTTP 统一响应封装：成功/失败 JSON 结构与 ErrorCode 配合，供各 API Handler 快速返回标准格式。

//

package common

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// response 标准成功响应体：业务码、消息与数据载荷。
type response struct {
	Code    int         `json:"code"`
	Message interface{} `json:"message"`
	Data    interface{} `json:"data"`
}

// errorResponse 错误响应体：仅含业务码与错误消息，不含 data 字段。
type errorResponse struct {
	Code    int         `json:"code"`
	Message interface{} `json:"message"`
}

// SuccessWithData 返回带 data 与 message 的成功响应（HTTP 200）。
func SuccessWithData(c *gin.Context, data interface{}, message interface{}) {
	c.JSON(http.StatusOK, response{
		Code:    int(CodeSuccess),
		Data:    data,
		Message: message,
	})
}

// SuccessNoMessage 返回仅含 data 的成功响应，省略 message 字段。
func SuccessNoMessage(c *gin.Context, data interface{}) {
	c.JSON(http.StatusOK, response{
		Code: int(CodeSuccess),
		Data: data,
	})
}

// SuccessNoData 返回仅含 message 的成功响应，data 置为 nil。
func SuccessNoData(c *gin.Context, message interface{}) {
	c.JSON(http.StatusOK, response{
		Code:    int(CodeSuccess),
		Data:    nil,
		Message: message,
	})
}

// SuccessWithMessage 返回仅含字符串 message 的成功响应。
func SuccessWithMessage(c *gin.Context, message string) {
	c.JSON(http.StatusOK, response{
		Code:    int(CodeSuccess),
		Message: message,
	})
}

// ErrorWithCode 按自定义整型码与消息返回错误 JSON（HTTP 仍为 200）。
func ErrorWithCode(c *gin.Context, code int, message string) {
	c.JSON(http.StatusOK, errorResponse{
		Code:    code,
		Message: message,
	})
}

// ResponseWithCodeData 使用 ErrorCode 枚举返回带 data 的通用响应。
func ResponseWithCodeData(c *gin.Context, code ErrorCode, data interface{}, message string) {
	c.JSON(http.StatusOK, response{
		Code:    int(code),
		Data:    data,
		Message: message,
	})
}

// ResponseWithHttpCodeData 同时指定 HTTP 状态码与业务 ErrorCode。
func ResponseWithHttpCodeData(c *gin.Context, httpCode int, code ErrorCode, data interface{}, message string) {
	c.JSON(httpCode, response{
		Code:    int(code),
		Data:    data,
		Message: message,
	})
}
