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

// langfuse.go — Langfuse 集成 HTTP 处理器：租户级 API Key 的增删查。

//

package handler

import (
	"github.com/gin-gonic/gin"

	"ragflow/internal/common"
	"ragflow/internal/entity"
	"ragflow/internal/service"
)

// LangfuseService Handler 依赖的行为接口（便于 mock 测试）。
// mocking in tests).
type LangfuseService interface {
	SetAPIKey(tenantID, secretKey, publicKey, host string) (*entity.TenantLangfuse, common.ErrorCode, error)
	GetAPIKey(tenantID string) (*entity.LangfuseInfoResponse, common.ErrorCode, string, error)
	DeleteAPIKey(tenantID string) (bool, common.ErrorCode, string, error)
}

// LangfuseHandler 处理 /langfuse/api-key 相关请求。
type LangfuseHandler struct {
	langfuseService LangfuseService
}

// NewLangfuseHandler 构造 LangfuseHandler。
func NewLangfuseHandler(langfuseService LangfuseService) *LangfuseHandler {
	return &LangfuseHandler{langfuseService: langfuseService}
}

// NewLangfuse 无参构造，与其他 Handler 风格一致。
func NewLangfuse() *LangfuseHandler {
	return NewLangfuseHandler(service.NewLangfuseService())
}

// SetLangfuseRequest POST/PUT 请求体；空值校验在服务层（对齐 Python 提示）。
// the service layer to reproduce the Python "Missing required fields" message.
type SetLangfuseRequest struct {
	SecretKey string `json:"secret_key"`
	PublicKey string `json:"public_key"`
	Host      string `json:"host"`
}

// SetAPIKey 设置 Langfuse API Key。
func (h *LangfuseHandler) SetAPIKey(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	var req SetLangfuseRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		common.ResponseWithCodeData(c, common.CodeDataError, nil, "Invalid request: "+err.Error())
		return
	}

	row, code, err := h.langfuseService.SetAPIKey(user.ID, req.SecretKey, req.PublicKey, req.Host)
	if err != nil {
		common.ErrorWithCode(c, int(code), err.Error())
		return
	}

	// Echo back the stored keys, matching the Python langfuse_keys payload.
	common.SuccessWithData(c, gin.H{
		"tenant_id":  row.TenantID,
		"secret_key": row.SecretKey,
		"public_key": row.PublicKey,
		"host":       row.Host,
	}, "success")
}

// GetAPIKey 获取 Langfuse API Key。
func (h *LangfuseHandler) GetAPIKey(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	data, code, message, err := h.langfuseService.GetAPIKey(user.ID)
	if err != nil {
		common.ResponseWithCodeData(c, code, nil, message)
		return
	}
	common.ResponseWithCodeData(c, code, data, message)
}

// DeleteAPIKey 删除 Langfuse API Key。
func (h *LangfuseHandler) DeleteAPIKey(c *gin.Context) {
	user, errorCode, errorMessage := GetUser(c)
	if errorCode != common.CodeSuccess {
		common.ErrorWithCode(c, int(errorCode), errorMessage)
		return
	}

	ok, code, message, err := h.langfuseService.DeleteAPIKey(user.ID)
	if err != nil {
		common.ResponseWithCodeData(c, code, nil, message)
		return
	}
	// No record: mirror get_json_result(message=...) with data=nil.
	if message != "" {
		common.SuccessWithData(c, nil, message)
		return
	}
	common.SuccessWithData(c, ok, "success")
}
