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
// mcp.go — MCP（Model Context Protocol）服务器 DAO：租户级 MCP 端点的 CRUD、名称唯一性检查及安全排序字段映射。

//

package dao

import (
	"errors"
	"fmt"
	"strings"

	"ragflow/internal/entity"

	"gorm.io/gorm"
)

// MCPServerDAO MCP 服务器配置表的数据访问对象。
type MCPServerDAO struct{}

// InvalidMCPServerOrderByError 未知排序字段时返回与 Python 列表接口一致的错误形态。
type InvalidMCPServerOrderByError struct {
	Field string
}

// Error 格式化 AttributeError 风格的错误消息。
func (e *InvalidMCPServerOrderByError) Error() string {
	return fmt.Sprintf("AttributeError(%q)", fmt.Sprintf("type object 'MCPServer' has no attribute '%s'", e.Field))
}

// NewMCPServerDAO 创建 MCPServerDAO 实例。
func NewMCPServerDAO() *MCPServerDAO {
	return &MCPServerDAO{}
}

// GetByID 按 ID 查询 MCP 服务器，未找到返回 (nil, nil)。
func (dao *MCPServerDAO) GetByID(id string) (*entity.MCPServer, error) {
	var server entity.MCPServer
	if err := DB.Where("id = ?", id).First(&server).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return &server, nil
}

// ExistsByNameAndTenant 检查租户下 MCP 名称是否已占用。
func (dao *MCPServerDAO) ExistsByNameAndTenant(name, tenantID string) (bool, error) {
	var count int64
	if err := DB.Model(&entity.MCPServer{}).
		Where("name = ? AND tenant_id = ?", name, tenantID).
		Count(&count).Error; err != nil {
		return false, err
	}
	return count > 0, nil
}

// CreateMCPServer 插入新的 MCP 服务器配置。
func (dao *MCPServerDAO) CreateMCPServer(server *entity.MCPServer) error {
	return DB.Create(server).Error
}

// ListMCPServers 按租户列出 MCP 服务器，支持 ID 过滤、关键词与排序。
func (dao *MCPServerDAO) ListMCPServers(tenantID string, ids []string, keywords string, orderby string, desc bool) ([]*entity.MCPServer, int64, error) {
	var servers []*entity.MCPServer
	var total int64

	query := DB.Model(&entity.MCPServer{}).Where("tenant_id = ?", tenantID)

	if len(ids) > 0 {
		query = query.Where("id IN ?", ids)
	}

	if keywords != "" {
		query = query.Where("LOWER(name) LIKE ?", "%"+strings.ToLower(keywords)+"%")
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	orderColumn, err := mcpServerOrderColumn(orderby)
	if err != nil {
		return nil, 0, err
	}
	orderDirection := "ASC"
	if desc {
		orderDirection = "DESC"
	}
	query = query.Order(orderColumn + " " + orderDirection)

	if err := query.
		Select("id", "name", "server_type", "url", "description", "variables", "create_date", "update_date").
		Find(&servers).Error; err != nil {
		return nil, 0, err
	}

	return servers, total, nil
}

// GetByIDAndTenant 按 ID 与 tenant_id 查询（租户隔离）。
func (dao *MCPServerDAO) GetByIDAndTenant(id, tenantID string) (*entity.MCPServer, error) {
	var server entity.MCPServer
	if err := DB.Where("id = ? AND tenant_id = ?", id, tenantID).First(&server).Error; err != nil {
		return nil, err
	}
	return &server, nil
}

// DeleteMCPServer 删除租户拥有的 MCP 服务器，返回是否删除成功。
func (dao *MCPServerDAO) DeleteMCPServer(id, tenantID string) (bool, error) {
	result := DB.Where("id = ? AND tenant_id = ?", id, tenantID).Delete(&entity.MCPServer{})
	if result.Error != nil {
		return false, result.Error
	}
	return result.RowsAffected > 0, nil
}

// UpdateMCPServer 部分更新租户 MCP 服务器配置。
func (dao *MCPServerDAO) UpdateMCPServer(id, tenantID string, updates map[string]interface{}) (bool, error) {
	result := DB.Model(&entity.MCPServer{}).
		Where("id = ? AND tenant_id = ?", id, tenantID).
		Updates(updates)
	if result.Error != nil {
		return false, result.Error
	}
	return result.RowsAffected > 0, nil
}

// mcpServerOrderColumn 将 API 排序字段映射为数据库列名，非法字段返回错误。
func mcpServerOrderColumn(orderby string) (string, error) {
	switch orderby {
	case "id":
		return "id", nil
	case "name":
		return "name", nil
	case "server_type":
		return "server_type", nil
	case "url":
		return "url", nil
	case "update_time", "update_date":
		return "update_date", nil
	case "create_time", "create_date":
		return "create_date", nil
	default:
		return "", &InvalidMCPServerOrderByError{Field: orderby}
	}
}
