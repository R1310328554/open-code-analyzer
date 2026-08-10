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
// tenant_model.go — 租户模型启用状态数据访问层：读写 tenant_model 表，供 /api/v1/models 批量查询租户级模型开关覆盖。

//

package dao

import (
	"ragflow/internal/entity"

	"gorm.io/gorm"
)

// TenantModelDAO 租户模型表的数据访问对象。
type TenantModelDAO struct{}

// NewTenantModelDAO 创建租户模型 DAO 实例。
func NewTenantModelDAO() *TenantModelDAO {
	return &TenantModelDAO{}
}

// Create 插入单条租户模型记录。
func (dao *TenantModelDAO) Create(instance *entity.TenantModel) error {
	return DB.Create(instance).Error
}

// CreateBatch 在事务中逐条批量插入租户模型。
func (dao *TenantModelDAO) CreateBatch(models []*entity.TenantModel) error {
	if len(models) == 0 {
		return nil
	}

	return DB.Transaction(func(tx *gorm.DB) error {
		for _, model := range models {
			if err := tx.Create(model).Error; err != nil {
				return err
			}
		}
		return nil
	})
}

// DeleteByModelID 按模型 ID 硬删除记录。
func (dao *TenantModelDAO) DeleteByModelID(modelID string) (int64, error) {
	result := DB.Unscoped().Where("id = ?", modelID).Delete(&entity.TenantModel{})
	return result.RowsAffected, result.Error
}

// DeleteByModelIDAndProviderIDAndInstanceID 按模型、provider 与 instance 三元组硬删除。
func (dao *TenantModelDAO) DeleteByModelIDAndProviderIDAndInstanceID(modelID, providerID, instanceID string) (int64, error) {
	result := DB.Unscoped().Where("id = ? AND provider_id = ? AND instance_id = ?", modelID, providerID, instanceID).Delete(&entity.TenantModel{})
	return result.RowsAffected, result.Error
}

// DeleteByProviderIDAndInstanceID 按 provider 与 instance 硬删除关联模型。
func (dao *TenantModelDAO) DeleteByProviderIDAndInstanceID(provideID, instanceID string) (int64, error) {
	result := DB.Unscoped().Where("provider_id = ? AND instance_id = ?", provideID, instanceID).Delete(&entity.TenantModel{})
	return result.RowsAffected, result.Error
}

// DeleteByProviderIDAndInstanceIDAndModelName 按 provider、instance 与 model_name 硬删除。
func (dao *TenantModelDAO) DeleteByProviderIDAndInstanceIDAndModelName(provideID, instanceID, modelName string) (int64, error) {
	result := DB.Unscoped().Where("provider_id = ? AND instance_id = ? AND model_name = ?", provideID, instanceID, modelName).Delete(&entity.TenantModel{})
	return result.RowsAffected, result.Error
}

// UpdateStatusByIDAndScope 在指定 provider+instance 范围内更新模型 status。
func (dao *TenantModelDAO) UpdateStatusByIDAndScope(modelID, providerID, instanceID, status string) (int64, error) {
	result := DB.Model(&entity.TenantModel{}).Where("id = ? AND provider_id = ? AND instance_id = ?", modelID, providerID, instanceID).Update("status", status)
	return result.RowsAffected, result.Error
}

// GetByID 按主键查询租户模型。
func (dao *TenantModelDAO) GetByID(id string) (*entity.TenantModel, error) {
	var model entity.TenantModel
	err := DB.Where("id = ?", id).First(&model).Error
	if err != nil {
		return nil, err
	}
	return &model, nil
}

// GetModelByProviderIDAndInstanceIDAndModelName 按 provider、instance 与 model_name 查单条。
func (dao *TenantModelDAO) GetModelByProviderIDAndInstanceIDAndModelName(providerID, instanceID, modelName string) (*entity.TenantModel, error) {
	var model entity.TenantModel
	err := DB.Where("provider_id = ? AND instance_id = ? AND model_name = ?", providerID, instanceID, modelName).First(&model).Error
	if err != nil {
		return nil, err
	}
	return &model, nil
}

// GetModelsByProviderIDAndInstanceIDAndModelName 同上条件但返回全部匹配行。
func (dao *TenantModelDAO) GetModelsByProviderIDAndInstanceIDAndModelName(providerID, instanceID, modelName string) ([]*entity.TenantModel, error) {
	var models []*entity.TenantModel
	err := DB.Where("provider_id = ? AND instance_id = ? AND model_name = ?", providerID, instanceID, modelName).Find(&models).Error
	if err != nil {
		return nil, err
	}
	return models, nil
}

// GetByProviderIDAndInstanceIDAndModelTypeAndModelName 增加 model_type 维度的精确查询。
func (dao *TenantModelDAO) GetByProviderIDAndInstanceIDAndModelTypeAndModelName(providerID, instanceID, modelType, modelName string) (*entity.TenantModel, error) {
	var model entity.TenantModel
	err := DB.Where("provider_id = ? AND instance_id = ? AND model_type = ? AND model_name = ?", providerID, instanceID, modelType, modelName).First(&model).Error
	if err != nil {
		return nil, err
	}
	return &model, nil
}

// GetModelsByInstanceID 按 instance_id 列出全部模型。
func (dao *TenantModelDAO) GetModelsByInstanceID(instanceID string) ([]*entity.TenantModel, error) {
	var models []*entity.TenantModel
	err := DB.Where("instance_id = ?", instanceID).Find(&models).Error
	if err != nil {
		return nil, err
	}
	return models, nil
}

// GetModelsByProviderIDsAndInstanceIDs 批量查询 provider/instance 组合下的租户模型覆盖；对齐 Python get_models_by_provider_ids_and_instance_ids。Go 端只读此表，空结果表示沿用厂商默认启用状态。
func (dao *TenantModelDAO) GetModelsByProviderIDsAndInstanceIDs(providerIDs, instanceIDs []string) ([]*entity.TenantModel, error) {
	models := make([]*entity.TenantModel, 0)
	if len(providerIDs) == 0 || len(instanceIDs) == 0 {
		return models, nil
	}
	err := DB.Where("provider_id IN ? AND instance_id IN ?", providerIDs, instanceIDs).Find(&models).Error
	if err != nil {
		return nil, err
	}
	return models, nil
}
