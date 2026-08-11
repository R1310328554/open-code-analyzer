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

// errors.ts — TypeScript SDK 错误类层次，与 Go 端语义对齐。

// PaddleOCRAPIError 所有 SDK 错误的基类。
// errors.ts — TypeScript SDK 错误类层次，与 Go 端语义对齐。

// PaddleOCRAPIError 所有 SDK 错误的基类。
export class PaddleOCRAPIError extends Error {
  constructor(message: string, options?: ErrorOptions) {
    super(message);
    this.name = "PaddleOCRAPIError";
    this.cause = options?.cause;
  }
}

// AuthError 认证失败。
// AuthError 认证失败。
export class AuthError extends PaddleOCRAPIError {
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = "AuthError";
  }
}

// InvalidRequestError 请求参数无效。
// InvalidRequestError 请求参数无效。
export class InvalidRequestError extends PaddleOCRAPIError {
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = "InvalidRequestError";
  }
}

// APIError 带 statusCode 的 HTTP 错误。
// APIError 带 statusCode 的 HTTP 错误。
export class APIError extends PaddleOCRAPIError {
  statusCode: number;
  constructor(statusCode: number, message: string, options?: ErrorOptions) {
    super(`HTTP ${statusCode}: ${message}`, options);
    this.name = "APIError";
    this.statusCode = statusCode;
  }
}

// RateLimitError 速率限制（429）。
// RateLimitError 速率限制（429）。
export class RateLimitError extends APIError {
  constructor(message: string, options?: ErrorOptions) {
    super(429, message, options);
    this.name = "RateLimitError";
  }
}

export class ServiceUnavailableError extends APIError {
  constructor(statusCode: number, message: string, options?: ErrorOptions) {
    super(statusCode, message, options);
    this.name = "ServiceUnavailableError";
  }
}

// JobFailedError 异步任务执行失败。
// JobFailedError 异步任务执行失败。
export class JobFailedError extends PaddleOCRAPIError {
  jobId: string;
  errorMsg: string;
  constructor(jobId: string, errorMsg: string, options?: ErrorOptions) {
    super(`Job ${jobId} failed: ${errorMsg}`, options);
    this.name = "JobFailedError";
    this.jobId = jobId;
    this.errorMsg = errorMsg;
  }
}

export class RequestTimeoutError extends PaddleOCRAPIError {
  timeoutMs: number;
  constructor(timeoutMs: number, options?: ErrorOptions) {
    super(`Request timed out after ${timeoutMs}ms`, options);
    this.name = "RequestTimeoutError";
    this.timeoutMs = timeoutMs;
  }
}

// PollTimeoutError 轮询等待超时。
// PollTimeoutError 轮询等待超时。
export class PollTimeoutError extends PaddleOCRAPIError {
  jobId: string;
  timeoutMs: number;
  constructor(jobId: string, timeoutMs: number, options?: ErrorOptions) {
    super(`Timed out after ${timeoutMs}ms waiting for job ${jobId}`, options);
    this.name = "PollTimeoutError";
    this.jobId = jobId;
    this.timeoutMs = timeoutMs;
  }
}

// NetworkError 网络层错误。
// NetworkError 网络层错误。
export class NetworkError extends PaddleOCRAPIError {
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = "NetworkError";
  }
}

// FileNotFoundError 本地路径不存在。
// FileNotFoundError 本地路径不存在。
export class FileNotFoundError extends PaddleOCRAPIError {
  path: string;
  constructor(path: string, options?: ErrorOptions) {
    super(`File not found: ${path}`, options);
    this.name = "FileNotFoundError";
    this.path = path;
  }
}

export class ResponseFormatError extends PaddleOCRAPIError {
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = "ResponseFormatError";
  }
}

// ResultParseError 解析 API 结果 JSONL 失败。
// ResultParseError 解析 API 结果 JSONL 失败。
export class ResultParseError extends PaddleOCRAPIError {
  constructor(message: string, options?: ErrorOptions) {
    super(message, options);
    this.name = "ResultParseError";
  }
}
