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
//

// canvas.go — Agent 画布实体：用户画布、模板库与版本快照；DSL 存 JSONMap 工作流图。

package entity

// UserCanvas 用户 Agent 画布（权限、发布状态、DSL）
type UserCanvas struct {
	ID             string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	Avatar         *string `gorm:"column:avatar;type:longtext" json:"avatar,omitempty"`
	UserID         string  `gorm:"column:user_id;size:255;not null;index" json:"user_id"`
	Tags           string  `gorm:"column:tags;size:512;not null;default:'';index" json:"tags"`
	Title          *string `gorm:"column:title;size:255" json:"title,omitempty"`
	Permission     string  `gorm:"column:permission;size:16;not null;default:me;index" json:"permission"`
	Release        bool    `gorm:"column:release;not null;default:false;index" json:"release"`
	Description    *string `gorm:"column:description;type:longtext" json:"description,omitempty"`
	CanvasType     *string `gorm:"column:canvas_type;size:32;index" json:"canvas_type,omitempty"`
	CanvasCategory string  `gorm:"column:canvas_category;size:32;not null;default:agent_canvas;index" json:"canvas_category"`
	DSL            JSONMap `gorm:"column:dsl;type:longtext" json:"dsl,omitempty"`
	BaseModel
}

// TableName specify table name
func (UserCanvas) TableName() string {
	return "user_canvas"
}

// CanvasTemplate 可复用画布模板（多语言 title/description）
type CanvasTemplate struct {
	ID             string    `gorm:"column:id;primaryKey;size:32" json:"id"`
	Avatar         *string   `gorm:"column:avatar;type:longtext" json:"avatar,omitempty"`
	Title          JSONMap   `gorm:"column:title;type:longtext" json:"title"`
	Description    JSONMap   `gorm:"column:description;type:longtext" json:"description"`
	CanvasType     *string   `gorm:"column:canvas_type;size:32;index" json:"canvas_type,omitempty"`
	CanvasTypes    JSONSlice `gorm:"column:canvas_types;type:longtext" json:"canvas_types,omitempty"`
	CanvasCategory string    `gorm:"column:canvas_category;size:32;not null;default:agent_canvas;index" json:"canvas_category"`
	DSL            JSONMap   `gorm:"column:dsl;type:longtext" json:"dsl,omitempty"`
	BaseModel
}

// TableName specify table name
func (CanvasTemplate) TableName() string {
	return "canvas_template"
}

// UserCanvasVersion 画布历史版本快照
type UserCanvasVersion struct {
	ID           string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	UserCanvasID string  `gorm:"column:user_canvas_id;size:255;not null;index" json:"user_canvas_id"`
	Title        *string `gorm:"column:title;size:255" json:"title,omitempty"`
	Description  *string `gorm:"column:description;type:longtext" json:"description,omitempty"`
	Release      bool    `gorm:"column:release;not null;default:false;index" json:"release"`
	DSL          JSONMap `gorm:"column:dsl;type:longtext" json:"dsl,omitempty"`
	BaseModel
}

// TableName specify table name
func (UserCanvasVersion) TableName() string {
	return "user_canvas_version"
}

// canvas_category 默认 agent_canvas；UserCanvas.permission 与 kb 包 TenantPermission 语义一致（me/team）。CanvasTemplate.title/description 使用 JSONMap 支持多语言模板库；UserCanvasVersion 在 release=true 时可作为对外发布快照。DSL 字段存储 Agent 工作流图 JSON，与 internal/agent/canvas 运行时共享 schema。
