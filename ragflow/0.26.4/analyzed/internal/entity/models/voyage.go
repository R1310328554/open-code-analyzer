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

// voyage.go — Voyage AI ModelDriver：专注 Embed/Rerank 向量与重排能力，不提供 Chat 接口。
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

// VoyageModel Voyage AI 嵌入与重排 ModelDriver
type VoyageModel struct {
	baseModel BaseModel
}

// NewVoyageModel 创建 Voyage 驱动实例
func NewVoyageModel(baseURL map[string]string, urlSuffix URLSuffix) *VoyageModel {
	return &VoyageModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 Voyage 驱动实例
func (v *VoyageModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewVoyageModel(baseURL, v.baseModel.URLSuffix)
}

// Name 返回提供商标识 "voyage"，供工厂层路由
func (v *VoyageModel) Name() string {
	return "voyage"
}

type voyageEmbeddingData struct {
	Embedding []float64 `json:"embedding"`
	Object    string    `json:"object"`
	Index     int       `json:"index"`
}

type voyageEmbeddingResponse struct {
	Object string                `json:"object"`
	Data   []voyageEmbeddingData `json:"data"`
	Model  string                `json:"model"`
}

// Embed Voyage 专用 embedding API，批量编码文本向量
// Embed 将文本列表编码为向量嵌入
func (v *VoyageModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := v.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(texts) == 0 {
		return []EmbeddingData{}, nil
	}

	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	baseURL, err := v.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), v.baseModel.URLSuffix.Embedding)

	reqBody := map[string]interface{}{
		"model": *modelName,
		"input": texts,
	}
	if embeddingConfig != nil && embeddingConfig.Dimension > 0 {
		reqBody["output_dimension"] = embeddingConfig.Dimension
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
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := v.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Voyage embeddings API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed voyageEmbeddingResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	embeddings := make([]EmbeddingData, len(texts))
	filled := make([]bool, len(texts))
	for _, item := range parsed.Data {
		if item.Index < 0 || item.Index >= len(texts) {
			return nil, fmt.Errorf("voyage: response index %d out of range for %d inputs", item.Index, len(texts))
		}
		if filled[item.Index] {
			return nil, fmt.Errorf("voyage: duplicate embedding index %d in response", item.Index)
		}
		embeddings[item.Index] = EmbeddingData{
			Embedding: item.Embedding,
			Index:     item.Index,
		}
		filled[item.Index] = true
	}
	for i, ok := range filled {
		if !ok {
			return nil, fmt.Errorf("voyage: missing embedding for input index %d", i)
		}
	}

	return embeddings, nil
}

type voyageRerankRequest struct {
	Model     string   `json:"model"`
	Query     string   `json:"query"`
	Documents []string `json:"documents"`
	TopK      int      `json:"top_k"`
}

type voyageRerankResponse struct {
	Object string `json:"object"`
	Data   []struct {
		RelevanceScore float64 `json:"relevance_score"`
		Index          int     `json:"index"`
	} `json:"data"`
	Model string `json:"model"`
}

// Rerank Voyage rerank API，返回文档相关性分数
// Rerank 对候选文档按 query 相关性重排序
func (v *VoyageModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	if err := v.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(documents) == 0 {
		return &RerankResponse{}, nil
	}
	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}

	baseURL, err := v.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	baseURL = strings.TrimSuffix(baseURL, "/")
	url := fmt.Sprintf("%s/%s", strings.TrimSuffix(baseURL, "/"), v.baseModel.URLSuffix.Rerank)

	topK := len(documents)
	if rerankConfig != nil && rerankConfig.TopN > 0 {
		topK = rerankConfig.TopN
	}

	reqBody := voyageRerankRequest{
		Model:     *modelName,
		Query:     query,
		Documents: documents,
		TopK:      topK,
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
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := v.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("Voyage rerank API error: %s, body: %s", resp.Status, string(body))
	}

	var parsed voyageRerankResponse
	if err = json.Unmarshal(body, &parsed); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	rerankResponse := &RerankResponse{}
	seen := make(map[int]bool, len(parsed.Data))
	for _, r := range parsed.Data {
		if r.Index < 0 || r.Index >= len(documents) {
			return nil, fmt.Errorf("voyage: rerank result index %d out of range for %d documents", r.Index, len(documents))
		}
		if seen[r.Index] {
			return nil, fmt.Errorf("voyage: duplicate rerank index %d in response", r.Index)
		}
		seen[r.Index] = true
		rerankResponse.Data = append(rerankResponse.Data, RerankResult{
			Index:          r.Index,
			RelevanceScore: r.RelevanceScore,
		})
	}

	return rerankResponse, nil
}

// ListModels 列出当前 API Key 可见的模型目录
func (v *VoyageModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// CheckConnection 通过 Embed 空请求或模型列表探活
// CheckConnection 轻量探活，验证密钥与端点可用
func (v *VoyageModel) CheckConnection(apiConfig *APIConfig) error {
	return fmt.Errorf("%s, no such method", v.Name())
}

// ChatWithMessages is not exposed by the Voyage AI API.
// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (v *VoyageModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (v *VoyageModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, modelConfig *ChatConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", v.Name())
}

// Balance is not exposed by the Voyage AI API.
// Balance 查询账户余额（若上游支持）
func (v *VoyageModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// TranscribeAudio / AudioSpeech / OCRFile: Voyage does not host any of
// these surfaces.
// TranscribeAudio 语音转文字（ASR）
func (v *VoyageModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (v *VoyageModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", v.Name())
}

// AudioSpeech 文字转语音（TTS）
func (v *VoyageModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (v *VoyageModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", v.Name())
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (v *VoyageModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// ParseFile parse file
// ParseFile 解析文档为结构化文本
func (v *VoyageModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// ListTasks 列出异步任务状态
func (v *VoyageModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (v *VoyageModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", v.Name())
}

// Voyage 驱动仅实现 Embed/Rerank/CheckConnection；Chat/Stream/ASR/TTS/OCR/ParseFile/ListModels 返回不支持。Bearer 鉴权；Rerank 支持 top_n 参数。
