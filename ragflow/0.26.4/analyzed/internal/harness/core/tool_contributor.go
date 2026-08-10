package core

// tool_contributor.go — ToolContributor 接口：中间件在构建阶段贡献工具与 ReturnDirectly。


import (
	"context"

	"ragflow/internal/harness/core/schema"
)

// ToolContributor 可选接口，中间件可在构建阶段贡献工具、ToolInfo 与 ReturnDirectly。
// 在 BeforeAgent 之前收集，确保 ToolsNode 与中间件均可使用。
//
// The agent loop collects contributions BEFORE calling BeforeAgent, ensuring
// that tools are available for both ToolsNode construction and BeforeAgent
// middleware processing. This replaces the unreliable pattern of modifying
// rc.Tools in BeforeAgent (which doesn't propagate to ToolsNode) and the
// timing-coupled BindToConfig pattern.
//
// Example:
//
//	type myMiddleware struct {
//	    BaseMiddleware[M]
//	}
//
//	func (m *myMiddleware) ContributeTools(ctx context.Context) []Tool {
//	    return []Tool{NewBaseTool("my_tool", "Does something", myFunc)}
//	}
type ToolContributor[M MessageType] interface {
	// ContributeTools 返回需加入智能体的 Tool 列表。
	// Called once during agent build (before BeforeAgent).
	ContributeTools(ctx context.Context) []Tool

	// ContributeToolInfos 返回需绑定到模型的 ToolInfo（如无对应 Tool 的元工具）。
	// model. These are merged with auto-generated infos from ContributeTools.
	// Use this for special entries that don't correspond to a Tool (e.g.,
	// a meta-tool like "search_tools" for dynamic tool search).
	ContributeToolInfos(ctx context.Context) []*schema.ToolInfo

	// ContributeReturnDirectly 返回执行后应立即返回的工具名集合。
	// to return immediately after execution. Merged with config-level
	// ReturnDirectly.
	ContributeReturnDirectly(ctx context.Context) map[string]bool
}

// ---- 收集辅助函数 ----

// collectContributorTools 汇总所有 ToolContributor 中间件贡献的工具。
func collectContributorTools[M MessageType](ctx context.Context, middlewares []TypedReActMiddleware[M]) []Tool {
	var all []Tool
	for _, mw := range middlewares {
		if mw == nil {
			continue
		}
		if c, ok := mw.(ToolContributor[M]); ok {
			all = append(all, c.ContributeTools(ctx)...)
		}
	}
	return all
}

// collectContributorToolInfos 汇总所有中间件贡献的 ToolInfo。
func collectContributorToolInfos[M MessageType](ctx context.Context, middlewares []TypedReActMiddleware[M]) []*schema.ToolInfo {
	var all []*schema.ToolInfo
	for _, mw := range middlewares {
		if mw == nil {
			continue
		}
		if c, ok := mw.(ToolContributor[M]); ok {
			all = append(all, c.ContributeToolInfos(ctx)...)
		}
	}
	return all
}

// collectContributorReturnDirectly 合并所有中间件的 ReturnDirectly 配置。
func collectContributorReturnDirectly[M MessageType](ctx context.Context, middlewares []TypedReActMiddleware[M]) map[string]bool {
	all := make(map[string]bool)
	for _, mw := range middlewares {
		if mw == nil {
			continue
		}
		if c, ok := mw.(ToolContributor[M]); ok {
			for k, v := range c.ContributeReturnDirectly(ctx) {
				all[k] = v
			}
		}
	}
	return all
}
