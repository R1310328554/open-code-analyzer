package agent

import (
	"context"
	"fmt"
	"sort"

	"github.com/ollama/ollama/api"
)

// ToolContext 向工具执行传递运行时上下文（如工作目录）。
type ToolContext struct {
	WorkingDir string
}

// ToolResult 封装工具执行返回的内容与更新后的工作目录。
type ToolResult struct {
	Content    string
	WorkingDir string
}

// Tool 定义智能体可调用的函数工具契约。
type Tool interface {
	Name() string
	Description() string
	Schema() api.ToolFunction
	Execute(context.Context, ToolContext, map[string]any) (ToolResult, error)
}

// ApprovalRequired 由需要用户审批的工具实现。
type ApprovalRequired interface {
	RequiresApproval(map[string]any) bool
}

// ScopedTool is implemented by tools that need per-invocation approval
// scoping beyond the tool name (e.g. shell commands scoped to the exact
// command string). Tools that don't implement this are scoped by name only.
type ScopedTool interface {

// ScopedTool 由需按调用粒度审批的工具实现（如 shell 按命令字符串）。
	ApprovalScope(args map[string]any) string
}

// Registry 维护名称到 Tool 实例的注册表。
type Registry struct {
	tools map[string]Tool
}

// Register 注册工具；同名后者覆盖前者。
func (r *Registry) Register(tool Tool) {
	if r == nil || tool == nil {
		return
	}
	if r.tools == nil {
		r.tools = make(map[string]Tool)
	}
	r.tools[tool.Name()] = tool
}

// Get 按名称查找工具。
func (r *Registry) Get(name string) (Tool, bool) {
	if r == nil {
		return nil, false
	}
	tool, ok := r.tools[name]
	return tool, ok
}

// Names 返回已注册工具名（字典序）。
func (r *Registry) Names() []string {
	if r == nil {
		return nil
	}
	names := make([]string, 0, len(r.tools))
	for name := range r.tools {
		names = append(names, name)
	}
	sort.Strings(names)
	return names
}

// Tools 导出为 Ollama API 工具定义列表。
func (r *Registry) Tools() api.Tools {
	if r == nil {
		return nil
	}
	names := r.Names()
	apiTools := make(api.Tools, 0, len(names))
	for _, name := range names {
		tool := r.tools[name]
		apiTools = append(apiTools, api.Tool{
			Type:     "function",
			Function: tool.Schema(),
		})
	}
	return apiTools
}

// Execute 按 ToolCall 调度工具执行。
func (r *Registry) Execute(ctx context.Context, toolCtx ToolContext, call api.ToolCall) (ToolResult, error) {
	tool, ok := r.Get(call.Function.Name)
	if !ok {
		return ToolResult{}, fmt.Errorf("unknown tool: %s", call.Function.Name)
	}
	return tool.Execute(ctx, toolCtx, call.Function.Arguments.ToMap())
}

// ToolRequiresApproval 判断工具在当前参数下是否需要审批。
func ToolRequiresApproval(tool Tool, args map[string]any) bool {
	if tool == nil {
		return false
	}
	if t, ok := tool.(ApprovalRequired); ok {
		return t.RequiresApproval(args)
	}
	return false
}
