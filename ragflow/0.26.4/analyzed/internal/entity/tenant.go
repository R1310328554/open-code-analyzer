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

// tenant.go — 租户实体：工作空间根对象，绑定各模态默认模型 ID 与租户级模型实例引用。
//

package entity

// Tenant 租户 GORM 实体（表 tenant）
type Tenant struct {
	// ID 租户主键
	ID              string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	// Name 租户显示名称
	Name            *string `gorm:"column:name;size:100;index" json:"name,omitempty"`
	// PublicKey 对外 API 公钥（可选）
	PublicKey       *string `gorm:"column:public_key;size:255;index" json:"public_key,omitempty"`
	// LLMID 默认对话 LLM 标识
	LLMID           string  `gorm:"column:llm_id;size:128;not null;index" json:"llm_id"`
	// TenantLLMID 关联 tenant_llm 自增 ID（租户自有密钥配置）
	TenantLLMID     *int64  `gorm:"column:tenant_llm_id;index" json:"tenant_llm_id,omitempty"`
	// EmbdID 默认嵌入模型
	EmbdID          string  `gorm:"column:embd_id;size:128;not null;index" json:"embd_id"`
	// TenantEmbdID 租户级嵌入模型实例引用
	TenantEmbdID    *int64  `gorm:"column:tenant_embd_id;index" json:"tenant_embd_id,omitempty"`
	// ASRID 默认语音识别模型
	ASRID           string  `gorm:"column:asr_id;size:128;not null;index" json:"asr_id"`
	TenantASRID     *int64  `gorm:"column:tenant_asr_id;index" json:"tenant_asr_id,omitempty"`
	// Img2TxtID 默认图生文模型
	Img2TxtID       string  `gorm:"column:img2txt_id;size:128;not null;index" json:"img2txt_id"`
	TenantImg2TxtID *int64  `gorm:"column:tenant_img2txt_id;index" json:"tenant_img2txt_id,omitempty"`
	// RerankID 默认重排模型
	RerankID        string  `gorm:"column:rerank_id;size:128;not null;index" json:"rerank_id"`
	TenantRerankID  *int64  `gorm:"column:tenant_rerank_id;index" json:"tenant_rerank_id,omitempty"`
	// TTSID 默认语音合成模型
	TTSID           string  `gorm:"column:tts_id;size:256;index" json:"tts_id,omitempty"`
	TenantTTSID     *int64  `gorm:"column:tenant_tts_id;index" json:"tenant_tts_id,omitempty"`
	// ParserIDs 可用文档解析器 ID 列表
	ParserIDs       string  `gorm:"column:parser_ids;size:256;not null;index" json:"parser_ids"`
	// OCRID 默认 OCR 模型
	OCRID           string  `gorm:"column:ocr_id;size:256" json:"ocr_id,omitempty"`
	TenantOCRID     *int64  `gorm:"column:tenant_ocr_id" json:"tenant_ocr_id,omitempty"`
	// Credit 租户可用额度/积分
	Credit          int64   `gorm:"column:credit;default:512;index" json:"credit"`
	// Status 租户状态（启用/禁用等）
	Status          *string `gorm:"column:status;size:1;index" json:"status,omitempty"`
	BaseModel
}

// TableName 返回 GORM 表名 tenant
func (Tenant) TableName() string {
	return "tenant"
}

// *_id 为平台模型目录标识；tenant_*_id 指向租户在 tenant_llm 等表的自有配置。新建租户时 credit 默认 512；parser_ids 控制可用解析流水线。
