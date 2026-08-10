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

// tenant_llm.go — 租户 LLM 凭据实体：按租户+厂商+模型名唯一索引，存储 API Key 与 token 用量。
//

package entity

// TenantLLM 租户 LLM 配置 GORM 实体（表 tenant_llm）
// 与 Python 版对齐：自增主键 id，(tenant_id, llm_factory, llm_name) 联合唯一索引
type TenantLLM struct {
	// ID 自增主键
	ID         int64   `gorm:"column:id;primaryKey;autoIncrement" json:"id"`
	// TenantID 所属租户
	TenantID   string  `gorm:"column:tenant_id;size:32;not null;index:idx_tenant_llm_unique,unique" json:"tenant_id"`
	// LLMFactory 模型厂商/工厂名
	LLMFactory string  `gorm:"column:llm_factory;size:128;not null;index:idx_tenant_llm_unique,unique" json:"llm_factory"`
	// ModelType 模型类型（chat/embedding 等）
	ModelType  *string `gorm:"column:model_type;size:128;index" json:"model_type,omitempty"`
	// LLMName 具体模型名（空串表示厂商级默认）
	LLMName    *string `gorm:"column:llm_name;size:128;index:idx_tenant_llm_unique,unique;default:\"\"" json:"llm_name,omitempty"`
	// APIKey 租户自有 API 密钥
	APIKey     *string `gorm:"column:api_key;type:longtext" json:"api_key,omitempty"`
	// APIBase 自定义 API 端点
	APIBase    *string `gorm:"column:api_base;size:255" json:"api_base,omitempty"`
	// MaxTokens 单次请求 token 上限
	MaxTokens  int64   `gorm:"column:max_tokens;default:8192;index" json:"max_tokens"`
	// UsedTokens 累计已消耗 token
	UsedTokens int64   `gorm:"column:used_tokens;default:0;index" json:"used_tokens"`
	// Status 启用状态
	Status     string  `gorm:"column:status;size:1;not null;default:1;index" json:"status"`
	BaseModel
}

// TableName 返回 GORM 表名 tenant_llm
func (TenantLLM) TableName() string {
	return "tenant_llm"
}

// 计费与配额：used_tokens 在每次 LLM 调用后累加。tenant 表的 tenant_llm_id 等外键指向本表 id。
