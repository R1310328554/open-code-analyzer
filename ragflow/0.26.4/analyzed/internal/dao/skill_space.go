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
// skill_space.go — 技能空间（Skill Space）数据访问层：管理租户下技能命名空间的 CRUD、软删除、CAS 状态切换及物理清理。

//

package dao

import (
	"ragflow/internal/entity"
)

// SkillSpaceDAO 技能空间表的数据访问对象。
type SkillSpaceDAO struct{}

// NewSkillSpaceDAO 创建技能空间 DAO 实例。
func NewSkillSpaceDAO() *SkillSpaceDAO {
	return &SkillSpaceDAO{}
}

// Create 插入新技能空间记录。
func (dao *SkillSpaceDAO) Create(space *entity.SkillSpace) error {
	return DB.Create(space).Error
}

// GetByID 按 ID 查询有效（active）技能空间。
func (dao *SkillSpaceDAO) GetByID(id string) (*entity.SkillSpace, error) {
	var space entity.SkillSpace
	err := DB.Where("id = ? AND status = ?", id, entity.SpaceStatusActive).First(&space).Error
	if err != nil {
		return nil, err
	}
	return &space, nil
}

// GetByTenantID 列出租户下全部有效技能空间，按 create_time 降序。
func (dao *SkillSpaceDAO) GetByTenantID(tenantID string) ([]*entity.SkillSpace, error) {
	var spaces []*entity.SkillSpace
	err := DB.Where("tenant_id = ? AND status = ?", tenantID, entity.SpaceStatusActive).Order("create_time DESC").Find(&spaces).Error
	return spaces, err
}

// GetByTenantAndName 按租户 ID 与名称查询有效技能空间。
func (dao *SkillSpaceDAO) GetByTenantAndName(tenantID, name string) (*entity.SkillSpace, error) {
	var space entity.SkillSpace
	err := DB.Where("tenant_id = ? AND name = ? AND status = ?", tenantID, name, entity.SpaceStatusActive).First(&space).Error
	if err != nil {
		return nil, err
	}
	return &space, nil
}

// GetByTenantAndNameAnyStatus 按租户与名称查询，不限制 status。
func (dao *SkillSpaceDAO) GetByTenantAndNameAnyStatus(tenantID, name string) (*entity.SkillSpace, error) {
	var space entity.SkillSpace
	err := DB.Where("tenant_id = ? AND name = ?", tenantID, name).First(&space).Error
	if err != nil {
		return nil, err
	}
	return &space, nil
}

// GetByIDAnyStatus 按 ID 查询，不限制 status。
func (dao *SkillSpaceDAO) GetByIDAnyStatus(id string) (*entity.SkillSpace, error) {
	var space entity.SkillSpace
	err := DB.Where("id = ?", id).First(&space).Error
	if err != nil {
		return nil, err
	}
	return &space, nil
}

// GetByFolderID 按关联文件夹 ID 查询有效技能空间。
func (dao *SkillSpaceDAO) GetByFolderID(folderID string) (*entity.SkillSpace, error) {
	var space entity.SkillSpace
	err := DB.Where("folder_id = ? AND status = ?", folderID, entity.SpaceStatusActive).First(&space).Error
	if err != nil {
		return nil, err
	}
	return &space, nil
}

// Update 全量保存技能空间实体。
func (dao *SkillSpaceDAO) Update(space *entity.SkillSpace) error {
	return DB.Save(space).Error
}

// UpdateByID 按 ID 部分更新字段。
func (dao *SkillSpaceDAO) UpdateByID(id string, updates map[string]interface{}) error {
	return DB.Model(&entity.SkillSpace{}).Where("id = ?", id).Updates(updates).Error
}

// Delete 软删除技能空间（status 置为已删除）。
func (dao *SkillSpaceDAO) Delete(id string) error {
	return DB.Model(&entity.SkillSpace{}).Where("id = ?", id).Update("status", entity.SpaceStatusDeleted).Error
}

// CASStatus 原子比较并交换空间 status；当前 status 与 expected 一致时更新为 newStatus 并返回 true。
func (dao *SkillSpaceDAO) CASStatus(id string, expectedStatus, newStatus string) (bool, error) {
	result := DB.Model(&entity.SkillSpace{}).
		Where("id = ? AND status = ?", id, expectedStatus).
		Update("status", newStatus)
	if result.Error != nil {
		return false, result.Error
	}
	return result.RowsAffected > 0, nil
}

// DeletePermanentByName 物理删除已软删除（status=0）的同名空间，用于清理历史残留；不会删除仍有效的空间。
func (dao *SkillSpaceDAO) DeletePermanentByName(tenantID, name string) error {
	return DB.Unscoped().Where("tenant_id = ? AND name = ? AND status = ?", tenantID, name, entity.SpaceStatusDeleted).Delete(&entity.SkillSpace{}).Error
}

// CountByTenant 统计租户下有效技能空间数量。
func (dao *SkillSpaceDAO) CountByTenant(tenantID string) (int64, error) {
	var count int64
	err := DB.Model(&entity.SkillSpace{}).Where("tenant_id = ? AND status = ?", tenantID, entity.SpaceStatusActive).Count(&count).Error
	return count, err
}
