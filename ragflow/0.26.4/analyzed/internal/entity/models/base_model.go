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

// base_model.go — 模型驱动公共基类与 HTTP 工具：BaseURL 区域解析、SSE 流解析、JSON POST 与模型列表规范化。
//

package models

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

// BaseModel 各 SaaS 驱动的共享 HTTP 客户端与 BaseURL/URLSuffix 配置
type BaseModel struct {
	BaseURL          map[string]string
	URLSuffix        URLSuffix
	httpClient       *http.Client
	AllowEmptyAPIKey bool
}

// APIConfigCheck 校验 API 密钥非空（AllowEmptyAPIKey 时跳过）
func (b *BaseModel) APIConfigCheck(apiConfig *APIConfig) error {
	if b.AllowEmptyAPIKey {
		return nil
	}

	if apiConfig == nil || apiConfig.ApiKey == nil || strings.TrimSpace(*apiConfig.ApiKey) == "" {
		return fmt.Errorf("api key is required")
	}

	return nil
}

// BearerAuth 从 APIConfig 构造 Authorization Bearer 头；密钥为空时返回空串
func BearerAuth(apiConfig *APIConfig) string {
	if apiConfig == nil || apiConfig.ApiKey == nil {
		return ""
	}
	key := strings.TrimSpace(*apiConfig.ApiKey)
	if key == "" {
		return ""
	}
	return fmt.Sprintf("Bearer %s", key)
}

// GetBaseURL 按 region 或 apiConfig.BaseURL 解析端点基址
func (b *BaseModel) GetBaseURL(apiConfig *APIConfig) (string, error) {
	if apiConfig != nil && apiConfig.BaseURL != nil && *apiConfig.BaseURL != "" {
		return strings.TrimSuffix(*apiConfig.BaseURL, "/"), nil
	}

	region := "default"
	hasRegion := false
	if apiConfig != nil && apiConfig.Region != nil {
		hasRegion = true
		region = *apiConfig.Region
	}

	baseURL, ok := b.BaseURL[region]
	if !ok || baseURL == "" {
		if (!hasRegion || region == "") && b.BaseURL != nil {
			if defaultBaseURL, ok := b.BaseURL["default"]; ok && defaultBaseURL != "" {
				return defaultBaseURL, nil
			}
		}
		return "", fmt.Errorf("no base URL configured for region %q", region)
	}

	return baseURL, nil
}

// ParseSSEStream 解析 OpenAI 兼容 SSE 流；data: 行 JSON 解析失败时返回 invalid SSE event 错误，避免静默吞掉截断流
func ParseSSEStream[T any](r io.Reader, onEvent func(event T) error) (done bool, err error) {
	scanner := bufio.NewScanner(r)
	scanner.Buffer(make([]byte, 64*1024), 1024*1024)
	for scanner.Scan() {
		line := scanner.Text()
		if !strings.HasPrefix(line, "data:") {
			continue
		}
		data := strings.TrimSpace(line[5:])
		if data == "" {
			continue
		}
		if data == "[DONE]" {
			return true, nil
		}
		var event T
		if err := json.Unmarshal([]byte(data), &event); err != nil {
			return false, fmt.Errorf("invalid SSE event: %w", err)
		}
		if err := onEvent(event); err != nil {
			return false, err
		}
	}
	return false, scanner.Err()
}

// ParseSSEStreamTolerant 与 ParseSSEStream 类似，但跳过畸形 JSON 帧；仅用于上游偶发脏帧且测试已确认安全的驱动
func ParseSSEStreamTolerant[T any](r io.Reader, onEvent func(event T) error) (done bool, err error) {
	scanner := bufio.NewScanner(r)
	scanner.Buffer(make([]byte, 64*1024), 1024*1024)
	for scanner.Scan() {
		line := scanner.Text()
		if !strings.HasPrefix(line, "data:") {
			continue
		}
		data := strings.TrimSpace(line[5:])
		if data == "" {
			continue
		}
		if data == "[DONE]" {
			return true, nil
		}
		var event T
		if err := json.Unmarshal([]byte(data), &event); err != nil {
			continue
		}
		if err := onEvent(event); err != nil {
			return false, err
		}
	}
	return false, scanner.Err()
}

// ParseListModel 规范化 /models 列表响应；跳过空 ID，并通过 ProviderManager 补全维度/token 元数据
func ParseListModel(modelList ModelList) []ListModelResponse {
	var models []ListModelResponse
	pm := GetProviderManager()
	for _, model := range modelList.Models {
		modelName := strings.TrimSpace(model.ID)
		if modelName == "" {
			continue
		}
		var modelResponse ListModelResponse
		var modelEntity *Model
		if pm != nil {
			modelEntity = pm.GetModelByNameOrAlias(modelName)
		}
		if model.OwnedBy != "" {
			modelName = modelName + "@" + model.OwnedBy
		}
		modelResponse.Name = modelName
		if modelEntity != nil {
			modelResponse.MaxDimension = modelEntity.MaxDimension
			modelResponse.Dimensions = modelEntity.Dimensions
			modelResponse.MaxTokens = modelEntity.MaxTokens
			modelResponse.ModelTypes = modelEntity.ModelTypes
			modelResponse.Thinking = modelEntity.Thinking
			modelResponse.Dimensions = modelEntity.Dimensions
		}

		models = append(models, modelResponse)
	}
	return models
}

// NewDriverHTTPClient 返回带标准连接池与 60s 响应头超时的 HTTP 客户端
func NewDriverHTTPClient() *http.Client {
	var t *http.Transport
	if dt, ok := http.DefaultTransport.(*http.Transport); ok {
		t = dt.Clone()
	} else {
		t = &http.Transport{Proxy: http.ProxyFromEnvironment}
	}
	t.MaxIdleConns = 100
	t.MaxIdleConnsPerHost = 10
	t.IdleConnTimeout = 90 * time.Second
	t.DisableCompression = false
	t.ResponseHeaderTimeout = 60 * time.Second
	return &http.Client{Transport: t}
}

// PostJSONRequest 序列化 body 为 JSON 并发起 POST（可选 Authorization 头）
func PostJSONRequest(ctx context.Context, client *http.Client, url, auth string, body map[string]interface{}) (*http.Response, error) {
	data, err := json.Marshal(body)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewBuffer(data))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	if auth != "" {
		req.Header.Set("Authorization", auth)
	}
	return client.Do(req)
}

// ReadErrorBody 读取错误响应体全文，供日志与错误消息拼接
func ReadErrorBody(r io.Reader) string {
	b, _ := io.ReadAll(r)
	return string(b)
}

// BaseModel 被各厂商驱动嵌入；GetBaseURL 优先租户自定义 BaseURL，否则按 region 键查 BaseURL map。ParseSSEStream 遇 [DONE] 终止；NewDriverHTTPClient 克隆 DefaultTransport 并设置 MaxIdleConns=100。
