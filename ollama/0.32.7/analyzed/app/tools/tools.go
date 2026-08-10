//go:build windows || darwin

// Package tools 定义 Agent 工具接口、注册表与 JSON Schema 导出。
package tools

import (
	"context"
	"encoding/json"
	"fmt"
)

// Tool 为所有 Agent 工具必须实现的接口。
// Tool defines the interface that all tools must implement
type Tool interface {
	// Name returns the unique identifier for the tool
	Name() string

	// Description returns a human-readable description of what the tool does
	Description() string

	// Schema returns the JSON schema for the tool's parameters
	Schema() map[string]any

	// Execute runs the tool with the given arguments and returns result to store in db, and a string result for the model
	Execute(ctx context.Context, args map[string]any) (any, string, error)

	// Prompt returns a prompt for the tool
	Prompt() string
}

// Registry 维护可用工具集合及工作目录上下文。
// Registry manages the available tools and their execution
type Registry struct {
	tools      map[string]Tool
	workingDir string // Working directory for all tool operations
}

// NewRegistry 创建空的工具注册表。
// NewRegistry creates a new tool registry with no tools
func NewRegistry() *Registry {
	return &Registry{
		tools: make(map[string]Tool),
	}
}

// Register 按工具 Name 注册到注册表。
// Register adds a tool to the registry
func (r *Registry) Register(tool Tool) {
	r.tools[tool.Name()] = tool
}

// Get 按名称查找工具。
// Get retrieves a tool by name
func (r *Registry) Get(name string) (Tool, bool) {
	tool, exists := r.tools[name]
	return tool, exists
}

// List 返回全部已注册工具。
// List returns all available tools
func (r *Registry) List() []Tool {
	tools := make([]Tool, 0, len(r.tools))
	for _, tool := range r.tools {
		tools = append(tools, tool)
	}
	return tools
}

// SetWorkingDir 设置工具执行时的默认工作目录。
// SetWorkingDir sets the working directory for all tool operations
func (r *Registry) SetWorkingDir(dir string) {
	r.workingDir = dir
}

// Execute 按名称查找并执行工具。
// Execute runs a tool with the given name and arguments
func (r *Registry) Execute(ctx context.Context, name string, args map[string]any) (any, string, error) {
	tool, ok := r.tools[name]
	if !ok {
		return nil, "", fmt.Errorf("unknown tool: %s", name)
	}

	result, text, err := tool.Execute(ctx, args)
	if err != nil {
		return nil, "", err
	}
	return result, text, nil
}

// ToolCall 表示模型发起的一次工具调用请求。
// ToolCall represents a request to execute a tool
type ToolCall struct {
	ID       string       `json:"id"`
	Type     string       `json:"type"`
	Function ToolFunction `json:"function"`
}

// ToolFunction 描述被调用的函数名与 JSON 参数。
// ToolFunction represents the function call details
type ToolFunction struct {
	Name      string          `json:"name"`
	Arguments json.RawMessage `json:"arguments"`
}

// ToolResult 表示工具执行结果或错误信息。
// ToolResult represents the result of a tool execution
type ToolResult struct {
	ToolCallID string `json:"tool_call_id"`
	Content    any    `json:"content"`
	Error      string `json:"error,omitempty"`
}

// AvailableTools 导出全部工具的 name/description/schema 供 API 使用。
// ToolSchemas returns all tools as schema maps suitable for API calls
func (r *Registry) AvailableTools() []map[string]any {
	schemas := make([]map[string]any, 0, len(r.tools))
	for _, tool := range r.tools {
		schema := map[string]any{
			"name":        tool.Name(),
			"description": tool.Description(),
			"schema":      tool.Schema(),
		}
		schemas = append(schemas, schema)
	}
	return schemas
}

// ToolNames 返回所有已注册工具名称列表。
// ToolNames returns a list of all tool names
func (r *Registry) ToolNames() []string {
	names := make([]string, 0, len(r.tools))
	for name := range r.tools {
		names = append(names, name)
	}
	return names
}
