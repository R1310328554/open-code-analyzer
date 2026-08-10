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
// user.go — 用户（User）数据访问层：提供用户 CRUD、邮箱/Token 登录查询、昵称读取及软/硬删除。

//

package dao

import (
	"context"

	"ragflow/internal/entity"
)

// UserDAO 用户表的数据访问对象。
type UserDAO struct{}

// NewUserDAO 创建用户 DAO 实例。
func NewUserDAO() *UserDAO {
	return &UserDAO{}
}

// Create 插入新用户记录。
func (dao *UserDAO) Create(user *entity.User) error {
	return DB.Create(user).Error
}

// GetByID 按数值主键查询用户。
func (dao *UserDAO) GetByID(id uint) (*entity.User, error) {
	var user entity.User
	err := DB.First(&user, id).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// GetByTenantID 按租户 ID（即 user.id 字符串）查询用户。
func (dao *UserDAO) GetByTenantID(tenantID string) (*entity.User, error) {
	var user entity.User
	err := DB.Where("id = ?", tenantID).First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// GetNicknameByID 按字符串 id 查询用户昵称（支持 context 传递）。
func (dao *UserDAO) GetNicknameByID(ctx context.Context, id string) (string, error) {
	var nickname string
	err := DB.WithContext(ctx).
		Model(&entity.User{}).
		Where("id = ?", id).
		Select("nickname").
		Scan(&nickname).Error
	return nickname, err
}

// GetByEmail 按邮箱精确查询用户。
func (dao *UserDAO) GetByEmail(email string) (*entity.User, error) {
	var user entity.User
	query := DB.Where("email = ?", email)
	err := query.First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// GetByAccessToken 按 access_token 查询用户（会话校验）。
func (dao *UserDAO) GetByAccessToken(token string) (*entity.User, error) {
	var user entity.User
	err := DB.Where("access_token = ?", token).First(&user).Error
	if err != nil {
		return nil, err
	}
	return &user, nil
}

// Update 全量保存用户实体。
func (dao *UserDAO) Update(user *entity.User) error {
	return DB.Save(user).Error
}

// UpdateAccessToken 更新用户 access_token 字段。
func (dao *UserDAO) UpdateAccessToken(user *entity.User, token string) error {
	return DB.Model(user).Update("access_token", token).Error
}

// List 分页列出用户并返回总数（注释称过滤已删除，实现为全表计数）。
func (dao *UserDAO) List(offset, limit int) ([]*entity.User, int64, error) {
	var users []*entity.User
	var total int64

	// 统计用户总数（与 Find 使用同一 Model 查询）
	if err := DB.Model(&entity.User{}).Count(&total).Error; err != nil {
		return nil, 0, err
	}

	query := DB.Model(&entity.User{})
	if offset > 0 {
		query = query.Offset(offset)
	}
	if limit > 0 {
		query = query.Limit(limit)
	}
	err := query.Find(&users).Error
	return users, total, err
}

// Delete 按数值 ID 硬删除用户。
func (dao *UserDAO) Delete(id uint) error {
	return DB.Delete(&entity.User{}, id).Error
}

// DeleteByID 按字符串 ID 软删除（status 置 0）。
func (dao *UserDAO) DeleteByID(id string) error {
	return DB.Model(&entity.User{}).Where("id = ?", id).Update("status", "0").Error
}

// HardDelete 按字符串 ID 物理删除用户。
func (dao *UserDAO) HardDelete(id string) error {
	return DB.Unscoped().Where("id = ?", id).Delete(&entity.User{}).Error
}

// ListByEmail 按邮箱列出全部匹配用户。
func (dao *UserDAO) ListByEmail(email string) ([]*entity.User, error) {
	var users []*entity.User
	err := DB.Where("email = ?", email).Find(&users).Error
	return users, err
}
