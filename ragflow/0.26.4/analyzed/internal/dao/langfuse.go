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
// langfuse.go — 租户 Langfuse 可观测性凭证 DAO：管理 public/secret key 与 host 的 CRUD 及 upsert 保存。

//

package dao

import (
	"errors"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	"ragflow/internal/entity"
)

// LangfuseDAO 租户 Langfuse 集成凭证的数据访问对象。
type LangfuseDAO struct{}

// NewLangfuse 创建 LangfuseDAO 实例。
func NewLangfuse() *LangfuseDAO {
	return &LangfuseDAO{}
}

// GetByTenantID 按租户查询凭证；无记录时返回 (nil, nil)，对齐 Python DoesNotExist→None。
func (dao *LangfuseDAO) GetByTenantID(tenantID string) (*entity.TenantLangfuse, error) {
	var row entity.TenantLangfuse
	err := DB.Where("tenant_id = ?", tenantID).First(&row).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &row, nil
}

// Create 插入新的 Langfuse 凭证行。
func (dao *LangfuseDAO) Create(row *entity.TenantLangfuse) error {
	return DB.Create(row).Error
}

// UpdateByTenantID 按 tenant_id 部分更新凭证字段。
func (dao *LangfuseDAO) UpdateByTenantID(tenantID string, updates map[string]any) error {
	res := DB.Model(&entity.TenantLangfuse{}).Where("tenant_id = ?", tenantID).Updates(updates)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

// DeleteByTenantID 删除租户凭证行，无记录时返回 ErrRecordNotFound。
func (dao *LangfuseDAO) DeleteByTenantID(tenantID string) error {
	res := DB.Where("tenant_id = ?", tenantID).Delete(&entity.TenantLangfuse{})
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

// SaveByTenantID 按 tenant_id upsert 凭证（冲突时更新 key 与 host）。
func (dao *LangfuseDAO) SaveByTenantID(row *entity.TenantLangfuse) error {
	return DB.Clauses(clause.OnConflict{
		Columns: []clause.Column{{Name: "tenant_id"}},
		DoUpdates: clause.Assignments(map[string]any{
			"secret_key": row.SecretKey,
			"public_key": row.PublicKey,
			"host":       row.Host,
		}),
	}).Create(row).Error
}

// DeleteExistingByTenantID 事务内删除已存在的租户凭证行。
func (dao *LangfuseDAO) DeleteExistingByTenantID(tenantID string) error {
	return DB.Transaction(func(tx *gorm.DB) error {
		var row entity.TenantLangfuse
		err := tx.Where("tenant_id = ?", tenantID).First(&row).Error
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return gorm.ErrRecordNotFound
			}
			return err
		}
		return tx.Delete(&row).Error
	})
}
