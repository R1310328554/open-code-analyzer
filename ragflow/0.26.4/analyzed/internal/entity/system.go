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

// system.go — 系统级键值配置实体：全局 settings 表，按 name 主键存储各模块运行时参数。
//

package entity

// SystemSettings 系统设置 GORM 实体（表 system_settings）
type SystemSettings struct {
	// Name 配置项键名（主键）
	Name     string `gorm:"column:name;primaryKey;size:128" json:"name"`
	// Source 配置来源模块标识
	Source   string `gorm:"column:source;size:32;not null" json:"source"`
	// DataType 值类型（string/int/json 等），供反序列化路由
	DataType string `gorm:"column:data_type;size:32;not null" json:"data_type"`
	// Value 配置值（longtext，可为 JSON 字符串）
	Value    string `gorm:"column:value;type:longtext;not null" json:"value"`
	BaseModel
}

// TableName 返回 GORM 表名 system_settings
func (SystemSettings) TableName() string {
	return "system_settings"
}

// 与租户级配置分离：此处存平台/安装级开关。DAO 层按 name 点查；value 解释依赖 data_type 字段。
