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
// user_tenant.go — 用户-租户关系 DAO：维护成员身份、角色、软删除状态及租户成员列表联表查询。

//

package dao

import (
	"context"
	"fmt"

	"ragflow/internal/entity"
)

// UserTenantDAO 用户-租户关联表的数据访问对象。
type UserTenantDAO struct{}

// NewUserTenantDAO 创建 UserTenantDAO 实例。
func NewUserTenantDAO() *UserTenantDAO {
	return &UserTenantDAO{}
}

// Create 插入新的用户-租户关系。
func (dao *UserTenantDAO) Create(userTenant *entity.UserTenant) error {
	return DB.Create(userTenant).Error
}

// GetByID 按 ID 查询有效（status=1）关系。
func (dao *UserTenantDAO) GetByID(id string) (*entity.UserTenant, error) {
	var userTenant entity.UserTenant
	err := DB.Where("id = ? AND status = ?", id, "1").First(&userTenant).Error
	if err != nil {
		return nil, err
	}
	return &userTenant, nil
}

// Update 全量保存关系实体。
func (dao *UserTenantDAO) Update(userTenant *entity.UserTenant) error {
	return DB.Save(userTenant).Error
}

// Delete 软删除：将 status 设为 "0"。
func (dao *UserTenantDAO) Delete(id string) error {
	return DB.Model(&entity.UserTenant{}).Where("id = ?", id).Update("status", "0").Error
}

// GetByUserID 按用户 ID 查全部有效关系。
func (dao *UserTenantDAO) GetByUserID(userID string) ([]*entity.UserTenant, error) {
	return dao.GetByUserIDWithContext(context.Background(), userID)
}

// GetByUserIDWithContext 带 context 的 GetByUserID。
func (dao *UserTenantDAO) GetByUserIDWithContext(ctx context.Context, userID string) ([]*entity.UserTenant, error) {
	var relations []*entity.UserTenant
	err := DB.WithContext(ctx).Where("user_id = ? AND status = ?", userID, "1").Find(&relations).Error
	return relations, err
}

// GetByTenantID 按租户 ID 查全部有效成员关系。
func (dao *UserTenantDAO) GetByTenantID(tenantID string) ([]*entity.UserTenant, error) {
	var relations []*entity.UserTenant
	err := DB.Where("tenant_id = ? AND status = ?", tenantID, "1").Find(&relations).Error
	return relations, err
}

// GetTenantIDsByUserID 返回用户所属全部租户 ID 列表。
func (dao *UserTenantDAO) GetTenantIDsByUserID(userID string) ([]string, error) {
	var tenantIDs []string
	err := DB.Model(&entity.UserTenant{}).
		Select("tenant_id").
		Where("user_id = ? AND status = ?", userID, "1").
		Pluck("tenant_id", &tenantIDs).Error
	return tenantIDs, err
}

// FilterByUserIDAndTenantID 按 user_id+tenant_id 精确查单条有效关系。
func (dao *UserTenantDAO) FilterByUserIDAndTenantID(userID, tenantID string) (*entity.UserTenant, error) {
	var userTenant entity.UserTenant
	err := DB.Where("user_id = ? AND tenant_id = ? AND status = ?", userID, tenantID, "1").
		First(&userTenant).Error
	if err != nil {
		return nil, err
	}
	return &userTenant, nil
}

// GetByUserIDAndRole 按用户 ID 与角色查关系列表。
func (dao *UserTenantDAO) GetByUserIDAndRole(userID, role string) ([]*entity.UserTenant, error) {
	var relations []*entity.UserTenant
	err := DB.Where("user_id = ? AND role = ? AND status = ?", userID, role, "1").Find(&relations).Error
	return relations, err
}

// GetNumMembers 统计租户成员数（不含 owner）。
func (dao *UserTenantDAO) GetNumMembers(tenantID string) (int64, error) {
	var count int64
	err := DB.Model(&entity.UserTenant{}).
		Where("tenant_id = ? AND status = ? AND role != ?", tenantID, "1", "owner").
		Count(&count).Error
	return count, err
}

