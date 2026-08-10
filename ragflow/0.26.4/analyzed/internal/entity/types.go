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

// types.go — 模型类型枚举：entity 包内各模态模型的字符串常量，供路由与校验使用。
//

package entity

// ModelType 模型能力类型（字符串别名）
type ModelType string

const (
	// ModelTypeChat 对话/补全模型
	ModelTypeChat ModelType = "chat"
	// ModelTypeEmbedding 文本嵌入模型
	ModelTypeEmbedding ModelType = "embedding"
	// ModelTypeSpeech2Text 语音转文字（ASR）
	ModelTypeSpeech2Text ModelType = "speech2text"
	// ModelTypeImage2Text 图像理解/图生文
	ModelTypeImage2Text ModelType = "image2text"
	// ModelTypeRerank 检索重排模型
	ModelTypeRerank ModelType = "rerank"
	// ModelTypeTTS 文字转语音
	ModelTypeTTS ModelType = "tts"
	// ModelTypeOCR 光学字符识别
	ModelTypeOCR ModelType = "ocr"
)

// 与 tenant_model.model_type、tenant_llm.model_type 字段值对齐；新增模态时需同步扩展 ModelDriver 接口实现。
