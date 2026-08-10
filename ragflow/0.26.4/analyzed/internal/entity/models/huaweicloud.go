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

// huaweicloud.go — 华为云 ModelArts/盘古 ModelDriver：多区域 Chat/Embed/Rerank，DeepSeek v4 与 thinking 开关适配。
//

package models

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/http"
	"strings"

	"github.com/goccy/go-json"
)

// HuaweiCloudModel 华为云 ModelArts ModelDriver
type HuaweiCloudModel struct {
	baseModel BaseModel
}

// NewHuaweiCloudModel 创建华为云驱动实例
func NewHuaweiCloudModel(baseURL map[string]string, urlSuffix URLSuffix) *HuaweiCloudModel {
	return &HuaweiCloudModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 HuaweiCloud 驱动实例
func (h *HuaweiCloudModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewHuaweiCloudModel(baseURL, h.baseModel.URLSuffix)
}

// Name 返回提供商标识 "huaweicloud"，供工厂层路由
func (h *HuaweiCloudModel) Name() string {
	return "huaweicloud"
}

// huaweiCloudRegion 解析 API 区域
func huaweiCloudRegion(api *APIConfig) string {
	region := "default"
	if api != nil && api.Region != nil && *api.Region != "" {
		region = *api.Region
	}
	return region
}

// huaweiCloudRegionForModel 按模型名选择区域（DeepSeek 等特化路由）
func huaweiCloudRegionForModel(api *APIConfig, modelName string) string {
	region := huaweiCloudRegion(api)
	return region
}

// huaweiCloudMessages 将 Message 转为华为云 API 格式
func huaweiCloudMessages(messages []Message) []map[string]interface{} {
	apiMessages := make([]map[string]interface{}, len(messages))
	for i, msg := range messages {
		apiMessages[i] = map[string]interface{}{
			"role":    msg.Role,
			"content": msg.Content,
		}
	}
	return apiMessages
}

// huaweiCloudIsDeepSeekV4 判断是否为 DeepSeek v4 系列模型
func huaweiCloudIsDeepSeekV4(modelName string) bool {
	model := strings.ToLower(modelName)
	return strings.Contains(model, "deepseek-v4-pro") || strings.Contains(model, "deepseek-v4-flash")
}

// huaweiCloudSupportsThinkingToggle 判断模型是否支持 thinking 开关
func huaweiCloudSupportsThinkingToggle(modelName string) bool {
	model := strings.ToLower(modelName)
	switch {
	case strings.Contains(model, "deepseek-v4-pro"),
		strings.Contains(model, "deepseek-v4-flash"),
		strings.Contains(model, "deepseek-v3.1"),
		strings.Contains(model, "deepseek-v3.2"),
		strings.Contains(model, "qwen3-235b-a22b"),
		strings.Contains(model, "qwen3-32b"),
		strings.Contains(model, "qwen3-30b-a3b"),
		strings.Contains(model, "glm-5.1"),
		strings.Contains(model, "glm-5"),
		strings.Contains(model, "kimi-k2.6"):
		return true
	default:
		return false
	}
}

// huaweiCloudUsesV1Chat 判断是否走 v1 chat 路径
func huaweiCloudUsesV1Chat(modelName string) bool {
	model := strings.ToLower(modelName)
	return model == "deepseek-v3"
}

// huaweiCloudChatModelName 映射内部模型名为华为云 API 模型 ID
func huaweiCloudChatModelName(modelName string) string {
	if huaweiCloudUsesV1Chat(modelName) {
		return strings.ToLower(modelName)
	}
	return modelName
}

// huaweiCloudAuthorization 构造 Authorization 头（Bearer 或 X-Auth-Token）
func huaweiCloudAuthorization(apiKey string) string {
	key := strings.TrimSpace(apiKey)
	if strings.HasPrefix(strings.ToLower(key), "bearer ") {
		key = strings.TrimSpace(key[len("bearer "):])
	}
	return fmt.Sprintf("Bearer %s", key)
}

// chatURL 按模型选择 chat completions 端点
func (h *HuaweiCloudModel) chatURL(baseURL, modelName string) string {
	suffix := h.baseModel.URLSuffix.Chat
	if huaweiCloudUsesV1Chat(modelName) {
		if h.baseModel.URLSuffix.AsyncChat != "" {
			suffix = h.baseModel.URLSuffix.AsyncChat
		} else {
			suffix = "v1/chat/completions"
		}
	}
	return baseURL + "/" + strings.TrimPrefix(suffix, "/")
}

// huaweiCloudApplyChatConfig 将 ChatConfig 采样/thinking 参数写入请求体
func huaweiCloudApplyChatConfig(req map[string]any, modelName string, chatModelConfig *ChatConfig) {
	if chatModelConfig == nil {
		return
	}
	if chatModelConfig.MaxTokens != nil {
		req["max_tokens"] = *chatModelConfig.MaxTokens
	}
	if chatModelConfig.Temperature != nil {
		req["temperature"] = *chatModelConfig.Temperature
	}
	if chatModelConfig.TopP != nil {
		req["top_p"] = *chatModelConfig.TopP
	}
	if chatModelConfig.Stop != nil && len(*chatModelConfig.Stop) > 0 {
		req["stop"] = *chatModelConfig.Stop
	}
	if chatModelConfig.Thinking == nil || !huaweiCloudSupportsThinkingToggle(modelName) {
		return
	}

	thinkingType := "disabled"
	if *chatModelConfig.Thinking {
		thinkingType = "enabled"
		if huaweiCloudIsDeepSeekV4(modelName) {
			effort := "high"
			if chatModelConfig.Effort != nil && *chatModelConfig.Effort != "" {
				effort = *chatModelConfig.Effort
			}
			switch strings.ToLower(effort) {
			case "none", "low", "medium":
				thinkingType = "disabled"
			case "max":
				req["reasoning_effort"] = "max"
			default:
				req["reasoning_effort"] = "high"
			}
		}
	}

	req["thinking"] = map[string]interface{}{
		"type": thinkingType,
	}
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (h *HuaweiCloudModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := h.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	baseURLRegion := huaweiCloudRegionForModel(apiConfig, modelName)
	baseURLConfig := &APIConfig{Region: &baseURLRegion}
	if apiConfig != nil {
		baseURLConfig.BaseURL = apiConfig.BaseURL
	}
	baseURL, err := h.baseModel.GetBaseURL(baseURLConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")

	url := h.chatURL(baseURL, modelName)

	reqb := map[string]interface{}{
		"model":    huaweiCloudChatModelName(modelName),
		"messages": huaweiCloudMessages(messages),
		"stream":   false,
	}
	huaweiCloudApplyChatConfig(reqb, modelName, chatModelConfig)

	jsonData, err := json.Marshal(reqb)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", huaweiCloudAuthorization(*apiConfig.ApiKey))

	rep, err := h.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer rep.Body.Close()

	body, err := io.ReadAll(rep.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if rep.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Huawei Cloud chat API error: status %d, body: %s", rep.StatusCode, string(body))
	}

	var result map[string]interface{}
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	choices, ok := result["choices"].([]interface{})
	if !ok || len(choices) == 0 {
		return nil, fmt.Errorf("no choices in response")
	}

	firstChoice, ok := choices[0].(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("invalid choice format")
	}

	messageMap, ok := firstChoice["message"].(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("invalid message format")
	}
	content, ok := messageMap["content"].(string)
	if !ok {
		return nil, fmt.Errorf("invalid content format")
	}

	reasonContent := ""
	if r, ok := messageMap["reasoning_content"].(string); ok {
		reasonContent = r
		if reasonContent != "" && reasonContent[0] == '\n' {
			reasonContent = reasonContent[1:]
		}
	}

	return &ChatResponse{
		Answer:        &content,
		ReasonContent: &reasonContent,
	}, nil
}

// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (h *HuaweiCloudModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := h.baseModel.APIConfigCheck(apiConfig); err != nil {
		return err
	}

	if sender == nil {
		return fmt.Errorf("sender is required")
	}
	if modelName == "" {
		return fmt.Errorf("model name is required")
	}
	if len(messages) == 0 {
		return fmt.Errorf("messages is empty")
	}

	baseURLRegion := huaweiCloudRegionForModel(apiConfig, modelName)
	baseURLConfig := &APIConfig{Region: &baseURLRegion}
	if apiConfig != nil {
		baseURLConfig.BaseURL = apiConfig.BaseURL
	}
	baseURL, err := h.baseModel.GetBaseURL(baseURLConfig)
	if err != nil {
		return err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := h.chatURL(baseURL, modelName)

	reqBody := map[string]interface{}{
		"model":    huaweiCloudChatModelName(modelName),
		"messages": huaweiCloudMessages(messages),
		"stream":   true,
	}
	if chatModelConfig != nil && chatModelConfig.Stream != nil && !*chatModelConfig.Stream {
		return fmt.Errorf("stream must be true in ChatStreamlyWithSender")
	}
	huaweiCloudApplyChatConfig(reqBody, modelName, chatModelConfig)

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), streamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", huaweiCloudAuthorization(*apiConfig.ApiKey))

	resp, err := h.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("Huawei Cloud stream API error: status %d, body: %s", resp.StatusCode, string(body))
	}

	sawTerminal := false
	done, err := ParseSSEStream[map[string]interface{}](resp.Body, func(event map[string]interface{}) error {
		if apiErr, ok := event["error"]; ok {
			return fmt.Errorf("huaweicloud: upstream stream error: %v", apiErr)
		}

		choices, ok := event["choices"].([]interface{})
		if !ok || len(choices) == 0 {
			return nil
		}
		firstChoice, ok := choices[0].(map[string]interface{})
		if !ok {
			return nil
		}
		delta, ok := firstChoice["delta"].(map[string]interface{})
		if !ok {
			return nil
		}

		if r, ok := delta["reasoning_content"].(string); ok && r != "" {
			if err := sender(nil, &r); err != nil {
				return err
			}
		}
		if content, ok := delta["content"].(string); ok && content != "" {
			if err := sender(&content, nil); err != nil {
				return err
			}
		}
		if finishReason, ok := firstChoice["finish_reason"].(string); ok && finishReason != "" {
			sawTerminal = true
		}
		return nil
	})
	if err != nil {
		return fmt.Errorf("failed to scan response body: %w", err)
	}
	if !done && !sawTerminal {
		return fmt.Errorf("huaweicloud: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	if err := sender(&endOfStream, nil); err != nil {
		return err
	}
	return nil
}

type huaweiCloudEmbeddingResponse struct {
	Data []struct {
		Embedding []float64 `json:"embedding"`
		Index     *int      `json:"index"`
	} `json:"data"`
}

// Embed 将文本列表编码为向量嵌入
func (h *HuaweiCloudModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := h.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}

	baseURLRegion := huaweiCloudRegion(apiConfig)
	baseURLConfig := &APIConfig{Region: &baseURLRegion}
	if apiConfig != nil {
		baseURLConfig.BaseURL = apiConfig.BaseURL
	}
	baseURL, err := h.baseModel.GetBaseURL(baseURLConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, strings.TrimPrefix(h.baseModel.URLSuffix.Embedding, "/"))

	reqBody := map[string]interface{}{
		"model":           *modelName,
		"input":           texts,
		"encoding_format": "float",
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", huaweiCloudAuthorization(*apiConfig.ApiKey))

	resp, err := h.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Huawei Cloud embedding API error: status %d, body: %s", resp.StatusCode, string(body))
	}

	var parsed huaweiCloudEmbeddingResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to decode response: %w", err)
	}
	if len(parsed.Data) != len(texts) {
		return nil, fmt.Errorf("expected %d embeddings, got %d", len(texts), len(parsed.Data))
	}

	embeddings := make([]EmbeddingData, len(texts))
	seen := make([]bool, len(texts))
	for _, item := range parsed.Data {
		if item.Index == nil {
			return nil, fmt.Errorf("missing index field in embedding item")
		}
		idx := *item.Index
		if idx < 0 || idx >= len(texts) {
			return nil, fmt.Errorf("embedding index %d out of range", idx)
		}
		if seen[idx] {
			return nil, fmt.Errorf("duplicate embedding index %d", idx)
		}
		if len(item.Embedding) == 0 {
			return nil, fmt.Errorf("empty embedding at index %d", idx)
		}
		seen[idx] = true
		embeddings[idx] = EmbeddingData{
			Embedding: item.Embedding,
			Index:     idx,
		}
	}

	for i, ok := range seen {
		if !ok {
			return nil, fmt.Errorf("missing embedding index %d", i)
		}
	}

	return embeddings, nil
}

