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

// Package paddleocr 提供 PaddleOCR 官方 API 的 Go 客户端。
//
// 使用 NewClient 创建客户端，通过 WithToken 或环境变量 PADDLEOCR_ACCESS_TOKEN 认证。
// OCR 接口支持 PP-OCR 系列模型；ParseDocument 支持文档解析模型。
// SubmitOCR / SubmitDocumentParsing 返回 Operation，可用 Poll 轮询或 Wait 阻塞等待。
// SaveResource 及 Save*ResultResources 将结果资源下载到本地目录。
// 请求超时与轮询超时分别由 WithRequestTimeout / WithPollTimeout 配置。
// 错误类型（AuthError、InvalidRequestError、APIError 等）可用于 errors.As 类型断言。
//
// Package paddleocr 提供 PaddleOCR 官方 API 的 Go 客户端。
//
// 使用 NewClient 创建客户端，通过 WithToken 或环境变量 PADDLEOCR_ACCESS_TOKEN 认证。
// OCR 接口支持 PP-OCR 系列模型；ParseDocument 支持文档解析模型。
// SubmitOCR / SubmitDocumentParsing 返回 Operation，可用 Poll 轮询或 Wait 阻塞等待。
// SaveResource 及 Save*ResultResources 将结果资源下载到本地目录。
// 请求超时与轮询超时分别由 WithRequestTimeout / WithPollTimeout 配置。
// 错误类型（AuthError、InvalidRequestError、APIError 等）可用于 errors.As 类型断言。
//
// Package paddleocr provides a Go client for the PaddleOCR official API.
//
// Create a client with NewClient and authenticate with WithToken or the
// PADDLEOCR_ACCESS_TOKEN environment variable. Use OCR for supported OCR models
// and ParseDocument for document parsing models.
// SubmitOCR and SubmitDocumentParsing return an Operation for non-blocking
// status checks with Poll or typed waits with WaitOCR and WaitDocumentParsing.
// SaveResource downloads one result resource URL. SaveOCRResultResources and
// SaveDocumentParsingResultResources save resources from typed result objects
// into an existing directory.
//
// Request timeout and polling timeout are configured separately with
// WithRequestTimeout and WithPollTimeout. Errors are exposed as typed values,
// such as AuthError, InvalidRequestError, APIError, ResponseFormatError, and
// ResultParseError, and are suitable for errors.As.
package paddleocr
