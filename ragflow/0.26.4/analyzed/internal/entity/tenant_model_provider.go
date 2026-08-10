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

// tenant_model_provider.go — 租户模型提供商：租户维度注册的厂商名，(tenant_id, provider_name) 唯一。
//

package entity

// TenantModelProvider 模型提供商 GORM 实体（表 tenant_model_provider）
type TenantModelProvider struct {
	// ID 提供商主键
	ID           string `gorm:"column:id;primaryKey;size:32" json:"id"`
	// ProviderName 厂商标识（如 OpenAI、Azure）
	ProviderName string `gorm:"column:provider_name;size:128;not null;index:idx_tenant_provider_unique,unique" json:"provider_name"`
	// TenantID 所属租户
	TenantID     string `gorm:"column:tenant_id;size:32;not null;index;index:idx_tenant_provider_unique,unique" json:"tenant_id"`
	BaseModel
}

// TableName 返回 GORM 表名 tenant_model_provider
func (TenantModelProvider) TableName() string {
	return "tenant_model_provider"
}

// 新栈顶层：先建 provider，再挂 instance 与 model。provider_name 与 ModelDriver.Name() 对齐以便工厂路由。
