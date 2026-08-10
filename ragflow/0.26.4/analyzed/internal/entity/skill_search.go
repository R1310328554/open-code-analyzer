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

// skill_search.go — 技能搜索配置与结果类型：租户/空间级向量+BM25 混合检索参数与字段权重。
//

package entity

import "time"

// FieldWeight 单字段是否启用及 BM25/向量融合权重
type FieldWeight struct {
	Enabled bool    `json:"enabled"`
	Weight  float64 `json:"weight"`
}

// FieldConfig 技能索引字段权重（name/tags/description/content）
type FieldConfig struct {
	Name        FieldWeight `json:"name"`
	Tags        FieldWeight `json:"tags"`
	Description FieldWeight `json:"description"`
	Content     FieldWeight `json:"content"`
}

// DefaultFieldConfig 返回默认字段权重配置
func DefaultFieldConfig() FieldConfig {
	return FieldConfig{
		Name:        FieldWeight{Enabled: true, Weight: 3.0},
		Tags:        FieldWeight{Enabled: true, Weight: 2.0},
		Description: FieldWeight{Enabled: true, Weight: 1.0},
		Content:     FieldWeight{Enabled: false, Weight: 0.5},
	}
}

// SkillSearchConfig 技能搜索配置 GORM 实体（表 skill_search_configs）
type SkillSearchConfig struct {
	ID                     string  `gorm:"column:id;primaryKey;size:32" json:"id"`
	TenantID               string  `gorm:"column:tenant_id;size:32;not null;index" json:"tenant_id"`
	SpaceID                string  `gorm:"column:space_id;size:128;not null;default:'default';index" json:"space_id"`
	EmbdID                 string  `gorm:"column:embd_id;size:128;not null" json:"embd_id"`
	Status                 string  `gorm:"column:status;size:1;default:1" json:"status"`
	// VectorSimilarityWeight 向量相似度在混合分中的权重
	VectorSimilarityWeight float64 `gorm:"column:vector_similarity_weight;default:0.3" json:"vector_similarity_weight"`
	// SimilarityThreshold 最低相似度阈值
	SimilarityThreshold    float64 `gorm:"column:similarity_threshold;default:0.2" json:"similarity_threshold"`
	FieldConfig            JSONMap `gorm:"column:field_config;type:json" json:"field_config"`
	RerankID               *string `gorm:"column:rerank_id;size:128" json:"rerank_id,omitempty"`
	TenantRerankID         *int64  `gorm:"column:tenant_rerank_id" json:"tenant_rerank_id,omitempty"`
	TopK                   int64   `gorm:"column:top_k;default:10" json:"top_k"`
	IndexVersion           string  `gorm:"column:index_version;size:32;default:'1.0.0'" json:"index_version"`
	BaseModel
}

// TableName 返回 GORM 表名 skill_search_configs
func (SkillSearchConfig) TableName() string {
	return "skill_search_configs"
}

// ToMap 转为 API JSON 响应 map（格式化 update_time）
func (s *SkillSearchConfig) ToMap() map[string]interface{} {
	result := map[string]interface{}{
		"id":                       s.ID,
		"tenant_id":                s.TenantID,
		"space_id":                 s.SpaceID,
		"embd_id":                  s.EmbdID,
		"vector_similarity_weight": s.VectorSimilarityWeight,
		"similarity_threshold":     s.SimilarityThreshold,
		"field_config":             s.FieldConfig,
		"top_k":                    s.TopK,
		"index_version":            s.IndexVersion,
		"status":                   s.Status,
	}

	if s.RerankID != nil {
		result["rerank_id"] = *s.RerankID
	}
	if s.TenantRerankID != nil {
		result["tenant_rerank_id"] = *s.TenantRerankID
	}
	if s.CreateTime != nil {
		result["create_time"] = s.CreateTime
	}
	if s.UpdateTime != nil {
		result["update_time"] = time.UnixMilli(*s.UpdateTime).Format("2006-01-02 15:04:05")
	}

	return result
}

// SkillSearchResult 单次技能检索命中结果
type SkillSearchResult struct {
	SkillID      string   `json:"skill_id"`
	FolderID     string   `json:"folder_id"` // 文件系统目录 ID，用于拉取技能文件
	Name         string   `json:"name"`
	Description  string   `json:"description"`
	Tags         []string `json:"tags"`
	Score        float64  `json:"score"`
	BM25Score    float64  `json:"bm25_score,omitempty"`
	VectorScore  float64  `json:"vector_score,omitempty"`
	IndexVersion string   `json:"index_version,omitempty"`
	CreateTime   int64    `json:"create_time,omitempty"`
	Version      string   `json:"version,omitempty"` // 索引中的技能版本号
}

// SkillSearchConfig 按 tenant_id+space_id 隔离；field_config 存 JSONMap 字段权重；rerank_id 可选二次重排。SkillSearchResult 同时返回 BM25 与向量分项得分。
