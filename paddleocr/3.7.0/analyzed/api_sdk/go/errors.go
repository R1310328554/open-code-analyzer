// Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// errors.go — PaddleOCR API 客户端错误类型层次，便于 errors.As 精确捕获。

// errors.go — PaddleOCR API 客户端错误类型层次，便于 errors.As 精确捕获。

package paddleocr

import "fmt"

// PaddleOCRAPIError 为所有 SDK 错误的基类，含消息与可选 Cause。
// PaddleOCRAPIError 为所有 SDK 错误的基类，含消息与可选 Cause。
type PaddleOCRAPIError struct {
	Message string
	Cause   error
}

func (e *PaddleOCRAPIError) Error() string {
	if e.Message == "" && e.Cause != nil {
		return e.Cause.Error()
	}
	return e.Message
}

func (e *PaddleOCRAPIError) Unwrap() error {
	return e.Cause
}

// AuthError 认证失败（401/403 或缺少令牌）。
// AuthError 认证失败（401/403 或缺少令牌）。
type AuthError struct {
	PaddleOCRAPIError
}

// InvalidRequestError 请求参数不合法（400）。
// InvalidRequestError 请求参数不合法（400）。
type InvalidRequestError struct {
	PaddleOCRAPIError
}

// APIError 通用 HTTP 错误，携带状态码。
// APIError 通用 HTTP 错误，携带状态码。
type APIError struct {
	StatusCode int
	PaddleOCRAPIError
}

func (e *APIError) Error() string {
	return fmt.Sprintf("HTTP %d: %s", e.StatusCode, e.Message)
}

// RateLimitError 触发速率限制（429）。
// RateLimitError 触发速率限制（429）。
type RateLimitError struct {
	APIError
}

// ServiceUnavailableError 服务不可用（503/504）。
// ServiceUnavailableError 服务不可用（503/504）。
type ServiceUnavailableError struct {
	APIError
}

// JobFailedError 异步任务执行失败。
// JobFailedError 异步任务执行失败。
type JobFailedError struct {
	JobID    string
	ErrorMsg string
	PaddleOCRAPIError
}

func (e *JobFailedError) Error() string {
	return fmt.Sprintf("Job %s failed: %s", e.JobID, e.ErrorMsg)
}

// RequestTimeoutError HTTP 请求超时。
// RequestTimeoutError HTTP 请求超时。
type RequestTimeoutError struct {
	PaddleOCRAPIError
}

// PollTimeoutError 轮询等待任务完成超时。
// PollTimeoutError 轮询等待任务完成超时。
type PollTimeoutError struct {
	JobID   string
	Elapsed float64
	PaddleOCRAPIError
}

func (e *PollTimeoutError) Error() string {
	return fmt.Sprintf("Timed out after %.1fs waiting for job %s", e.Elapsed, e.JobID)
}

// NetworkError 底层网络或连接错误。
// NetworkError 底层网络或连接错误。
type NetworkError struct {
	PaddleOCRAPIError
}

// FileNotFoundError 本地文件或目录不存在。
// FileNotFoundError 本地文件或目录不存在。
type FileNotFoundError struct {
	Path string
	PaddleOCRAPIError
}

func (e *FileNotFoundError) Error() string {
	return fmt.Sprintf("File not found: %s", e.Path)
}

// ResponseFormatError API 响应格式不符合预期。
// ResponseFormatError API 响应格式不符合预期。
type ResponseFormatError struct {
	PaddleOCRAPIError
}

// ResultParseError 解析 OCR/文档解析结果 JSONL 失败。
// ResultParseError 解析 OCR/文档解析结果 JSONL 失败。
type ResultParseError struct {
	PaddleOCRAPIError
}
