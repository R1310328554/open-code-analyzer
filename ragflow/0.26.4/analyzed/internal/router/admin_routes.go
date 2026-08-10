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
//

// admin_routes.go — Phase 6 租户 canvas 运行时覆盖路由，与主路由表分离便于扩展。
package router

import (
	"github.com/gin-gonic/gin"

	"ragflow/internal/handler"
)

// RegisterAdminRuntimeRoutes 在 /admin 组注册 POST canvas-runtime/:tenant_id，设置租户运行时 go/python/auto。
//
// The single route is:
//
//	POST /api/v1/admin/canvas-runtime/:tenant_id
//	body:    {"runtime": "go" | "python" | "auto"}
//	response: 200 {"code":0,"tenant_id":...,"runtime":...,"message":"ok"}
//
// The handler h must be non-nil. A handler with a nil selector (e.g.
// the server started before Redis was reachable) still serves this
// route — SetTenantRuntime responds with HTTP 500 and
// ErrSelectorNotConfigured. The previous version of this function
// silently no-op'd on a nil handler, which made the route disappear
// after a Redis outage at boot and only re-appear on the next process
// restart. Review follow-up: keep the route hot, surface a clear error
// to the operator.
func RegisterAdminRuntimeRoutes(g *gin.RouterGroup, h *handler.AdminRuntimeHandler) {
	if g == nil || h == nil {
		return
	}
	g.POST("/canvas-runtime/:tenant_id", h.SetTenantRuntime)
}
// admin_routes.go — Phase 6 管理端路由：租户级 canvas 运行时（go/python/auto）覆盖接口。
