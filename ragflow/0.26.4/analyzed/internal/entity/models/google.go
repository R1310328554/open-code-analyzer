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

// google.go — Google Gemini ModelDriver：基于 google.golang.org/genai SDK 的 Chat/Embed/ListModels。
//

package models

import (
	"context"
	"fmt"
	"ragflow/internal/common"
	"strings"

	"google.golang.org/genai"
)

// googleModelPage 分页模型列表中间结构
type googleModelPage struct {
	items         []DSModel
	nextPageToken string
}

// collectGoogleModelNames 分页拉取 Google 模型名并 ParseListModel 规范化
func collectGoogleModelNames(ctx context.Context, listPage func(context.Context, string) (googleModelPage, error)) ([]ListModelResponse, error) {
	var models []DSModel
	pageToken := ""

	for {
		page, err := listPage(ctx, pageToken)
		if err != nil {
			return nil, err
		}

		models = append(models, page.items...)
		if page.nextPageToken == "" {
			return ParseListModel(ModelList{Models: models}), nil
		}
		pageToken = page.nextPageToken
	}
}

var googleListModels = func(ctx context.Context, config *genai.ClientConfig) ([]ListModelResponse, error) {
	client, err := genai.NewClient(ctx, config)
	if err != nil {
		return nil, err
	}

	return collectGoogleModelNames(ctx, func(ctx context.Context, pageToken string) (googleModelPage, error) {
		models, err := client.Models.List(ctx, &genai.ListModelsConfig{PageToken: pageToken})
		if err != nil {
			return googleModelPage{}, err
		}

		var modelNames []DSModel
		for _, m := range models.Items {
			modelName := strings.TrimSpace(m.DisplayName)
			if modelName == "" {
				modelName = strings.TrimSpace(m.Name)
			}
			if modelName != "" {
				modelNames = append(modelNames, DSModel{
					ID:      modelName,
					OwnedBy: "Google",
				})
			}
		}
		return googleModelPage{items: modelNames, nextPageToken: models.NextPageToken}, nil
	})
}

// GoogleModel Google Gemini ModelDriver，走 genai SDK
type GoogleModel struct {
	baseModel BaseModel
}

