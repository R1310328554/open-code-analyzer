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
// error_code.go — 统一错误码与预定义错误变量：涵盖业务异常、许可证校验及 HTTP 语义状态码，供 API 层统一返回。

//

package common

import "errors"

// ErrorCode 为整型业务/HTTP 错误码枚举。
type ErrorCode int

// 错误码常量：0 成功；10–110 业务类；320–326 许可证；400+ HTTP 语义。
const (
	// CodeSuccess 操作成功。
	CodeSuccess                ErrorCode = 0
	// CodeNotEffective 资源尚未生效。
	CodeNotEffective           ErrorCode = 10
	// CodeExceptionError 系统内部异常。
	CodeExceptionError         ErrorCode = 100
	// CodeArgumentError 参数非法。
	CodeArgumentError          ErrorCode = 101
	// CodeDataError 数据校验或持久化失败。
	CodeDataError              ErrorCode = 102
	// CodeOperatingError 业务操作被拒绝。
	CodeOperatingError         ErrorCode = 103
	// CodeTimeoutError 请求或任务超时。
	CodeTimeoutError           ErrorCode = 104
	// CodeConnectionError 下游连接失败。
	CodeConnectionError        ErrorCode = 105
	// CodeRunning 任务仍在执行中。
	CodeRunning                ErrorCode = 106
	// CodeResourceExhausted 配额或资源耗尽。
	CodeResourceExhausted      ErrorCode = 107
	// CodePermissionError 权限不足。
	CodePermissionError        ErrorCode = 108
	// CodeAuthenticationError 身份认证失败。
	CodeAuthenticationError    ErrorCode = 109
	// CodeParamError 请求参数格式错误。
	CodeParamError             ErrorCode = 110
	// CodeLicenseValid 许可证有效。
	CodeLicenseValid           ErrorCode = 320
	// CodeLicenseInactiveError 许可证未激活。
	CodeLicenseInactiveError   ErrorCode = 321
	// CodeLicenseExpiredError 许可证已过期。
	CodeLicenseExpiredError    ErrorCode = 322
	// CodeLicenseDigestError 许可证摘要不匹配。
	CodeLicenseDigestError     ErrorCode = 323
	// CodeLicenseTimeRollback 检测到系统时间回拨。
	CodeLicenseTimeRollback    ErrorCode = 324
	// CodeLicenseNotFound 未找到许可证文件。
	CodeLicenseNotFound        ErrorCode = 325
	// CodeLicenseUnexpectedError 许可证校验未知错误。
	CodeLicenseUnexpectedError ErrorCode = 326
	// CodeBadRequest HTTP 400 请求无效。
	CodeBadRequest             ErrorCode = 400
	// CodeUnauthorized HTTP 401 未授权。
	CodeUnauthorized           ErrorCode = 401
	// CodeForbidden HTTP 403 禁止访问。
	CodeForbidden              ErrorCode = 403
	// CodeNotFound HTTP 404 资源不存在。
	CodeNotFound               ErrorCode = 404
	// CodeConflict HTTP 409 资源冲突。
	CodeConflict               ErrorCode = 409
	// CodeServerError HTTP 500 服务器内部错误。
	CodeServerError            ErrorCode = 500
	// CodeNotImplemented HTTP 501 功能未实现。
	CodeNotImplemented         ErrorCode = 501
)

// errorMessages 为错误码到英文提示的映射表。
var errorMessages = map[ErrorCode]string{
	CodeSuccess:                "Success",
	CodeNotEffective:           "Not effective",
	CodeExceptionError:         "System exception",
	CodeArgumentError:          "Invalid argument",
	CodeDataError:              "Data error",
	CodeOperatingError:         "Operation error",
	CodeTimeoutError:           "Timeout",
	CodeConnectionError:        "Connection error",
	CodeRunning:                "System running",
	CodeResourceExhausted:      "Resource exhausted",
	CodePermissionError:        "Permission denied",
	CodeAuthenticationError:    "Authentication failed",
	CodeParamError:             "Invalid parameters",
	CodeLicenseValid:           "License valid",
	CodeLicenseInactiveError:   "License inactive",
	CodeLicenseExpiredError:    "License expired",
	CodeLicenseDigestError:     "License digest error",
	CodeLicenseTimeRollback:    "License time rollback detected",
	CodeLicenseNotFound:        "License not found",
	CodeLicenseUnexpectedError: "Unexpected license error",
	CodeBadRequest:             "Bad request",
	CodeUnauthorized:           "Unauthorized",
	CodeForbidden:              "Forbidden",
	CodeNotFound:               "Resource not found",
	CodeConflict:               "Resource conflict",
	CodeServerError:            "Internal server error",
}

// Message 返回错误码对应的人类可读描述，未知码返回 Unknown error。
func (e ErrorCode) Message() string {
	if msg, ok := errorMessages[e]; ok {
		return msg
	}
	return "Unknown error"
}

// 预定义 sentinel 错误，供 errors.Is 判断。
var (
	// ErrInvalidToken 令牌无效或已过期。
	ErrInvalidToken = errors.New("invalid token")
	// ErrNotAdmin 当前用户非管理员。
	ErrNotAdmin     = errors.New("user is not admin")
	// ErrUserInactive 用户账号已停用。
	ErrUserInactive = errors.New("user is inactive")
	// ErrUserNotFound 用户不存在。
	ErrUserNotFound = errors.New("user not found")
	// ErrNotFound is returned when an object is not found
	// ErrNotFound 通用对象未找到。
	ErrNotFound = errors.New("object not found")
	// ErrBucketNotFound is returned when a bucket is not found
	// ErrBucketNotFound 存储桶不存在。
	ErrBucketNotFound = errors.New("bucket not found")
	// ErrTaskNotFound 异步任务 ID 不存在。
	ErrTaskNotFound   = errors.New("task id not found")
)
