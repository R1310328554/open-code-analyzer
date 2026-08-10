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
//

// Package deepdoc — 可选 deepdoc 视觉服务的 Go 客户端（DLA / OCR / TSR）。
// 协议自 deepdoc/vision/dla_cli.py 与 docs/agent-port/deepdoc-endpoints.md 还原。仅 DLA 有远程 HTTP 端点；Python 侧 OCR/TSR 为本地 ONNX，此处 OCR/TSR 桩返回 ErrNoRemoteEndpoint。
package deepdoc

import (
	"context"
	"errors"
	"io"
	"net/http"
	"os"
	"time"

	"go.opentelemetry.io/contrib/instrumentation/net/http/otelhttp"
)

// ErrNoURL 客户端未配置 base URL（DEEPDOC_URL / TENSORRT_DLA_SVR 未设置）时返回。
var ErrNoURL = errors.New("deepdoc: not configured (set DEEPDOC_URL or TENSORRT_DLA_SVR)")

// ErrNoRemoteEndpoint OCR/TSR 无远程端点（Python 为本地 ONNX），调用即返回此错误。
var ErrNoRemoteEndpoint = errors.New("deepdoc: no remote endpoint exists (Python deepdoc is local-ONNX only)")

// ErrInvalidResponse 响应体校验失败（如缺少 bboxes）；按 Python 约定触发重试。
var ErrInvalidResponse = errors.New("deepdoc: invalid response")

// DefaultPerAttemptTimeout 单次请求超时，对齐 Python DLAClient @timeout(18)。
const DefaultPerAttemptTimeout = 18 * time.Second

// DefaultMaxAttempts 最大重试次数，对齐 Python 3 次循环。
const DefaultMaxAttempts = 3

// DefaultBackoff 重试初始退避；每次翻倍，上限 MaxBackoff。
const DefaultBackoff = 200 * time.Millisecond

// MaxBackoff 指数退避的上限。
const MaxBackoff = 3 * time.Second

// predictPath DLA 预测路径，见 deepdoc-endpoints.md §2.1。
const predictPath = "/predict"

// Client 连接可选 deepdoc 服务；baseURL 为空时 Enabled() 为 false，HTTP 方法直接返回 ErrNoURL。
type Client struct {
	baseURL     string
	httpClient  *http.Client
	maxAttempts int
	backoff     time.Duration
}

// Option 构造期配置 Client；测试可指向 httptest、覆盖超时等。
type Option func(*Client)

// WithHTTPClient 覆盖底层 HTTP 客户端。
func WithHTTPClient(hc *http.Client) Option {
	return func(c *Client) { c.httpClient = hc }
}

// WithMaxAttempts 覆盖单次调用最大重试次数。
func WithMaxAttempts(n int) Option {
	return func(c *Client) { c.maxAttempts = n }
}

// WithBackoff 覆盖重试初始退避时间。
func WithBackoff(d time.Duration) Option {
	return func(c *Client) { c.backoff = d }
}

// NewClient 从环境变量构造 Client；优先 DEEPDOC_URL，否则 TENSORRT_DLA_SVR；均未设置则未启用。
func NewClient(opts ...Option) *Client {
	url := os.Getenv("DEEPDOC_URL")
	if url == "" {
		url = os.Getenv("TENSORRT_DLA_SVR")
	}
	return NewClientWithURL(url, opts...)
}

// NewClientWithURL 显式指定 base URL，主要用于测试。
func NewClientWithURL(baseURL string, opts ...Option) *Client {
	c := &Client{
		baseURL:     baseURL,
		maxAttempts: DefaultMaxAttempts,
		backoff:     DefaultBackoff,
	}
	for _, opt := range opts {
		opt(c)
	}
	if c.httpClient == nil {
		// otelhttp.NewTransport is a no-op when no OTel exporter is
		// configured (see plan §2.10.4) — safe default.
		c.httpClient = &http.Client{
			Timeout:   DefaultPerAttemptTimeout,
			Transport: otelhttp.NewTransport(http.DefaultTransport),
		}
	}
	return c
}

// Enabled 是否已配置远程 deepdoc URL；false 时 HTTP 方法立即 ErrNoURL。
func (c *Client) Enabled() bool {
	return c != nil && c.baseURL != ""
}

// bodyBuilder 每次重试生成新的请求体工厂，返回 (body, contentType)。
type bodyBuilder func() (io.Reader, string)

// doPost 带重试与指数退避的 POST，语义对齐 Python DLAClient。网络错误/5xx/校验失败可重试；4xx 与 ctx 取消不重试；成功返回校验后的响应字节。
func (c *Client) doPost(ctx context.Context, url string, buildBody bodyBuilder, validate func([]byte) error) ([]byte, error) {
	if !c.Enabled() {
		return nil, ErrNoURL
	}
	var lastErr error
	backoff := c.backoff
	for attempt := 1; attempt <= c.maxAttempts; attempt++ {
		body, contentType := buildBody()
		req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, body)
		if err != nil {
			return nil, err
		}
		if contentType != "" {
			req.Header.Set("Content-Type", contentType)
		}
		resp, err := c.httpClient.Do(req)
		if err != nil {
			lastErr = err
		} else {
			data, readErr := io.ReadAll(resp.Body)
			resp.Body.Close()
			switch {
			case readErr != nil:
				lastErr = readErr
			case resp.StatusCode >= 500:
				lastErr = &httpError{Status: resp.Status, Body: string(data), retryable: true}
			case resp.StatusCode >= 400:
				// 4xx is a config error, not transient — surface
				// immediately without retrying.
				return nil, &httpError{Status: resp.Status, Body: string(data), retryable: false}
			case validate != nil:
				if vErr := validate(data); vErr != nil {
					lastErr = vErr
				} else {
					return data, nil
				}
			default:
				return data, nil
			}
		}
		if attempt < c.maxAttempts {
			select {
			case <-ctx.Done():
				return nil, ctx.Err()
			case <-time.After(backoff):
			}
			backoff *= 2
			if backoff > MaxBackoff {
				backoff = MaxBackoff
			}
		}
	}
	if lastErr == nil {
		lastErr = ErrInvalidResponse
	}
	return nil, lastErr
}

// httpError 携带 HTTP 状态与响应体；retryable 表示 doPost 已耗尽重试。
type httpError struct {
	Status    string
	Body      string
	retryable bool
}

// Error 格式化 deepdoc HTTP 错误信息。
func (e *httpError) Error() string {
	return "deepdoc: " + e.Status + ": " + e.Body
}
