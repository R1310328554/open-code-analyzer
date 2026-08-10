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
// kb.go — 知识库（Knowledgebase）数据访问层：租户权限、解析配置深合并、重名去重及文档计数原子更新。

//

package dao

import (
	"errors"
	"fmt"
	"path"
	"ragflow/internal/entity"

	"strconv"
	"strings"

	"gorm.io/gorm"
)

// GetTenantIDByKBID 包级辅助函数：按知识库 ID 返回 tenant_id，供 service 与 engine 共用以避免循环依赖。
func GetTenantIDByKBID(kbID string) (string, error) {
	kbDAO := NewKnowledgebaseDAO()
	kb, err := kbDAO.GetByID(kbID)
	if err != nil {
		return "", fmt.Errorf("knowledgebase not found: %w", err)
	}
	return kb.TenantID, nil
}

// KnowledgebaseDAO 知识库表的数据访问对象。
type KnowledgebaseDAO struct{}

// IsNotFoundErr 判断错误是否为 GORM 记录未找到。
func IsNotFoundErr(err error) bool {
	return errors.Is(err, gorm.ErrRecordNotFound)
}

// NewKnowledgebaseDAO 创建知识库 DAO 实例。
func NewKnowledgebaseDAO() *KnowledgebaseDAO {
	return &KnowledgebaseDAO{}
}

// Create 插入新知识库记录。
func (dao *KnowledgebaseDAO) Create(kb *entity.Knowledgebase) error {
	return DB.Create(kb).Error
}

// Update 全量保存知识库实体。
func (dao *KnowledgebaseDAO) Update(kb *entity.Knowledgebase) error {
	return DB.Save(kb).Error
}

// UpdateByID 按 ID 部分更新知识库字段。
func (dao *KnowledgebaseDAO) UpdateByID(id string, updates map[string]interface{}) error {
	return DB.Model(&entity.Knowledgebase{}).Where("id = ?", id).Updates(updates).Error
}

// Delete 软删除：将 status 设为 invalid。
func (dao *KnowledgebaseDAO) Delete(id string) error {
	return DB.Model(&entity.Knowledgebase{}).Where("id = ?", id).Update("status", string(entity.StatusInvalid)).Error
}

// GetByID 按 ID 查询有效状态的知识库。
func (dao *KnowledgebaseDAO) GetByID(id string) (*entity.Knowledgebase, error) {
	var kb entity.Knowledgebase
	err := DB.Where("id = ? AND status = ?", id, string(entity.StatusValid)).First(&kb).Error
	if err != nil {
		return nil, err
	}
	return &kb, nil
}

// GetByIDAndTenantID 按 ID 与租户 ID 联合查询有效知识库。
func (dao *KnowledgebaseDAO) GetByIDAndTenantID(id, tenantID string) (*entity.Knowledgebase, error) {
	var kb entity.Knowledgebase
	err := DB.Where("id = ? AND tenant_id = ? AND status = ?", id, tenantID, string(entity.StatusValid)).First(&kb).Error
	if err != nil {
		return nil, err
	}
	return &kb, nil
}

// GetByIDs 批量按 ID 查询有效知识库。
func (dao *KnowledgebaseDAO) GetByIDs(ids []string) ([]*entity.Knowledgebase, error) {
	var kbs []*entity.Knowledgebase
	err := DB.Where("id IN ? AND status = ?", ids, string(entity.StatusValid)).Find(&kbs).Error
	return kbs, err
}

// GetByName 按名称与租户 ID 查询知识库。
func (dao *KnowledgebaseDAO) GetByName(name, tenantID string) (*entity.Knowledgebase, error) {
	var kb entity.Knowledgebase
	err := DB.Where("name = ? AND tenant_id = ? AND status = ?", name, tenantID, string(entity.StatusValid)).First(&kb).Error
	if err != nil {
		return nil, err
	}
	return &kb, nil
}

// GetByCreatedBy 列出指定用户创建的有效知识库。
func (dao *KnowledgebaseDAO) GetByCreatedBy(createdBy string) ([]*entity.Knowledgebase, error) {
	var kbs []*entity.Knowledgebase
	err := DB.Where("created_by = ? AND status = ?", createdBy, string(entity.StatusValid)).Find(&kbs).Error
	return kbs, err
}

