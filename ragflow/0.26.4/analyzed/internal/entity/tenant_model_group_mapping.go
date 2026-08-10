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

// tenant_model_group_mapping.go — 模型组成员映射：联合主键绑定 group、provider、instance 与 model，含权重与状态。
//

package entity

// TenantModelGroupMapping 模型组-模型映射 GORM 实体（表 tenant_model_group_mapping）
type TenantModelGroupMapping struct {
	// GroupID 所属模型组
	GroupID    string `gorm:"column:group_id;primaryKey;size:32;index" json:"group_id"`
	// ProviderID 模型提供商
	ProviderID string `gorm:"column:provider_id;primaryKey;size:32" json:"provider_id"`
	// InstanceID 凭据实例
	InstanceID string `gorm:"column:instance_id;primaryKey;size:32" json:"instance_id"`
	// ModelID 租户模型条目
	ModelID    string `gorm:"column:model_id;primaryKey;size:32;index" json:"model_id"`
	// Weight 加权路由权重（默认 100）
	Weight     int    `gorm:"column:weight;default:100" json:"weight"`
	// Status 映射状态（active 参与路由）
	Status     string `gorm:"column:status;size:32;default:'active'" json:"status"`
	BaseModel
}

// TableName 返回 GORM 表名 tenant_model_group_mapping
func (TenantModelGroupMapping) TableName() string {
	return "tenant_model_group_mapping"
}

// 四列联合主键保证组内模型唯一；Selector 按 weight 做 weighted 随机或 failover 排序。
