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
// search.go — 搜索应用（Search）数据访问层：管理租户搜索应用的 CRUD、分页列表、分享详情联表查询及软删除权限校验。

//

package dao

import (
	"ragflow/internal/entity"
	"strings"
)

// SearchDAO 搜索应用表的数据访问对象。
type SearchDAO struct{}

// NewSearchDAO 创建搜索 DAO 实例。
func NewSearchDAO() *SearchDAO {
	return &SearchDAO{}
}

// SearchDetailRow 分享详情接口使用的联表查询结果结构。
type SearchDetailRow struct {
	// ID 搜索应用主键
	ID           string         `gorm:"column:id"`
	// Avatar 应用头像
	Avatar       *string        `gorm:"column:avatar"`
	// TenantID 所属租户 ID
	TenantID     string         `gorm:"column:tenant_id"`
	// Name 应用名称
	Name         string         `gorm:"column:name"`
	// Description 应用描述
	Description  *string        `gorm:"column:description"`
	// CreatedBy 创建者用户 ID
	CreatedBy    string         `gorm:"column:created_by"`
	// SearchConfig 搜索配置 JSON
	SearchConfig entity.JSONMap `gorm:"column:search_config"`
	// UpdateTime 最后更新时间戳
	UpdateTime   *int64         `gorm:"column:update_time"`
	// Nickname 租户所有者昵称
	Nickname     *string        `gorm:"column:nickname"`
	// TenantAvatar 租户所有者头像
	TenantAvatar *string        `gorm:"column:tenant_avatar"`
}

