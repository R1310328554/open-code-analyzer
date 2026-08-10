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

// skill_space.go — 技能空间实体：租户级技能库，绑定文件夹、嵌入/重排模型与 TopK 检索参数。
//

package entity

import "time"

// 技能空间状态常量（字符串枚举，与 DB status 列对应）
const (
	SpaceStatusActive   = "1" // 正常可用空间
	SpaceStatusDeleted  = "0" // 已软删除空间
	SpaceStatusDeleting = "2" // 异步删除进行中
)

// SkillSpace 技能空间 GORM 实体（表 skill_spaces），承载一组技能及其检索配置
type SkillSpace struct {
	// ID 空间主键
	ID          string `gorm:"column:id;primaryKey;size:32" json:"id"`
	// TenantID 所属租户
	TenantID    string `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	// Name 空间显示名称
	Name        string `gorm:"column:name;size:128;not null" json:"name"`
	// FolderID 关联文件系统目录，技能文件存放于此
	FolderID    string `gorm:"column:folder_id;size:32;not null" json:"folder_id"`
	// Description 空间描述
	Description string `gorm:"column:description;type:text" json:"description"`
	// EmbdID 技能检索使用的嵌入模型 ID
	EmbdID      string `gorm:"column:embd_id;size:128" json:"embd_id"`
	// RerankID 可选重排模型 ID
	RerankID    string `gorm:"column:rerank_id;size:128" json:"rerank_id"`
	// TopK 默认检索返回条数
	TopK        int    `gorm:"column:top_k;default:10" json:"top_k"`
	// Status 空间状态（见 SpaceStatus* 常量）
	Status      string `gorm:"column:status;size:1;default:1" json:"status"`
	BaseModel
}

// TableName 返回 GORM 表名 skill_spaces
func (SkillSpace) TableName() string {
	return "skill_spaces"
}

// StatusDescription 将 status 码转为英文可读字符串（active/deleted/deleting）
func (s *SkillSpace) StatusDescription() string {
	switch s.Status {
	case SpaceStatusActive:
		return "active"
	case SpaceStatusDeleted:
		return "deleted"
	case SpaceStatusDeleting:
		return "deleting"
	default:
		return "unknown"
	}
}

// ToMap 转为 API JSON 响应 map，省略空字段并格式化 update_time
func (s *SkillSpace) ToMap() map[string]interface{} {
	result := map[string]interface{}{
		"id":        s.ID,
		"tenant_id": s.TenantID,
		"name":      s.Name,
		"folder_id": s.FolderID,
		"top_k":     s.TopK,
		"status":    s.StatusDescription(),
	}

	if s.Description != "" {
		result["description"] = s.Description
	}
	if s.EmbdID != "" {
		result["embd_id"] = s.EmbdID
	}
	if s.RerankID != "" {
		result["rerank_id"] = s.RerankID
	}
	if s.CreateTime != nil {
		result["create_time"] = s.CreateTime
	}
	if s.UpdateTime != nil {
		result["update_time"] = time.UnixMilli(*s.UpdateTime).Format("2006-01-02 15:04:05")
	}

	return result
}

// 删除为软删+异步清理：StatusDeleting 期间不可写入新技能。ToMap 仅在有值时输出 description/embd_id/rerank_id。嵌入 BaseModel 继承 create/update 时间戳。
