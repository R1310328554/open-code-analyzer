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

package utility

// http_client.go 提供带 Builder 模式的可配置 HTTP 客户端。

import (
	"bytes"
	"crypto/tls"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"time"
)

// HTTPClient 是可配置的 HTTP 客户端，封装主机、端口、SSL、超时与请求头。
type HTTPClient struct {
	host       string
	port       int
	useSSL     bool
	timeout    time.Duration
	headers    map[string]string
	httpClient *http.Client
}

// HTTPClientBuilder 用于链式构建 HTTPClient。
type HTTPClientBuilder struct {
	client *HTTPClient
}

// NewHTTPClientBuilder 创建 Builder，默认 localhost:80、30s 超时。
func NewHTTPClientBuilder() *HTTPClientBuilder {
	return &HTTPClientBuilder{
		client: &HTTPClient{
			host:    "localhost",
			port:    80,
			useSSL:  false,
			timeout: 30 * time.Second,
			headers: make(map[string]string),
		},
	}
}

// WithHost 设置目标主机。
func (b *HTTPClientBuilder) WithHost(host string) *HTTPClientBuilder {
	b.client.host = host
	return b
}

// WithPort 设置目标端口。
func (b *HTTPClientBuilder) WithPort(port int) *HTTPClientBuilder {
	b.client.port = port
	return b
}

// WithSSL 启用或禁用 HTTPS（关闭时允许 InsecureSkipVerify）。
func (b *HTTPClientBuilder) WithSSL(useSSL bool) *HTTPClientBuilder {
	b.client.useSSL = useSSL
	return b
}

// WithTimeout 设置请求超时。
func (b *HTTPClientBuilder) WithTimeout(timeout time.Duration) *HTTPClientBuilder {
	b.client.timeout = timeout
	return b
}

// WithHeader 添加单个请求头。
func (b *HTTPClientBuilder) WithHeader(key, value string) *HTTPClientBuilder {
	b.client.headers[key] = value
	return b
}

// WithHeaders 批量设置请求头。
func (b *HTTPClientBuilder) WithHeaders(headers map[string]string) *HTTPClientBuilder {
	for key, value := range headers {
		b.client.headers[key] = value
	}
	return b
}

// Build 构建并返回配置完成的 HTTPClient。
func (b *HTTPClientBuilder) Build() *HTTPClient {
	transport := &http.Transport{
		TLSClientConfig: &tls.Config{
			InsecureSkipVerify: false,
		},
	}

	// 未启用 SSL 时允许跳过证书校验（内网/开发场景）
	if !b.client.useSSL {
		transport.TLSClientConfig.InsecureSkipVerify = true
	}

	b.client.httpClient = &http.Client{
		Timeout:   b.client.timeout,
		Transport: transport,
	}

	return b.client
}

// SetHost 运行时修改主机。
func (c *HTTPClient) SetHost(host string) {
	c.host = host
}

// SetPort 运行时修改端口。
func (c *HTTPClient) SetPort(port int) {
	c.port = port
}

// SetSSL 运行时切换 SSL。
func (c *HTTPClient) SetSSL(useSSL bool) {
	c.useSSL = useSSL
}

// SetTimeout 运行时修改超时并同步更新 http.Client。
func (c *HTTPClient) SetTimeout(timeout time.Duration) {
	c.timeout = timeout
	c.httpClient.Timeout = timeout
}

// SetHeader 设置单个请求头。
func (c *HTTPClient) SetHeader(key, value string) {
	c.headers[key] = value
}

// SetHeaders 替换全部请求头。
func (c *HTTPClient) SetHeaders(headers map[string]string) {
	c.headers = headers
}

// AddHeader 追加请求头，保留已有项。
func (c *HTTPClient) AddHeader(key, value string) {
	c.headers[key] = value
}

// GetHeaders 返回请求头副本，避免外部修改。
func (c *HTTPClient) GetHeaders() map[string]string {
	headersCopy := make(map[string]string)
	for k, v := range c.headers {
		headersCopy[k] = v
	}
	return headersCopy
}

