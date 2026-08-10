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

// agent_routes.go — Agent 画布 REST 端点集中注册（plan §4.8）。
package router

import (
	"github.com/gin-gonic/gin"

	"ragflow/internal/handler"
)

// RegisterAgentRoutes 将模板/CRUD/运行/会话/Webhook 等路由挂到 /agents 子组。
//
// The existing GET /api/v1/agents (added in commit 0a7662cf3) is replaced
// by this registration so the route count, ordering and middleware all
// live in one place. The original GET is preserved verbatim at
// router.go:349 until the orchestrator swaps it for a call to this
// function.
func RegisterAgentRoutes(g *gin.RouterGroup, h *handler.AgentHandler) {
	if g == nil || h == nil {
		return
	}
	// 发现与元数据接口。
	g.GET("/templates", h.ListAgentTemplates)
	g.GET("/prompts", h.Prompts)
	g.GET("/tags", h.ListAgentTags)

	// Agent 画布增删改查与运行控制。
	g.GET("", h.ListAgents)
	g.POST("", h.CreateAgent)
	g.GET("/:canvas_id", h.GetAgent)
	g.PUT("/:canvas_id", h.UpdateAgent)
	g.DELETE("/:canvas_id", h.DeleteAgent)
	g.POST("/:canvas_id/run", h.RunAgent)
	g.DELETE("/:canvas_id/run", h.CancelAgent)
	g.POST("/:canvas_id/publish", h.PublishAgent)
	g.PUT("/:canvas_id/tags", h.UpdateAgentTags)
	g.POST("/:canvas_id/reset", h.ResetAgent)

	// 附件上传与下载。
	g.GET("/download", h.DownloadAgentFile)
	g.GET("/attachments/:attachment_id/download", h.DownloadAttachment)
	g.GET("/attachments/:attachment_id/preview", h.PreviewAttachment)
	g.POST("/:canvas_id/upload", h.UploadAgentFile)

	// 组件输入表单与调试。
	g.GET("/:canvas_id/components/:component_id/input-form", h.GetComponentInputForm)
	g.POST("/:canvas_id/components/:component_id/debug", h.DebugComponent)

	// 发布版本管理。
	g.GET("/:canvas_id/versions", h.ListVersions)
	g.GET("/:canvas_id/versions/:version_id", h.GetVersion)
	g.DELETE("/:canvas_id/versions/:version_id", h.DeleteVersion)

	// 运行会话列表与生命周期。
	g.GET("/:canvas_id/sessions", h.ListAgentSessions)
	g.POST("/:canvas_id/sessions", h.CreateAgentSession)
	g.GET("/:canvas_id/sessions/:session_id", h.GetAgentSession)
	g.DELETE("/:canvas_id/sessions", h.DeleteAgentSession)
	g.DELETE("/:canvas_id/sessions/:session_id", h.DeleteAgentSession)

	// 运行日志与 Webhook 触发（六 HTTP 方法同路径）。
	g.GET("/:canvas_id/logs/:message_id", h.GetAgentLogs)
	g.GET("/:canvas_id/webhook/logs", h.GetAgentWebhookLogs)
	// Webhook trigger endpoints. The Python agent API
	// (api/apps/restful_apis/agent_api.py:1563-1564) registers six
	// HTTP methods on a single path. Gin has no Match() helper, so we
	// register each verb explicitly via registerAnyMethod. The handler
	// is identical for all six; semantics differ only by
	// c.Request.Method.
	registerAnyMethod(g, "/:canvas_id/webhook", h.Webhook)
	registerAnyMethod(g, "/:canvas_id/webhook/test", h.Webhook)

	// Top-level actions (no canvas id in path).
	// NOTE: `/chat/completion` (singular) is intentionally NOT registered.
	// The singular form was a historical typo in earlier Python releases —
	// no client, SDK, or doc ever called it, and the Python side
	// (api/apps/restful_apis/agent_api.py) has since removed the route.
	// See plan: .claude/plans/agent-api-gaps-go-port.md §Gap E.
	g.POST("/chat/completions", h.AgentChatCompletions)
	g.POST("/rerun", h.RerunAgent)
	g.POST("/test_db_connection", h.TestDBConnection)
}

// registerAnyMethod 显式注册六种 HTTP 方法到同一路径，对齐 Python Flask 多方法路由。
//
// Centralising the registration here keeps RegisterAgentRoutes readable
// when both the production trigger and the test trigger share the same
// six-method shape.
func registerAnyMethod(g *gin.RouterGroup, path string, h gin.HandlerFunc) {
	if g == nil || h == nil {
		return
	}
	g.POST(path, h)
	g.GET(path, h)
	g.PUT(path, h)
	g.PATCH(path, h)
	g.DELETE(path, h)
	g.HEAD(path, h)
}
// agent_routes.go — Agent 画布 CRUD、运行、会话、Webhook 等 REST 路由注册。
