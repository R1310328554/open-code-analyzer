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

// memory.go — Agent 长期记忆实体：嵌入/LLM 绑定、遗忘策略、权限与提示词模板。
//

package entity

// Memory Agent 记忆库配置（向量模型、LLM、容量、FIFO 遗忘策略、温度）
type Memory struct {
	ID               string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	Name             string  `gorm:"column:name;size:128;not null" json:"name"`
	Avatar           *string `gorm:"column:avatar;type:longtext" json:"avatar,omitempty"`
	TenantID         string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	MemoryType       int64   `gorm:"column:memory_type;default:1;index" json:"memory_type"`
	StorageType      string  `gorm:"column:storage_type;size:32;not null;default:table;index" json:"storage_type"`
	EmbdID           string  `gorm:"column:embd_id;size:128;not null" json:"embd_id"`
	TenantEmbdID     *int64  `gorm:"column:tenant_embd_id;index" json:"tenant_embd_id,omitempty"`
	LLMID            string  `gorm:"column:llm_id;size:128;not null" json:"llm_id"`
	TenantLLMID      *int64  `gorm:"column:tenant_llm_id;index" json:"tenant_llm_id,omitempty"`
	Permissions      string  `gorm:"column:permissions;size:16;not null;default:me;index" json:"permissions"`
	Description      *string `gorm:"column:description;type:longtext" json:"description,omitempty"`
	MemorySize       int64   `gorm:"column:memory_size;default:5242880;not null" json:"memory_size"`
	ForgettingPolicy string  `gorm:"column:forgetting_policy;size:32;not null;default:FIFO" json:"forgetting_policy"`
	Temperature      float64 `gorm:"column:temperature;default:0.5;not null" json:"temperature"`
	SystemPrompt     *string `gorm:"column:system_prompt;type:longtext" json:"system_prompt,omitempty"`
	UserPrompt       *string `gorm:"column:user_prompt;type:longtext" json:"user_prompt,omitempty"`
	BaseModel
}

// TableName 返回表名 memory
func (Memory) TableName() string {
	return "memory"
}

// MemoryListItem 记忆列表项：嵌入 Memory 并附加 owner_name（来自 user 表 JOIN）；MemoryType 在 Service 层转为 []string 供 API 响应。
type MemoryListItem struct {
	Memory
	OwnerName *string `json:"owner_name,omitempty"`
}

// storage_type 默认 table；permissions 控制 me/team 可见性；forgetting_policy 默认 FIFO；memory_size 默认 5MB。embd_id/llm_id 关联租户或平台级模型配置。
