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

// mineru_local.go — MinerU 本地部署 ModelDriver：multipart 上传 PDF 字节流异步解析，ShowTask 查询本地任务状态。
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
)

// MinerULocalModel MinerU 本地部署 ModelDriver
type MinerULocalModel struct {
	baseModel BaseModel
}

// NewMinerLocalUModel 创建 MinerU 本地驱动
func NewMinerLocalUModel(baseURL map[string]string, urlSuffix URLSuffix) *MinerULocalModel {
	return &MinerULocalModel{
		baseModel: BaseModel{
			BaseURL:          baseURL,
			URLSuffix:        urlSuffix,
			AllowEmptyAPIKey: true,
			httpClient:       NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 MinerULocal 驱动实例
func (m *MinerULocalModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewMinerLocalUModel(baseURL, m.baseModel.URLSuffix)
}

// Name 返回提供商标识 "minerulocal"，供工厂层路由
func (m *MinerULocalModel) Name() string {
	return "mineru"
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (m *MinerULocalModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (m *MinerULocalModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, modelConfig *ChatConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s no such method", m.Name())
}

// Embed 将文本列表编码为向量嵌入
func (m *MinerULocalModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// Rerank 对候选文档按 query 相关性重排序
func (m *MinerULocalModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// TranscribeAudio 语音转文字（ASR）
func (m *MinerULocalModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (m *MinerULocalModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s no such method", m.Name())
}

// AudioSpeech 文字转语音（TTS）
func (m *MinerULocalModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (m *MinerULocalModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s no such method", m.Name())
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (m *MinerULocalModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// ListModels 列出当前 API Key 可见的模型目录
func (m *MinerULocalModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// Balance 查询账户余额（若上游支持）
func (m *MinerULocalModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (m *MinerULocalModel) CheckConnection(apiConfig *APIConfig) error {
	return fmt.Errorf("%s no such method", m.Name())
}

// ParseFile multipart 上传 PDF 字节流，backend 默认 pipeline，返回 task_id
func (m *MinerULocalModel) ParseFile(modelName *string, content []byte, documentURL *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	if err := m.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if len(content) == 0 {
		return nil, fmt.Errorf("local MinerU API requires file content byte array, but content is empty")
	}

	resolvedBaseURL, err := m.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	apiURL := fmt.Sprintf("%s/%s", resolvedBaseURL, m.baseModel.URLSuffix.DocumentParse)

	var body bytes.Buffer
	writer := multipart.NewWriter(&body)

	// Get file
	part, err := writer.CreateFormFile("files", "upload_document.pdf")
	if err != nil {
		return nil, fmt.Errorf("failed to create multipart file field: %w", err)
	}
	if _, err = part.Write(content); err != nil {
		return nil, fmt.Errorf("failed to write file content: %w", err)
	}

	if modelName != nil && *modelName != "" {
		_ = writer.WriteField("backend", *modelName)
	} else {
		_ = writer.WriteField("backend", "pipeline")
	}

	if err = writer.Close(); err != nil {
		return nil, fmt.Errorf("failed to close multipart writer: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), longOpCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", apiURL, &body)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", writer.FormDataContentType())

	if auth := BearerAuth(apiConfig); auth != "" {
		req.Header.Set("Authorization", auth)
	}

	resp, err := m.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK && resp.StatusCode != 202 {
		return nil, fmt.Errorf("local MinerU API failed with status %d: %s (URL: %s)", resp.StatusCode, string(respBody), apiURL)
	}

	var result map[string]interface{}
	if err := json.Unmarshal(respBody, &result); err != nil {
		return nil, fmt.Errorf("failed to parse response JSON: %w, body: %s", err, string(respBody))
	}
	// Get task ID
	var taskID string
	if dataMap, ok := result["data"].(map[string]interface{}); ok {
		if tid, ok := dataMap["task_id"].(string); ok {
			taskID = tid
		}
	} else if tid, ok := result["task_id"].(string); ok {
		taskID = tid
	}

	if taskID == "" {
		return nil, fmt.Errorf("failed to extract task_id from local MinerU response: %s", string(respBody))
	}

	return &ParseFileResponse{
		TaskID: taskID,
	}, nil
}

// ListTasks 列出异步任务状态
func (m *MinerULocalModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// ShowTask 查询本地 MinerU 解析任务进度与结果 URL
// ShowTask 按 taskID 查询单个异步任务详情
func (m *MinerULocalModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	if err := m.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if taskID == "" {
		return nil, fmt.Errorf("taskID is empty")
	}

	resolvedBaseURL, err := m.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	url := fmt.Sprintf("%s/%s/%s/result", resolvedBaseURL, m.baseModel.URLSuffix.Task, taskID)
	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create status request: %w", err)
	}

	if auth := BearerAuth(apiConfig); auth != "" {
		req.Header.Set("Authorization", auth)
	}

	resp, err := m.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send status request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read status response: %w", err)
	}

	if resp.StatusCode != http.StatusOK && resp.StatusCode != 202 {
		return nil, fmt.Errorf("MinerU local status API failed with status %d: %s", resp.StatusCode, string(body))
	}

	// parse JSON
	var result map[string]interface{}

	if err := json.Unmarshal(body, &result); err != nil {
		return nil, fmt.Errorf("failed to parse JSON: %w", err)
	}

	content := ""

	// results
	results, ok := result["results"].(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("missing results field")
	}

	// Get markdown
	for _, fileObj := range results {

		fileMap, ok := fileObj.(map[string]interface{})
		if !ok {
			continue
		}

		md, ok := fileMap["md_content"].(string)
		if ok {
			content = md
			break
		}
	}

	if content == "" {
		return nil, fmt.Errorf("md_content not found")
	}

	return &TaskResponse{
		Segments: []TaskSegment{
			{
				Index:   1,
				Content: content,
			},
		},
	}, nil
}

// MinerU 本地驱动实现 ParseFile/ShowTask；ParseFile 要求非空 content 字节流；AllowEmptyAPIKey 可选 Bearer。Chat/Embed/Rerank/ASR/TTS/OCR/ListTasks/CheckConnection 返回不支持。
