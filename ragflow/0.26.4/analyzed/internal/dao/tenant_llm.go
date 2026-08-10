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
// tenant_llm.go — 租户 LLM 配置数据访问层：管理租户绑定的模型厂商、API Key、复合模型名解析及 MyLLMs 列表。

//

package dao

import (
	"fmt"
	"ragflow/internal/entity"
)

// TenantLLMDAO 租户 LLM 配置表的数据访问对象。
type TenantLLMDAO struct{}

// NewTenantLLMDAO 创建租户 LLM DAO 实例。
func NewTenantLLMDAO() *TenantLLMDAO {
	return &TenantLLMDAO{}
}

// GetByID 按自增主键查询租户 LLM 记录。
func (dao *TenantLLMDAO) GetByID(id int64) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM
	err := DB.Where("id = ?", id).First(&tenantLLM).Error
	if err != nil {
		return nil, err
	}
	return &tenantLLM, nil
}

// GetByTenantAndModelName 按租户、厂商名与模型名精确查询。
func (dao *TenantLLMDAO) GetByTenantAndModelName(tenantID, providerName string, modelName string) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM
	err := DB.Where("tenant_id = ? AND llm_factory = ? AND llm_name = ?", tenantID, providerName, modelName).First(&tenantLLM).Error
	if err != nil {
		return nil, err
	}
	return &tenantLLM, nil
}

// GetByTenantNameAndType 按租户、模型名与 model_type 查询。
func (dao *TenantLLMDAO) GetByTenantNameAndType(tenantID, modelName string, modelType entity.ModelType) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM
	err := DB.Where("tenant_id = ? AND llm_name = ? AND model_type = ?", tenantID, modelName, modelType).First(&tenantLLM).Error
	if err != nil {
		return nil, err
	}
	return &tenantLLM, nil
}

// GetByTenantAndType 按租户与 model_type 查询单条记录。
func (dao *TenantLLMDAO) GetByTenantAndType(tenantID string, modelType entity.ModelType) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM
	err := DB.Where("tenant_id = ? AND model_type = ?", tenantID, modelType).First(&tenantLLM).Error
	if err != nil {
		return nil, err
	}
	return &tenantLLM, nil
}

// GetByTenantAndFactory 按租户、model_type 与 llm_factory 查询。
func (dao *TenantLLMDAO) GetByTenantAndFactory(tenantID string, modelType entity.ModelType, factory string) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM
	err := DB.Where("tenant_id = ? AND model_type = ? AND llm_factory = ?", tenantID, modelType, factory).First(&tenantLLM).Error
	if err != nil {
		return nil, err
	}
	return &tenantLLM, nil
}

// ListByTenant 列出租户下全部 LLM 配置。
func (dao *TenantLLMDAO) ListByTenant(tenantID string) ([]entity.TenantLLM, error) {
	var tenantLLMs []entity.TenantLLM
	err := DB.Where("tenant_id = ?", tenantID).Find(&tenantLLMs).Error
	if err != nil {
		return nil, err
	}
	return tenantLLMs, nil
}

// GetByTenantFactoryAndModelName 按租户、厂商与模型名三元组查询。
func (dao *TenantLLMDAO) GetByTenantFactoryAndModelName(tenantID, factory, modelName string) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM
	err := DB.Where("tenant_id = ? AND llm_factory = ? AND llm_name = ?", tenantID, factory, modelName).First(&tenantLLM).Error
	if err != nil {
		return nil, err
	}
	return &tenantLLM, nil
}

// Create 插入新租户 LLM 记录。
func (dao *TenantLLMDAO) Create(tenantLLM *entity.TenantLLM) error {
	return DB.Create(tenantLLM).Error
}

// Update 全量保存租户 LLM 实体。
func (dao *TenantLLMDAO) Update(tenantLLM *entity.TenantLLM) error {
	return DB.Save(tenantLLM).Error
}

// Delete 按租户、厂商与模型名硬删除记录。
func (dao *TenantLLMDAO) Delete(tenantID, factory, modelName string) error {
	return DB.Where("tenant_id = ? AND llm_factory = ? AND llm_name = ?", tenantID, factory, modelName).Delete(&entity.TenantLLM{}).Error
}