// GetBaseURL 返回 scheme://host:port 形式的基础 URL。
func (c *HTTPClient) GetBaseURL() string {
	scheme := "http"
	if c.useSSL {
		scheme = "https"
	}
	return fmt.Sprintf("%s://%s:%d", scheme, c.host, c.port)
}

// GetFullURL 拼接基础 URL 与路径（自动补前导 /）。
func (c *HTTPClient) GetFullURL(path string) string {
	baseURL := c.GetBaseURL()
	// Ensure path starts with /
	if path != "" && path[0] != '/' {
		path = "/" + path
	}
	return baseURL + path
}

// prepareRequest 创建带已配置请求头的 HTTP 请求。
func (c *HTTPClient) prepareRequest(method, urlStr string, body io.Reader) (*http.Request, error) {
	req, err := http.NewRequest(method, urlStr, body)
	if err != nil {
		return nil, err
	}

	// 注入 Builder/Set 阶段配置的请求头
	for key, value := range c.headers {
		req.Header.Set(key, value)
	}

	return req, nil
}

// Get 发起 GET 请求。
func (c *HTTPClient) Get(path string) (*http.Response, error) {
	urlStr := c.GetFullURL(path)
	req, err := c.prepareRequest(http.MethodGet, urlStr, nil)
	if err != nil {
		return nil, err
	}
	return c.httpClient.Do(req)
}

// GetWithParams 发起带 query 参数的 GET 请求。
func (c *HTTPClient) GetWithParams(path string, params map[string]string) (*http.Response, error) {
	urlStr := c.GetFullURL(path)
	u, err := url.Parse(urlStr)
	if err != nil {
		return nil, err
	}

	query := u.Query()
	for key, value := range params {
		query.Set(key, value)
	}
	u.RawQuery = query.Encode()

	req, err := c.prepareRequest(http.MethodGet, u.String(), nil)
	if err != nil {
		return nil, err
	}
	return c.httpClient.Do(req)
}

// Post 发起 POST 请求。
func (c *HTTPClient) Post(path string, body []byte) (*http.Response, error) {
	urlStr := c.GetFullURL(path)
	req, err := c.prepareRequest(http.MethodPost, urlStr, bytes.NewReader(body))
	if err != nil {
		return nil, err
	}
	return c.httpClient.Do(req)
}

// PostJSON 以 application/json 发起 POST。
func (c *HTTPClient) PostJSON(path string, body []byte) (*http.Response, error) {
	c.SetHeader("Content-Type", "application/json")
	return c.Post(path, body)
}

// Put 发起 PUT 请求。
func (c *HTTPClient) Put(path string, body []byte) (*http.Response, error) {
	urlStr := c.GetFullURL(path)
	req, err := c.prepareRequest(http.MethodPut, urlStr, bytes.NewReader(body))
	if err != nil {
		return nil, err
	}
	return c.httpClient.Do(req)
}

// Delete 发起 DELETE 请求。
func (c *HTTPClient) Delete(path string) (*http.Response, error) {
	urlStr := c.GetFullURL(path)
	req, err := c.prepareRequest(http.MethodDelete, urlStr, nil)
	if err != nil {
		return nil, err
	}
	return c.httpClient.Do(req)
}

// Do 以指定 HTTP 方法发起通用请求。
func (c *HTTPClient) Do(method, path string, body []byte) (*http.Response, error) {
	urlStr := c.GetFullURL(path)
	var bodyReader io.Reader
	if body != nil {
		bodyReader = bytes.NewReader(body)
	}
	req, err := c.prepareRequest(method, urlStr, bodyReader)
	if err != nil {
		return nil, err
	}
	return c.httpClient.Do(req)
}
// http_client.go — 可配置 HTTP 客户端与 Builder，封装 GET/POST/PUT/DELETE 及 JSON 请求。
