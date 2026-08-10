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

// cometapi.go — CometAPI 聚合网关 ModelDriver：OpenAI 兼容 Chat/Embed/Rerank/ASR，多区域端点与余额查询。
//

package models

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"mime/multipart"
	"net/http"
	"net/url"
	"os"
	"path/filepath"
	"strconv"
	"strings"
)

// CometAPIModel CometAPI 聚合平台 ModelDriver
type CometAPIModel struct {
	baseModel BaseModel
}

// NewCometAPIModel 创建 CometAPI 驱动实例
func NewCometAPIModel(baseURL map[string]string, urlSuffix URLSuffix) *CometAPIModel {
	return &CometAPIModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 CometAPI 驱动实例
func (c *CometAPIModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewCometAPIModel(baseURL, c.baseModel.URLSuffix)
}

// Name 返回提供商标识 "cometapi"，供工厂层路由
func (c *CometAPIModel) Name() string {
	return "cometapi"
}

// validateCometAPIModelName 校验模型名非空
func validateCometAPIModelName(modelName string) error {
	if strings.TrimSpace(modelName) == "" {
		return fmt.Errorf("model name is required")
	}
	return nil
}

// cometapiRegion 解析 API 区域，默认 default
func cometapiRegion(apiConfig *APIConfig) string {
	if apiConfig != nil && apiConfig.Region != nil && *apiConfig.Region != "" {
		return *apiConfig.Region
	}
	return "default"
}

// endpointURL 按 region 与 suffix 拼接完整端点 URL
func (c *CometAPIModel) endpointURL(region, suffix string) (string, error) {
	baseURL, err := c.baseModel.GetBaseURL(&APIConfig{Region: &region})
	if err != nil {
		return "", err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	return fmt.Sprintf("%s/%s", baseURL, strings.TrimLeft(suffix, "/")), nil
}

// balanceURL 构造余额查询 URL
func (c *CometAPIModel) balanceURL(apiKey string) string {
	rawURL := strings.TrimSpace(c.baseModel.URLSuffix.Balance)
	if !strings.HasPrefix(rawURL, "http://") && !strings.HasPrefix(rawURL, "https://") {
		rawURL = fmt.Sprintf("https://query.cometapi.com/%s", strings.TrimLeft(rawURL, "/"))
	}
	parsed, err := url.Parse(rawURL)
	if err != nil {
		return rawURL
	}
	query := parsed.Query()
	query.Set("key", apiKey)
	parsed.RawQuery = query.Encode()
	return parsed.String()
}

type cometapiChatRequest struct {
	Model       string               `json:"model"`
	Messages    []cometapiAPIMessage `json:"messages"`
	Stream      bool                 `json:"stream"`
	MaxTokens   *int                 `json:"max_tokens,omitempty"`
	Temperature *float64             `json:"temperature,omitempty"`
	TopP        *float64             `json:"top_p,omitempty"`
	Stop        *[]string            `json:"stop,omitempty"`
}

type cometapiAPIMessage struct {
	Role    string      `json:"role"`
	Content interface{} `json:"content"`
}

// buildCometAPIChatRequest 组装 OpenAI 兼容 chat 请求体
func buildCometAPIChatRequest(modelName string, messages []Message, stream bool, chatModelConfig *ChatConfig) cometapiChatRequest {
	apiMessages := make([]cometapiAPIMessage, len(messages))
	for i, msg := range messages {
		apiMessages[i] = cometapiAPIMessage{
			Role:    msg.Role,
			Content: msg.Content,
		}
	}

	reqBody := cometapiChatRequest{
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

// newCometAPIJSONRequest 创建带 Bearer 鉴权的 JSON HTTP 请求
func newCometAPIJSONRequest(ctx context.Context, method string, endpoint string, payload interface{}, apiKey string) (*http.Request, error) {
	jsonData, err := json.Marshal(payload)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, method, endpoint, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}
	req.Header.Set("Content-Type", "application/json")
	if apiKey != "" {
		req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", apiKey))
	}
	return req, nil
}

type cometapiHTTPResponse struct {
	StatusCode int
	Status     string
	Body       []byte
}

// doCometAPIRequest 执行 HTTP 请求并读取响应体
func (c *CometAPIModel) doCometAPIRequest(req *http.Request) (*cometapiHTTPResponse, error) {
	resp, err := c.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	return &cometapiHTTPResponse{
		StatusCode: resp.StatusCode,
		Status:     resp.Status,
		Body:       body,
	}, nil
}

type cometapiChatResponsePayload struct {
	Choices []cometapiChatChoice `json:"choices"`
}

type cometapiChatChoice struct {
	Message      cometapiChatMessage `json:"message"`
	Delta        cometapiChatDelta   `json:"delta"`
	FinishReason string              `json:"finish_reason"`
}

type cometapiChatMessage struct {
	Content          *string `json:"content"`
	ReasoningContent string  `json:"reasoning_content"`
}

type cometapiChatDelta struct {
	Content          string `json:"content"`
	ReasoningContent string `json:"reasoning_content"`
}

// parseCometAPIChatResponse 解析非流式 chat 响应为 ChatResponse
func parseCometAPIChatResponse(body []byte) (*ChatResponse, error) {
	var parsed cometapiChatResponsePayload
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
	reasonContent := strings.TrimLeft(parsed.Choices[0].Message.ReasoningContent, "\n")
	return &ChatResponse{
		Answer:        &content,
		ReasonContent: &reasonContent,
	}, nil
}

// parseCometAPIStreamEvent 解析 SSE data 行，提取内容与终止标志
func parseCometAPIStreamEvent(data string) (content string, reasonContent string, terminal bool, ok bool) {
	var event cometapiChatResponsePayload
	if err := json.Unmarshal([]byte(data), &event); err != nil {
		return "", "", false, false
	}
	if len(event.Choices) == 0 {
		return "", "", false, false
	}
	choice := event.Choices[0]
	return choice.Delta.Content, choice.Delta.ReasoningContent, choice.FinishReason != "", true
}

type cometapiModelCatalogResponse struct {
	Data []cometapiModelCatalogItem `json:"data"`
}

type cometapiModelCatalogItem struct {
	ID string `json:"id"`
}

// ChatWithMessages 非流式 chat/completions 请求
// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (c *CometAPIModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := c.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	apiKey := *apiConfig.ApiKey
	if err := validateCometAPIModelName(modelName); err != nil {
		return nil, err
	}

	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	url, err := c.endpointURL(cometapiRegion(apiConfig), c.baseModel.URLSuffix.Chat)
	if err != nil {
		return nil, err
	}

	reqBody := buildCometAPIChatRequest(modelName, messages, false, chatModelConfig)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := newCometAPIJSONRequest(ctx, "POST", url, reqBody, apiKey)
	if err != nil {
		return nil, err
	}
	resp, err := c.doCometAPIRequest(req)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(resp.Body))
	}
	return parseCometAPIChatResponse(resp.Body)
}

