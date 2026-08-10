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

// mineru.go — MinerU 云端文档解析 ModelDriver：异步 ParseFile（公网 URL 提交）、ShowTask 轮询任务状态，不支持 Chat。
//

package models

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
)

// MinerUModel MinerU 云端文档解析 ModelDriver
type MinerUModel struct {
	baseModel BaseModel
}

// NewMinerUModel 创建 MinerU 云端驱动
func NewMinerUModel(baseURL map[string]string, urlSuffix URLSuffix) *MinerUModel {
	return &MinerUModel{
		baseModel: BaseModel{
			BaseURL:    baseURL,
			URLSuffix:  urlSuffix,
			httpClient: NewDriverHTTPClient(),
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 MinerU 驱动实例
func (m *MinerUModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewMinerUModel(baseURL, m.baseModel.URLSuffix)
}

// Name 返回提供商标识 "mineru"，供工厂层路由
func (m *MinerUModel) Name() string {
	return "mineru.net"
}

// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (m *MinerUModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (m *MinerUModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, modelConfig *ChatConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s no such method", m.Name())
}

// Embed 将文本列表编码为向量嵌入
func (m *MinerUModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// Rerank 对候选文档按 query 相关性重排序
func (m *MinerUModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// TranscribeAudio 语音转文字（ASR）
func (m *MinerUModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (m *MinerUModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s no such method", m.Name())
}

// AudioSpeech 文字转语音（TTS）
func (m *MinerUModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (m *MinerUModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s no such method", m.Name())
}

// OCRFile 对图片/PDF 执行 OCR 识别
func (m *MinerUModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// ListModels 列出当前 API Key 可见的模型目录
func (m *MinerUModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// Balance 查询账户余额（若上游支持）
func (m *MinerUModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (m *MinerUModel) CheckConnection(apiConfig *APIConfig) error {
	return fmt.Errorf("%s no such method", m.Name())
}

type mineruTaskSubmitResponse struct {
	Code int `json:"code"`
	Data struct {
		TaskID string `json:"task_id"`
	} `json:"data"`
	Msg     string `json:"msg"`
	TraceID string `json:"trace_id"`
}

// ParseFile 通过公网 documentURL 提交解析任务，返回 task_id（不支持直传字节）
func (m *MinerUModel) ParseFile(modelName *string, content []byte, documentURL *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	if err := m.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	if documentURL == nil || *documentURL == "" {
		return nil, fmt.Errorf("MinerU API requires a valid public document URL; direct file upload is not supported")
	}

	if err := m.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	resolvedBaseURL, err := m.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	apiURL := fmt.Sprintf("%s/api/%s", resolvedBaseURL, m.baseModel.URLSuffix.DocumentParse)

	reqBody := map[string]interface{}{
		"url": *documentURL,
	}

	if modelName != nil && *modelName != "" {
		reqBody["model_version"] = *modelName
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal request: %w", err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), longOpCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "POST", apiURL, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := m.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("MinerU API failed with status %d: %s", resp.StatusCode, string(body))
	}

	var taskResp mineruTaskSubmitResponse
	if err := json.Unmarshal(body, &taskResp); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	if taskResp.Code != 0 {
		return nil, fmt.Errorf("MinerU task creation failed (code %d): %s", taskResp.Code, taskResp.Msg)
	}

	return &ParseFileResponse{
		TaskID: taskResp.Data.TaskID,
	}, nil
}

// ListTasks 列出异步任务状态
func (m *MinerUModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s no such method", m.Name())
}

type mineruTaskQueryResponse struct {
	Code int `json:"code"`
	Data struct {
		TaskID          string `json:"task_id"`
		State           string `json:"state"` // including: pending, running, done, failed, converting
		FullZipURL      string `json:"full_zip_url"`
		ErrMsg          string `json:"err_msg"`
		ExtractProgress struct {
			ExtractedPages int `json:"extracted_pages"`
			TotalPages     int `json:"total_pages"`
		} `json:"extract_progress"`
	} `json:"data"`
	Msg string `json:"msg"`
}

// ShowTask 按 task_id 查询 MinerU 解析任务状态与 full_zip_url
// ShowTask 按 taskID 查询单个异步任务详情
func (m *MinerUModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	if err := m.baseModel.APIConfigCheck(apiConfig); err != nil {
		return nil, err
	}

	// URL: https://mineru.net/api/v4/extract/task/{task_id}
	resolvedBaseURL, err := m.baseModel.GetBaseURL(apiConfig)
	if err != nil {
		return nil, err
	}
	apiURL := fmt.Sprintf("%s/api/%s/%s", resolvedBaseURL, m.baseModel.URLSuffix.DocumentParse, taskID)

	ctx, cancel := context.WithTimeout(context.Background(), nonStreamCallTimeout)
	defer cancel()

	req, err := http.NewRequestWithContext(ctx, "GET", apiURL, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", *apiConfig.ApiKey))

	resp, err := m.baseModel.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("MinerU query API failed with status %d: %s", resp.StatusCode, string(body))
	}

	var queryResp mineruTaskQueryResponse
	if err := json.Unmarshal(body, &queryResp); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	if queryResp.Code != 0 {
		return nil, fmt.Errorf("MinerU task query failed: %s", queryResp.Msg)
	}

	// failed state
	if queryResp.Data.State == "failed" {
		return nil, fmt.Errorf("MinerU task failed: %s", queryResp.Data.ErrMsg)
	}

	content := ""
	if queryResp.Data.State == "done" {
		content = queryResp.Data.FullZipURL
	} else if queryResp.Data.State == "running" {
		content = fmt.Sprintf("Task is running... Progress: %d / %d pages",
			queryResp.Data.ExtractProgress.ExtractedPages,
			queryResp.Data.ExtractProgress.TotalPages)
	} else {
		// queue or formating
		content = fmt.Sprintf("Task state: %s", queryResp.Data.State)
	}

	return &TaskResponse{
		Segments: []TaskSegment{
			{
				Index:   0,
				Content: content,
			},
		},
	}, nil
}

// MinerU 云端驱动仅实现 ParseFile/ShowTask；ParseFile 要求有效公网 URL；ShowTask 返回 pending/running/done/failed 状态。Chat/Embed/Rerank/ASR/TTS/OCR/ListTasks 返回不支持。