// GetMyLLMs 联表 llm_factories 返回已配置 API Key 的模型列表（含 logo、tags）。
func (dao *TenantLLMDAO) GetMyLLMs(tenantID string) ([]entity.MyLLM, error) {
	var myLLMs []entity.MyLLM

	err := DB.Table("tenant_llm tl").
		Select("tl.id, tl.llm_factory, lf.logo, lf.tags, tl.model_type, tl.llm_name, tl.used_tokens, tl.status").
		Joins("JOIN llm_factories lf ON tl.llm_factory = lf.name").
		Where("tl.tenant_id = ? AND tl.api_key IS NOT NULL", tenantID).
		Find(&myLLMs).Error
	if err != nil {
		return nil, err
	}
	return myLLMs, nil
}

// ListValidByTenant 列出 api_key 非空且 status 有效的租户 LLM。
func (dao *TenantLLMDAO) ListValidByTenant(tenantID string) ([]*entity.TenantLLM, error) {
	var tenantLLMs []*entity.TenantLLM
	err := DB.Where("tenant_id = ? AND api_key IS NOT NULL AND api_key != ? AND status = ?", tenantID, "", "1").Find(&tenantLLMs).Error
	if err != nil {
		return nil, err
	}
	return tenantLLMs, nil
}

// ListAllByTenant 列出租户下全部 LLM 记录。
func (dao *TenantLLMDAO) ListAllByTenant(tenantID string) ([]*entity.TenantLLM, error) {
	var tenantLLMs []*entity.TenantLLM
	err := DB.Where("tenant_id = ?", tenantID).Find(&tenantLLMs).Error
	if err != nil {
		return nil, err
	}
	return tenantLLMs, nil
}

// InsertMany 批量插入租户 LLM，空切片直接返回。
func (dao *TenantLLMDAO) InsertMany(tenantLLMs []*entity.TenantLLM) error {
	if len(tenantLLMs) == 0 {
		return nil
	}
	return DB.Create(&tenantLLMs).Error
}

// DeleteByTenantID 按租户 ID 硬删除全部 LLM 配置。
func (dao *TenantLLMDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ?", tenantID).Delete(&entity.TenantLLM{})
	return result.RowsAffected, result.Error
}

// splitModelNameAndFactory 从 "模型名@厂商" 复合串解析模型名与厂商，对齐 Python split_model_name_and_factory。若 @ 后缀不在 llm_factories 表中则整串视为模型名。
func splitModelNameAndFactory(modelName string) (string, string) {
	// 从右向左找最后一个 @，支持 model@sub@factory 形式
	lastAtIndex := -1
	for i := len(modelName) - 1; i >= 0; i-- {
		if modelName[i] == '@' {
			lastAtIndex = i
			break
		}
	}

	// 无 @ 分隔符则原样返回模型名，厂商为空
	if lastAtIndex == -1 {
		return modelName, ""
	}

	// 拆分为模型名部分与潜在厂商后缀
	modelNamePart := modelName[:lastAtIndex]
	factory := modelName[lastAtIndex+1:]

	// 校验 @ 后缀是否为 llm_factories 中已注册厂商
	var factoryCount int64
	DB.Model(&entity.LLMFactories{}).Where("name = ?", factory).Count(&factoryCount)

	// 厂商不在库中则将整串当作模型名，厂商返回空
	if factoryCount == 0 {
		return modelName, ""
	}

	return modelNamePart, factory
}

// GetByTenantIDAndLLMName 由 llm_id 解析 tenant_llm 记录，支持纯模型名或 model@factory 格式；LocalAI/HuggingFace/OpenAI-API-Compatible 另有 ___厂商 后缀特殊匹配。
func (dao *TenantLLMDAO) GetByTenantIDAndLLMName(tenantID string, llmName string) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM

	// 解析复合模型名
	modelName, factory := splitModelNameAndFactory(llmName)

	// 先仅用模型名查询
	err := DB.Where("tenant_id = ? AND llm_name = ?", tenantID, modelName).First(&tenantLLM).Error
	if err == nil {
		return &tenantLLM, nil
	}

	// 若指定厂商则用模型名+厂商联合查询
	if factory != "" {
		err = DB.Where("tenant_id = ? AND llm_name = ? AND llm_factory = ?", tenantID, modelName, factory).First(&tenantLLM).Error
		if err == nil {
			return &tenantLLM, nil
		}

		// LocalAI/HuggingFace 等厂商在库内模型名带 ___厂商 后缀的特殊匹配
		if factory == "LocalAI" || factory == "HuggingFace" || factory == "OpenAI-API-Compatible" {
			specialModelName := modelName + "___" + factory
			err = DB.Where("tenant_id = ? AND llm_name = ?", tenantID, specialModelName).First(&tenantLLM).Error
			if err == nil {
				return &tenantLLM, nil
			}
		}
	}

	// 全部尝试失败则返回未找到错误
	return nil, err
}

