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
// chat.go — 对话（dialog）数据访问：租户对话 CRUD、分页列表、名称去重及权限校验查询。

//

package dao

import (
	"fmt"
	"strings"
	"time"

	"gorm.io/gorm"

	"ragflow/internal/entity"
)

// ChatDAO 对话/助手（dialog 表）数据访问对象。
type ChatDAO struct{}

// NewChatDAO 构造 ChatDAO 实例。
func NewChatDAO() *ChatDAO {
	return &ChatDAO{}
}

// ListByTenantID 按租户与可选 status 过滤，create_time 降序。
func (dao *ChatDAO) ListByTenantID(tenantID string, status string) ([]*entity.Chat, error) {
	var chats []*entity.Chat

	query := DB.Model(&entity.Chat{}).
		Where("tenant_id = ?", tenantID)

	if status != "" {
		query = query.Where("status = ?", status)
	}

	// Order by create_time desc
	if err := query.Order("create_time DESC").Find(&chats).Error; err != nil {
		return nil, err
	}

	return chats, nil
}

// ListByTenantIDs 多租户分页列表，JOIN user 取 nickname/avatar，支持关键词搜索。
func (dao *ChatDAO) ListByTenantIDs(tenantIDs []string, userID string, page, pageSize int, orderby string, desc bool, keywords string) ([]*entity.Chat, int64, error) {
	var chats []*entity.Chat
	var total int64

	// Build query with join to user table for nickname and avatar
	query := DB.Model(&entity.Chat{}).
		Select(`
			dialog.*,
			user.nickname,
			user.avatar as tenant_avatar
		`).
		Joins("LEFT JOIN user ON dialog.tenant_id = user.id")

	if len(tenantIDs) > 0 {
		query = query.Where("(dialog.tenant_id IN ? OR dialog.tenant_id = ?) AND dialog.status = ?", tenantIDs, userID, "1")
	} else {
		query = query.Where("dialog.tenant_id = ? AND dialog.status = ?", userID, "1")
	}

	// Apply keyword filter
	if keywords != "" {
		query = query.Where("LOWER(dialog.name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}

	// Apply ordering
	orderDirection := "ASC"
	if desc {
		orderDirection = "DESC"
	}
	query = query.Order(orderby + " " + orderDirection)

	// Count total
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Apply pagination
	if page > 0 && pageSize > 0 {
		offset := (page - 1) * pageSize
		if err := query.Offset(offset).Limit(pageSize).Find(&chats).Error; err != nil {
			return nil, 0, err
		}
	} else {
		if err := query.Find(&chats).Error; err != nil {
			return nil, 0, err
		}
	}

	return chats, total, nil
}

// ListByOwnerIDs 按 owner 租户 ID 过滤，内存计数分页（无 OFFSET/LIMIT）。
func (dao *ChatDAO) ListByOwnerIDs(ownerIDs []string, userID string, orderby string, desc bool, keywords string) ([]*entity.Chat, int64, error) {
	var chats []*entity.Chat

	// Build query with join to user table
	query := DB.Model(&entity.Chat{}).
		Select(`
			dialog.*,
			user.nickname,
			user.avatar as tenant_avatar
		`).
		Joins("LEFT JOIN user ON dialog.tenant_id = user.id").
		Where("(dialog.tenant_id IN ? OR dialog.tenant_id = ?) AND dialog.status = ?", ownerIDs, userID, "1")

	// Apply keyword filter
	if keywords != "" {
		query = query.Where("LOWER(dialog.name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}

	// Filter by owner IDs (additional filter to ensure tenant_id is in ownerIDs)
	query = query.Where("dialog.tenant_id IN ?", ownerIDs)

	// Apply ordering
	orderDirection := "ASC"
	if desc {
		orderDirection = "DESC"
	}
	query = query.Order(orderby + " " + orderDirection)

	// Get all matching records
	if err := query.Find(&chats).Error; err != nil {
		return nil, 0, err
	}

	total := int64(len(chats))

	return chats, total, nil
}

// GetByID 按主键 ID 查询单条对话。
func (dao *ChatDAO) GetByID(id string) (*entity.Chat, error) {
	var chat entity.Chat
	err := DB.Where("id = ?", id).First(&chat).Error
	if err != nil {
		return nil, err
	}
	return &chat, nil
}

// GetByIDAndStatus 按 ID 与 status 联合查询。
func (dao *ChatDAO) GetByIDAndStatus(id string, status string) (*entity.Chat, error) {
	var chat entity.Chat
	err := DB.Where("id = ? AND status = ?", id, status).First(&chat).Error
	if err != nil {
		return nil, err
	}
	return &chat, nil
}

// GetExistingNames 获取租户下已有对话名称列表（用于重名检测）。
func (dao *ChatDAO) GetExistingNames(tenantID string, status string) ([]string, error) {
	var names []string
	err := DB.Model(&entity.Chat{}).
		Where("tenant_id = ? AND status = ?", tenantID, status).
		Pluck("name", &names).Error
	return names, err
}

// ExistsByNameTenantStatus 检查同名对话是否已存在于租户下。
func (dao *ChatDAO) ExistsByNameTenantStatus(name, tenantID, status string) (bool, error) {
	var count int64
	err := DB.Model(&entity.Chat{}).
		Where("name = ? AND tenant_id = ? AND status = ?", name, tenantID, status).
		Count(&count).Error
	return count > 0, err
}

// Create 插入新对话记录。
func (dao *ChatDAO) Create(chat *entity.Chat) error {
	return DB.Create(chat).Error
}

// UpdateByID 按 ID 部分更新，自动刷新 update_time/update_date。
func (dao *ChatDAO) UpdateByID(id string, updates map[string]interface{}) error {
	if updates == nil {
		updates = make(map[string]interface{})
	}

	now := time.Now().Local()
	updates["update_time"] = now.UnixMilli()
	updates["update_date"] = now.Truncate(time.Second)

	result := DB.Session(&gorm.Session{SkipHooks: true}).Model(&entity.Chat{}).Where("id = ?", id).Updates(updates)
	if result.Error != nil {
		return result.Error
	}
	if result.RowsAffected == 0 {
		var count int64
		if err := DB.Model(&entity.Chat{}).Where("id = ?", id).Count(&count).Error; err != nil {
			return err
		}
		if count == 0 {
			return gorm.ErrRecordNotFound
		}
	}
	return nil
}

// UpdateManyByID 事务内批量按 ID 更新多条对话。
func (dao *ChatDAO) UpdateManyByID(updates []map[string]interface{}) error {
	if len(updates) == 0 {
		return nil
	}

	// Use transaction for batch update
	tx := DB.Begin()
	if tx.Error != nil {
		return tx.Error
	}

	for _, update := range updates {
		id, ok := update["id"].(string)
		if !ok {
			tx.Rollback()
			return fmt.Errorf("invalid id in update")
		}

		// Remove id from updates map
		updatesWithoutID := make(map[string]interface{})
		for k, v := range update {
			if k != "id" {
				updatesWithoutID[k] = v
			}
		}

		if err := tx.Model(&entity.Chat{}).Where("id = ?", id).Updates(updatesWithoutID).Error; err != nil {
			tx.Rollback()
			return err
		}
	}

	return tx.Commit().Error
}

// DeleteByTenantID 硬删除租户下全部对话。
func (dao *ChatDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ?", tenantID).Delete(&entity.Chat{})
	return result.RowsAffected, result.Error
}

// GetAllDialogIDsByTenantID 列出租户下全部 dialog ID。
func (dao *ChatDAO) GetAllDialogIDsByTenantID(tenantID string) ([]string, error) {
	var dialogIDs []string
	err := DB.Model(&entity.Chat{}).
		Where("tenant_id = ?", tenantID).
		Pluck("id", &dialogIDs).Error
	return dialogIDs, err
}

// QueryByTenantIDAndID 校验租户+对话+状态三元组，供 get_chat 权限验证。
func (dao *ChatDAO) QueryByTenantIDAndID(tenantID string, chatID string, status string) ([]*entity.Chat, error) {
	var chats []*entity.Chat
	err := DB.Where("tenant_id = ? AND id = ? AND status = ?", tenantID, chatID, status).Find(&chats).Error
	return chats, err
}
