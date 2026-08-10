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
// skill_search_config.go — 技能搜索配置数据访问层：管理租户/空间维度的向量检索参数、字段权重及默认配置懒创建。

//

package dao

import (
	"ragflow/internal/entity"
	"ragflow/internal/utility"
	"strings"
)

// SkillSearchConfigDAO 技能搜索配置表的数据访问对象。
type SkillSearchConfigDAO struct{}

// defaultSkillSpaceID 未指定 space_id 时的默认空间标识
const defaultSkillSpaceID = "default"

// normalizeSpaceID 去除首尾空白，空字符串回退为 defaultSkillSpaceID。
func normalizeSpaceID(spaceID string) string {
	spaceID = strings.TrimSpace(spaceID)
	if spaceID == "" {
		return defaultSkillSpaceID
	}
	return spaceID
}

// NewSkillSearchConfigDAO 创建技能搜索配置 DAO 实例。
func NewSkillSearchConfigDAO() *SkillSearchConfigDAO {
	return &SkillSearchConfigDAO{}
}

// Create 插入新技能搜索配置。
func (dao *SkillSearchConfigDAO) Create(config *entity.SkillSearchConfig) error {
	return DB.Create(config).Error
}

// GetByID 按主键查询有效（status=1）配置。
func (dao *SkillSearchConfigDAO) GetByID(id string) (*entity.SkillSearchConfig, error) {
	var config entity.SkillSearchConfig
	err := DB.Where("id = ? AND status = ?", id, "1").First(&config).Error
	if err != nil {
		return nil, err
	}
	return &config, nil
}

// GetByTenantID 按租户 ID 与空间 ID 查询单条有效配置。
func (dao *SkillSearchConfigDAO) GetByTenantID(tenantID, spaceID string) (*entity.SkillSearchConfig, error) {
	var config entity.SkillSearchConfig
	err := DB.Where("tenant_id = ? AND space_id = ? AND status = ?", tenantID, normalizeSpaceID(spaceID), "1").First(&config).Error
	if err != nil {
		return nil, err
	}
	return &config, nil
}

// GetLatestByTenantID 按 update_time 降序取最新配置；优先返回 embd_id 非空的用户保存配置，而非自动创建的占位记录。
func (dao *SkillSearchConfigDAO) GetLatestByTenantID(tenantID, spaceID string) (*entity.SkillSearchConfig, error) {
	var config entity.SkillSearchConfig
	// 优先查找 embd_id 非空的用户保存配置
	err := DB.Where("tenant_id = ? AND space_id = ? AND status = ? AND embd_id != ?", tenantID, normalizeSpaceID(spaceID), "1", "").Order("update_time desc").First(&config).Error
	if err == nil {
		return &config, nil
	}
	// 若无用户配置则回退为任意最新有效配置
	err = DB.Where("tenant_id = ? AND space_id = ? AND status = ?", tenantID, normalizeSpaceID(spaceID), "1").Order("update_time desc").First(&config).Error
	if err != nil {
		return nil, err
	}
	return &config, nil
}

// GetByTenantAndEmbdID 按租户、空间与 embedding 模型 ID 精确查询。
func (dao *SkillSearchConfigDAO) GetByTenantAndEmbdID(tenantID, spaceID, embdID string) (*entity.SkillSearchConfig, error) {
	var config entity.SkillSearchConfig
	err := DB.Where("tenant_id = ? AND space_id = ? AND embd_id = ? AND status = ?", tenantID, normalizeSpaceID(spaceID), embdID, "1").First(&config).Error
	if err != nil {
		return nil, err
	}
	return &config, nil
}

// GetOrCreate 存在则返回，否则按租户+空间+embd_id 创建默认配置。
func (dao *SkillSearchConfigDAO) GetOrCreate(tenantID, spaceID, embdID string) (*entity.SkillSearchConfig, error) {
	spaceID = normalizeSpaceID(spaceID)
	config, err := dao.GetByTenantAndEmbdID(tenantID, spaceID, embdID)
	if err == nil {
		return config, nil
	}

	// 组装默认 field_config 与相似度参数后入库
	return dao.CreateWithTenantSpace(tenantID, spaceID, embdID)
}