// ListByTenantIDs 按租户 ID 列表分页列出有效搜索应用，联表 user 获取昵称与头像。
func (dao *SearchDAO) ListByTenantIDs(tenantIDs []string, userID string, page, pageSize int, orderby string, desc bool, keywords string) ([]*entity.Search, int64, error) {
	var searches []*entity.Search
	var total int64

	// 联表 user 获取昵称与租户头像
	query := DB.Model(&entity.Search{}).
		Select(`
			search.*,
			user.nickname,
			user.avatar as tenant_avatar
		`).
		Joins("LEFT JOIN user ON search.tenant_id = user.id")

	if len(tenantIDs) > 0 {
		query = query.Where("(search.tenant_id IN ? OR search.tenant_id = ?) AND search.status = ?", tenantIDs, userID, "1")
	} else {
		query = query.Where("search.tenant_id = ? AND search.status = ?", userID, "1")
	}

	// 按名称关键词模糊过滤
	if keywords != "" {
		query = query.Where("LOWER(search.name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}

	// 应用排序字段与升降序
	orderDirection := "ASC"
	if desc {
		orderDirection = "DESC"
	}
	query = query.Order(orderby + " " + orderDirection)

	// 统计符合条件的总数
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 应用分页偏移与条数限制
	if page > 0 && pageSize > 0 {
		offset := (page - 1) * pageSize
		if err := query.Offset(offset).Limit(pageSize).Find(&searches).Error; err != nil {
			return nil, 0, err
		}
	} else {
		if err := query.Find(&searches).Error; err != nil {
			return nil, 0, err
		}
	}

	return searches, total, nil
}

// ListByOwnerIDs 按所有者租户 ID 列表查询搜索应用（内存计数，无数据库分页）。
func (dao *SearchDAO) ListByOwnerIDs(ownerIDs []string, userID string, orderby string, desc bool, keywords string) ([]*entity.Search, int64, error) {
	var searches []*entity.Search

	// 联表 user 表构建查询
	query := DB.Model(&entity.Search{}).
		Select(`
			search.*,
			user.nickname,
			user.avatar as tenant_avatar
		`).
		Joins("LEFT JOIN user ON search.tenant_id = user.id").
		Where("(search.tenant_id IN ? OR search.tenant_id = ?) AND search.status = ?", ownerIDs, userID, "1")

	// Apply keyword filter
	if keywords != "" {
		query = query.Where("LOWER(search.name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}

	// 额外限定 tenant_id 必须在 ownerIDs 内
	query = query.Where("search.tenant_id IN ?", ownerIDs)

	// Apply ordering
	orderDirection := "ASC"
	if desc {
		orderDirection = "DESC"
	}
	query = query.Order(orderby + " " + orderDirection)

	// 拉取全部匹配记录并在内存中计数
	if err := query.Find(&searches).Error; err != nil {
		return nil, 0, err
	}

	total := int64(len(searches))

	return searches, total, nil
}

// GetByID 按主键查询搜索应用。
func (dao *SearchDAO) GetByID(id string) (*entity.Search, error) {
	var search entity.Search
	err := DB.Where("id = ?", id).First(&search).Error
	if err != nil {
		return nil, err
	}
	return &search, nil
}

// GetDetailByID 联表租户用户获取分享详情，对齐 Python SearchService.get_detail。
func (dao *SearchDAO) GetDetailByID(searchID string) (*SearchDetailRow, error) {
	var detail SearchDetailRow
	err := DB.Table("search").
		Select(`
			search.id,
			search.avatar,
			search.tenant_id,
			search.name,
			search.description,
			search.created_by,
			search.search_config,
			search.update_time,
			user.nickname,
			user.avatar AS tenant_avatar
		`).
		Joins("JOIN user ON user.id = search.tenant_id AND user.status = ?", "1").
		Where("search.id = ? AND search.status = ?", searchID, "1").
		Scan(&detail).Error
	if err != nil {
		return nil, err
	}
	if detail.ID == "" {
		return nil, nil
	}
	return &detail, nil
}

// GetByNameAndTenant 按名称与租户 ID 查询同名搜索应用列表。
func (dao *SearchDAO) GetByNameAndTenant(name string, tenantID string) ([]*entity.Search, error) {
	var searches []*entity.Search
	err := DB.Where("name = ? AND tenant_id = ? AND status = ?", name, tenantID, "1").Find(&searches).Error
	return searches, err
}

// Create 插入新搜索应用记录。
func (dao *SearchDAO) Create(search *entity.Search) error {
	return DB.Create(search).Error
}

// QueryByTenantIDAndID 校验指定租户下是否存在该搜索应用，用于详情 API 权限验证（对齐 Python SearchService.query）。
func (dao *SearchDAO) QueryByTenantIDAndID(tenantID string, searchID string) ([]*entity.Search, error) {
	var searches []*entity.Search
	err := DB.Where("tenant_id = ? AND id = ? AND status = ?", tenantID, searchID, "1").Find(&searches).Error
	return searches, err
}

// DeleteByID 软删除搜索应用（status 置为 "0"），对齐 Python delete_by_id。
func (dao *SearchDAO) DeleteByID(id string) error {
	return DB.Model(&entity.Search{}).Where("id = ?", id).Update("status", "0").Error
}

// Accessible4Deletion 判断用户是否有权删除该搜索应用（须为创建者且 status 有效），对齐 Python accessible4deletion。
func (dao *SearchDAO) Accessible4Deletion(searchID string, userID string) (bool, error) {
	var search entity.Search
	err := DB.Where("id = ? AND created_by = ? AND status = ?", searchID, userID, "1").First(&search).Error
	return err == nil, err
}

// GetByTenantIDAndID 按租户 ID 与搜索 ID 精确查询单条有效记录。
func (dao *SearchDAO) GetByTenantIDAndID(tenantID string, searchID string) (*entity.Search, error) {
	var search entity.Search
	err := DB.Where("tenant_id = ? AND id = ? AND status = ?", tenantID, searchID, "1").First(&search).Error
	if err != nil {
		return nil, err
	}
	return &search, nil
}

// UpdateByID 按 ID 部分更新搜索应用字段。
func (dao *SearchDAO) UpdateByID(id string, updates map[string]interface{}) error {
	return DB.Model(&entity.Search{}).Where("id = ?", id).Updates(updates).Error
}
