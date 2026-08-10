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
//

// memory.go — Agent 记忆（Memory）数据访问层：
// 位标志类型、租户筛选列表及与 Python memory_service.py 对齐的 CRUD。
package dao

import (
	"context"
	"fmt"
	"ragflow/internal/entity"
	"strings"
)

// Memory 类型位标志常量，与 Python MemoryType 枚举一致
const (
	MemoryTypeRaw        = 0b0001 // 原始记忆（二进制 0001）
	MemoryTypeSemantic   = 0b0010 // 语义记忆（二进制 0010）
	MemoryTypeEpisodic   = 0b0100 // 情景记忆（二进制 0100）
	MemoryTypeProcedural = 0b1000 // 程序性记忆（二进制 1000）
)

// MemoryTypeMap 记忆类型名称到位标志的映射，供 service 包使用
var MemoryTypeMap = map[string]int{
	"raw":        MemoryTypeRaw,
	"semantic":   MemoryTypeSemantic,
	"episodic":   MemoryTypeEpisodic,
	"procedural": MemoryTypeProcedural,
}

// CalculateMemoryType 将类型名称数组合并为位标志整数
//
// Parameters:
//   - memoryTypeNames: 记忆类型名称数组
//
// Returns:
//   - int64: 位标志整数值
//
// Example:
//
//	CalculateMemoryType([]string{"raw", "semantic"}) returns 3 (0b0011)
func CalculateMemoryType(memoryTypeNames []string) int64 {
	memoryType := 0
	for _, name := range memoryTypeNames {
		lowerName := strings.ToLower(name)
		if mt, ok := MemoryTypeMap[lowerName]; ok {
			memoryType |= mt
		}
	}
	return int64(memoryType)
}

// GetMemoryTypeHuman 将位标志解码为可读类型名称列表
//
// Parameters:
//   - memoryType: 表示记忆类型的位标志整数
//
// Returns:
//   - []string: 可读类型名称数组
//
// Example:
//
//	GetMemoryTypeHuman(3) returns ["raw", "semantic"]
func GetMemoryTypeHuman(memoryType int64) []string {
	var result []string
	if memoryType&int64(MemoryTypeRaw) != 0 {
		result = append(result, "raw")
	}
	if memoryType&int64(MemoryTypeSemantic) != 0 {
		result = append(result, "semantic")
	}
	if memoryType&int64(MemoryTypeEpisodic) != 0 {
		result = append(result, "episodic")
	}
	if memoryType&int64(MemoryTypeProcedural) != 0 {
		result = append(result, "procedural")
	}
	return result
}

// MemoryDAO 处理全部 Memory 相关数据库操作
type MemoryDAO struct{}

// NewMemoryDAO 创建 MemoryDAO 实例
//
// Returns:
//   - *MemoryDAO: 已初始化的 DAO 实例
func NewMemoryDAO() *MemoryDAO {
	return &MemoryDAO{}
}

// Create 向数据库插入新的记忆记录
//
// Parameters:
//   - memory: 记忆实体指针
//
// Returns:
//   - error: 数据库操作错误
func (dao *MemoryDAO) Create(memory *entity.Memory) error {
	return DB.Create(memory).Error
}

// GetByID 按 ID 从数据库查询记忆记录
//
// Parameters:
//   - id: 记忆 ID
//
// Returns:
//   - *entity.Memory: 记忆实体指针
//   - error: Database operation error
func (dao *MemoryDAO) GetByID(id string) (*entity.Memory, error) {
	return dao.GetByIDWithContext(context.Background(), id)
}

// GetByIDWithContext 带 context 按 ID 查询记忆，支持超时与取消。
func (dao *MemoryDAO) GetByIDWithContext(ctx context.Context, id string) (*entity.Memory, error) {
	var memory entity.Memory
	err := DB.WithContext(ctx).Where("id = ?", id).First(&memory).Error
	if err != nil {
		return nil, err
	}
	return &memory, nil
}

