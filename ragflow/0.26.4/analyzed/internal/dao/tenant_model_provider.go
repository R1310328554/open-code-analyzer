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
// tenant_model_provider.go — 租户模型厂商数据访问层：管理租户已接入的 model provider 及 /api/v1/models 列表入口查询。

//

package dao

import (
	"ragflow/internal/entity"
)

// TenantModelProviderDAO 租户模型厂商表的数据访问对象。
type TenantModelProviderDAO struct{}

// NewTenantModelProviderDAO 创建模型厂商 DAO 实例。
func NewTenantModelProviderDAO() *TenantModelProviderDAO {
	return &TenantModelProviderDAO{}
}

// Create 插入租户与 model provider 的关联记录。
func (dao *TenantModelProviderDAO) Create(provider *entity.TenantModelProvider) error {
	return DB.Create(provider).Error
}

// GetByID 按主键查询 provider 记录。
func (dao *TenantModelProviderDAO) GetByID(id string) (*entity.TenantModelProvider, error) {
	var provider entity.TenantModelProvider
	err := DB.Where("id = ?", id).First(&provider).Error
	if err != nil {
		return nil, err
	}
	return &provider, nil
}

// GetByTenantIDAndProviderName 按租户 ID 与 provider_name 精确查询。
func (dao *TenantModelProviderDAO) GetByTenantIDAndProviderName(tenantID, providerName string) (*entity.TenantModelProvider, error) {
	var provider entity.TenantModelProvider
	err := DB.Where("tenant_id = ? AND provider_name = ?", tenantID, providerName).First(&provider).Error
	if err != nil {
		return nil, err
	}
	return &provider, nil
}

// DeleteByTenantID 按租户 ID 硬删除全部 provider 关联。
func (dao *TenantModelProviderDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ?", tenantID).Delete(&entity.TenantModelProvider{})
	return result.RowsAffected, result.Error
}

// DeleteByTenantIDAndProviderName 按租户与 provider_name 硬删除单条关联。
func (dao *TenantModelProviderDAO) DeleteByTenantIDAndProviderName(tenantID, providerName string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ? AND provider_name = ?", tenantID, providerName).Delete(&entity.TenantModelProvider{})
	return result.RowsAffected, result.Error
}

// ListByID 返回指定租户已接入的全部 provider_name 列表。
func (dao *TenantModelProviderDAO) ListByID(id string) ([]string, error) {
	var providerNames []string
	err := DB.Model(&entity.TenantModelProvider{}).
		Where("tenant_id = ?", id).
		Pluck("provider_name", &providerNames).Error
	return providerNames, err
}

// GetByTenantID 返回租户下全部 provider 行，为 /api/v1/models 列表入口，对齐 Python get_by_tenant_id；后续再联查 instance 与 model 表组装完整响应。
func (dao *TenantModelProviderDAO) GetByTenantID(tenantID string) ([]*entity.TenantModelProvider, error) {
	var providers []*entity.TenantModelProvider
	err := DB.Where("tenant_id = ?", tenantID).Find(&providers).Error
	if err != nil {
		return nil, err
	}
	return providers, nil
}
