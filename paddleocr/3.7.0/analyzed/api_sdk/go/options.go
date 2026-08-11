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

// options.go — Client 构造选项函数式配置与默认 API 端点常量。

// options.go — Client 构造选项函数式配置与默认 API 端点常量。

package paddleocr

import (
	"net/http"
	"time"
)

// DefaultBaseURL 官方 API 默认基础 URL。
// DefaultBaseURL 官方 API 默认基础 URL。
const DefaultBaseURL = "https://paddleocr.aistudio-app.com"

const apiPath = "/api/v2/ocr/jobs"

// ClientOption 函数式选项，用于 NewClient 配置令牌、超时与 HTTP 客户端。
// ClientOption 函数式选项，用于 NewClient 配置令牌、超时与 HTTP 客户端。
type ClientOption func(*Client)

// WithToken 设置 API 访问令牌。
// WithToken 设置 API 访问令牌。
func WithToken(token string) ClientOption {
	return func(c *Client) {
		c.token = token
	}
}

// WithBaseURL 覆盖 API 基础 URL。
// WithBaseURL 覆盖 API 基础 URL。
func WithBaseURL(url string) ClientOption {
	return func(c *Client) {
		c.baseURL = url
	}
}

func WithTimeout(d time.Duration) ClientOption {
	return func(c *Client) {
		c.requestTimeout = d
		c.pollTimeout = d
	}
}

// WithRequestTimeout 设置单次 HTTP 请求超时。
// WithRequestTimeout 设置单次 HTTP 请求超时。
func WithRequestTimeout(d time.Duration) ClientOption {
	return func(c *Client) {
		c.requestTimeout = d
	}
}

// WithPollTimeout 设置轮询等待任务完成的总超时。
// WithPollTimeout 设置轮询等待任务完成的总超时。
func WithPollTimeout(d time.Duration) ClientOption {
	return func(c *Client) {
		c.pollTimeout = d
	}
}

func WithClientPlatform(clientPlatform string) ClientOption {
	return func(c *Client) {
		c.clientPlatform = clientPlatform
	}
}

// WithHTTPClient 注入自定义 http.Client 实例。
// WithHTTPClient 注入自定义 http.Client 实例。
func WithHTTPClient(hc *http.Client) ClientOption {
	return func(c *Client) {
		c.httpClient = hc
	}
}

// Bool 辅助函数：返回 bool 指针，便于填充可选 JSON 字段。
// Bool 辅助函数：返回 bool 指针，便于填充可选 JSON 字段。
func Bool(v bool) *bool {
	return &v
}
