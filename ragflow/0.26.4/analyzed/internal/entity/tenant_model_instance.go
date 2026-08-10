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

// tenant_model_instance.go — 租户模型凭证实例：同一 provider 下可配置多个 API Key 实例（主备/分区域）。
//

package entity

// TenantModelInstance 模型实例 GORM 实体（表 tenant_model_instance）
type TenantModelInstance struct {
	// ID 实例主键
	ID           string `gorm:"column:id;primaryKey;size:32" json:"id"`
	// InstanceName 实例显示名
	InstanceName string `gorm:"column:instance_name;size:128;not null" json:"instance_name"`
	// ProviderID 所属 provider
	ProviderID   string `gorm:"column:provider_id;size:32;not null" json:"provider_id"`
	// APIKey 该实例使用的 API 密钥
	APIKey       string `gorm:"column:api_key;size:512;not null" json:"api_key"`
	// Status 实例状态
	Status       string `gorm:"column:status;size:32;default:'active'" json:"status"`
	// Extra 扩展配置 JSON（如 base_url、region）
	Extra        string `gorm:"column:extra;size:512;default:'{}'" json:"extra"`
	BaseModel
}

// TableName 返回 GORM 表名 tenant_model_instance
func (TenantModelInstance) TableName() string {
	return "tenant_model_instance"
}

// 敏感字段 api_key 仅在服务端使用；多个 model 可共享同一 instance 以降低密钥管理成本。
