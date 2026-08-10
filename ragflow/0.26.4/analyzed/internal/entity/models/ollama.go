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

// ollama.go — Ollama 本地推理 ModelDriver：原生 /api/chat 与 /api/embeddings，AllowEmptyAPIKey 支持无密钥本地部署。
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
)

// OllamaModel Ollama 本地推理 ModelDriver
type OllamaModel struct {
	baseModel BaseModel
}

// contentToText 从 Message.Content 提取纯文本（支持多模态数组首条 text）
// Content may be a raw string or an OpenAI-style multimodal array
// ([]interface{} where each element is {"type": "text", "text": "..."}).
// The first non-empty "text" value found is returned; empty string on no match.
func contentToText(content interface{}) string {
	switch c := content.(type) {
	case string:
		return c
	case []interface{}:
		for _, item := range c {
			if part, ok := item.(map[string]interface{}); ok {
				if text, ok := part["text"].(string); ok && text != "" {
					return text
				}
			}
		}
	}
	return ""
}

// NewOllamaModel 创建 Ollama 驱动（AllowEmptyAPIKey=true）
func NewOllamaModel(baseURL map[string]string, urlSuffix URLSuffix) *OllamaModel {
	return &OllamaModel{
		baseModel: BaseModel{
			BaseURL:          baseURL,
			URLSuffix:        urlSuffix,
			AllowEmptyAPIKey: true,
			httpClient:       NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 Ollama 驱动实例
func (o *OllamaModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewOllamaModel(baseURL, o.baseModel.URLSuffix)
}

// Name 返回提供商标识 "ollama"，供工厂层路由
func (o *OllamaModel) Name() string {
	return "ollama"
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (o *OllamaModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if len(messages) == 0 {
		return nil, fmt.Errorf("message is nil")
	}

	resolvedBaseURL, err := o.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", resolvedBaseURL, o.baseModel.URLSuffix.Chat)

	// For qwen/glm models, use async chat endpoint
	modelType := strings.Split(modelName, "_")[0]
	if modelType == "qwen" || modelType == "glm" {
		url = fmt.Sprintf("%s/%s", resolvedBaseURL, o.baseModel.URLSuffix.AsyncChat)
	}

	// Convert messages to API format
	apiMessages := make([]map[string]interface{}, len(messages))
	for i, msg := range messages {
		apiMessages[i] = map[string]interface{}{
			"role":    msg.Role,
			"content": contentToText(msg.Content),
		}
	}

	// Build request body
	reqBody := map[string]interface{}{
		"model":       modelName,
		"messages":    apiMessages,
		"stream":      false,
		"temperature": 1,
	}

	if chatModelConfig != nil {
		if chatModelConfig.Stream != nil {
			reqBody["stream"] = *chatModelConfig.Stream
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

		if chatModelConfig.Effort != nil && *chatModelConfig.Effort != "" {
			if strings.HasPrefix(strings.ToLower(modelName), "gpt-oss") {
				reqBody["think"] = *chatModelConfig.Effort
			}
		} else if chatModelConfig.Thinking != nil {
			if *chatModelConfig.Thinking {
				reqBody["think"] = true
			}
		}
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

	resp, err := o.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	// Parse response
	var result map[string]interface{}
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	message, ok := result["message"].(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("failed to parse response: message not found")
	}

	content, ok := message["content"].(string)
	if !ok {
		return nil, fmt.Errorf("failed to parse response: content not found")
	}

	reasonContent, _ := message["thinking"].(string)

	chatResponse := &ChatResponse{
		Answer:        &content,
		ReasonContent: &reasonContent,
	}

	return chatResponse, nil
}

// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (o *OllamaModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, modelConfig *ChatConfig, sender func(*string, *string) error) error {
	if len(messages) == 0 {
		return fmt.Errorf("messages is empty")
	}

	resolvedBaseURL, err := o.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return err
	}
	url := fmt.Sprintf("%s/%s", resolvedBaseURL, o.baseModel.URLSuffix.Chat)
	modelType := strings.Split(modelName, "-")[0]
	if modelType == "qwen" || modelType == "glm" {
		url = fmt.Sprintf("%s/%s", resolvedBaseURL, o.baseModel.URLSuffix.AsyncChat)
	}

	// Convert messages to API format (supporting multimodal content)
	apiMessages := make([]map[string]interface{}, len(messages))
	for i, msg := range messages {
		apiMessages[i] = map[string]interface{}{
			"role":    msg.Role,
			"content": contentToText(msg.Content),
		}
	}

	// Build request body with streaming enabled
	reqBody := map[string]interface{}{
		"model":    modelName,
		"messages": apiMessages,
		"stream":   true,
	}

	if modelConfig.Stream != nil {
		reqBody["stream"] = *modelConfig.Stream
	}

	if modelConfig.MaxTokens != nil {
		reqBody["max_tokens"] = *modelConfig.MaxTokens
	}

	if modelConfig.Temperature != nil {
		reqBody["temperature"] = *modelConfig.Temperature
	}

	if modelConfig.DoSample != nil {
		reqBody["do_sample"] = *modelConfig.DoSample
	}

	if modelConfig.TopP != nil {
		reqBody["top_p"] = *modelConfig.TopP
	}

	if modelConfig.Stop != nil {
		reqBody["stop"] = *modelConfig.Stop
	}

	if modelConfig.Effort != nil && *modelConfig.Effort != "" {
		if strings.HasPrefix(strings.ToLower(modelName), "gpt-oss") {
			reqBody["think"] = *modelConfig.Effort
		}
	} else if modelConfig.Thinking != nil {
		if *modelConfig.Thinking {
			reqBody["think"] = true
		}
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), streamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := o.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	// SSE parsing: read line by line
	scanner := bufio.NewScanner(resp.Body)
	scanner.Buffer(make([]byte, 64*1024), 1024*1024)
	for scanner.Scan() {
		line := strings.TrimSpace(scanner.Text())

		// ignore the blank
		if line == "" {
			continue
		}

		// Parse the JSON event
		var event map[string]interface{}
		if err = json.Unmarshal([]byte(line), &event); err != nil {
			continue
		}

		if messageMap, ok := event["message"].(map[string]interface{}); ok {
			if reasoningContent, exists := messageMap["thinking"].(string); exists && reasoningContent != "" {
				if err := sender(nil, &reasoningContent); err != nil {
					return err
				}
			}
			if content, exists := messageMap["content"].(string); exists && content != "" {
				if err := sender(&content, nil); err != nil {
					return err
				}
			}
		}

		if done, ok := event["done"].(bool); ok && done {
			break
		}
	}

	// Send [DONE] marker for OpenAI compatibility with RAGFlow frontend
	endOfStream := "[DONE]"
	if err := sender(&endOfStream, nil); err != nil {
		return err
	}

	return scanner.Err()
}

// Embed 将文本列表编码为向量嵌入
func (o *OllamaModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := o.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}

	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	resolvedBaseURL, err := o.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL := resolvedBaseURL
	if baseURL == "" {
		baseURL = resolvedBaseURL
	}
	if baseURL == "" {
		return nil, fmt.Errorf("missing base URL: please configure the local access address for Ollama (e.g., http://127.0.0.1:11434/v1)")
	}

	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), o.baseModel.URLSuffix.Embedding)

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

	resp, err := o.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Ollama embeddings API error: %s, body: %s", resp.Status, string(body))
	}

	var embedResp struct {
		Model      string      `json:"model"`
		Embeddings [][]float64 `json:"embeddings"`
	}

	if err = json.Unmarshal(body, &embedResp); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w", err)
	}

	if len(embedResp.Embeddings) == 0 {
		return nil, fmt.Errorf("no embeddings returned")
	}

	embeddings := make([]EmbeddingData, 0, len(embedResp.Embeddings))

	for i, emb := range embedResp.Embeddings {
		if len(emb) == 0 {
			return nil, fmt.Errorf("empty embedding at index %d", i)
		}

		embeddings = append(embeddings, EmbeddingData{
			Embedding: emb,
			Index:     i,
		})
	}

	return embeddings, nil
}

