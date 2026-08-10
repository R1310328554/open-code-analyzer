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
// tenant_model_group_mapping.go — 模型分组映射数据访问层：按复合主键（group、provider、instance、model）查询分组与模型的关联。

//

package dao

import (
	"ragflow/internal/entity"
)

// TenantModelGroupMappingDAO 租户模型分组映射表的数据访问对象。
type TenantModelGroupMappingDAO struct{}

// NewTenantModelGroupMappingDAO 创建分组映射 DAO 实例。
func NewTenantModelGroupMappingDAO() *TenantModelGroupMappingDAO {
	return &TenantModelGroupMappingDAO{}
}

// GetByID 按 group_id、provider_id、instance_id、model_id 四元组查询映射。
func (dao *TenantModelGroupMappingDAO) GetByID(groupID, providerID, instanceID, modelID string) (*entity.TenantModelGroupMapping, error) {
	var mapping entity.TenantModelGroupMapping
	err := DB.Where("group_id = ? AND provider_id = ? AND instance_id = ? AND model_id = ?", groupID, providerID, instanceID, modelID).First(&mapping).Error
	if err != nil {
		return nil, err
	}
	return &mapping, nil
}
