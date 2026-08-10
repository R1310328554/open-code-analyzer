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

// tenant_model.go — 租户模型目录实体：将 provider+instance 下的具体模型注册为租户可用条目。
//

package entity

// TenantModel 租户模型 GORM 实体（表 tenant_model）
type TenantModel struct {
	// ID 模型条目主键
	ID         string `gorm:"column:id;primaryKey;size:32" json:"id"`
	// ModelName 上游模型名称
	ModelName  string `gorm:"column:model_name;size:128" json:"model_name"`
	// ProviderID 关联 tenant_model_provider
	ProviderID string `gorm:"column:provider_id;size:32;not null" json:"provider_id"`
	// InstanceID 关联 tenant_model_instance（含 API Key）
	InstanceID string `gorm:"column:instance_id;size:32;not null;index" json:"instance_id"`
	// ModelType 模型类型（chat/embedding/rerank 等）
	ModelType  string `gorm:"column:model_type;size:32;not null" json:"model_type"`
	// Status 条目状态（active/inactive）
	Status     string `gorm:"column:status;size:32;default:'active'" json:"status"`
	// Extra 扩展 JSON 配置
	Extra      string `gorm:"column:extra;size:1024;default:'{}'" json:"extra"`
	BaseModel
}

// TableName 返回 GORM 表名 tenant_model
func (TenantModel) TableName() string {
	return "tenant_model"
}

// 新模型管理栈：provider → instance → model 三级引用。路由层按 model_type 与 status 过滤可用模型。
