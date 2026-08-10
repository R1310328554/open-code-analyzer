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

// futurmix.go — FuturMix OpenAI 兼容网关 ModelDriver：Chat 非流式/流式，Embed/Rerank/ListModels 未公开暴露。
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

// FuturMixModel FuturMix 聚合网关 ModelDriver
type FuturMixModel struct {
	baseModel BaseModel
}

// NewFuturMixModel 创建 FuturMix 驱动实例
func NewFuturMixModel(baseURL map[string]string, urlSuffix URLSuffix) *FuturMixModel {
	return &FuturMixModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 FuturMix 驱动实例
func (f *FuturMixModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewFuturMixModel(baseURL, f.baseModel.URLSuffix)
}

// Name 返回提供商标识 "futurmix"，供工厂层路由
func (f *FuturMixModel) Name() string {
	return "futurmix"
}

// endpointURL 按 region 拼接 FuturMix 端点
func (f *FuturMixModel) endpointURL(region, suffix string) (string, error) {
	baseURL, err := f.baseModel.GetBaseURL(&APIConfig{Region: &region})
	if err != nil {
		return "", err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	return fmt.Sprintf("%s/%s", baseURL, strings.TrimLeft(suffix, "/")), nil
}

// futurmixRegion 解析 API 区域
func futurmixRegion(apiConfig *APIConfig) string {
	if apiConfig != nil && apiConfig.Region != nil && *apiConfig.Region != "" {
		return *apiConfig.Region
	}
	return "default"
}

func newFuturMixJSONRequest(ctx context.Context, method, endpoint string, payload interface{}, apiKey string) (*http.Request, error) {
	var body io.Reader
	if payload != nil {
		jsonData, err := json.Marshal(payload)
		if err != nil {
			return nil, fmt.Errorf("failed to marshal request: %w", err)
		}
		body = bytes.NewBuffer(jsonData)
	}

	req, err := http.NewRequestWithContext(ctx, method, endpoint, body)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	if apiKey != "" {
		req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", apiKey))
	}
	return req, nil
}

type futurmixAPIMessage struct {
	Role    string      `json:"role"`
	Content interface{} `json:"content"`
}

type futurmixChatRequest struct {
	Model       string               `json:"model"`
	Messages    []futurmixAPIMessage `json:"messages"`
	Stream      bool                 `json:"stream"`
	MaxTokens   *int                 `json:"max_tokens,omitempty"`
	Temperature *float64             `json:"temperature,omitempty"`
	TopP        *float64             `json:"top_p,omitempty"`
	Stop        *[]string            `json:"stop,omitempty"`
}

// buildFuturMixChatRequest 组装 OpenAI 兼容 chat 请求
func buildFuturMixChatRequest(modelName string, messages []Message, stream bool, chatModelConfig *ChatConfig) futurmixChatRequest {
	apiMessages := make([]futurmixAPIMessage, len(messages))
	for i, msg := range messages {
		apiMessages[i] = futurmixAPIMessage{
			Role:    msg.Role,
			Content: msg.Content,
		}
	}
	reqBody := futurmixChatRequest{
		Model:    modelName,
		Messages: apiMessages,
		Stream:   stream,
	}
	if chatModelConfig != nil {
		reqBody.MaxTokens = chatModelConfig.MaxTokens
		reqBody.Temperature = chatModelConfig.Temperature
		reqBody.TopP = chatModelConfig.TopP
		reqBody.Stop = chatModelConfig.Stop
	}
	return reqBody
}

type futurmixChatChoice struct {
	Message      futurmixChatMessage `json:"message"`
	Delta        futurmixChatDelta   `json:"delta"`
	FinishReason string              `json:"finish_reason"`
}

type futurmixChatMessage struct {
	Content          *string `json:"content"`
	ReasoningContent string  `json:"reasoning_content"`
}

type futurmixChatDelta struct {
	Content          string `json:"content"`
	ReasoningContent string `json:"reasoning_content"`
}

type futurmixChatResponse struct {
	Choices []futurmixChatChoice `json:"choices"`
}

// ChatWithMessages 非流式 chat/completions
// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (f *FuturMixModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := f.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	apiKey := *apiConfig.ApiKey
	if strings.TrimSpace(modelName) == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	endpoint, err := f.endpointURL(futurmixRegion(apiConfig), f.baseModel.URLSuffix.Chat)
	if err != nil {
		return nil, err
	}

	reqBody := buildFuturMixChatRequest(modelName, messages, false, chatModelConfig)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := newFuturMixJSONRequest(ctx, "POST", endpoint, reqBody, apiKey)
	if err != nil {
		return nil, err
	}

	resp, err := f.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("futurmix chat API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed futurmixChatResponse
	if err := json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	if len(parsed.Choices) == 0 {
		return nil, fmt.Errorf("no choices in response")
	}
	if parsed.Choices[0].Message.Content == nil {
		return nil, fmt.Errorf("invalid content format")
	}

	content := *parsed.Choices[0].Message.Content
	reasonContent := parsed.Choices[0].Message.ReasoningContent
	return &ChatResponse{
		Answer:        &content,
		ReasonContent: &reasonContent,
	}, nil
}

// ChatStreamlyWithSender 流式 chat/completions
// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (f *FuturMixModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := f.baseModel.APIConfigCheck(apiConfig); err != nil {
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
	apiKey := *apiConfig.ApiKey

	endpoint, err := f.endpointURL(futurmixRegion(apiConfig), f.baseModel.URLSuffix.Chat)
	if err != nil {
		return err
	}

	if chatModelConfig != nil && chatModelConfig.Stream != nil && !*chatModelConfig.Stream {
		return fmt.Errorf("stream must be true in ChatStreamlyWithSender")
	}

	reqBody := buildFuturMixChatRequest(modelName, messages, true, chatModelConfig)

	req, err := newFuturMixJSONRequest(context.Background(), "POST", endpoint, reqBody, apiKey)
	if err != nil {
		return err
	}

	resp, err := f.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("futurmix chat stream API error: %s, body: %s", resp.Status, string(body))
	}

	sawTerminal := false
	done, err := ParseSSEStream[futurmixChatResponse](resp.Body, func(event futurmixChatResponse) error {
		if len(event.Choices) == 0 {
			return nil
		}
		choice := event.Choices[0]
		if choice.Delta.ReasoningContent != "" {
			r := choice.Delta.ReasoningContent
			if err := sender(nil, &r); err != nil {
				return err
			}
		}
		if choice.Delta.Content != "" {
			c := choice.Delta.Content
			if err := sender(&c, nil); err != nil {
				return err
			}
		}
		if choice.FinishReason != "" {
			sawTerminal = true
		}
		return nil
	})
	if err != nil {
		return fmt.Errorf("failed to scan response body: %w", err)
	}
	if !done && !sawTerminal {
		return fmt.Errorf("futurmix: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	return sender(&endOfStream, nil)
}

// Embed FuturMix 公开文档未暴露 embed API
// Embed 将文本列表编码为向量嵌入
func (f *FuturMixModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// Rerank FuturMix 公开文档未暴露 rerank API
// Rerank 对候选文档按 query 相关性重排序
func (f *FuturMixModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// ListModels FuturMix 未文档化 models 列表端点
// ListModels 列出当前 API Key 可见的模型目录
func (f *FuturMixModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// CheckConnection is not exposed by the FuturMix API.
// CheckConnection 轻量探活，验证密钥与端点可用
func (f *FuturMixModel) CheckConnection(apiConfig *APIConfig) error {
	return fmt.Errorf("%s, no such method", f.Name())
}

// Balance is not exposed by the FuturMix public API.
// Balance 查询账户余额（若上游支持）
func (f *FuturMixModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// TranscribeAudio is not exposed by the FuturMix API per the docs.
// TranscribeAudio 语音转文字（ASR）
func (f *FuturMixModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (f *FuturMixModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", f.Name())
}

// AudioSpeech is not exposed by the FuturMix API per the docs.
// AudioSpeech 文字转语音（TTS）
func (f *FuturMixModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (f *FuturMixModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", f.Name())
}

// OCRFile is not exposed by the FuturMix API per the docs.
// OCRFile 对图片/PDF 执行 OCR 识别
func (f *FuturMixModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// ParseFile is not exposed by the FuturMix API per the docs.
// ParseFile 解析文档为结构化文本
func (f *FuturMixModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// ListTasks is not exposed by the FuturMix API per the docs.
// ListTasks 列出异步任务状态
func (f *FuturMixModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// ShowTask is not exposed by the FuturMix API per the docs.
// ShowTask 按 taskID 查询单个异步任务详情
func (f *FuturMixModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", f.Name())
}

// FuturMix 线协议与 OpenAI 兼容；Bearer 鉴权；SSE 流以 [DONE] 终止。Embed/Rerank/ListModels 返回不支持；ASR/TTS/OCR 同理。
