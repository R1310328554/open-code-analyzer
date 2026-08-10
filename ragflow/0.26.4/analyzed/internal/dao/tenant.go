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
// tenant.go — 租户（Tenant）数据访问层：查询用户关联租户、所有者租户详情及租户 CRUD。

//

package dao

import (
	"ragflow/internal/entity"
)

// TenantDAO 租户表的数据访问对象。
type TenantDAO struct{}

// NewTenantDAO 创建租户 DAO 实例。
func NewTenantDAO() *TenantDAO {
	return &TenantDAO{}
}

// GetJoinedTenantsByUserID 查询用户以 normal 角色加入的有效租户及默认模型 ID。
func (dao *TenantDAO) GetJoinedTenantsByUserID(userID string) ([]*TenantWithRole, error) {
	var results []*TenantWithRole

	err := DB.Model(&entity.Tenant{}).
		Select("tenant.id as tenant_id, tenant.name, tenant.llm_id, tenant.embd_id, tenant.asr_id, tenant.img2txt_id, user_tenant.role").
		Joins("INNER JOIN user_tenant ON user_tenant.tenant_id = tenant.id").
		Where("user_tenant.user_id = ? AND user_tenant.status = ? AND user_tenant.role = ? AND tenant.status = ?", userID, "1", "normal", "1").
		Scan(&results).Error

	return results, err
}

// TenantWithRole 用户加入租户时的联表视图（含角色与默认模型配置）。
type TenantWithRole struct {
	// TenantID 租户 ID
	TenantID  string `gorm:"column:tenant_id" json:"tenant_id"`
	// Name 租户名称
	Name      string `gorm:"column:name" json:"name"`
	// LLMID 默认大语言模型 ID
	LLMID     string `gorm:"column:llm_id" json:"llm_id"`
	// EmbDID 默认嵌入模型 ID
	EmbDID    string `gorm:"column:embd_id" json:"embd_id"`
	// ASRID 默认语音识别模型 ID
	ASRID     string `gorm:"column:asr_id" json:"asr_id"`
	// Img2TxtID 默认图生文模型 ID
	Img2TxtID string `gorm:"column:img2txt_id" json:"img2txt_id"`
	// Role 用户在租户中的角色
	Role      string `gorm:"column:role" json:"role"`
}

// TenantInfo 所有者租户的完整模型配置视图（含 rerank、TTS、OCR、parser_ids 等）。
type TenantInfo struct {
	TenantID  string  `gorm:"column:tenant_id" json:"tenant_id"`
	Name      *string `gorm:"column:name" json:"name,omitempty"`
	LLMID     string  `gorm:"column:llm_id" json:"llm_id"`
	EmbDID    string  `gorm:"column:embd_id" json:"embd_id"`
	RerankID  string  `gorm:"column:rerank_id" json:"rerank_id"`
	ASRID     string  `gorm:"column:asr_id" json:"asr_id"`
	Img2TxtID string  `gorm:"column:img2txt_id" json:"img2txt_id"`
	TTSID     *string `gorm:"column:tts_id" json:"tts_id,omitempty"`
	OCRID     *string `gorm:"column:ocr_id" json:"ocr_id,omitempty"`
	ParserIDs string  `gorm:"column:parser_ids" json:"parser_ids"`
	Role      string  `gorm:"column:role" json:"role"`
}

// GetInfoByUserID 查询用户作为 owner 的有效租户及完整模型配置。
func (dao *TenantDAO) GetInfoByUserID(userID string) ([]*TenantInfo, error) {
	var results []*TenantInfo

	err := DB.Model(&entity.Tenant{}).
		Select("tenant.id as tenant_id, tenant.name, tenant.llm_id, tenant.embd_id, tenant.rerank_id, tenant.asr_id, tenant.img2txt_id, tenant.tts_id, tenant.ocr_id, tenant.parser_ids, user_tenant.role").
		Joins("INNER JOIN user_tenant ON user_tenant.tenant_id = tenant.id").
		Where("user_tenant.user_id = ? AND user_tenant.status = ? AND user_tenant.role = ? AND tenant.status = ?", userID, "1", "owner", "1").
		Scan(&results).Error

	return results, err
}

// GetByID 按 ID 查询有效租户。
func (dao *TenantDAO) GetByID(id string) (*entity.Tenant, error) {
	var tenant entity.Tenant
	err := DB.Where("id = ? AND status = ?", id, "1").First(&tenant).Error
	if err != nil {
		return nil, err
	}
	return &tenant, nil
}

// Create 插入新租户记录。
func (dao *TenantDAO) Create(tenant *entity.Tenant) error {
	return DB.Create(tenant).Error
}

// Delete 软删除租户（status 置 0）。
func (dao *TenantDAO) Delete(id string) error {
	return DB.Model(&entity.Tenant{}).Where("id = ?", id).Update("status", "0").Error
}

// Update 按 ID 部分更新租户字段。
func (dao *TenantDAO) Update(id string, updates map[string]interface{}) error {
	return DB.Model(&entity.Tenant{}).Where("id = ?", id).Updates(updates).Error
}

// HardDelete 按 ID 物理删除租户。
func (dao *TenantDAO) HardDelete(id string) error {
	return DB.Unscoped().Where("id = ?", id).Delete(&entity.Tenant{}).Error
}