// GetByTenantID 列出租户下的全部记忆
//
// Parameters:
//   - tenantID: 租户 ID
//
// Returns:
//   - []*entity.Memory: 记忆实体指针数组
//   - error: Database operation error
func (dao *MemoryDAO) GetByTenantID(tenantID string) ([]*entity.Memory, error) {
	var memories []*entity.Memory
	err := DB.Where("tenant_id = ?", tenantID).Find(&memories).Error
	return memories, err
}

// GetByNameAndTenant 按名称与租户检查记忆是否存在，用于重名去重
//
// Parameters:
//   - name: 记忆名称
//   - tenantID: Tenant ID
//
// Returns:
//   - []*entity.Memory: 匹配的记忆列表（存在性检查）
//   - error: Database operation error
func (dao *MemoryDAO) GetByNameAndTenant(name string, tenantID string) ([]*entity.Memory, error) {
	var memories []*entity.Memory
	err := DB.Where("name = ? AND tenant_id = ?", name, tenantID).Find(&memories).Error
	return memories, err
}

// GetByIDs 按 ID 列表批量查询记忆
//
// Parameters:
//   - ids: 记忆 ID 列表
//
// Returns:
//   - []*model.Memory: Memory model pointer array
//   - error: Database operation error
func (dao *MemoryDAO) GetByIDs(ids []string) ([]*entity.Memory, error) {
	var memories []*entity.Memory
	err := DB.Where("id IN ?", ids).Find(&memories).Error
	return memories, err
}

// UpdateByID 按 ID 部分更新记忆，自动转换 memory_type 与 temperature 字段类型
//
// Parameters:
//   - id: Memory ID
//   - updates: 待更新字段 map
//
// Returns:
//   - error: Database operation error
//
// 字段类型处理说明：
//   - memory_type: []string 转为位标志整数
//   - temperature: string 转为 float64
//   - name: 直接使用字符串
//   - permissions、forgetting_policy: 直接使用字符串
//
// Example:
//
//	updates := map[string]interface{}{"name": "NewName", "memory_type": []string{"semantic"}}
//	err := dao.UpdateByID("memory123", updates)
func (dao *MemoryDAO) UpdateByID(id string, updates map[string]interface{}) error {
	if updates == nil || len(updates) == 0 {
		return nil
	}

	for key, value := range updates {
		switch key {
		case "memory_type":
			if types, ok := value.([]string); ok {
				updates[key] = CalculateMemoryType(types)
			}
		case "temperature":
			if tempStr, ok := value.(string); ok {
				var temp float64
				fmt.Sscanf(tempStr, "%f", &temp)
				updates[key] = temp
			}
		}
	}

	return DB.Model(&entity.Memory{}).Where("id = ?", id).Updates(updates).Error
}

// DeleteByID 按 ID 删除记忆记录
//
// Parameters:
//   - id: Memory ID
//
// Returns:
//   - error: Database operation error
//
// Example:
//
//	err := dao.DeleteByID("memory123")
func (dao *MemoryDAO) DeleteByID(id string) error {
	return DB.Where("id = ?", id).Delete(&entity.Memory{}).Error
}

// GetWithOwnerNameByID 联表 user 查询记忆详情并填充 owner_name
//
// Parameters:
//   - id: Memory ID
//
// Returns:
//   - *entity.MemoryListItem: 含所有者昵称的记忆详情
//   - error: Database operation error
//
// Example:
//
//	memory, err := dao.GetWithOwnerNameByID("memory123")
func (dao *MemoryDAO) GetWithOwnerNameByID(id string) (*entity.MemoryListItem, error) {
	querySQL := `
		SELECT m.id, m.name, m.avatar, m.tenant_id, m.memory_type,
			m.storage_type, m.embd_id, m.tenant_embd_id, m.llm_id, m.tenant_llm_id,
			m.permissions, m.description, m.memory_size, m.forgetting_policy,
			m.temperature, m.system_prompt, m.user_prompt, m.create_time, m.create_date,
			m.update_time, m.update_date,
			u.nickname as owner_name
		FROM memory m
		LEFT JOIN user u ON m.tenant_id = u.id
		WHERE m.id = ?
	`

	var rawResult struct {
		entity.Memory
		OwnerName *string `gorm:"column:owner_name"`
	}

	if err := DB.Raw(querySQL, id).Scan(&rawResult).Error; err != nil {
		return nil, err
	}

	return &entity.MemoryListItem{
		Memory:    rawResult.Memory,
		OwnerName: rawResult.OwnerName,
	}, nil
}

