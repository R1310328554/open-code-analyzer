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
//

// dummy.go — 占位 ModelDriver：工厂 default 分支兜底，所有接口返回 not implemented/no such method。

package models

import (
	"fmt"
)

// DummyModel 未知厂商占位驱动，实现 ModelDriver 全接口桩
type DummyModel struct {
	baseModel BaseModel
}

// NewDummyModel 创建 Dummy 驱动实例
func NewDummyModel(baseURL map[string]string, urlSuffix URLSuffix) *DummyModel {
	return &DummyModel{
		baseModel: BaseModel{
			BaseURL:   baseURL,
			URLSuffix: urlSuffix,
		},
	}
}

// NewInstance 按租户/区域 BaseURL 创建新的 Dummy 驱动实例
func (d *DummyModel) NewInstance(baseURL map[string]string) ModelDriver {
	return NewDummyModel(baseURL, d.baseModel.URLSuffix)
}

// Name 返回提供商标识 "dummy"，供工厂层路由
func (d *DummyModel) Name() string {
	return "dummy"
}

// ChatWithMessages 未实现，返回 not implemented
// ChatWithMessages 非流式多轮对话，返回完整回复与 token 用量
func (d *DummyModel) ChatWithMessages(modelName string, messages []Message, apiConfig *APIConfig, chatModelConfig *ChatConfig) (*ChatResponse, error) {
	return nil, fmt.Errorf("not implemented")
}

// ChatStreamlyWithSender 未实现，返回 not implemented
// ChatStreamlyWithSender 流式对话，通过 sender 回调推送增量内容与推理片段
func (d *DummyModel) ChatStreamlyWithSender(modelName string, messages []Message, apiConfig *APIConfig, modelConfig *ChatConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("not implemented")
}

// Embed 未实现，返回 not implemented
// Embed 将文本列表编码为向量嵌入
func (d *DummyModel) Embed(modelName *string, texts []string, apiConfig *APIConfig, embeddingConfig *EmbeddingConfig) ([]EmbeddingData, error) {
	return nil, fmt.Errorf("not implemented")
}

// ListModels 列出当前 API Key 可见的模型目录
func (d *DummyModel) ListModels(apiConfig *APIConfig) ([]ListModelResponse, error) {
	return nil, fmt.Errorf("not implemented")
}

// Balance 查询账户余额（若上游支持）
func (d *DummyModel) Balance(apiConfig *APIConfig) (map[string]interface{}, error) {
	return nil, fmt.Errorf("no such method")
}

// CheckConnection 轻量探活，验证密钥与端点可用
func (d *DummyModel) CheckConnection(apiConfig *APIConfig) error {
	return fmt.Errorf("no such method")
}

// Rerank 未实现
// Rerank 对候选文档按 query 相关性重排序
func (d *DummyModel) Rerank(modelName *string, query string, documents []string, apiConfig *APIConfig, rerankConfig *RerankConfig) (*RerankResponse, error) {
	return nil, fmt.Errorf("%s, Rerank not implemented", d.Name())
}

// TranscribeAudio 不支持
// TranscribeAudio 语音转文字（ASR）
func (d *DummyModel) TranscribeAudio(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig) (*ASRResponse, error) {
	return nil, fmt.Errorf("%s, no such method", d.Name())
}

// TranscribeAudioWithSender 流式 ASR，增量推送识别文本
func (d *DummyModel) TranscribeAudioWithSender(modelName *string, file *string, apiConfig *APIConfig, asrConfig *ASRConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", d.Name())
}

// AudioSpeech 不支持 TTS
// AudioSpeech 文字转语音（TTS）
func (d *DummyModel) AudioSpeech(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig) (*TTSResponse, error) {
	return nil, fmt.Errorf("%s, no such method", d.Name())
}

// AudioSpeechWithSender 流式 TTS 输出
func (d *DummyModel) AudioSpeechWithSender(modelName *string, audioContent *string, apiConfig *APIConfig, ttsConfig *TTSConfig, sender func(*string, *string) error) error {
	return fmt.Errorf("%s, no such method", d.Name())
}

// OCRFile 不支持 OCR
// OCRFile 对图片/PDF 执行 OCR 识别
func (d *DummyModel) OCRFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, ocrConfig *OCRConfig) (*OCRFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", d.Name())
}

// ParseFile 不支持文档解析
// ParseFile 解析文档为结构化文本
func (d *DummyModel) ParseFile(modelName *string, content []byte, url *string, apiConfig *APIConfig, parseFileConfig *ParseFileConfig) (*ParseFileResponse, error) {
	return nil, fmt.Errorf("%s, no such method", d.Name())
}

// ListTasks 列出异步任务状态
func (d *DummyModel) ListTasks(apiConfig *APIConfig) ([]ListTaskStatus, error) {
	return nil, fmt.Errorf("%s, no such method", d.Name())
}

// ShowTask 按 taskID 查询单个异步任务详情
func (d *DummyModel) ShowTask(taskID string, apiConfig *APIConfig) (*TaskResponse, error) {
	return nil, fmt.Errorf("%s, no such method", d.Name())
}

// Dummy 驱动在 ModelFactory 未匹配厂商时作为兜底；不发起任何上游 HTTP 请求；CheckConnection 同样返回 no such method。