// CreateWithTenantSpace 为租户+空间创建带默认字段权重与阈值的配置。
func (dao *SkillSearchConfigDAO) CreateWithTenantSpace(tenantID, spaceID, embdID string) (*entity.SkillSearchConfig, error) {
	spaceID = normalizeSpaceID(spaceID)
	defaultFieldConfig := entity.DefaultFieldConfig()
	fieldConfigMap := entity.JSONMap{
		"name": map[string]interface{}{
			"enabled": defaultFieldConfig.Name.Enabled,
			"weight":  defaultFieldConfig.Name.Weight,
		},
		"tags": map[string]interface{}{
			"enabled": defaultFieldConfig.Tags.Enabled,
			"weight":  defaultFieldConfig.Tags.Weight,
		},
		"description": map[string]interface{}{
			"enabled": defaultFieldConfig.Description.Enabled,
			"weight":  defaultFieldConfig.Description.Weight,
		},
		"content": map[string]interface{}{
			"enabled": defaultFieldConfig.Content.Enabled,
			"weight":  defaultFieldConfig.Content.Weight,
		},
	}

	defaultConfig := &entity.SkillSearchConfig{
		ID:                     utility.GenerateUUID(),
		TenantID:               tenantID,
		SpaceID:                spaceID,
		EmbdID:                 embdID,
		VectorSimilarityWeight: 0.3,
		SimilarityThreshold:    0.2,
		FieldConfig:            fieldConfigMap,
		TopK:                   10,
		Status:                 "1",
	}

	if err := dao.Create(defaultConfig); err != nil {
		return nil, err
	}
	return defaultConfig, nil
}

// DeleteAllByTenantSpace 软删除租户+空间下全部配置（新建前清理用）。
func (dao *SkillSearchConfigDAO) DeleteAllByTenantSpace(tenantID, spaceID string) error {
	spaceID = normalizeSpaceID(spaceID)
	return DB.Model(&entity.SkillSearchConfig{}).
		Where("tenant_id = ? AND space_id = ?", tenantID, spaceID).
		Update("status", "0").Error
}

// DeleteAllByTenantSpaceExceptID 软删除除指定 ID 外该租户+空间下的全部有效配置。
func (dao *SkillSearchConfigDAO) DeleteAllByTenantSpaceExceptID(tenantID, spaceID, exceptID string) error {
	spaceID = normalizeSpaceID(spaceID)
	return DB.Model(&entity.SkillSearchConfig{}).
		Where("tenant_id = ? AND space_id = ? AND id != ? AND status = ?", tenantID, spaceID, exceptID, "1").
		Update("status", "0").Error
}

// Update 按 ID 部分更新有效配置。
func (dao *SkillSearchConfigDAO) Update(id string, updates map[string]interface{}) error {
	return DB.Model(&entity.SkillSearchConfig{}).Where("id = ? AND status = ?", id, "1").Updates(updates).Error
}

// UpdateByTenantID 按租户+空间更新有效配置。
func (dao *SkillSearchConfigDAO) UpdateByTenantID(tenantID, spaceID string, updates map[string]interface{}) error {
	result := DB.Model(&entity.SkillSearchConfig{}).Where("tenant_id = ? AND space_id = ? AND status = ?", tenantID, normalizeSpaceID(spaceID), "1").Updates(updates)
	return result.Error
}

// UpdateByTenantAndEmbdID 按租户、空间与 embd_id 部分更新。
func (dao *SkillSearchConfigDAO) UpdateByTenantAndEmbdID(tenantID, spaceID, embdID string, updates map[string]interface{}) error {
	result := DB.Model(&entity.SkillSearchConfig{}).Where("tenant_id = ? AND space_id = ? AND embd_id = ? AND status = ?", tenantID, normalizeSpaceID(spaceID), embdID, "1").Updates(updates)
	return result.Error
}

// Delete 按 ID 软删除配置（status 置 0）。
func (dao *SkillSearchConfigDAO) Delete(id string) error {
	return DB.Model(&entity.SkillSearchConfig{}).Where("id = ?", id).Update("status", "0").Error
}
