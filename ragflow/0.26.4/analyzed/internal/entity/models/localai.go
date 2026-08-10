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

// localai.go — LocalAI 自托管推理 ModelDriver：OpenAI 兼容 Chat/Embed/Rerank/ASR/TTS，SSE 流 idle 超时与 reasoning 字段解析。
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
	"sync"
	"time"
)

// localAIStreamIdleTimeout 限制 ChatStreamlyWithSender 流式 idle 超时（秒）
var localAIStreamIdleTimeout = 60 * time.Second

// LocalAIModel LocalAI 自托管 ModelDriver
type LocalAIModel struct {
	baseModel BaseModel
}

// NewLocalAIModel 创建 LocalAI 驱动（AllowEmptyAPIKey）
func NewLocalAIModel(baseURL map[string]string, urlSuffix URLSuffix) *LocalAIModel {
	return &LocalAIModel{
		baseModel: BaseModel{
			BaseURL:          baseURL,
			URLSuffix:        urlSuffix,
			AllowEmptyAPIKey: true,
			httpClient:       NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 LocalAI 驱动实例
func (l *LocalAIModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewLocalAIModel(baseURL, l.baseModel.URLSuffix)
}

// Name 返回提供商标识 "localai"，供工厂层路由
func (l *LocalAIModel) Name() string {
	return "localai"
}

var localAIReasoningFields = []string{"reasoning_content", "reasoning", "thinking"}

// extractLocalAIReasoning 按优先级从 message 提取思维链字段
func extractLocalAIReasoning(m map[string]interface{}) string {
	for _, k := range localAIReasoningFields {
		if v, ok := m[k].(string); ok && v != "" {
			return v
		}
	}
	return ""
}

func addLocalAIReasoningRequestParams(reqBody map[string]interface{}, cfg *ChatConfig) {
	if cfg == nil {
		return
	}
	if cfg.Effort != nil && *cfg.Effort != "" {
		reqBody["reasoning_effort"] = *cfg.Effort
	}
	if cfg.Thinking != nil {
		reqBody["enable_thinking"] = *cfg.Thinking
	}
}

// ChatWithMessages 非流式 chat/completions，解析 reasoning 字段为 ReasonContent
// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (l *LocalAIModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := l.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	baseURL, err := l.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), l.baseModel.URLSuffix.Chat)

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
		"stream":   false,
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
		addLocalAIReasoningRequestParams(reqBody, chatModelConfig)
	}
	jsonData, err := json.Marshal(reqBody)
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
	if auth := BearerAuth(apiConfig); auth != "" {
		req.Header.Set("Authorization", auth)
	}

	resp, err := l.baseModel.httpClient.Do(req)
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

	// Pull the chain-of-thought from whichever field the upstream model
	// used. See localAIReasoningFields for the priority order.
	reasonContent := extractLocalAIReasoning(messageMap)

	return &ChatResponse{
		Answer:        &content,
		ReasonContent: &reasonContent,
	}, nil
}

// ChatStreamlyWithSender 流式 SSE（OpenAI 形态 data:/[DONE]），经 sender 推送 delta
// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (l *LocalAIModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := l.baseModel.APIConfigCheck(apiConfig); err != nil {
		return err
	}

	if sender == nil {
		return fmt.Errorf("sender is required")
	}

	if len(messages) == 0 {
		return fmt.Errorf("messages is empty")
	}

	baseURL, err := l.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return err
	}
	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), l.baseModel.URLSuffix.Chat)

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
		"stream":   true,
	}

	if chatModelConfig != nil {
		if chatModelConfig.Stream != nil && !*chatModelConfig.Stream {
			return fmt.Errorf("stream must be true in ChatStreamlyWithSender")
		}

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
		addLocalAIReasoningRequestParams(reqBody, chatModelConfig)
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	if auth := BearerAuth(apiConfig); auth != "" {
		req.Header.Set("Authorization", auth)
	}

	resp, err := l.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	lastActive := time.Now()
	var lastActiveMu sync.Mutex
	done := make(chan struct{})
	defer close(done)
	go func() {
		ticker := time.NewTicker(localAIStreamIdleTimeout / 4)
		defer ticker.Stop()
		for {
			select {
			case <-done:
				return
			case now := <-ticker.C:
				lastActiveMu.Lock()
				idle := now.Sub(lastActive)
				lastActiveMu.Unlock()
				if idle >= localAIStreamIdleTimeout {
					cancel()
					return
				}
			}
		}
	}()

	sawTerminal := false
	streamDone, err := ParseSSEStream[map[string]interface{}](resp.Body, func(event map[string]interface{}) error {
		lastActiveMu.Lock()
		lastActive = time.Now()
		lastActiveMu.Unlock()

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

		if reasoning := extractLocalAIReasoning(delta); reasoning != "" {
			if err := sender(nil, &reasoning); err != nil {
				return err
			}
		}

		content, ok := delta["content"].(string)
		if ok && content != "" {
			if err := sender(&content, nil); err != nil {
				return err
			}
		}

		finishReason, ok := firstChoice["finish_reason"].(string)
		if ok && finishReason != "" {
			sawTerminal = true
		}
		return nil
	})
	if err != nil {
		if ctx.Err() != nil {
			return fmt.Errorf("localai: stream idle for more than %s, aborted", localAIStreamIdleTimeout)
		}
		return fmt.Errorf("failed to scan response body: %w", err)
	}
	if !streamDone && !sawTerminal {
		return fmt.Errorf("localai: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	if err := sender(&endOfStream, nil); err != nil {
		return err
	}

	return nil
}

type localAIEmbeddingData struct {
	Embedding []float64 `json:"embedding"`
	Object    string    `json:"object"`
	Index     int       `json:"index"`
}

type localAIEmbeddingResponse struct {
	Data   []localAIEmbeddingData `json:"data"`
	Model  string                 `json:"model"`
	Object string                 `json:"object"`
}

// Embed 将文本列表编码为向量嵌入
func (l *LocalAIModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := l.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}

	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	baseURL, err := l.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), l.baseModel.URLSuffix.Embedding)

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

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	if auth := BearerAuth(apiConfig); auth != "" {
		req.Header.Set("Authorization", auth)
	}

	resp, err := l.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("LocalAI embeddings API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed localAIEmbeddingResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	embeddings := make([]EmbeddingData, len(texts))
	filled := make([]bool, len(texts))
	for _, item := range parsed.Data {
		if item.Index < 0 || item.Index >= len(texts) {
			return nil, fmt.Errorf("localai: response index %d out of range for %d inputs", item.Index, len(texts))
		}
		if filled[item.Index] {
			return nil, fmt.Errorf("localai: duplicate embedding index %d in response", item.Index)
		}
		embeddings[item.Index] = EmbeddingData{
			Embedding: item.Embedding,
			Index:     item.Index,
		}
		filled[item.Index] = true
	}
	for i, ok := range filled {
		if !ok {
			return nil, fmt.Errorf("localai: missing embedding for input index %d", i)
		}
	}

	return embeddings, nil
}

