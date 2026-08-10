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

// ppio.go — PPIO 算力平台 ModelDriver：OpenAI 兼容 chat/completions 与模型目录 endpoint 辅助函数。
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

// PPIOModel PPIO 算力平台 ModelDriver
//
// PPIO 暴露 OpenAI 兼容的 chat/completions 与模型列表端点
type PPIOModel struct {
	baseModel BaseModel
}

// NewPPIOModel 创建 PPIO 驱动实例
func NewPPIOModel(baseURL map[string]string, urlSuffix URLSuffix) *PPIOModel {
	return &PPIOModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 PPIO 驱动实例
func (p *PPIOModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewPPIOModel(baseURL, p.baseModel.URLSuffix)
}

// Name 返回提供商标识 "ppio"，供工厂层路由
func (p *PPIOModel) Name() string {
	return "ppio"
}

// endpoint 拼接 PPIO API 完整 URL（baseURL + suffix）
func (p *PPIOModel) endpoint(apiConfig *APIConfig, suffix string) (string, error) {
	baseURL, err := p.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return "", err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	return fmt.Sprintf("%s/%s", baseURL, strings.TrimPrefix(suffix, "/")), nil
}

func ppioChatPayload(modelName string, messages []Message, stream bool, chatModelConfig *ChatConfig) map[string]interface{} {
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
	}

	return reqBody
}

type ppioChatMessage struct {
	Content          string `json:"content"`
	ReasoningContent string `json:"reasoning_content"`
	Reasoning        string `json:"reasoning"`
}

type ppioChatChoice struct {
	Message      ppioChatMessage `json:"message"`
	Delta        ppioChatMessage `json:"delta"`
	FinishReason string          `json:"finish_reason"`
}

type ppioChatResponse struct {
	Choices      []ppioChatChoice `json:"choices"`
	Error        interface{}      `json:"error"`
	FinishReason string           `json:"finish_reason"`
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (p *PPIOModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := p.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if strings.TrimSpace(modelName) == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	url, err := p.endpoint(apiConfig, p.baseModel.URLSuffix.Chat)
	if err != nil {
		return nil, err
	}

	jsonData, err := json.Marshal(ppioChatPayload(modelName, messages, false, chatModelConfig))
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

	var result ppioChatResponse
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	if result.Error != nil {
		return nil, fmt.Errorf("ppio: upstream error: %v", result.Error)
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
func (p *PPIOModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
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

	url, err := p.endpoint(apiConfig, p.baseModel.URLSuffix.Chat)
	if err != nil {
		return err
	}

	jsonData, err := json.Marshal(ppioChatPayload(modelName, messages, true, chatModelConfig))
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
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))
	req.Header.Set("Accept", "text/event-stream")

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
	done, err := ParseSSEStream[ppioChatResponse](resp.Body, func(event ppioChatResponse) error {
		if event.Error != nil {
			return fmt.Errorf("ppio: upstream stream error: %v", event.Error)
		}
		if len(event.Choices) == 0 {
			return nil
		}

		choice := event.Choices[0]
		reasoning := choice.Delta.ReasoningContent
		if reasoning == "" {
			reasoning = choice.Delta.Reasoning
		}
		if reasoning != "" {
			if err := sender(nil, &reasoning); err != nil {
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
		return fmt.Errorf("ppio: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	return sender(&endOfStream, nil)
}

type ppioModelInfo struct {
	ID string `json:"id"`
}

type ppioListModelsResponse struct {
	Data  []DSModel   `json:"data"`
	Error interface{} `json:"error"`
}

// ListModels 列出当前 API Key 可见的模型目录
func (p *PPIOModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	if err := p.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	url, err := p.endpoint(apiConfig, p.baseModel.URLSuffix.Models)
	if err != nil {
		return nil, err
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
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

	var result ppioListModelsResponse
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	if result.Error != nil {
		return nil, fmt.Errorf("ppio: upstream error: %v", result.Error)
	}

	return ParseListModel(ModelList{Models: result.Data}), nil
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (p *PPIOModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := p.ListModels(apiConfig)
	return err
}

// Embed 将文本列表编码为向量嵌入
func (p *PPIOModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// Rerank 对候选文档按 query 相关性重排序
func (p *PPIOModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// Balance 查询账户余额（若上游支持）
func (p *PPIOModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// TranscribeAudio 语音转文字（ASR）
func (p *PPIOModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (p *PPIOModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", p.Name())
}

// AudioSpeech 文字转语音（TTS）
func (p *PPIOModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (p *PPIOModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", p.Name())
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (p *PPIOModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// ParseFile 解析文档为结构化文本
func (p *PPIOModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// ListTasks 列出异步任务状态
func (p *PPIOModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (p *PPIOModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", p.Name())
}

// PPIO 驱动实现 Chat/ListModels/CheckConnection；Embed/Rerank/ASR/TTS/OCR/ParseFile/Balance 返回不支持。