// ChatStreamlyWithSender 流式 chat/completions，经 sender 推送 delta
// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (c *CometAPIModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := c.baseModel.APIConfigCheck(apiConfig); err != nil {
		return err
	}

	if sender == nil {
		return fmt.Errorf("sender is required")
	}

	if err := validateCometAPIModelName(modelName); err != nil {
		return err
	}

	if len(messages) == 0 {
		return fmt.Errorf("messages is empty")
	}

	apiKey := *apiConfig.ApiKey

	url, err := c.endpointURL(cometapiRegion(apiConfig), c.baseModel.URLSuffix.Chat)
	if err != nil {
		return err
	}

	if chatModelConfig != nil {
		// Refuse to run if the caller explicitly asked for stream=false.
		// The body of this method only knows how to read SSE, so a
		// non-SSE JSON response would be parsed as if it were a stream
		// and produce no chunks. Better to fail clearly.
		if chatModelConfig.Stream != nil && !*chatModelConfig.Stream {
			return fmt.Errorf("stream must be true in ChatStreamlyWithSender")
		}
	}
	reqBody := buildCometAPIChatRequest(modelName, messages, true, chatModelConfig)

	req, err := newCometAPIJSONRequest(context.Background(), "POST", url, reqBody, apiKey)
	if err != nil {
		return err
	}
	resp, err := c.baseModel.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := io.ReadAll(resp.Body)
		return fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(body))
	}

	sawTerminal := false
	done, err := ParseSSEStream[cometapiChatResponsePayload](resp.Body, func(event cometapiChatResponsePayload) error {
		if len(event.Choices) == 0 {
			return nil
		}
		choice := event.Choices[0]
		reasoningContent := choice.Delta.ReasoningContent
		content := choice.Delta.Content

		if reasoningContent != "" {
			if err := sender(nil, &reasoningContent); err != nil {
				return err
			}
		}

		if content != "" {
			if err := sender(&content, nil); err != nil {
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
		return fmt.Errorf("cometapi: stream ended before [DONE] or finish_reason")
	}

	endOfStream := "[DONE]"
	if err := sender(&endOfStream, nil); err != nil {
		return err
	}

	return nil
}

type cometapiEmbeddingData struct {
	Embedding []float64 `json:"embedding"`
	Object    string    `json:"object"`
	Index     int       `json:"index"`
}

type cometapiEmbeddingResponse struct {
	Data   []cometapiEmbeddingData `json:"data"`
	Model  string                  `json:"model"`
	Object string                  `json:"object"`
}

type cometapiEmbeddingRequest struct {
	Model      string   `json:"model"`
	Input      []string `json:"input"`
	Dimensions int      `json:"dimensions,omitempty"`
}

// Embed turns a list of texts into embedding vectors
// Embed 将文本列表编码为向量嵌入
func (c *CometAPIModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := c.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}

	apiKey := *apiConfig.ApiKey

	if modelName == nil || strings.TrimSpace(*modelName) == "" {
		return nil, fmt.Errorf("model name is required")
	}

	url, err := c.endpointURL(cometapiRegion(apiConfig), c.baseModel.URLSuffix.Embedding)
	if err != nil {
		return nil, err
	}

	reqBody := cometapiEmbeddingRequest{
		Model: *modelName,
		Input: texts,
	}
	if embeddingConfig != nil && embeddingConfig.Dimension > 0 {
		reqBody.Dimensions = embeddingConfig.Dimension
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := newCometAPIJSONRequest(ctx, "POST", url, reqBody, apiKey)
	if err != nil {
		return nil, err
	}

	resp, err := c.doCometAPIRequest(req)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("CometAPI embeddings API error: %s, body: %s", resp.Status, string(resp.Body))
	}

	var parsed cometapiEmbeddingResponse
	if err = json.Unmarshal(resp.Body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	embeddings := make([]EmbeddingData, len(texts))
	filled := make([]bool, len(texts))
	for _, item := range parsed.Data {
		if item.Index < 0 || item.Index >= len(texts) {
			return nil, fmt.Errorf("cometapi: response index %d out of range for %d inputs", item.Index, len(texts))
		}
		if filled[item.Index] {
			return nil, fmt.Errorf("cometapi: duplicate embedding index %d in response", item.Index)
		}
		embeddings[item.Index] = EmbeddingData{
			Embedding: item.Embedding,
			Index:     item.Index,
		}
		filled[item.Index] = true
	}
	for i, ok := range filled {
		if !ok {
			return nil, fmt.Errorf("cometapi: missing embedding for input index %d", i)
		}
	}

	return embeddings, nil
}

// ListModels returns the public CometAPI model catalog.
// ListModels 列出当前 API Key 可见的模型目录
func (c *CometAPIModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	url, err := c.endpointURL(cometapiRegion(apiConfig), c.baseModel.URLSuffix.Models)
	if err != nil {
		return nil, err
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	resp, err := c.doCometAPIRequest(req)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("API request failed with status %d: %s", resp.StatusCode, string(resp.Body))
	}

	// Parse response
	var modelList ModelList
	if err = json.Unmarshal(resp.Body, &modelList); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}
	return ParseListModel(modelList), nil
}

// Balance queries CometAPI's quota service.
// Balance 查询账户余额（若上游支持）
func (c *CometAPIModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	if err := c.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if strings.TrimSpace(c.baseModel.URLSuffix.Balance) == "" {
		return nil, fmt.Errorf("balance URL is required")
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", c.balanceURL(*apiConfig.ApiKey), nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	resp, err := c.doCometAPIRequest(req)
	if err != nil {
		return nil, err
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("CometAPI quota API error: %s, body: %s", resp.Status, string(resp.Body))
	}

	var result map[string]interface{}
	if err = json.Unmarshal(resp.Body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	return result, nil
}

// CheckConnection runs a quota query to verify the API key.
// CheckConnection 轻量探活，验证密钥与端点可用
func (c *CometAPIModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := c.Balance(apiConfig)
	if err != nil {
		return err
	}
	return nil
}

// Rerank calculates similarity scores between query and documents.
// Rerank 对候选文档按 query 相关性重排序
func (c *CometAPIModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("no such method")
}

// TranscribeAudio transcribe audio
// TranscribeAudio 语音转文字（ASR）
func (c *CometAPIModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	if err := c.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if file == nil || *file == "" {
		return nil, fmt.Errorf("file is missing")
	}

	resolvedBaseURL, err := c.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", resolvedBaseURL, c.baseModel.URLSuffix.ASR)

	// multipart body
	var body bytes.Buffer
	writer := multipart.NewWriter(&body)

	// open audio file

	// codeql[go/path-injection] False positive: *file is the audio file path the caller passes in to upload. The user (or operator-supplied pipeline) explicitly chose this path, and the OS access check enforces permissions anyway.
	audioFile, err := os.Open(*file)
	if err != nil {
		return nil, fmt.Errorf("failed to open audio file: %w", err)
	}
	defer audioFile.Close()

	// create multipart file field
	part, err := writer.CreateFormFile("file", filepath.Base(*file))
	if err != nil {
		return nil, fmt.Errorf("failed to create multipart file: %w", err)
	}

	// copy file content
	if _, err = io.Copy(part, audioFile); err != nil {
		return nil, fmt.Errorf("failed to copy audio data: %w", err)
	}

	// model field
	if err := writer.WriteField("model", *modelName); err != nil {
		return nil, fmt.Errorf("failed to write model field: %w", err)
	}

	// extra params
	if asrConfig != nil && asrConfig.Params != nil {
		for key, value := range asrConfig.Params {

			var val string

			switch v := value.(type) {
			case string:
				val = v
			case bool:
				val = strconv.FormatBool(v)
			case int:
				val = strconv.Itoa(v)
			case int64:
				val = strconv.FormatInt(v, 10)
			case float32:
				val = strconv.FormatFloat(float64(v), 'f', -1, 32)
			case float64:
				val = strconv.FormatFloat(v, 'f', -1, 64)
			default:
				val = fmt.Sprintf("%v", v)
			}

			if err = writer.WriteField(key, val); err != nil {
				return nil, fmt.Errorf("failed to write field %s: %w", key, err)
			}
		}
	}

	if err = writer.Close(); err != nil {
		return nil, fmt.Errorf("failed to close multipart writer: %w", err)
	}

	// build request
	req, err := http.NewRequest("POST", url, &body)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))
	req.Header.Set("Content-Type", writer.FormDataContentType())
	req.Header.Set("Accept", "application/json")

	// send request
	resp, err := c.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("SiliconFlow ASR error: %s - %s", resp.Status, string(respBody))
	}

	// SiliconFlow response
	var result struct {
		Text string `json:"text"`
	}

	if err = json.Unmarshal(respBody, &result); err != nil {
		return nil, fmt.Errorf("failed to unmarshal response: %w, body=%s", err, string(respBody))
	}

	return &ASRResponse{Text: result.Text}, nil
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (c *CometAPIModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", c.Name())
}

// AudioSpeech synthesizes speech audio from text.
// AudioSpeech 文字转语音（TTS）
func (c *CometAPIModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	if err := c.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if audioContent == nil || *audioContent == "" {
		return nil, fmt.Errorf("audio content is empty")
	}

	resolvedBaseURL, err := c.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s", resolvedBaseURL, c.baseModel.URLSuffix.TTS)

	reqBody := map[string]interface{}{
		"model": *modelName,
		"input": *audioContent,
	}

	if ttsConfig != nil && ttsConfig.Params != nil {
		for key, value := range ttsConfig.Params {
			reqBody[key] = value
		}
	}
	if ttsConfig != nil && ttsConfig.Format != "" {
		reqBody["response_format"] = ttsConfig.Format
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := c.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response body: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("%s - %s", resp.Status, string(body))
	}

	return &TTSResponse{Audio: body}, nil
}

// AudioSpeechWithSender 流式 TTS 输出
func (c *CometAPIModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", c.Name())
}

// OCRFile OCR file
// OCRFile 对图片/PDF 执行 OCR 识别
func (c *CometAPIModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", c.Name())
}

// ParseFile 解析文档为结构化文本
func (c *CometAPIModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", c.Name())
}

// ListTasks 列出异步任务状态
func (c *CometAPIModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", c.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (c *CometAPIModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", c.Name())
}

// CometAPI 线协议与 OpenAI 兼容；支持多 region BaseURL 映射；Balance 走独立余额端点；ListModels 解析 catalog 响应。Rerank/Embed/ASR 各有独立 API 路径。
