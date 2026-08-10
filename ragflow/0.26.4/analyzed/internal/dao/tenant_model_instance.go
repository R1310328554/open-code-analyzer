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
// tenant_model_instance.go — 租户模型实例数据访问层：管理 provider 下的 API 实例（含去重创建、按 Key 查询及批量枚举）。

//

package dao

import (
	"errors"
	"fmt"
	"ragflow/internal/entity"

	"gorm.io/gorm"
)

// TenantModelInstanceDAO 租户模型实例表的数据访问对象。
type TenantModelInstanceDAO struct{}

// NewTenantModelInstanceDAO 创建模型实例 DAO 实例。
func NewTenantModelInstanceDAO() *TenantModelInstanceDAO {
	return &TenantModelInstanceDAO{}
}

// Create 在事务中创建实例，同一 provider 下 instance_name 不可重复。
func (dao *TenantModelInstanceDAO) Create(instance *entity.TenantModelInstance) error {
	// 开启事务并检查同 provider 下是否已有同名实例
	tx := DB.Begin()
	defer tx.Rollback()
	var existingInstance entity.TenantModelInstance
	err := tx.Where("provider_id = ? AND instance_name = ?", instance.ProviderID, instance.InstanceName).First(&existingInstance).Error
	if err == nil {
		return fmt.Errorf("instance %s already exists", instance.InstanceName)
	}
	if !errors.Is(err, gorm.ErrRecordNotFound) {
		return err
	}
	err = tx.Create(instance).Error
	if err != nil {
		return err
	}
	tx.Commit()
	return nil
}

// GetAllInstancesByProviderID 列出指定 provider 下的全部实例。
func (dao *TenantModelInstanceDAO) GetAllInstancesByProviderID(providerID string) ([]*entity.TenantModelInstance, error) {
	var instances []*entity.TenantModelInstance
	err := DB.Where("provider_id = ?", providerID).Find(&instances).Error
	if err != nil {
		return nil, err
	}
	return instances, nil
}

// GetByProviderIDs 批量按 provider_id 列表查询实例；空输入返回空切片，对齐 Python get_by_provider_ids。
func (dao *TenantModelInstanceDAO) GetByProviderIDs(providerIDs []string) ([]*entity.TenantModelInstance, error) {
	instances := make([]*entity.TenantModelInstance, 0)
	if len(providerIDs) == 0 {
		return instances, nil
	}
	err := DB.Where("provider_id IN ?", providerIDs).Find(&instances).Error
	if err != nil {
		return nil, err
	}
	return instances, nil
}

// GetInstanceByApiKey 按 api_key 与 provider_id 查询实例。
func (dao *TenantModelInstanceDAO) GetInstanceByApiKey(apiKey, providerID string) (*entity.TenantModelInstance, error) {
	var instance entity.TenantModelInstance
	err := DB.Where("api_key = ? && provider_id = ?", apiKey, providerID).First(&instance).Error
	if err != nil {
		return nil, err
	}
	return &instance, nil
}

// GetByProviderIDAndInstanceName 按 provider 与 instance_name 精确查询。
func (dao *TenantModelInstanceDAO) GetByProviderIDAndInstanceName(providerID, instanceName string) (*entity.TenantModelInstance, error) {
	var instance entity.TenantModelInstance
	err := DB.Where("provider_id = ? AND instance_name = ?", providerID, instanceName).First(&instance).Error
	if err != nil {
		return nil, err
	}
	return &instance, nil
}

// GetByID 按主键查询模型实例。
func (dao *TenantModelInstanceDAO) GetByID(id string) (*entity.TenantModelInstance, error) {
	var instance entity.TenantModelInstance
	err := DB.Where("id = ?", id).First(&instance).Error
	if err != nil {
		return nil, err
	}
	return &instance, nil
}

// DeleteByProviderIDAndInstanceName 按 provider 与 instance_name 硬删除。
func (dao *TenantModelInstanceDAO) DeleteByProviderIDAndInstanceName(providerID, instanceName string) (int64, error) {
	result := DB.Unscoped().Where("provider_id = ? and instance_name = ?", providerID, instanceName).Delete(&entity.TenantModelInstance{})
	return result.RowsAffected, result.Error
}
