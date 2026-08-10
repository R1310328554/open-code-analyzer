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

// tenant_model_group.go — 租户模型组实体：按 group_type 聚合多模型，支持 weighted 等负载策略。
//

package entity

// TenantModelGroup 租户模型组 GORM 实体（表 tenant_model_group）
type TenantModelGroup struct {
	// ID 模型组主键
	ID        string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	// GroupType 组类型（如 chat_default、embedding 等语义标签）
	GroupType string  `gorm:"column:group_type;size:32;not null" json:"group_type"`
	// ModelName 对外暴露的统一模型名（可选）
	ModelName *string `gorm:"column:model_name;size:128" json:"model_name,omitempty"`
	// Strategy 组内路由策略（默认 weighted 加权随机）
	Strategy  string  `gorm:"column:strategy;size:32;default:'weighted'" json:"strategy"`
	BaseModel
}

// TableName 返回 GORM 表名 tenant_model_group
func (TenantModelGroup) TableName() string {
	return "tenant_model_group"
}

// 组成员通过 tenant_model_group_mapping 维护；租户可为一类能力配置主备/多活模型池。