// Rerank 对候选文档按 query 相关性重排序
func (h *HuaweiCloudModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	if err := h.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if query == "" {
		return nil, fmt.Errorf("query is required")
	}
	if len(documents) == 0 {
		return &RerankResponse{}, nil
	}

	baseURLRegion := huaweiCloudRegion(apiConfig)
	baseURLConfig := &APIConfig{Region: &baseURLRegion}
	if apiConfig != nil {
		baseURLConfig.BaseURL = apiConfig.BaseURL
	}
	baseURL, err := h.baseModel.GetBaseURL(baseURLConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, strings.TrimPrefix(h.baseModel.URLSuffix.Rerank, "/"))

	reqBody := map[string]interface{}{
		"model":     *modelName,
		"query":     query,
		"documents": documents,
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", huaweiCloudAuthorization(*apiConfig.ApiKey))

	resp, err := h.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Huawei Cloud rerank API error: status %d, body: %s", resp.StatusCode, string(body))
	}

	var parsed struct {
		Results []struct {
			Index          int     `json:"index"`
			RelevanceScore float64 `json:"relevance_score"`
		} `json:"results"`
	}
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to decode response: %w", err)
	}

	limit := len(parsed.Results)
	if rerankConfig != nil && rerankConfig.TopN > 0 && rerankConfig.TopN < limit {
		limit = rerankConfig.TopN
	}

	result := &RerankResponse{
		Data: make([]RerankResult, 0, limit),
	}
	for i := 0; i < limit; i++ {
		item := parsed.Results[i]
		if item.Index < 0 || item.Index >= len(documents) {
			return nil, fmt.Errorf("rerank index %d out of range", item.Index)
		}
		result.Data = append(result.Data, RerankResult{
			Index:          item.Index,
			RelevanceScore: item.RelevanceScore,
		})
	}

	return result, nil
}

