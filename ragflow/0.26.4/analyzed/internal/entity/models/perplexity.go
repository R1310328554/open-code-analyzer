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

// perplexity.go — Perplexity AI ModelDriver：OpenAI 兼容 Chat/Embed，chatPayload/chatURL 辅助构建请求。
//

package models

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strings"
)

// PerplexityModel Perplexity AI 搜索增强 ModelDriver
type PerplexityModel struct {
	baseModel BaseModel
}

// NewPerplexityModel 创建 Perplexity 驱动实例
func NewPerplexityModel(baseURL map[string]string, urlSuffix URLSuffix) *PerplexityModel {
	return &PerplexityModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 Perplexity 驱动实例
func (p *PerplexityModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewPerplexityModel(baseURL, p.baseModel.URLSuffix)
}

// Name 返回提供商标识 "perplexity"，供工厂层路由
func (p *PerplexityModel) Name() string {
	return "perplexity"
}

// chatPayload 构建 Perplexity chat 请求体（含 stream 与采样参数）
func (p *PerplexityModel) chatPayload(modelName string, messages []Message, stream bool, chatModelConfig *ChatConfig) map[string]interface{} {
	apiMessages := make([]map[string]interface{}, len(messages))
	for i, msg := range messages {
		apiMessages[i] = map[string]interface{}{
			"role":    msg.Role,
			"content": msg.Content,
		}
	}

	reqBody := map[string]interface{}{
		"model":    modelName,
		"messages": apiMessages,
		"stream":   stream,
	}

	if chatModelConfig != nil {
		if chatModelConfig.MaxTokens != nil {
			reqBody["max_tokens"] = *chatModelConfig.MaxTokens
		}
		if chatModelConfig.Temperature != nil {
			reqBody["temperature"] = *chatModelConfig.Temperature
		}
		if chatModelConfig.TopP != nil {
			reqBody["top_p"] = *chatModelConfig.TopP
		}
		if chatModelConfig.Stop != nil {
			reqBody["stop"] = *chatModelConfig.Stop
		}
		// Perplexity sonar-reasoning* models accept reasoning_effort.
		if chatModelConfig.Effort != nil && strings.Contains(strings.ToLower(modelName), "reasoning") {
			reqBody["reasoning_effort"] = *chatModelConfig.Effort
		}
	}

	return reqBody
}

// chatURL 解析 Perplexity chat/completions 完整 URL
func (p *PerplexityModel) chatURL(apiConfig *APIConfig) (string, error) {

	baseURL, err := p.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return "", err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	return fmt.Sprintf("%s/%s", baseURL, p.baseModel.URLSuffix.Chat), nil
}

type perplexityChatMessage struct {
	Content          string `json:"content"`
	ReasoningContent string `json:"reasoning_content"`
	Reasoning        string `json:"reasoning"`
}

type perplexityChatChoice struct {
	Message      perplexityChatMessage `json:"message"`
	Delta        perplexityChatMessage `json:"delta"`
	FinishReason string                `json:"finish_reason"`
}

type perplexityChatResponse struct {
	Choices      []perplexityChatChoice `json:"choices"`
	Error        interface{}            `json:"error"`
	FinishReason string                 `json:"finish_reason"`
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (p *PerplexityModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := p.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if strings.TrimSpace(modelName) == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	url, err := p.chatURL(apiConfig)
	if err != nil {
		return nil, err
	}

	jsonData, err := json.Marshal(p.chatPayload(modelName, messages, false, chatModelConfig))
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
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := p.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	var result perplexityChatResponse
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	if result.Error != nil {
		return nil, fmt.Errorf("perplexity: upstream error: %v", result.Error)
	}
	if len(result.Choices) == 0 {
		return nil, fmt.Errorf("no choices in response")
	}

	content := result.Choices[0].Message.Content
	reasonContent := result.Choices[0].Message.ReasoningContent
	if reasonContent == "" {
		reasonContent = result.Choices[0].Message.Reasoning
	}
	return &ChatResponse{
		Answer:        &content,
		ReasonContent: &reasonContent,
	}, nil
}

// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (p *PerplexityModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := p.baseModel.APIConfigCheck(apiConfig); err != nil {
		return err
	}

	if sender == nil {
		return fmt.Errorf("sender is required")
	}
	if strings.TrimSpace(modelName) == "" {
		return fmt.Errorf("model name is required")
	}
	if len(messages) == 0 {
		return fmt.Errorf("messages is empty")
	}
	if chatModelConfig != nil && chatModelConfig.Stream != nil && !*chatModelConfig.Stream {
		return fmt.Errorf("stream must be true in ChatStreamlyWithSender")
	}

	url, err := p.chatURL(apiConfig)
	if err != nil {
		return err
	}

	jsonData, err := json.Marshal(p.chatPayload(modelName, messages, true, chatModelConfig))
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	// ResponseHeaderTimeout caps the initial header wait. This context
	// also caps the body-read phase so a stalled SSE stream cannot hold
	// the caller's goroutine and connection indefinitely.
	ctx, cancel := context.WithTimeout(context.Background(), streamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Accept", "text/event-stream")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := p.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	sawTerminal := false
	done, err := ParseSSEStream[perplexityChatResponse](resp.Body, func(event perplexityChatResponse) error {
		if event.Error != nil {
			return fmt.Errorf("perplexity: upstream stream error: %v", event.Error)
		}
		if len(event.Choices) == 0 {
			return nil
		}

		choice := event.Choices[0]
		if choice.Delta.ReasoningContent != "" {
			if err := sender(nil, &choice.Delta.ReasoningContent); err != nil {
				return err
			}
		}
		if choice.Delta.Reasoning != "" {
			if err := sender(nil, &choice.Delta.Reasoning); err != nil {
				return err
			}
		}
		if choice.Delta.Content != "" {
			if err := sender(&choice.Delta.Content, nil); err != nil {
				return err
			}
		}
		if choice.FinishReason != "" || event.FinishReason != "" {
			sawTerminal = true
		}
		return nil
	})
	if err != nil {
		return fmt.Errorf("failed to scan response body: %w", err)
	}
	if !done && !sawTerminal {
		return fmt.Errorf("perplexity: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	return sender(&endOfStream, nil)
}

type perplexityModelInfo struct {
	ID string `json:"id"`
}

type perplexityModelListResponse struct {
	Data []DSModel `json:"data"`
}

// ListModels 列出当前 API Key 可见的模型目录
func (p *PerplexityModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	if err := p.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	baseURL, err := p.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, p.baseModel.URLSuffix.Models)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := p.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	// Perplexity returns OpenAI-style {"data": [{"id": "..."}]}. Older
	// or alternate payloads may return a bare array; accept both.
	var wrapped perplexityModelListResponse
	if err = json.Unmarshal(body, &wrapped); err == nil && len(wrapped.Data) > 0 {
		return ParseListModel(ModelList{Models: wrapped.Data}), nil
	}

	var bare []DSModel
	if err = json.Unmarshal(body, &bare); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	return ParseListModel(ModelList{Models: bare}), nil
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (p *PerplexityModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := p.ListModels(apiConfig)
	return err
}

type perplexityEmbeddingDataItem struct {
	Embedding []float64 `json:"embedding"`
	Index     int       `json:"index"`
	Object    string    `json:"object"`
}

type perplexityEmbeddingResponse struct {
	Data  []perplexityEmbeddingDataItem `json:"data"`
	Model string                        `json:"model"`
	Error interface{}                   `json:"error"`
}

// Embed 将文本列表编码为向量嵌入
func (p *PerplexityModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := p.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}
	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	baseURL, err := p.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", baseURL, p.baseModel.URLSuffix.Embedding)

	reqBody := map[string]interface{}{
		"model": *modelName,
		"input": texts,
	}
	if embeddingConfig != nil && embeddingConfig.Dimension > 0 {
		reqBody["dimensions"] = embeddingConfig.Dimension
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
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := p.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("perplexity embeddings API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed perplexityEmbeddingResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	if parsed.Error != nil {
		return nil, fmt.Errorf("perplexity: upstream error: %v", parsed.Error)
	}

	embeddings := make([]EmbeddingData, 0, len(parsed.Data))
	for _, item := range parsed.Data {
		embeddings = append(embeddings, EmbeddingData{
			Embedding: item.Embedding,
			Index:     item.Index,
		})
	}
	return embeddings, nil
}

// Rerank 对候选文档按 query 相关性重排序
func (p *PerplexityModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// Balance 查询账户余额（若上游支持）
func (p *PerplexityModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// TranscribeAudio 语音转文字（ASR）
func (p *PerplexityModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (p *PerplexityModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", p.Name())
}

// AudioSpeech 文字转语音（TTS）
func (p *PerplexityModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (p *PerplexityModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", p.Name())
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (p *PerplexityModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// ParseFile 解析文档为结构化文本
func (p *PerplexityModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// ListTasks 列出异步任务状态
func (p *PerplexityModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (p *PerplexityModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// Perplexity 驱动实现 Chat/Embed/ListModels/CheckConnection；Rerank/ASR/TTS/OCR/ParseFile/Balance 返回不支持。