type localAIRerankRequest struct {
	Model     string   `json:"model"`
	Query     string   `json:"query"`
	Documents []string `json:"documents"`
	TopN      int      `json:"top_n"`
}

type localAIRerankResponse struct {
	Results []struct {
		Index          int     `json:"index"`
		RelevanceScore float64 `json:"relevance_score"`
	} `json:"results"`
}

// Rerank 对候选文档按 query 相关性重排序
func (l *LocalAIModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	if err := l.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if len(documents) == 0 {
		return &RerankResponse{}, nil
	}
	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	baseURL, err := l.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), l.baseModel.URLSuffix.Rerank)

	topN := len(documents)
	if rerankConfig != nil && rerankConfig.TopN > 0 {
		topN = rerankConfig.TopN
	}

	reqBody := localAIRerankRequest{
		Model:     *modelName,
		Query:     query,
		Documents: documents,
		TopN:      topN,
	}

	jsonData, err := json.Marshal(reqBody)
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
	if auth := BearerAuth(apiConfig); auth != "" {
		req.Header.Set("Authorization", auth)
	}

	resp, err := l.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("LocalAI rerank API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed localAIRerankResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	rerankResponse := &RerankResponse{}
	for _, r := range parsed.Results {
		if r.Index < 0 || r.Index >= len(documents) {
			return nil, fmt.Errorf("localai: rerank result index %d out of range for %d documents", r.Index, len(documents))
		}
		rerankResponse.Data = append(rerankResponse.Data, RerankResult{
			Index:          r.Index,
			RelevanceScore: r.RelevanceScore,
		})
	}

	return rerankResponse, nil
}

// ListModels 列出当前 API Key 可见的模型目录
func (l *LocalAIModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	if err := l.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	baseURL, err := l.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), l.baseModel.URLSuffix.Models)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	if auth := BearerAuth(apiConfig); auth != "" {
		req.Header.Set("Authorization", auth)
	}

	resp, err := l.baseModel.httpClient.Do(req)
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

	// Parse response
	var modelList ModelList
	if err = json.Unmarshal(body, &modelList); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	if modelList.Models == nil {
		return nil, fmt.Errorf("invalid models list format")
	}

	return ParseListModel(modelList), nil
}

// Balance 查询账户余额（若上游支持）
func (l *LocalAIModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("no such method")
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (l *LocalAIModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := l.ListModels(apiConfig)
	if err != nil {
		return err
	}
	return nil
}

// TranscribeAudio 语音转文字（ASR）
func (l *LocalAIModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", l.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (l *LocalAIModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", l.Name())
}

// AudioSpeech 文字转语音（TTS）
func (l *LocalAIModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", l.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (l *LocalAIModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", l.Name())
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (l *LocalAIModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", l.Name())
}

// ParseFile LocalAI 暂不支持文档解析
// ParseFile 解析文档为结构化文本
func (l *LocalAIModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", l.Name())
}

// ListTasks 列出异步任务状态
func (l *LocalAIModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", l.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (l *LocalAIModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", l.Name())
}

// LocalAI 驱动覆盖 Chat/Embed/Rerank/ASR/TTS/ListModels；extractLocalAIReasoning 兼容 reasoning_content/reasoning/thinking 等字段；OCR/ParseFile 返回不支持。