// TranscribeAudio 语音转文字（ASR）
func (h *HuaweiCloudModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", h.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (h *HuaweiCloudModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", h.Name())
}

// AudioSpeech 文字转语音（TTS）
func (h *HuaweiCloudModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", h.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (h *HuaweiCloudModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", h.Name())
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (h *HuaweiCloudModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", h.Name())
}

// ParseFile 解析文档为结构化文本
func (h *HuaweiCloudModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", h.Name())
}

// ListModels 列出当前 API Key 可见的模型目录
func (h *HuaweiCloudModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	if err := h.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	baseURLRegion := huaweiCloudRegion(apiConfig)
	baseURLConfig := &APIConfig{Region: &baseURLRegion}
	if apiConfig != nil {
		baseURLConfig.BaseURL = apiConfig.BaseURL
	}
	baseURL, err := h.baseModel.GetBaseURL(baseURLConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, strings.TrimPrefix(h.baseModel.URLSuffix.Models, "/"))

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Authorization", huaweiCloudAuthorization(*apiConfig.ApiKey))

	resp, err := h.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Huawei Cloud models API error: status %d, body: %s", resp.StatusCode, string(body))
	}

	var parsed struct {
		Data []DSModel `json:"data"`
	}
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to decode response: %w", err)
	}

	if len(parsed.Data) == 0 {
		return nil, fmt.Errorf("no models in response")
	}
	return ParseListModel(ModelList{Models: parsed.Data}), nil
}

// Balance 查询账户余额（若上游支持）
func (h *HuaweiCloudModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s, no such method", h.Name())
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (h *HuaweiCloudModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := h.ListModels(apiConfig)
	return err
}

// ListTasks 列出异步任务状态
func (h *HuaweiCloudModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", h.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (h *HuaweiCloudModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", h.Name())
}

// 华为云驱动实现 Chat/Embed/Rerank/ListModels/CheckConnection；DeepSeek v4 与盘古系列有独立路由与 thinking 参数；ASR/TTS/OCR/ParseFile 返回不支持。
