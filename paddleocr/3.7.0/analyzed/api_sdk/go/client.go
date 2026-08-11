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

// client.go — PaddleOCR 官方 API 的 Go 客户端核心：认证、超时与 HTTP 传输配置。

// client.go — PaddleOCR 官方 API 的 Go 客户端核心：认证、超时与 HTTP 传输配置。

package paddleocr

import (
	"net/http"
	"os"
	"strings"
	"time"
)

// Client 封装访问令牌、基础 URL、任务端点及 HTTP 客户端实例。
// Client 封装访问令牌、基础 URL、任务端点及 HTTP 客户端实例。
type Client struct {
	token          string
	baseURL        string
	jobsURL        string
	requestTimeout time.Duration
	pollTimeout    time.Duration
	clientPlatform string
	httpClient     *http.Client
}

// NewClient 通过可选 ClientOption 构造客户端；令牌可从环境变量 PADDLEOCR_ACCESS_TOKEN 读取。
// NewClient 通过可选 ClientOption 构造客户端；令牌可从环境变量 PADDLEOCR_ACCESS_TOKEN 读取。
func NewClient(opts ...ClientOption) (*Client, error) {
	c := &Client{
		requestTimeout: 5 * time.Minute,
		pollTimeout:    10 * time.Minute,
	}
	for _, opt := range opts {
		opt(c)
	}
	if c.token == "" {
		c.token = os.Getenv("PADDLEOCR_ACCESS_TOKEN")
	}
	if c.token == "" {
		return nil, &AuthError{PaddleOCRAPIError{Message: "Token is required. Set PADDLEOCR_ACCESS_TOKEN or use WithToken()."}}
	}
	if c.baseURL == "" {
		c.baseURL = os.Getenv("PADDLEOCR_BASE_URL")
	}
	if c.baseURL == "" {
		c.baseURL = DefaultBaseURL
	}
	c.baseURL = strings.TrimRight(c.baseURL, "/")
	c.jobsURL = c.baseURL + apiPath
	if c.httpClient == nil {
		c.httpClient = &http.Client{Timeout: c.requestTimeout}
	}
	return c, nil
}

// setClientPlatformHeader 在请求头写入 Client-Platform 标识（若已配置）。
// setClientPlatformHeader 在请求头写入 Client-Platform 标识（若已配置）。
func (c *Client) setClientPlatformHeader(req *http.Request) {
	if c.clientPlatform != "" {
		req.Header.Set("Client-Platform", c.clientPlatform)
	}
}
