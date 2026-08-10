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

// api_token.go — 租户 API Key 管理：ListAPIKeys / CreateKey / DeleteKey，仅 owner 角色可操作。

//

package handler

import (
	"net/http"
	"ragflow/internal/common"
	"ragflow/internal/dao"
	"ragflow/internal/entity"

	"ragflow/internal/service"

	"github.com/gin-gonic/gin"
)

// ListAPIKeys 列出当前租户的全部 API Key
func (h *SystemHandler) ListAPIKeys(c *gin.Context) {
	// 从 gin 上下文读取当前登录用户
	user, exists := c.Get("user")
	if !exists {
		common.ResponseWithHttpCodeData(c, http.StatusUnauthorized, 401, nil, "Unauthorized")
		return
	}

	userModel, ok := user.(*entity.User)
	if !ok {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, 500, nil, "Invalid user data")
		return
	}

	// 查询用户 owner 角色所属租户
	userTenantDAO := dao.NewUserTenantDAO()
	tenants, err := userTenantDAO.GetByUserIDAndRole(userModel.ID, "owner")
	if err != nil || len(tenants) == 0 {
		common.ResponseWithHttpCodeData(c, http.StatusBadRequest, 400, nil, "Tenant not found")
		return
	}

	tenantID := tenants[0].TenantID

	// 拉取租户下全部 Key
	keys, err := h.systemService.ListAPIKeys(tenantID)
	if err != nil {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, 500, nil, "Failed to list keys")
		return
	}

	common.SuccessWithData(c, keys, "success")
}

// CreateKey 为租户创建新 API Key
func (h *SystemHandler) CreateKey(c *gin.Context) {
	// Get current user from context
	user, exists := c.Get("user")
	if !exists {
		common.ResponseWithHttpCodeData(c, http.StatusUnauthorized, 401, nil, "Unauthorized")
		return
	}

	userModel, ok := user.(*entity.User)
	if !ok {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, 500, nil, "Invalid user data")
		return
	}

	// Get user's tenant with owner role
	userTenantDAO := dao.NewUserTenantDAO()
	tenants, err := userTenantDAO.GetByUserIDAndRole(userModel.ID, "owner")
	if err != nil || len(tenants) == 0 {
		common.ResponseWithHttpCodeData(c, http.StatusBadRequest, 400, nil, "Tenant not found")
		return
	}

	tenantID := tenants[0].TenantID

	// 解析创建请求体
	var req service.CreateAPIKeyRequest
	if err = c.ShouldBind(&req); err != nil {
		common.ResponseWithHttpCodeData(c, http.StatusBadRequest, 400, nil, "Invalid request")
		return
	}

	// 调用服务层创建 Key
	key, err := h.systemService.CreateAPIKey(tenantID, &req)
	if err != nil {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, 500, nil, "Failed to create key")
		return
	}

	common.SuccessWithData(c, key, "success")
}

// DeleteKey 按 path 参数删除指定 Key
func (h *SystemHandler) DeleteKey(c *gin.Context) {
	// Get current user from context
	user, exists := c.Get("user")
	if !exists {
		common.ResponseWithHttpCodeData(c, http.StatusUnauthorized, 401, nil, "Unauthorized")
		return
	}

	userModel, ok := user.(*entity.User)
	if !ok {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, 500, nil, "Invalid user data")
		return
	}

	// Get user's tenant with owner role
	userTenantDAO := dao.NewUserTenantDAO()
	tenants, err := userTenantDAO.GetByUserIDAndRole(userModel.ID, "owner")
	if err != nil || len(tenants) == 0 {
		common.ResponseWithHttpCodeData(c, http.StatusBadRequest, 400, nil, "Tenant not found")
		return
	}

	tenantID := tenants[0].TenantID

	// 从路径读取待删除的 key
	key := c.Param("key")
	if key == "" {
		common.ResponseWithHttpCodeData(c, http.StatusBadRequest, 400, nil, "Key is required")
		return
	}

	// 调用服务层删除 Key
	if err = h.systemService.DeleteAPIKey(tenantID, key); err != nil {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, 500, nil, "Failed to delete key")
		return
	}

	common.SuccessWithData(c, true, "success")
}

// 三个端点均要求 context 中存在 *entity.User 且能解析 owner 租户；无租户时返回 400 Tenant not found。
