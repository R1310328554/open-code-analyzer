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

// error.go — 全局错误与 404 处理：内部错误脱敏、NoRoute 与 Python 行为对齐。

//

package handler

import (
	"net/http"

	"ragflow/internal/common"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

// jsonInternalError 记录原始错误但向客户端返回通用消息，避免泄露实现细节。
// to avoid exposing internal implementation details in API responses.
func jsonInternalError(c *gin.Context, err error) {
	common.Warn("handler internal error",
		zap.Error(err),
		zap.String("method", c.Request.Method),
		zap.String("path", c.Request.URL.Path),
	)
	common.ResponseWithCodeData(c, common.CodeServerError, nil, common.CodeServerError.Message())
}

// HandleNoRoute 处理未匹配路由；GET /api/v1/auth/login/ 对齐 Python MethodNotAllowed 响应。
func HandleNoRoute(c *gin.Context) {
	// Python 兼容：GET /api/v1/auth/login/ 返回 HTTP 200 / code 100 MethodNotAllowed _repr
	// to a Werkzeug MethodNotAllowed in the Python API, which
	// server_error_response renders as HTTP 200 / code 100 with the
	// Gin 默认走 NoRoute，此处显式对齐 Python 行为。
	// NoRoute, so emit the same body here to keep the auth error paths
	// byte-for-byte aligned.
	if c.Request.Method == http.MethodGet && c.Request.URL.Path == "/api/v1/auth/login/" {
		common.ResponseWithCodeData(c, common.CodeExceptionError, false, "<MethodNotAllowed '405: Method Not Allowed'>")
		return
	}

	// 服务端记录 404 请求详情
	common.Logger.Warn("The requested URL was not found",
		zap.String("method", c.Request.Method),
		zap.String("path", c.Request.URL.Path),
		zap.String("query", c.Request.URL.RawQuery),
		zap.String("remote_addr", c.ClientIP()),
		zap.String("user_agent", c.Request.UserAgent()),
	)

	// 返回 JSON 404 错误体
	c.JSON(http.StatusNotFound, gin.H{
		"code":    404,
		"message": "Not Found: " + c.Request.URL.Path,
		"data":    nil,
		"error":   "Not Found",
	})
}