// GetByFilter 多条件筛选记忆列表，支持租户、类型位标志、存储类型与关键词分页
//
// Parameters:
//   - tenantIDs: 租户 ID 数组（空表示不限）
//   - memoryTypes: 记忆类型名称数组（空表示全部）
//   - storageType: 存储类型（空表示全部）
//   - keywords: 名称关键词（空表示不过滤）
//   - page: 页码（从 1 开始）
//   - pageSize: 每页条数
//
// Returns:
//   - []*entity.MemoryListItem: 含 owner_name 的记忆列表项
//   - int64: 匹配总数
//   - error: Database operation error
//
// Example:
//
//	memories, total, err := dao.GetByFilter([]string{"tenant1"}, []string{"semantic"}, "table", "test", 1, 10)
func (dao *MemoryDAO) GetByFilter(userID string, tenantIDs []string, memoryTypes []string, storageType string, keywords string, page int, pageSize int) ([]*entity.MemoryListItem, int64, error) {
	var conditions []string
	var args []interface{}

	if len(tenantIDs) > 0 {
		conditions = append(conditions, "m.tenant_id IN ?")
		args = append(args, tenantIDs)
	}

	if userID != "" {
		conditions = append(conditions, "(m.tenant_id = ? OR m.permissions = ?)")
		args = append(args, userID, "team")
	}

	if len(memoryTypes) > 0 {
		memoryTypeInt := CalculateMemoryType(memoryTypes)
		conditions = append(conditions, "m.memory_type & ? > 0")
		args = append(args, memoryTypeInt)
	}

	if storageType != "" {
		conditions = append(conditions, "m.storage_type = ?")
		args = append(args, storageType)
	}

	if keywords != "" {
		conditions = append(conditions, "m.name LIKE ?")
		args = append(args, "%"+keywords+"%")
	}

	whereClause := ""
	if len(conditions) > 0 {
		whereClause = "WHERE " + strings.Join(conditions, " AND ")
	}

	countSQL := fmt.Sprintf("SELECT COUNT(*) FROM memory m %s", whereClause)
	var total int64
	if err := DB.Raw(countSQL, args...).Scan(&total).Error; err != nil {
		return nil, 0, err
	}

	offset := (page - 1) * pageSize
	querySQL := fmt.Sprintf(`
		SELECT m.id, m.name, m.avatar, m.tenant_id, m.memory_type,
			m.storage_type, m.embd_id, m.tenant_embd_id, m.llm_id, m.tenant_llm_id,
			m.permissions, m.description, m.memory_size, m.forgetting_policy,
			m.temperature, m.system_prompt, m.user_prompt, m.create_time, m.create_date,
			m.update_time, m.update_date,
			u.nickname as owner_name
		FROM memory m
		LEFT JOIN user u ON m.tenant_id = u.id
		%s
		ORDER BY m.update_time DESC
		LIMIT ? OFFSET ?
	`, whereClause)

	queryArgs := append(args, pageSize, offset)

	var rawResults []struct {
		entity.Memory
		OwnerName *string `gorm:"column:owner_name"`
	}

	if err := DB.Raw(querySQL, queryArgs...).Scan(&rawResults).Error; err != nil {
		return nil, 0, err
	}

	memories := make([]*entity.MemoryListItem, len(rawResults))
	for i, r := range rawResults {
		memories[i] = &entity.MemoryListItem{
			Memory:    r.Memory,
			OwnerName: r.OwnerName,
		}
	}

	return memories, total, nil
}
