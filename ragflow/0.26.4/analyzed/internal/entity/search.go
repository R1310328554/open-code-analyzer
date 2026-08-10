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

// search.go — 搜索应用实体：租户级可配置搜索应用，search_config 存检索参数 JSON。
//

package entity

// Search 搜索应用 GORM 实体（表 search）
type Search struct {
	// ID 搜索应用主键
	ID           string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	Avatar       *string `gorm:"column:avatar;type:longtext" json:"avatar,omitempty"`
	// TenantID 所属租户
	TenantID     string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	// Name 应用名称
	Name         string  `gorm:"column:name;size:128;not null;index" json:"name"`
	Description  *string `gorm:"column:description;type:longtext" json:"description,omitempty"`
	CreatedBy    string  `gorm:"column:created_by;size:32;not null;index" json:"created_by"`
	// SearchConfig 检索配置 JSON（索引、过滤、排序等）
	SearchConfig JSONMap `gorm:"column:search_config;type:longtext;not null" json:"search_config"`
	Status       *string `gorm:"column:status;size:1;index" json:"status,omitempty"`
	BaseModel
}

// TableName 返回 GORM 表名 search
func (Search) TableName() string {
	return "search"
}

// Search 由租户用户创建；status 软删除标记；search_config 与前端搜索应用配置 schema 对应。嵌入 BaseModel 提供时间戳字段。