// NewGoogleModel 创建 Google 驱动实例
func NewGoogleModel(baseURL map[string]string, urlSuffix URLSuffix) *GoogleModel {
	return &GoogleModel{
		baseModel: BaseModel{
			BaseURL:   baseURL,
			URLSuffix: urlSuffix,
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 Google 驱动实例
func (g *GoogleModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewGoogleModel(baseURL, g.baseModel.URLSuffix)
}

// Name 返回提供商标识 "google"，供工厂层路由
func (g *GoogleModel) Name() string {
	return "google"
}

// clientConfig 构造 genai 客户端配置（Gemini API + 自定义 BaseURL）
func (g *GoogleModel) clientConfig(apiKey string, apiConfig *APIConfig) *genai.ClientConfig {
	return &genai.ClientConfig{APIKey: apiKey, Backend: genai.BackendGeminiAPI, HTTPOptions: genai.HTTPOptions{BaseURL: g.baseURL(apiConfig)}}
}

// baseURL 解析租户自定义或默认 Gemini 端点
func (g *GoogleModel) baseURL(apiConfig *APIConfig) string {
	baseURL, err := g.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		defaultConfig := &APIConfig{}
		if apiConfig != nil {
			defaultConfig.BaseURL = apiConfig.BaseURL
		}
		baseURL, err = g.baseModel.GetBaseURL(defaultConfig)
		if err != nil {
			return ""
		}
	}
	return strings.TrimSpace(baseURL)
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (g *GoogleModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	if err := g.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if strings.TrimSpace(modelName) == "" {
		return nil, fmt.Errorf("model name is empty")
	}

	if len(messages) == 0 {
		return nil, fmt.Errorf("messages is empty")
	}

	ctx := context.Background()
	client, err := genai.NewClient(ctx, g.clientConfig(strings.TrimSpace(*apiConfig.ApiKey), apiConfig))
	if err != nil {
		return nil, err
	}

	// Convert messages to Google SDK format
	var contents []*genai.Content
	for _, msg := range messages {
		var role genai.Role
		switch msg.Role {
		case "user":
			role = genai.RoleUser
		case "model", "assistant":
			role = genai.RoleModel
		default:
			role = genai.RoleUser
		}

		// Handle content based on type
		switch c := msg.Content.(type) {
		case string:
			contents = append(contents, genai.NewContentFromText(c, role))
		case []interface{}:
			// Multimodal content - group parts within a single content
			var parts []*genai.Part
			for _, item := range c {
				if itemMap, ok := item.(map[string]interface{}); ok {
					contentType, _ := itemMap["type"].(string)
					switch contentType {
					case "text":
						if text, ok := itemMap["text"].(string); ok {
							parts = append(parts, genai.NewPartFromText(text))
						}
					case "image_url":
						if imgMap, ok := itemMap["image_url"].(map[string]interface{}); ok {
							if url, ok := imgMap["url"].(string); ok {
								parts = append(parts, genai.NewPartFromURI(url, "image/jpeg"))
							}
						}
					}
				}
			}
			if len(parts) > 0 {
				contents = append(contents, genai.NewContentFromParts(parts, role))
			}
		}
	}

	// Generate content (non-streaming)
	response, err := client.Models.GenerateContent(ctx, modelName, contents, nil)
	if err != nil {
		return nil, err
	}

	// Extract text from response
	answer := response.Text()

	return &ChatResponse{Answer: &answer}, nil
}

// ChatStreamlyWithSender sends messages and streams response via sender function (best performance, no channel)
// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (g *GoogleModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig, sender func(*string, *string) error) error {
	if err := g.baseModel.APIConfigCheck(apiConfig); err != nil {
		return err
	}

	if len(messages) == 0 {
		return fmt.Errorf("messages is empty")
	}
	if strings.TrimSpace(modelName) == "" {
		return fmt.Errorf("model name is empty")
	}
	if sender == nil {
		return fmt.Errorf("sender is nil")
	}

	ctx := context.Background()
	client, err := genai.NewClient(ctx, g.clientConfig(strings.TrimSpace(*apiConfig.ApiKey), apiConfig))
	if err != nil {
		return err
	}

	// Convert messages to Google SDK format
	var contents []*genai.Content
	for _, msg := range messages {
		var role genai.Role
		switch msg.Role {
		case "user":
			role = genai.RoleUser
		case "model", "assistant":
			role = genai.RoleModel
		default:
			role = genai.RoleUser
		}

		// Handle content based on type
		switch c := msg.Content.(type) {
		case string:
			contents = append(contents, genai.NewContentFromText(c, role))
		case []interface{}:
			// Multimodal content - group parts within a single content
			var parts []*genai.Part
			for _, item := range c {
				if itemMap, ok := item.(map[string]interface{}); ok {
					contentType, _ := itemMap["type"].(string)
					switch contentType {
					case "text":
						if text, ok := itemMap["text"].(string); ok {
							parts = append(parts, genai.NewPartFromText(text))
						}
					case "image_url":
						if imgMap, ok := itemMap["image_url"].(map[string]interface{}); ok {
							if url, ok := imgMap["url"].(string); ok {
								parts = append(parts, genai.NewPartFromURI(url, "image/jpeg"))
							}
						}
					}
				}
			}
			if len(parts) > 0 {
				contents = append(contents, genai.NewContentFromParts(parts, role))
			}
		}
	}

	for response, err := range client.Models.GenerateContentStream(
		ctx,
		modelName,
		contents,
		nil,
	) {
		if err != nil {
			return err
		}

		content := response.Text()

		var responseContent string
		if chatModelConfig != nil && chatModelConfig.Thinking != nil && *chatModelConfig.Thinking {
			if len(response.Candidates) > 0 &&
				response.Candidates[0].Content != nil &&
				len(response.Candidates[0].Content.Parts) > 0 {
				responseContent = response.Candidates[0].Content.Parts[0].Text
			}
		}

		if responseContent != "" {
			common.Info(fmt.Sprintf("Thinking: %s", responseContent))
			if err = sender(nil, &responseContent); err != nil {
				return err
			}
		}

		if content != "" {
			common.Info(fmt.Sprintf("Answer: %s", content))
			if err = sender(&content, nil); err != nil {
				return err
			}
		}
	}

	return err
}

// Embed generates embeddings for a batch of texts using the Gemini embeddings API.
// The SDK routes to batchEmbedContents internally, so all texts are sent in one request.
// Embed 将文本列表编码为向量嵌入
func (g *GoogleModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	if err := g.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}
	if modelName == nil || *modelName == "" {
		return nil, fmt.Errorf("model name is required")
	}
	if len(texts) == 0 {
		return nil, fmt.Errorf("texts is empty")
	}

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	client, err := genai.NewClient(ctx, g.clientConfig(strings.TrimSpace(*apiConfig.ApiKey), apiConfig))
	if err != nil {
		return nil, fmt.Errorf("failed to create client: %w", err)
	}

	contents := make([]*genai.Content, len(texts))
	for i, text := range texts {
		contents[i] = genai.NewContentFromText(text, genai.RoleUser)
	}

	var cfg *genai.EmbedContentConfig
	if embeddingConfig != nil && embeddingConfig.Dimension > 0 {
		dim := int32(embeddingConfig.Dimension)
		cfg = &genai.EmbedContentConfig{OutputDimensionality: &dim}
	}

	resp, err := client.Models.EmbedContent(ctx, *modelName, contents, cfg)
	if err != nil {
		return nil, fmt.Errorf("failed to embed content: %w", err)
	}

	if len(resp.Embeddings) != len(texts) {
		return nil, fmt.Errorf("expected %d embeddings, got %d", len(texts), len(resp.Embeddings))
	}

	result := make([]EmbeddingData, len(resp.Embeddings))
	for i, emb := range resp.Embeddings {
		vec := make([]float64, len(emb.Values))
		for j, v := range emb.Values {
			vec[j] = float64(v)
		}
		result[i] = EmbeddingData{
			Embedding: vec,
			Index:     i,
		}
	}

	return result, nil
}

// ListModels 列出当前 API Key 可见的模型目录
func (g *GoogleModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	if err := g.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	return googleListModels(context.Background(), g.clientConfig(strings.TrimSpace(*apiConfig.ApiKey), apiConfig))
}

// Balance 查询账户余额（若上游支持）
func (g *GoogleModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("no such method")
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (g *GoogleModel) CheckConnection(apiConfig *APIConfig) error {
	_, err := g.ListModels(apiConfig)
	return err
}

// Rerank calculates similarity scores between query and documents
// Rerank 对候选文档按 query 相关性重排序
func (g *GoogleModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("%s, Rerank not implemented", g.Name())
}

// TranscribeAudio transcribe audio
// TranscribeAudio 语音转文字（ASR）
func (g *GoogleModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", g.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (g *GoogleModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", g.Name())
}

// AudioSpeech convert text to audio
// AudioSpeech 文字转语音（TTS）
func (g *GoogleModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", g.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (g *GoogleModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", g.Name())
}

// OCRFile OCR file
// OCRFile 对图片/PDF 执行 OCR 识别
func (g *GoogleModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", g.Name())
}

// ParseFile parse file
// ParseFile 解析文档为结构化文本
func (g *GoogleModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", g.Name())
}

// ListTasks 列出异步任务状态
func (g *GoogleModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", g.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (g *GoogleModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", g.Name())
}

// Google 驱动通过 genai SDK 调用 Gemini API；Chat/Embed 使用 GenerateContent/EmbedContent；ListModels 分页遍历 Models.List。Rerank/ASR/TTS/OCR 返回不支持。