// Rerank 对候选文档按 query 相关性重排序
func (o *OllamaModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("no such method")
}

// TranscribeAudio Ollama 暂不支持 ASR
// TranscribeAudio 语音转文字（ASR）
func (o *OllamaModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", o.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (o *OllamaModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", o.Name())
}

// AudioSpeech Ollama 暂不支持 TTS
// AudioSpeech 文字转语音（TTS）
func (o *OllamaModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", o.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (o *OllamaModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", o.Name())
}

// OCRFile Ollama 暂不支持 OCR
// OCRFile 对图片/PDF 执行 OCR 识别
func (o *OllamaModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", o.Name())
}

// ParseFile Ollama 暂不支持文档解析
// ParseFile 解析文档为结构化文本
func (o *OllamaModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", o.Name())
}

// ListModels 列出当前 API Key 可见的模型目录
func (o *OllamaModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {

	baseURL, err := o.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	if baseURL == "" {
		return nil, fmt.Errorf("missing base URL: please configure the local access address for Ollama (e.g., http://127.0.0.1:11434)")
	}

	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), o.baseModel.URLSuffix.Models)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := o.baseModel.httpClient.Do(req)
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

	// Ollama's /api/tags returns {"models":[{"name":...,"model":...}]}, a shape
	// that differs from the OpenAI list. Decode it into a local struct, map the
	// names into ModelList, then enrich through the shared ParseListModel helper
	// (issue #15853). Using a typed struct also avoids the previous unchecked
	// type assertions, which panicked when "models" was absent or malformed.
	var result struct {
		Models []struct {
			Name  string `json:"name"`
			Model string `json:"model"`
		} `json:"models"`
	}
	if err = json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	modelList := ModelList{Object: "list"}
	for _, m := range result.Models {
		name := strings.TrimSpace(m.Name)
		if name == "" {
			name = strings.TrimSpace(m.Model)
		}
		if name == "" {
			continue
		}
		modelList.Models = append(modelList.Models, DSModel{ID: name})
	}

	return ParseListModel(modelList), nil
}

// Balance 查询账户余额（若上游支持）
func (o *OllamaModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("no such method")
}

// CheckConnection 探测 Ollama 本地/远程端点是否可达
// CheckConnection 轻量探活，验证密钥与端点可用
func (o *OllamaModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := o.ListModels(apiConfig)
	return err
}

// ListTasks 列出异步任务状态
func (o *OllamaModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", o.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (o *OllamaModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", o.Name())
}

// Ollama 驱动实现 Chat/Embed/ListModels/CheckConnection；使用原生 Ollama API 而非 OpenAI 兼容层；Rerank/ASR/TTS/OCR/ParseFile 返回不支持。