// TenantInfoByUserID 用户视角的租户摘要（含昵称、邮箱等）。
type TenantInfoByUserID struct {
	TenantID   string `json:"tenant_id"`
	Role       string `json:"role"`
	Nickname   string `json:"nickname"`
	Email      string `json:"email"`
	Avatar     string `json:"avatar"`
	UpdateDate string `json:"update_date"`
}

// TenantMemberItem 租户成员列表项，含用户详情字段。
type TenantMemberItem struct {
	ID              string `json:"id"`
	UserID          string `json:"user_id"`
	Role            string `json:"role"`
	Status          string `json:"status"`
	Nickname        string `json:"nickname"`
	Email           string `json:"email"`
	Avatar          string `json:"avatar"`
	IsAuthenticated bool   `json:"is_authenticated"`
	IsActive        string `json:"is_active"`
	IsAnonymous     bool   `json:"is_anonymous"`
	IsSuperuser     bool   `json:"is_superuser"`
	UpdateDate      string `json:"update_date"`
}

// GetMembersByTenantID 返回租户下非 owner 成员及用户详情；update_date 格式对齐 Python API。
func (dao *UserTenantDAO) GetMembersByTenantID(tenantID string) ([]*TenantMemberItem, error) {
	var results []*TenantMemberItem
	err := DB.Table("user_tenant").
		Select("user_tenant.id, user_tenant.user_id, user_tenant.role, user_tenant.status, "+
			"user.nickname, user.email, user.avatar, user.is_authenticated, "+
			"user.status AS is_active, user.is_anonymous, user.is_superuser, "+
			"DATE_FORMAT(user.update_date, '%Y-%m-%dT%H:%i:%s') AS update_date").
		Joins("JOIN user ON user_tenant.user_id = user.id").
		Where("user_tenant.tenant_id = ? AND user_tenant.status = ? AND user_tenant.role != ?",
			tenantID, "1", "owner").
		Scan(&results).Error
	return results, err
}

// GetTenantsByUserID 返回用户加入的全部租户及租户主账号信息。
func (dao *UserTenantDAO) GetTenantsByUserID(userID string) ([]*TenantInfoByUserID, error) {
	var results []*TenantInfoByUserID
	err := DB.Table("user_tenant").
		Select("user_tenant.tenant_id, user_tenant.role, user.nickname, user.email, user.avatar, user.update_date").
		Joins("JOIN user ON user_tenant.tenant_id = user.id AND user_tenant.user_id = ? AND user_tenant.status = ?", userID, "1").
		Where("user_tenant.status = ?", "1").
		Scan(&results).Error
	return results, err
}

// DeleteByUserID 按用户 ID 硬删除全部关系。
func (dao *UserTenantDAO) DeleteByUserID(userID string) (int64, error) {
	result := DB.Unscoped().Where("user_id = ?", userID).Delete(&entity.UserTenant{})
	return result.RowsAffected, result.Error
}

// DeleteByTenantID 按租户 ID 硬删除全部关系。
func (dao *UserTenantDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ?", tenantID).Delete(&entity.UserTenant{})
	return result.RowsAffected, result.Error
}

// GetByUserIDAll 返回用户全部关系（含已软删）。
func (dao *UserTenantDAO) GetByUserIDAll(userID string) ([]*entity.UserTenant, error) {
	var relations []*entity.UserTenant
	err := DB.Where("user_id = ?", userID).Find(&relations).Error
	return relations, err
}

// DeleteByUserAndTenant 硬删除指定 user+tenant 关联行。
func (dao *UserTenantDAO) DeleteByUserAndTenant(userID, tenantID string) error {
	return DB.Unscoped().
		Where("user_id = ? AND tenant_id = ?", userID, tenantID).
		Delete(&entity.UserTenant{}).Error
}

// UpdateRoleByUserAndTenant 更新指定 user+tenant 的角色；无匹配行则报错。
func (dao *UserTenantDAO) UpdateRoleByUserAndTenant(userID, tenantID, role string) error {
	result := DB.Model(&entity.UserTenant{}).
		Where("user_id = ? AND tenant_id = ? AND status = ?", userID, tenantID, "1").
		Update("role", role)
	if result.Error != nil {
		return result.Error
	}
	if result.RowsAffected == 0 {
		return fmt.Errorf("no active membership found for user %s in tenant %s", userID, tenantID)
	}
	return nil
}