// Query 按 map 条件筛选有效知识库。
func (dao *KnowledgebaseDAO) Query(filters map[string]interface{}) ([]*entity.Knowledgebase, error) {
	var kbs []*entity.Knowledgebase
	query := DB.Where("status = ?", string(entity.StatusValid))

	for key, value := range filters {
		if value != nil && value != "" {
			query = query.Where(key+" = ?", value)
		}
	}

	err := query.Find(&kbs).Error
	return kbs, err
}

// QueryOne 按条件查询单条有效知识库。
func (dao *KnowledgebaseDAO) QueryOne(filters map[string]interface{}) (*entity.Knowledgebase, error) {
	var kb entity.Knowledgebase
	query := DB.Where("status = ?", string(entity.StatusValid))

	for key, value := range filters {
		if value != nil && value != "" {
			query = query.Where(key+" = ?", value)
		}
	}

	err := query.First(&kb).Error
	if err != nil {
		return nil, err
	}
	return &kb, nil
}

// Count 统计符合筛选条件的知识库数量。
func (dao *KnowledgebaseDAO) Count(filters map[string]interface{}) (int64, error) {
	var count int64
	query := DB.Model(&entity.Knowledgebase{}).Where("status = ?", string(entity.StatusValid))

	for key, value := range filters {
		if value != nil && value != "" {
			query = query.Where(key+" = ?", value)
		}
	}

	err := query.Count(&count).Error
	return count, err
}

