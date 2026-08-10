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

// llm.go — LLM 工厂与模型目录实体：llm_factories/llm 表映射、租户 Langfuse 集成凭证及 MyLLM 列表投影。
//

package entity

// LLMFactories LLM 厂商/工厂元数据（名称、Logo、标签、排序）
type LLMFactories struct {
	Name   string  `gorm:"column:name;primaryKey;size:128" json:"name"`
	Logo   *string `gorm:"column:logo;type:longtext" json:"logo,omitempty"`
	Tags   string  `gorm:"column:tags;size:255;not null;index" json:"tags"`
	Rank   int64   `gorm:"column:rank;default:0" json:"rank"`
	Status *string `gorm:"column:status;size:1;index" json:"status,omitempty"`
	BaseModel
}

// TableName 返回 GORM 表名
func (LLMFactories) TableName() string {
	return "llm_factories"
}

// LLM 平台级模型目录条目（名称、类型、厂商 ID、最大 token、工具支持）
type LLM struct {
	LLMName   string  `gorm:"column:llm_name;size:128;not null;primaryKey" json:"llm_name"`
	ModelType string  `gorm:"column:model_type;size:128;not null;index" json:"model_type"`
	FID       string  `gorm:"column:fid;size:128;not null;primaryKey" json:"fid"`
	MaxTokens int64   `gorm:"column:max_tokens;default:0" json:"max_tokens"`
	Tags      string  `gorm:"column:tags;size:255;not null;index" json:"tags"`
	IsTools   bool    `gorm:"column:is_tools;default:false" json:"is_tools"`
	Status    *string `gorm:"column:status;size:1;index" json:"status,omitempty"`
	BaseModel
}

// TableName specify table name
func (LLM) TableName() string {
	return "llm"
}

// TenantLangfuse 租户级 Langfuse 可观测性凭证（公钥/私钥/Host）
type TenantLangfuse struct {
	TenantID  string `gorm:"column:tenant_id;primaryKey;size:32" json:"tenant_id"`
	SecretKey string `gorm:"column:secret_key;size:2048;not null" json:"secret_key"`
	PublicKey string `gorm:"column:public_key;size:2048;not null" json:"public_key"`
	Host      string `gorm:"column:host;size:128;not null;index" json:"host"`
	BaseModel
}

// TableName specify table name
func (TenantLangfuse) TableName() string {
	return "tenant_langfuse"
}

// LangfuseInfoResponse GET /langfuse/api-key 响应体：存储凭证叠加解析出的 Langfuse 项目 id/name，字段顺序对齐 Python filter_by_tenant_with_info。
type LangfuseInfoResponse struct {
	TenantID    string `json:"tenant_id"`
	Host        string `json:"host"`
	SecretKey   string `json:"secret_key"`
	PublicKey   string `json:"public_key"`
	ProjectID   string `json:"project_id"`
	ProjectName string `json:"project_name"`
}

// MyLLM 租户已绑定 LLM 列表项（含厂商 Logo、已用 token、API Base）
type MyLLM struct {
	ID         string  `gorm:"column:id" json:"id"`
	LLMFactory string  `gorm:"column:llm_factory" json:"llm_factory"`
	Logo       *string `gorm:"column:logo" json:"logo,omitempty"`
	Tags       *string `gorm:"column:tags" json:"tags"`
	ModelType  *string `gorm:"column:model_type" json:"model_type"`
	LLMName    *string `gorm:"column:llm_name" json:"llm_name"`
	UsedTokens *int64  `gorm:"column:used_tokens" json:"used_tokens"`
	Status     *string `gorm:"column:status" json:"status"`
	APIBase    *string `gorm:"column:api_base" json:"api_base,omitempty"`
	MaxTokens  *int64  `gorm:"column:max_tokens" json:"max_tokens,omitempty"`
}

// 模块小结：LLMFactories 与 LLM 为全局模型注册表；TenantLangfuse 按 tenant_id 主键存储追踪密钥；MyLLM 为 JOIN 查询投影，供租户模型管理页展示用量与状态。