// GetByTenantIDLLMNameAndFactory 按租户、模型名与厂商三元组精确查询。
func (dao *TenantLLMDAO) GetByTenantIDLLMNameAndFactory(tenantID, llmName, factory string) (*entity.TenantLLM, error) {
	var tenantLLM entity.TenantLLM
	err := DB.Where("tenant_id = ? AND llm_name = ? AND llm_factory = ?", tenantID, llmName, factory).First(&tenantLLM).Error
	if err != nil {
		return nil, err
	}
	return &tenantLLM, nil
}

// LookupTenantLLMByID 按 ID 查找记录并返回 model@factory 复合名。
func LookupTenantLLMByID(tenantLLMDao *TenantLLMDAO, id int64) (*entity.TenantLLM, string, error) {
	tenantLLM, err := tenantLLMDao.GetByID(id)
	if err != nil {
		return nil, "", fmt.Errorf("failed to get tenant_llm by id %d: %w", id, err)
	}
	if tenantLLM == nil || tenantLLM.LLMName == nil || *tenantLLM.LLMName == "" {
		return nil, "", fmt.Errorf("tenant_llm record not found for id %d", id)
	}
	compositeName := fmt.Sprintf("%s@%s", *tenantLLM.LLMName, tenantLLM.LLMFactory)
	return tenantLLM, compositeName, nil
}

// LookupTenantLLMByName 按租户、模型名与类型查找，名称含 @ 时走厂商分支。
func LookupTenantLLMByName(tenantLLMDao *TenantLLMDAO, tenantID, name string, modelType entity.ModelType) (*entity.TenantLLM, string, error) {
	// 若名称含 @ 则解析厂商并委托 LookupTenantLLMByFactory
	modelName, factory := splitModelNameAndFactory(name)

	// 解析出有效厂商则按厂商路径查询
	if factory != "" {
		return LookupTenantLLMByFactory(tenantLLMDao, tenantID, factory, modelName, modelType)
	}

	tenantLLM, err := tenantLLMDao.GetByTenantNameAndType(tenantID, modelName, modelType)
	if err != nil {
		return nil, "", fmt.Errorf("failed to get tenant_llm by name %s: %w", name, err)
	}
	if tenantLLM == nil || tenantLLM.LLMName == nil || *tenantLLM.LLMName == "" {
		return nil, "", fmt.Errorf("tenant_llm record not found for name %s", name)
	}
	compositeName := fmt.Sprintf("%s@%s", *tenantLLM.LLMName, tenantLLM.LLMFactory)
	return tenantLLM, compositeName, nil
}

// LookupTenantLLMByFactory 按租户、厂商与模型名查找并返回复合名。
func LookupTenantLLMByFactory(tenantLLMDao *TenantLLMDAO, tenantID, factory, name string, modelType entity.ModelType) (*entity.TenantLLM, string, error) {
	tenantLLM, err := tenantLLMDao.GetByTenantFactoryAndModelName(tenantID, factory, name)
	if err != nil {
		return nil, "", fmt.Errorf("failed to get tenant_llm by factory %s and name %s: %w", factory, name, err)
	}
	if tenantLLM == nil || tenantLLM.LLMName == nil || *tenantLLM.LLMName == "" {
		return nil, "", fmt.Errorf("tenant_llm record not found for factory %s and name %s", factory, name)
	}
	compositeName := fmt.Sprintf("%s@%s", *tenantLLM.LLMName, tenantLLM.LLMFactory)
	return tenantLLM, compositeName, nil
}
