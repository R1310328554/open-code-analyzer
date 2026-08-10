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

// components.go — 组件目录端点（Phase 4）：GET /api/v1/components 按 category 列出 runtime 注册的可编排组件。

//

// Phase 4（port-rag-flow-pipeline-to-go）：暴露 GET /api/v1/components?category=ingestion,agent,shared
// 分类不区分大小写、逗号分隔；数据源为 runtime.DefaultRegistry，
// 无独立 catalog 映射（见计划 §4 task 2）。未知 category 返回 400 错误信封。
// 合法值对照 runtime.Category 三常量：agent / ingestion / shared。
// 未知 token 由 parseCategories 报错，前端可展示明确提示。
// 服务失败时同样走标准 {code,message,data} 信封。
// message instead of silently dropping the request.
package handler

import (
	"net/http"
	"ragflow/internal/common"
	"strings"

	"github.com/gin-gonic/gin"

	"ragflow/internal/agent/runtime"
	"ragflow/internal/service"
)

// ComponentsHandler 组件目录 HTTP 处理器。
type ComponentsHandler struct {
	svc *service.ComponentsService
}

// NewComponentsHandler 绑定无状态 ComponentsService（启动时构造一次）。
// instance. The service is stateless so the same pointer can be
// shared across handlers; construction happens once at server
// startup (cmd/server_main.go).
func NewComponentsHandler(svc *service.ComponentsService) *ComponentsHandler {
	return &ComponentsHandler{svc: svc}
}

// Get 处理 GET /api/v1/components。
//
// 查询参数：
//   - category（可选，逗号分隔）：agent / ingestion / shared；空表示全部。
//     不区分大小写。
//     缺省或空串表示返回所有分类。
//
// 成功响应：{ data: [ { name, category, inputs, outputs } ] }
//
//	{ "data": [ { "name": "...", "category": "...",
//	              "inputs": {...}, "outputs": {...} } ] }
//
// 未知 category → HTTP 400；服务异常 → HTTP 500。
// envelope (gin.H{code, message, data}) with HTTP 400. Service
// failures bubble up as HTTP 500 with the same envelope shape.
func (h *ComponentsHandler) Get(c *gin.Context) {
	raw := c.Query("category")
	cats, err := parseCategories(raw)
	if err != nil {
		common.ResponseWithHttpCodeData(c, http.StatusBadRequest, 400, nil, err.Error())
		return
	}

	out, err := h.svc.List(cats...)
	if err != nil {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, 500, nil, err.Error())
		return
	}

	common.SuccessWithData(c, out, "success")
}

// parseCategories 将逗号分隔的 category 字符串解析为 []runtime.Category。
// 空输入返回 nil 切片（表示不过滤）。
// 首个非法 token 触发 categoryError。
// yields an error; the first invalid token wins so the message
// identifies the offender.
func parseCategories(raw string) ([]runtime.Category, error) {
	raw = strings.TrimSpace(raw)
	if raw == "" {
		return nil, nil
	}
	var out []runtime.Category
	for _, p := range strings.Split(raw, ",") {
		p = strings.TrimSpace(strings.ToLower(p))
		if p == "" {
			continue
		}
		switch p {
		case "agent":
			out = append(out, runtime.CategoryAgent)
		case "ingestion":
			out = append(out, runtime.CategoryIngestion)
		case "shared":
			out = append(out, runtime.CategoryShared)
		default:
			return nil, &categoryError{value: p}
		}
	}
	return out, nil
}

// categoryError parseCategories 对未知 category 返回的错误类型。
// Error() 格式：unknown category: <value>（与计划测试契约一致）。
// expected plan test contract ("unknown category: <value>").
type categoryError struct {
	value string
}

func (e *categoryError) Error() string {
	return "unknown category: " + e.value
}
