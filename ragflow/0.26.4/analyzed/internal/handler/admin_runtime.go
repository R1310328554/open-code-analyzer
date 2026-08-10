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

// admin_runtime.go — 管理端 Canvas 运行时切换：Phase 6 金丝雀 API，按租户覆盖 go/python/auto 执行引擎。
//

package handler

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"

	"ragflow/internal/agent/runtime"
	"ragflow/internal/common"
)

// AdminRuntimeHandler 租户级 canvas 运行时覆盖 HTTP 处理器
// 供 Phase 6 金丝雀运维使用，仅依赖 runtime.Selector；
// 结构刻意保持精简。
// AdminRuntimeHandler 持有运行时选择器

type AdminRuntimeHandler struct {
	selector *runtime.Selector
}

// NewAdminRuntimeHandler 注入 Selector 构造处理器；
// selector 为 nil 视为配置错误，所有请求返回 500。
// the handler refuses every request with HTTP 500.
// NewAdminRuntimeHandler 创建处理器，selector 不可为 nil（否则运行时 500）
func NewAdminRuntimeHandler(selector *runtime.Selector) *AdminRuntimeHandler {
	return &AdminRuntimeHandler{selector: selector}
}

// setRuntimeRequest POST /api/v1/admin/canvas-runtime/:tenant_id 请求体
// runtime 必填；空值或未知值返回 400。
// unknown values yield 400.
type setRuntimeRequest struct {
	// Runtime 目标模式：go、python 或 auto
	Runtime string `json:"runtime"`
}

// setRuntimeResponse 成功时 200 响应体
type setRuntimeResponse struct {
	Code     common.ErrorCode `json:"code"`
	TenantID string           `json:"tenant_id"`
	Runtime  string           `json:"runtime"`
	Message  string           `json:"message"`
}

// ErrSelectorNotConfigured Selector 未配置时返回，映射 HTTP 500
// without a backing Selector. It maps to HTTP 500 in the response path.
var ErrSelectorNotConfigured = errors.New("admin runtime: selector not configured")

// SetTenantRuntime 实现 POST /api/v1/admin/canvas-runtime/:tenant_id
//
// 鉴权缺口：当前接受任意已认证请求；专用 admin 角色中间件待补。
// Phase 6 PR 在此标注缺口，金丝雀仅在内网操作。
// 生产上线前必须接入 admin 鉴权。
// trusted network. Production rollout MUST wire admin auth before opening
// this endpoint publicly.
// SetTenantRuntime 解析 tenant_id 与 runtime，写入 Selector 并返回确认
func (h *AdminRuntimeHandler) SetTenantRuntime(c *gin.Context) {
	if h.selector == nil {
		common.ResponseWithCodeData(c, common.CodeExceptionError, nil, ErrSelectorNotConfigured.Error())
		return
	}

	tenantID := c.Param("tenant_id")
	if tenantID == "" {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "tenant_id is required")
		return
	}

	var req setRuntimeRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "Invalid request body: "+err.Error())
		return
	}

	mode := runtime.RuntimeMode(req.Runtime)
	switch mode {
	case runtime.RuntimeGo, runtime.RuntimePython, runtime.RuntimeAuto: // 允许的三种运行时
		// allowed
	default:
		common.ResponseWithCodeData(c, common.CodeArgumentError, nil, "runtime must be one of: go, python, auto")
		return
	}

	if err := h.selector.Set(c.Request.Context(), tenantID, mode); err != nil {
		common.ResponseWithCodeData(c, common.CodeDataError, nil, err.Error())
		return
	}

	c.JSON(http.StatusOK, setRuntimeResponse{
		Code:     common.CodeSuccess,
		TenantID: tenantID,
		Runtime:  string(mode),
		Message:  "ok",
	})
}

// 与 agent/canvas 执行路径联动：Selector 持久化租户级 override。auto 表示按策略自动选择 go 或 python 引擎。
