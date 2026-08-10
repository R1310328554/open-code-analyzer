package core

// react.go — ReAct 中间件共享状态与上下文类型：TypedReActAgentState、ReActAgentContext、ToolContext 等。


import "ragflow/internal/harness/core/schema"

// TypedReActAgentState ReAct 中间件可访问的导出状态类型。
type TypedReActAgentState[M MessageType] struct {
	Messages            []M
	ToolInfos           []*schema.ToolInfo
	DeferredToolInfos   []*schema.ToolInfo
	Extra               map[string]any
	RemainingIterations int
}

type ReActAgentState = TypedReActAgentState[*schema.Message]

func NewReActAgentState[M MessageType](msgs []M, tools []*schema.ToolInfo, maxIter int) *TypedReActAgentState[M] {
	return &TypedReActAgentState[M]{
		Messages: msgs, ToolInfos: tools,
		RemainingIterations: maxIter, Extra: make(map[string]any),
	}
}

// ReActAgentContext 传给 BeforeAgent 中间件的上下文。
type ReActAgentContext struct {
	Instruction    string
	Tools          []Tool
	ReturnDirectly map[string]bool
	ToolSearchTool *schema.ToolInfo
}

// ToolContext 描述正在被包装调用的工具元数据。
type ToolContext struct {
	Name   string
	CallID string
}

// ToolCallsContext 描述一批已完成的工具调用。
type ToolCallsContext struct {
	ToolCalls []ToolContext
}

// NewReActAgentState 初始化消息、工具信息与剩余迭代次数。
