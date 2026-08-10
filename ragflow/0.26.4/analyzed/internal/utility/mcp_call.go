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

// Phase 3.7：MCP tools/call 实现。mcp_client.go 负责 tools/list 发现；
// 本文件补充 tools/call 调用路径，使 MCPToolAdapter 返回真实结果。
// 优先实现 streamable-HTTP（2025-03-26 规范）；legacy SSE 会话较复杂，暂推迟。

package utility

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"time"
)

// CallOptions 控制单次 tools/call 调用，复用 FetchTools 的 URL 安全与 DNS 固定。
type CallOptions struct {
	URL        string
	ServerType string
	Headers    map[string]string
	Variables  map[string]string
	ToolName   string
	Arguments  json.RawMessage // JSON-encoded argument object
	Timeout    time.Duration
	HTTPClient *http.Client
}

// CallResult 为 tools/call 响应：Text 拼接文本块，Content 保留结构化内容，IsError 标识工具级错误。
type CallResult struct {
	Text    string           `json:"text"`
	Content []map[string]any `json:"content"`
	IsError bool             `json:"is_error"`
}

// CallTool 按名称调用 MCP 工具；每调用独立会话（initialize → initialized → tools/call）。
func CallTool(ctx context.Context, opts CallOptions) (*CallResult, error) {
	if opts.URL == "" {
		return nil, errors.New("Invalid url.")
	}
	if opts.ToolName == "" {
		return nil, errors.New("MCP tool name is required")
	}
	if opts.Timeout <= 0 {
		opts.Timeout = 10 * time.Second
	}
	hostname, resolvedIP, err := AssertURLSafe(opts.URL)
	if err != nil {
		return nil, err
	}
	if opts.HTTPClient == nil {
		opts.HTTPClient = PinnedHTTPClient(hostname, resolvedIP, opts.Timeout)
	}
	headers, headerErr := renderHeaders(opts.Headers, opts.Variables)
	if headerErr != nil {
		return nil, headerErr
	}
	connectCtx, cancel := context.WithTimeout(ctx, opts.Timeout)
	defer cancel()

	switch opts.ServerType {
	case TransportStreamableHTTP, "":
		// 空 ServerType 默认 streamable-http；显式声明 SSE 时走 legacy 路径。
		return callToolStreamableHTTP(connectCtx, opts.URL, headers, opts.HTTPClient, opts.ToolName, opts.Arguments)
	case TransportSSE:
		return nil, errors.New("MCP tools/call on legacy SSE transport is not yet implemented in Go (Phase 3.7 deferred; use streamable-http)")
	default:
		return nil, fmt.Errorf("Unsupported MCP server type.")
	}
}

// callToolStreamableHTTP 驱动 streamable-HTTP 会话：initialize → initialized → tools/call。
func callToolStreamableHTTP(ctx context.Context, endpoint string, headers map[string]string, client *http.Client, toolName string, args json.RawMessage) (*CallResult, error) {
	sessionID, initRes, err := streamableSend(ctx, client, endpoint, "", headers, jsonRPCRequest{
		JSONRPC: jsonRPCVersion,
		ID:      0,
		Method:  "initialize",
		Params:  initializeParams(),
	}, true)
	if err != nil {
		return nil, err
	}
	if initRes.Error != nil {
		return nil, formatMCPError("initialize", initRes.Error)
	}

	if _, _, err = streamableSend(ctx, client, endpoint, sessionID, headers, jsonRPCRequest{
		JSONRPC: jsonRPCVersion,
		Method:  "notifications/initialized",
	}, false); err != nil {
		return nil, err
	}

	var argsAny any
	if len(args) > 0 {
		if err = json.Unmarshal(args, &argsAny); err != nil {
			return nil, fmt.Errorf("mcp tools/call: arguments are not valid JSON: %w", err)
		}
	}
	_, callRes, err := streamableSend(ctx, client, endpoint, sessionID, headers, jsonRPCRequest{
		JSONRPC: jsonRPCVersion,
		ID:      2,
		Method:  "tools/call",
		Params: map[string]any{
			"name":      toolName,
			"arguments": argsAny,
		},
	}, true)
	if err != nil {
		return nil, err
	}
	if callRes.Error != nil {
		return nil, formatMCPError("tools/call", callRes.Error)
	}
	return parseCallResult(callRes.Result)
}

// parseCallResult 解析 tools/call 响应，文本块拼接为 Text，完整 Content 保留供类型分支。
func parseCallResult(raw json.RawMessage) (*CallResult, error) {
	if len(raw) == 0 {
		return &CallResult{}, nil
	}
	var envelope struct {
		Content []map[string]any `json:"content"`
		IsError bool             `json:"isError"`
	}
	if err := json.Unmarshal(raw, &envelope); err != nil {
		return nil, fmt.Errorf("parse tools/call result: %w", err)
	}
	out := &CallResult{
		Content: envelope.Content,
		IsError: envelope.IsError,
	}
	for _, block := range envelope.Content {
		t, _ := block["type"].(string)
		if t != "text" {
			continue
		}
		if s, ok := block["text"].(string); ok {
			if out.Text != "" {
				out.Text += "\n"
			}
			out.Text += s
		}
	}
	return out, nil
}
// mcp_call.go — MCP tools/call 调用实现（streamable-HTTP 传输），复用 SSRF 防护与 DNS 固定。
