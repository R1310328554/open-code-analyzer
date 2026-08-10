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

// Package mcp 实现嵌入 RAGFlow Go 后端的 MCP（Model Context Protocol）服务端。
// 将检索、数据集列表、聊天列表等能力暴露为 MCP 工具，供外部 AI 客户端经 HTTP JSON-RPC 发现与调用。
package mcp

import (
	"encoding/json"
	"fmt"
)

// JSONRPCVersion 为协议版本字符串。
const JSONRPCVersion = "2.0"

// MCPProtocolVersion 为本服务端实现的 MCP 协议版本。
const MCPProtocolVersion = "2024-11-05"

// ServerName 标识本 MCP 服务端实例。
const ServerName = "ragflow-mcp-server"

// JSONRPCRequest 表示 JSON-RPC 2.0 请求。
type JSONRPCRequest struct {
	JSONRPC string          `json:"jsonrpc"`
	ID      json.RawMessage `json:"id"`
	Method  string          `json:"method"`
	Params  json.RawMessage `json:"params,omitempty"`
}

// JSONRPCResponse 表示 JSON-RPC 2.0 响应。
type JSONRPCResponse struct {
	JSONRPC string          `json:"jsonrpc"`
	ID      json.RawMessage `json:"id"`
	Result  interface{}     `json:"result,omitempty"`
	Error   *JSONRPCError   `json:"error,omitempty"`
}

// JSONRPCNotification 表示 JSON-RPC 2.0 通知（无 id，无需响应）。
type JSONRPCNotification struct {
	JSONRPC string          `json:"jsonrpc"`
	Method  string          `json:"method"`
	Params  json.RawMessage `json:"params,omitempty"`
}

// JSONRPCError 表示 JSON-RPC 2.0 错误对象。
type JSONRPCError struct {
	Code    int         `json:"code"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}

// Predefined JSON-RPC error codes.
const (
	ErrCodeParseError     = -32700
	ErrCodeInvalidRequest = -32600
	ErrCodeMethodNotFound = -32601
	ErrCodeInvalidParams  = -32602
	ErrCodeInternalError  = -32603
)

// InitializeResult 为 initialize 方法的响应载荷。
type InitializeResult struct {
	ProtocolVersion string       `json:"protocolVersion"`
	Capabilities    Capabilities `json:"capabilities"`
	ServerInfo      ServerInfo   `json:"serverInfo"`
}

// Capabilities 描述本服务端支持的 MCP 能力集合。
type Capabilities struct {
	Tools *ToolsCapability `json:"tools,omitempty"`
}

// ToolsCapability 表示服务端支持工具及可选的列表变更通知。
type ToolsCapability struct {
	ListChanged bool `json:"listChanged,omitempty"`
}

// ServerInfo 提供 MCP 服务端的标识信息。
type ServerInfo struct {
	Name    string `json:"name"`
	Version string `json:"version"`
}

// Tool 表示 MCP 工具定义。
type Tool struct {
	Name        string      `json:"name"`
	Description string      `json:"description"`
	InputSchema InputSchema `json:"inputSchema"`
}

// InputSchema 为工具输入参数的 JSON Schema。
type InputSchema struct {
	Type       string              `json:"type"`
	Properties map[string]Property `json:"properties,omitempty"`
	Required   []string            `json:"required,omitempty"`
}

// Property 描述 InputSchema 中的单个参数。
type Property struct {
	Type        string      `json:"type"`
	Description string      `json:"description,omitempty"`
	Default     interface{} `json:"default,omitempty"`
	Minimum     *float64    `json:"minimum,omitempty"`
	Maximum     *float64    `json:"maximum,omitempty"`
	Items       *Items      `json:"items,omitempty"`
}

// Items 描述数组类型属性的元素类型。
type Items struct {
	Type string `json:"type"`
}

// ListToolsResult 为 tools/list 方法的结果。
type ListToolsResult struct {
	Tools []Tool `json:"tools"`
}

// CallToolParams 为 tools/call 方法的 params 载荷。
type CallToolParams struct {
	Name      string                 `json:"name"`
	Arguments map[string]interface{} `json:"arguments,omitempty"`
}

// CallToolResult 为 tools/call 方法的结果。
type CallToolResult struct {
	Content []ContentItem `json:"content"`
	IsError bool          `json:"isError,omitempty"`
}

// ContentItem 为 CallToolResult.content 数组中的单项。
type ContentItem struct {
	Type string `json:"type"`
	Text string `json:"text,omitempty"`
	Data string `json:"data,omitempty"`
	// MIMEType 为数据的 MIME 类型（若存在）。
	MIMEType string `json:"mimeType,omitempty"`
}

// NewErrorResponse 创建带错误的 JSONRPCResponse。
func NewErrorResponse(id json.RawMessage, code int, message string) JSONRPCResponse {
	return JSONRPCResponse{
		JSONRPC: JSONRPCVersion,
		ID:      id,
		Error: &JSONRPCError{
			Code:    code,
			Message: message,
		},
	}
}

// NewSuccessResponse 创建带结果的 JSONRPCResponse。
func NewSuccessResponse(id json.RawMessage, result interface{}) JSONRPCResponse {
	return JSONRPCResponse{
		JSONRPC: JSONRPCVersion,
		ID:      id,
		Result:  result,
	}
}

// NewTextContent 创建 type="text" 的 ContentItem。
func NewTextContent(text string) ContentItem {
	return ContentItem{Type: "text", Text: text}
}

// NewTextResult 创建含单条文本 ContentItem 的 CallToolResult。
func NewTextResult(text string) *CallToolResult {
	return &CallToolResult{
		Content: []ContentItem{NewTextContent(text)},
	}
}

// NewErrorResult 创建表示工具执行错误的 CallToolResult。
func NewErrorResult(errMsg string) *CallToolResult {
	return &CallToolResult{
		Content: []ContentItem{NewTextContent(errMsg)},
		IsError: true,
	}
}

// NewParseError 创建标准 Parse Error 响应（id 为 null）。
func NewParseError() JSONRPCResponse {
	return JSONRPCResponse{
		JSONRPC: JSONRPCVersion,
		ID:      nil,
		Error: &JSONRPCError{
			Code:    ErrCodeParseError,
			Message: "Parse error",
		},
	}
}

// NewInvalidRequestError 创建标准 Invalid Request 响应。
func NewInvalidRequestError(id json.RawMessage, msg string) JSONRPCResponse {
	return JSONRPCResponse{
		JSONRPC: JSONRPCVersion,
		ID:      id,
		Error: &JSONRPCError{
			Code:    ErrCodeInvalidRequest,
			Message: fmt.Sprintf("Invalid Request: %s", msg),
		},
	}
}

// float64Ptr 返回 float64 指针，用于 Property minimum/maximum 默认值。
func float64Ptr(v float64) *float64 {
	return &v
}
// mcp/types.go — MCP 协议 JSON-RPC 与工具类型定义。