// GetByTenantIDs 按租户列表分页查询（含团队权限），对齐 Python get_by_tenant_ids。
func (dao *KnowledgebaseDAO) GetByTenantIDs(tenantIDs []string, userID string, pageNumber, itemsPerPage int, orderby string, desc bool, keywords, parserID string) ([]*entity.KnowledgebaseListItem, int64, error) {
	var kbs []*entity.KnowledgebaseListItem
	var total int64

	query := DB.Model(&entity.Knowledgebase{}).
		Select(`knowledgebase.id, knowledgebase.avatar, knowledgebase.name,
			knowledgebase.language, knowledgebase.description, knowledgebase.tenant_id,
			knowledgebase.permission, knowledgebase.doc_num, knowledgebase.token_num,
			knowledgebase.chunk_num, knowledgebase.parser_id, knowledgebase.embd_id,
			user.nickname, user.avatar as tenant_avatar, knowledgebase.update_time`).
		Joins("LEFT JOIN user ON knowledgebase.tenant_id = user.id").
		Where("((knowledgebase.tenant_id IN ? AND knowledgebase.permission = ?) OR knowledgebase.tenant_id = ?) AND knowledgebase.status = ?",
			tenantIDs, string(entity.TenantPermissionTeam), userID, string(entity.StatusValid))

	if keywords != "" {
		query = query.Where("LOWER(knowledgebase.name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}

	if parserID != "" {
		query = query.Where("knowledgebase.parser_id = ?", parserID)
	}

	if desc {
		query = query.Order("knowledgebase." + orderby + " DESC")
	} else {
		query = query.Order("knowledgebase." + orderby + " ASC")
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if pageNumber > 0 && itemsPerPage > 0 {
		offset := (pageNumber - 1) * itemsPerPage
		if err := query.Offset(offset).Limit(itemsPerPage).Scan(&kbs).Error; err != nil {
			return nil, 0, err
		}
	} else {
		if err := query.Scan(&kbs).Error; err != nil {
			return nil, 0, err
		}
	}

	return kbs, total, nil
}

// GetAllByTenantIDs 返回用户可访问的全部知识库，对齐 Python get_all_kb_by_tenant_ids。
func (dao *KnowledgebaseDAO) GetAllByTenantIDs(tenantIDs []string, userID string) ([]*entity.Knowledgebase, error) {
	var kbs []*entity.Knowledgebase

	err := DB.Where(
		"(tenant_id IN ? AND permission = ?) OR tenant_id = ?",
		tenantIDs, string(entity.TenantPermissionTeam), userID,
	).Order("create_time ASC").Find(&kbs).Error

	return kbs, err
}

// GetDetail 联表返回知识库详情及关联流水线名称，对齐 Python get_detail。
func (dao *KnowledgebaseDAO) GetDetail(kbID string) (*entity.KnowledgebaseDetail, error) {
	var detail entity.KnowledgebaseDetail

	err := DB.Table("knowledgebase").
		Select(`knowledgebase.id, knowledgebase.embd_id, knowledgebase.avatar, knowledgebase.name,
			knowledgebase.language, knowledgebase.description, knowledgebase.permission,
			knowledgebase.doc_num, knowledgebase.token_num, knowledgebase.chunk_num,
			knowledgebase.parser_id, knowledgebase.pipeline_id,
			user_canvas.title as pipeline_name, user_canvas.avatar as pipeline_avatar,
			knowledgebase.parser_config, knowledgebase.pagerank,
			knowledgebase.graphrag_task_id, knowledgebase.graphrag_task_finish_at,
			knowledgebase.raptor_task_id, knowledgebase.raptor_task_finish_at,
			knowledgebase.mindmap_task_id, knowledgebase.mindmap_task_finish_at,
			knowledgebase.create_time, knowledgebase.update_time`).
		Joins("LEFT JOIN user_canvas ON knowledgebase.pipeline_id = user_canvas.id").
		Where("knowledgebase.id = ? AND knowledgebase.status = ?", kbID, string(entity.StatusValid)).
		Scan(&detail).Error

	if err != nil {
		return nil, err
	}

	return &detail, nil
}

// Accessible 判断用户是否可访问知识库：所有者、me 权限仅本人、team 需为租户成员。
func (dao *KnowledgebaseDAO) Accessible(kbID, userID string) bool {
	var kb entity.Knowledgebase
	err := DB.Where("id = ? AND status = ?", kbID, string(entity.StatusValid)).First(&kb).Error
	if err != nil {
		return false
	}

	// 请求用户即知识库所属租户
	if kb.TenantID == userID {
		return true
	}

	// me 权限下非所有者不可访问
	if kb.Permission == string(entity.TenantPermissionMe) {
		return false
	}

	var count int64
	err = DB.Table("user_tenant").
		Where("tenant_id = ? AND user_id = ?", kb.TenantID, userID).
		Count(&count).Error

	if err != nil {
		return false
	}
	return count > 0
}

// Accessible4Deletion 判断用户是否为知识库创建者从而可删除。
func (dao *KnowledgebaseDAO) Accessible4Deletion(kbID, userID string) bool {
	var count int64
	err := DB.Model(&entity.Knowledgebase{}).
		Where("id = ? AND created_by = ? AND status = ?", kbID, userID, string(entity.StatusValid)).
		Count(&count).Error

	if err != nil {
		return false
	}
	return count > 0
}

// DuplicateName 若名称冲突则追加 (n) 后缀生成唯一名，对齐 Python duplicate_name。
func (dao *KnowledgebaseDAO) DuplicateName(name, tenantID string) string {
	const maxRetries = 1000

	currentName := name
	for retries := 0; retries < maxRetries; retries++ {
		var count int64
		err := DB.Model(&entity.Knowledgebase{}).
			Where("LOWER(name) = ? AND tenant_id = ? AND status = ?", strings.ToLower(currentName), tenantID, string(entity.StatusValid)).
			Count(&count).Error
		if err != nil || count == 0 {
			return currentName
		}

		suffix := path.Ext(currentName)
		stem := strings.TrimSuffix(currentName, suffix)
		mainPart, counter := splitNameCounter(stem)
		nextCounter := 1
		if counter > 0 {
			nextCounter = counter + 1
		}

		currentName = mainPart + "(" + strconv.Itoa(nextCounter) + ")" + suffix
	}

	return currentName
}

// splitNameCounter 解析文件名末尾 (数字) 后缀，返回主干与计数。
func splitNameCounter(name string) (string, int) {
	if !strings.HasSuffix(name, ")") {
		return name, 0
	}

	leftBracketIndex := strings.LastIndex(name, "(")
	if leftBracketIndex < 0 || leftBracketIndex >= len(name)-1 {
		return name, 0
	}

	counterValue := name[leftBracketIndex+1 : len(name)-1]
	counter, err := strconv.Atoi(counterValue)
	if err != nil {
		return name, 0
	}

	return strings.TrimRight(name[:leftBracketIndex], " "), counter
}

// AtomicIncreaseDocNumByID 原子递增 doc_num，对齐 Python atomic_increase_doc_num_by_id。
func (dao *KnowledgebaseDAO) AtomicIncreaseDocNumByID(kbID string) error {
	return DB.Model(&entity.Knowledgebase{}).
		Where("id = ?", kbID).
		Updates(map[string]interface{}{
			"doc_num": DB.Raw("doc_num + 1"),
		}).Error
}

// DecreaseDocumentNum 删除文档时递减 doc_num、chunk_num、token_num。
func (dao *KnowledgebaseDAO) DecreaseDocumentNum(kbID string, docNum, chunkNum, tokenNum int64) error {
	return DB.Model(&entity.Knowledgebase{}).
		Where("id = ?", kbID).
		Updates(map[string]interface{}{
			"doc_num":   DB.Raw("doc_num - ?", docNum),
			"chunk_num": DB.Raw("chunk_num - ?", chunkNum),
			"token_num": DB.Raw("token_num - ?", tokenNum),
		}).Error
}

// GetKBIDsByTenantID 返回租户下全部有效知识库 ID。
func (dao *KnowledgebaseDAO) GetKBIDsByTenantID(tenantID string) ([]string, error) {
	var kbIDs []string
	err := DB.Model(&entity.Knowledgebase{}).
		Where("tenant_id = ? AND status = ?", tenantID, string(entity.StatusValid)).
		Pluck("id", &kbIDs).Error
	return kbIDs, err
}

// GetAllIDs 返回系统中全部有效知识库 ID。
func (dao *KnowledgebaseDAO) GetAllIDs() ([]string, error) {
	var kbIDs []string
	err := DB.Model(&entity.Knowledgebase{}).
		Where("status = ?", string(entity.StatusValid)).
		Pluck("id", &kbIDs).Error
	return kbIDs, err
}

// UpdateParserConfig 深合并更新 parser_config JSON。
func (dao *KnowledgebaseDAO) UpdateParserConfig(id string, config map[string]interface{}) error {
	var kb entity.Knowledgebase
	if err := DB.Where("id = ? AND status = ?", id, string(entity.StatusValid)).First(&kb).Error; err != nil {
		return err
	}

	mergedConfig := mergeConfig(kb.ParserConfig, config)
	return DB.Model(&entity.Knowledgebase{}).
		Where("id = ?", id).
		Update("parser_config", mergedConfig).Error
}

// DeleteFieldMap 从 parser_config 中移除 field_map 键。
func (dao *KnowledgebaseDAO) DeleteFieldMap(id string) error {
	var kb entity.Knowledgebase
	if err := DB.Where("id = ? AND status = ?", id, string(entity.StatusValid)).First(&kb).Error; err != nil {
		return err
	}

	if kb.ParserConfig != nil {
		delete(kb.ParserConfig, "field_map")
		return DB.Model(&entity.Knowledgebase{}).
			Where("id = ?", id).
			Update("parser_config", kb.ParserConfig).Error
	}
	return nil
}

// GetFieldMap 合并多个知识库的 field_map 配置。
func (dao *KnowledgebaseDAO) GetFieldMap(ids []string) (map[string]interface{}, error) {
	conf := make(map[string]interface{})
	kbs, err := dao.GetByIDs(ids)
	if err != nil {
		return nil, err
	}

	for _, kb := range kbs {
		if kb.ParserConfig != nil {
			if fieldMap, ok := kb.ParserConfig["field_map"]; ok {
				if fm, ok := fieldMap.(map[string]interface{}); ok {
					for k, v := range fm {
						conf[k] = v
					}
				}
			}
		}
	}
	return conf, nil
}

// GetKBByIDAndUserID 通过 user_tenant 关联按 KB ID 与用户 ID 查询。
func (dao *KnowledgebaseDAO) GetKBByIDAndUserID(kbID, userID string) ([]*entity.Knowledgebase, error) {
	var kbs []*entity.Knowledgebase
	err := DB.Model(&entity.Knowledgebase{}).
		Joins("JOIN user_tenant ON user_tenant.tenant_id = knowledgebase.tenant_id").
		Where("knowledgebase.id = ? AND user_tenant.user_id = ?", kbID, userID).
		Limit(1).
		Find(&kbs).Error
	return kbs, err
}

// GetKBByNameAndUserID 通过 user_tenant 关联按名称与用户 ID 查询。
func (dao *KnowledgebaseDAO) GetKBByNameAndUserID(kbName, userID string) ([]*entity.Knowledgebase, error) {
	var kbs []*entity.Knowledgebase
	err := DB.Model(&entity.Knowledgebase{}).
		Joins("JOIN user_tenant ON user_tenant.tenant_id = knowledgebase.tenant_id").
		Where("knowledgebase.name = ? AND user_tenant.user_id = ?", kbName, userID).
		Limit(1).
		Find(&kbs).Error
	return kbs, err
}

// GetList 带 ID/名称筛选的分页列表，对齐 Python get_list。
func (dao *KnowledgebaseDAO) GetList(tenantIDs []string, userID string, pageNumber, itemsPerPage int, orderby string, desc bool, id, name string) ([]*entity.Knowledgebase, int64, error) {
	var kbs []*entity.Knowledgebase
	var total int64

	query := DB.Model(&entity.Knowledgebase{}).
		Where("((tenant_id IN ? AND permission = ?) OR tenant_id = ?) AND status = ?",
			tenantIDs, string(entity.TenantPermissionTeam), userID, string(entity.StatusValid))

	if id != "" {
		query = query.Where("id = ?", id)
	}
	if name != "" {
		query = query.Where("name = ?", name)
	}

	if desc {
		query = query.Order(orderby + " DESC")
	} else {
		query = query.Order(orderby + " ASC")
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if pageNumber > 0 && itemsPerPage > 0 {
		offset := (pageNumber - 1) * itemsPerPage
		if err := query.Offset(offset).Limit(itemsPerPage).Find(&kbs).Error; err != nil {
			return nil, 0, err
		}
	} else {
		if err := query.Find(&kbs).Error; err != nil {
			return nil, 0, err
		}
	}

	return kbs, total, nil
}

// mergeConfig 递归深合并两个 JSON 配置 map，数组去重合并。
func mergeConfig(old, new map[string]interface{}) map[string]interface{} {
	result := make(map[string]interface{})
	for k, v := range old {
		result[k] = v
	}

	for k, v := range new {
		if existing, ok := result[k]; ok {
			if existingMap, ok := existing.(map[string]interface{}); ok {
				if newMap, ok := v.(map[string]interface{}); ok {
					result[k] = mergeConfig(existingMap, newMap)
					continue
				}
			}
			if existingSlice, ok := existing.([]interface{}); ok {
				if newSlice, ok := v.([]interface{}); ok {
					merged := append(existingSlice, newSlice...)
					seen := make(map[interface{}]bool)
					unique := make([]interface{}, 0)
					for _, item := range merged {
						if !seen[item] {
							seen[item] = true
							unique = append(unique, item)
						}
					}
					result[k] = unique
					continue
				}
			}
		}
		result[k] = v
	}

	return result
}

// DeleteByTenantID 按租户 ID 硬删除全部知识库。
func (dao *KnowledgebaseDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ?", tenantID).Delete(&entity.Knowledgebase{})
	return result.RowsAffected, result.Error
}

// GetKBIDsByTenantIDSimple 返回租户下全部知识库 ID（含无效状态）。
func (dao *KnowledgebaseDAO) GetKBIDsByTenantIDSimple(tenantID string) ([]string, error) {
	var kbIDs []string
	err := DB.Model(&entity.Knowledgebase{}).
		Where("tenant_id = ?", tenantID).
		Pluck("id", &kbIDs).Error
	return kbIDs, err
}
