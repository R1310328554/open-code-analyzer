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

// n1n.go — n1n.ai 聚合网关 ModelDriver：OpenAI 兼容 Chat/Embed/Rerank/ASR/TTS，Cohere 形态 rerank 与异步任务 API。
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

// N1NModel n1n.ai 聚合网关 ModelDriver
type N1NModel struct {
	baseModel BaseModel
}

// NewN1NModel 创建 n1n.ai 驱动实例
func NewN1NModel(baseURL map[string]string, urlSuffix URLSuffix) *N1NModel {
	return &N1NModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 N1N 驱动实例
func (n *N1NModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewN1NModel(baseURL, n.baseModel.URLSuffix)
}

// Name 返回提供商标识 "n1n"，供工厂层路由
func (n *N1NModel) Name() string {
	return "n1n"
}

func (n *N1NModel) endpointURL(region, suffix string) (string, error) {
	baseURL, err := n.baseModel.GetBaseURL(&APIConfig{Region: &region})
	if err != nil {
		return "", err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	return fmt.Sprintf("%s/%s", baseURL, strings.TrimLeft(suffix, "/")), nil
}

func n1nRegion(apiConfig *APIConfig) string {
	if apiConfig != nil && apiConfig.Region != nil && *apiConfig.Region != "" {
		return *apiConfig.Region
	}
	return "default"
}

func newN1NJSONRequest(ctx context.Context, method, endpoint string, payload interface{}, apiKey string) (*http.Request, error) {
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

type n1nAPIMessage struct {
	Role    string      `json:"role"`
	Content interface{} `json:"content"`
}

type n1nThinking struct {
	Type string `json:"type"`
}

type n1nChatRequest struct {
	Model       string          `json:"model"`
	Messages    []n1nAPIMessage `json:"messages"`
	Stream      bool            `json:"stream"`
	MaxTokens   *int            `json:"max_tokens,omitempty"`
	Temperature *float64        `json:"temperature,omitempty"`
	TopP        *float64        `json:"top_p,omitempty"`
	Stop        *[]string       `json:"stop,omitempty"`
	Thinking    *n1nThinking    `json:"thinking,omitempty"`
}

func buildN1NChatRequest(modelName string, messages []Message, stream bool, chatModelConfig *ChatConfig) n1nChatRequest {
	apiMessages := make([]n1nAPIMessage, len(messages))
	for i, msg := range messages {
		apiMessages[i] = n1nAPIMessage{
			Role:    msg.Role,
			Content: msg.Content,
		}
	}
	reqBody := n1nChatRequest{
		Model:    modelName,
		Messages: apiMessages,
		Stream:   stream,
	}
	if chatModelConfig != nil {
		reqBody.MaxTokens = chatModelConfig.MaxTokens
		reqBody.Temperature = chatModelConfig.Temperature
		reqBody.TopP = chatModelConfig.TopP
		reqBody.Stop = chatModelConfig.Stop
		if chatModelConfig.Thinking != nil {
			if *chatModelConfig.Thinking {
				reqBody.Thinking = &n1nThinking{Type: "enabled"}
			} else {
				reqBody.Thinking = &n1nThinking{Type: "disabled"}
			}
		}
	}
	return reqBody
}

type n1nChatChoice struct {
	Message      n1nChatMessage `json:"message"`
	Delta        n1nChatDelta   `json:"delta"`
	FinishReason string         `json:"finish_reason"`
}

type n1nChatMessage struct {
	Content          *string `json:"content"`
	ReasoningContent string  `json:"reasoning_content"`
}

type n1nChatDelta struct {
	Content          string `json:"content"`
	ReasoningContent string `json:"reasoning_content"`
}

type n1nChatResponse struct {
	Choices []n1nChatChoice `json:"choices"`
}

// ChatWithMessages 非流式 POST /v1/chat/completions
// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (n *N1NModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	apiKey := *apiConfig.ApiKey
	if strings.TrimSpace(modelName) == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	endpoint, err := n.endpointURL(n1nRegion(apiConfig), n.baseModel.URLSuffix.Chat)
	if err != nil {
		return nil, err
	}

	reqBody := buildN1NChatRequest(modelName, messages, false, chatModelConfig)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := newN1NJSONRequest(ctx, "POST", endpoint, reqBody, apiKey)
	if err != nil {
		return nil, err
	}

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("n1n chat API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed n1nChatResponse
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
	chatResp := &ChatResponse{
		Answer: &content,
	}
	if parsed.Choices[0].Message.ReasoningContent != "" {
		reasonContent := parsed.Choices[0].Message.ReasoningContent
		chatResp.ReasonContent = &reasonContent
	}
	return chatResp, nil
}

// ChatStreamlyWithSender 流式 chat/completions
// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (n *N1NModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
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

	endpoint, err := n.endpointURL(n1nRegion(apiConfig), n.baseModel.URLSuffix.Chat)
	if err != nil {
		return err
	}

	if chatModelConfig != nil && chatModelConfig.Stream != nil && !*chatModelConfig.Stream {
		return fmt.Errorf("stream must be true in ChatStreamlyWithSender")
	}

	reqBody := buildN1NChatRequest(modelName, messages, true, chatModelConfig)

	req, err := newN1NJSONRequest(context.Background(), "POST", endpoint, reqBody, apiKey)
	if err != nil {
		return err
	}

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("n1n chat stream API error: %s, body: %s", resp.Status, string(body))
	}

	sawTerminal := false
	done, err := ParseSSEStream[n1nChatResponse](resp.Body, func(event n1nChatResponse) error {
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
		return fmt.Errorf("n1n: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	if err := sender(&endOfStream, nil); err != nil {
		return err
	}
	return nil
}

type n1nEmbeddingData struct {
	Embedding []float64 `json:"embedding"`
	Object    string    `json:"object"`
	Index     int       `json:"index"`
}

type n1nEmbeddingResponse struct {
	Data  []n1nEmbeddingData `json:"data"`
	Model string             `json:"model"`
}

type n1nEmbeddingRequest struct {
	Model      string   `json:"model"`
	Input      []string `json:"input"`
	Dimensions int      `json:"dimensions,omitempty"`
}

// Embed 批量文本向量化
// Embed 将文本列表编码为向量嵌入
func (n *N1NModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}
	apiKey := *apiConfig.ApiKey
	if modelName == nil || strings.TrimSpace(*modelName) == "" {
		return nil, fmt.Errorf("model name is required")
	}

	endpoint, err := n.endpointURL(n1nRegion(apiConfig), n.baseModel.URLSuffix.Embedding)
	if err != nil {
		return nil, err
	}

	reqBody := n1nEmbeddingRequest{
		Model: *modelName,
		Input: texts,
	}
	if embeddingConfig != nil && embeddingConfig.Dimension > 0 {
		reqBody.Dimensions = embeddingConfig.Dimension
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := newN1NJSONRequest(ctx, "POST", endpoint, reqBody, apiKey)
	if err != nil {
		return nil, err
	}

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("n1n embeddings API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed n1nEmbeddingResponse
	if err := json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	embeddings := make([]EmbeddingData, len(texts))
	filled := make([]bool, len(texts))
	for _, item := range parsed.Data {
		if item.Index < 0 || item.Index >= len(texts) {
			return nil, fmt.Errorf("n1n: embedding response index %d out of range for %d inputs", item.Index, len(texts))
		}
		if filled[item.Index] {
			return nil, fmt.Errorf("n1n: duplicate embedding index %d in response", item.Index)
		}
		embeddings[item.Index] = EmbeddingData{
			Embedding: item.Embedding,
			Index:     item.Index,
		}
		filled[item.Index] = true
	}
	for i, ok := range filled {
		if !ok {
			return nil, fmt.Errorf("n1n: missing embedding for input index %d", i)
		}
	}
	return embeddings, nil
}

type n1nRerankResult struct {
	Index          int     `json:"index"`
	RelevanceScore float64 `json:"relevance_score"`
}

type n1nRerankResponse struct {
	Results []n1nRerankResult `json:"results"`
}

type n1nRerankRequest struct {
	Model     string   `json:"model"`
	Query     string   `json:"query"`
	Documents []string `json:"documents"`
	TopN      int      `json:"top_n,omitempty"`
}

// Rerank 调用 /v1/rerank（Cohere 形态响应）对文档打分
// Rerank 对候选文档按 query 相关性重排序
func (n *N1NModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(documents) == 0 {
		return &RerankResponse{}, nil
	}
	apiKey := *apiConfig.ApiKey
	if modelName == nil || strings.TrimSpace(*modelName) == "" {
		return nil, fmt.Errorf("model name is required")
	}

	endpoint, err := n.endpointURL(n1nRegion(apiConfig), n.baseModel.URLSuffix.Rerank)
	if err != nil {
		return nil, err
	}

	reqBody := n1nRerankRequest{
		Model:     *modelName,
		Query:     query,
		Documents: documents,
	}
	if rerankConfig != nil && rerankConfig.TopN > 0 {
		reqBody.TopN = rerankConfig.TopN
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := newN1NJSONRequest(ctx, "POST", endpoint, reqBody, apiKey)
	if err != nil {
		return nil, err
	}

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("n1n rerank API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed n1nRerankResponse
	if err := json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	rerankResponse := &RerankResponse{}
	seen := make(map[int]bool, len(parsed.Results))
	for _, r := range parsed.Results {
		if r.Index < 0 || r.Index >= len(documents) {
			return nil, fmt.Errorf("n1n: rerank result index %d out of range for %d documents", r.Index, len(documents))
		}
		if seen[r.Index] {
			return nil, fmt.Errorf("n1n: duplicate rerank index %d in response", r.Index)
		}
		seen[r.Index] = true
		rerankResponse.Data = append(rerankResponse.Data, RerankResult{
			Index:          r.Index,
			RelevanceScore: r.RelevanceScore,
		})
	}
	return rerankResponse, nil
}

type n1nModelCatalogItem struct {
	ID string `json:"id"`
}

type n1nModelCatalogResponse struct {
	Data []DSModel `json:"data"`
}

// ListModels 返回 n1n.ai 实时模型目录
// ListModels 列出当前 API Key 可见的模型目录
func (n *N1NModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	if err := n.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	apiKey := *apiConfig.ApiKey

	endpoint, err := n.endpointURL(n1nRegion(apiConfig), n.baseModel.URLSuffix.Models)
	if err != nil {
		return nil, err
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := newN1NJSONRequest(ctx, "GET", endpoint, nil, apiKey)
	if err != nil {
		return nil, err
	}

	resp, err := n.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("n1n models API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed n1nModelCatalogResponse
	if err := json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return ParseListModel(ModelList{Models: parsed.Data}), nil
}

// CheckConnection 验证 API Key 与端点可用
// CheckConnection 轻量探活，验证密钥与端点可用
func (n *N1NModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := n.ListModels(apiConfig)
	return err
}

// Balance 查询账户余额（若上游支持）
func (n *N1NModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// TranscribeAudio 调用 /v1/audio/transcriptions 语音识别
// TranscribeAudio 语音转文字（ASR）
func (n *N1NModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (n *N1NModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", n.Name())
}

// AudioSpeech 调用 /v1/audio/speech 文字转语音
// AudioSpeech 文字转语音（TTS）
func (n *N1NModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (n *N1NModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", n.Name())
}

// OCRFile n1n.ai 未暴露 OCR API
// OCRFile 对图片/PDF 执行 OCR 识别
func (n *N1NModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// ParseFile n1n.ai 未暴露文档解析 API
// ParseFile 解析文档为结构化文本
func (n *N1NModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// ListTasks 列出 /v1/contents/generations/tasks 异步任务
// ListTasks 列出异步任务状态
func (n *N1NModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (n *N1NModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", n.Name())
}

// n1n.ai 驱动覆盖 Chat/Embed/Rerank/ASR/TTS/ListModels/ListTasks/ShowTask；Rerank 响应为 Cohere 形态；Balance 查询账户余额。OCR/ParseFile 返回不支持。
