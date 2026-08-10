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
// api_token.go — API 令牌与 Bot 会话 DAO：管理租户 API Token 及 api_4_conversation 表的 CRUD 与统计。

//

package dao

import (
	"errors"

	"ragflow/internal/entity"
)

// APITokenDAO 租户 API 访问令牌数据访问对象。
type APITokenDAO struct{}

// NewAPITokenDAO 构造 APITokenDAO 实例。
func NewAPITokenDAO() *APITokenDAO {
	return &APITokenDAO{}
}

// Create 插入新的 API 令牌记录。
func (dao *APITokenDAO) Create(apiToken *entity.APIToken) error {
	return DB.Create(apiToken).Error
}

// GetByTenantID 按租户 ID 列出全部 API 令牌。
func (dao *APITokenDAO) GetByTenantID(tenantID string) ([]*entity.APIToken, error) {
	var tokens []*entity.APIToken
	err := DB.Where("tenant_id = ?", tenantID).Find(&tokens).Error
	return tokens, err
}

// DeleteByTenantID 硬删除租户下所有 API 令牌。
func (dao *APITokenDAO) DeleteByTenantID(tenantID string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ?", tenantID).Delete(&entity.APIToken{})
	return result.RowsAffected, result.Error
}

// GetUserByAPIToken 按 access token 查找 API 令牌（鉴权入口）。
func (dao *APITokenDAO) GetUserByAPIToken(token string) (*entity.APIToken, error) {
	var apiToken entity.APIToken
	err := DB.Where("token = ?", token).First(&apiToken).Error
	if err != nil {
		return nil, err
	}
	return &apiToken, nil
}

// GetByBeta 按 beta 密钥查询（SDK/Bot 授权），对齐 Python APIToken.query(beta=)。
func (dao *APITokenDAO) GetByBeta(beta string) ([]*entity.APIToken, error) {
	var tokens []*entity.APIToken
	err := DB.Where("beta = ?", beta).Find(&tokens).Error
	return tokens, err
}

// DeleteByDialogIDs 按对话 ID 批量硬删除关联 API 令牌。
func (dao *APITokenDAO) DeleteByDialogIDs(dialogIDs []string) (int64, error) {
	if len(dialogIDs) == 0 {
		return 0, nil
	}
	result := DB.Unscoped().Where("dialog_id IN ?", dialogIDs).Delete(&entity.APIToken{})
	return result.RowsAffected, result.Error
}

// DeleteByTenantIDAndToken 按租户与 token 值精确删除单条令牌。
func (dao *APITokenDAO) DeleteByTenantIDAndToken(tenantID, token string) (int64, error) {
	result := DB.Unscoped().Where("tenant_id = ? AND token = ?", tenantID, token).Delete(&entity.APIToken{})
	return result.RowsAffected, result.Error
}

// API4ConversationDAO Bot/API 多轮会话（api_4_conversation 表）数据访问。
type API4ConversationDAO struct{}

// NewAPI4ConversationDAO 构造 API4ConversationDAO 实例。
func NewAPI4ConversationDAO() *API4ConversationDAO {
	return &API4ConversationDAO{}
}

// ConversationStatsRow 按日聚合的会话统计行（PV/UV/Token 等）。
type ConversationStatsRow struct {
	Dt       string  `gorm:"column:dt"`
	PV       int64   `gorm:"column:pv"`
	UV       int64   `gorm:"column:uv"`
	Tokens   float64 `gorm:"column:tokens"`
	Duration float64 `gorm:"column:duration"`
	Round    float64 `gorm:"column:round"`
	ThumbUp  int64   `gorm:"column:thumb_up"`
}

// Create 插入新会话行；ID/时间戳由调用方设置以对齐 Python Agent API。
func (dao *API4ConversationDAO) Create(conv *entity.API4Conversation) error {
	if conv == nil {
		return errors.New("api4 conversation: nil row")
	}
	return DB.Create(conv).Error
}

// Update 回写会话 Message JSON，每轮 Bot 完成后更新以携带历史上下文。
func (dao *API4ConversationDAO) Update(conv *entity.API4Conversation) error {
	if conv == nil {
		return errors.New("api4 conversation: nil row")
	}
	if conv.ID == "" {
		return errors.New("api4 conversation: empty id")
	}
	return DB.Save(conv).Error
}

// Stats 按租户与日期范围返回每日会话聚合统计，可按 source 过滤。
func (dao *API4ConversationDAO) Stats(tenantID, fromDate, toDate string, source *string) ([]ConversationStatsRow, error) {
	var rows []ConversationStatsRow
	dateExpr := "DATE_FORMAT(a.create_date, '%Y-%m-%d 00:00:00')"
	db := DB.Table("api_4_conversation AS a").
		Select(`
			DATE_FORMAT(a.create_date, '%Y-%m-%d 00:00:00') AS dt,
			COUNT(a.id) AS pv,
			COUNT(DISTINCT a.user_id) AS uv,
			COALESCE(SUM(a.tokens), 0) AS tokens,
			COALESCE(SUM(a.duration), 0) AS duration,
			COALESCE(AVG(a.round), 0) AS round,
			COALESCE(SUM(a.thumb_up), 0) AS thumb_up
		`).
		Joins("JOIN dialog AS d ON a.dialog_id = d.id AND d.tenant_id = ?", tenantID).
		Where("a.create_date >= ? AND a.create_date <= ?", fromDate, toDate)

	if source == nil {
		db = db.Where("a.source IS NULL")
	} else {
		db = db.Where("a.source = ?", *source)
	}

	err := db.Group(dateExpr).
		Order(dateExpr).
		Scan(&rows).Error
	return rows, err
}

// GetBySessionID 按会话 ID 与 Agent（dialog）ID 查询单条记录。
func (dao *API4ConversationDAO) GetBySessionID(sessionID, agentID string) (*entity.API4Conversation, error) {
	var result entity.API4Conversation
	tx := DB.Where("id = ? AND dialog_id = ?", sessionID, agentID).Find(&result)
	if tx.Error != nil {
		return nil, tx.Error
	}
	if tx.RowsAffected == 0 {
		return nil, nil
	}
	return &result, nil
}

// ListIDsByAgentID 列出某 Agent 下全部会话 ID。
func (dao *API4ConversationDAO) ListIDsByAgentID(agentID string) ([]string, error) {
	var ids []string
	err := DB.Model(&entity.API4Conversation{}).Where("dialog_id = ?", agentID).Pluck("id", &ids).Error
	return ids, err
}

// DeleteBySessionIDAndAgentID 按会话与 Agent ID 删除单条会话。
func (dao *API4ConversationDAO) DeleteBySessionIDAndAgentID(sessionID, agentID string) (int64, error) {
	result := DB.Where("id = ? AND dialog_id = ?", sessionID, agentID).Delete(&entity.API4Conversation{})
	return result.RowsAffected, result.Error
}

// DeleteByDialogIDs 按 dialog ID 批量硬删除会话。
func (dao *API4ConversationDAO) DeleteByDialogIDs(dialogIDs []string) (int64, error) {
	if len(dialogIDs) == 0 {
		return 0, nil
	}
	result := DB.Unscoped().Where("dialog_id IN ?", dialogIDs).Delete(&entity.API4Conversation{})
	return result.RowsAffected, result.Error
}
