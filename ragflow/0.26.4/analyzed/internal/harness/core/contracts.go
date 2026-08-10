package core

// contracts.go — agentcore 核心契约：工具/模型端点签名、Tool/Model 接口、中间件生命周期与 BaseTool 实现。


import (
	"context"
	"ragflow/internal/harness/core/schema"
)

// ---- 工具包装用端点类型 ----

// InvokableToolEndpoint 同步调用工具的函数签名。
type InvokableToolEndpoint func(ctx context.Context, args string, opts ...ToolOption) (string, error)

// StreamableToolEndpoint 流式调用工具的函数签名。
type StreamableToolEndpoint func(ctx context.Context, args string, opts ...ToolOption) (*schema.StreamReader[string], error)

// EnhancedInvokableToolEndpoint 增强工具同步调用签名，返回结构化 ToolResult。
// Enhanced tools return structured *schema.ToolResult instead of raw strings.
type EnhancedInvokableToolEndpoint func(ctx context.Context, args *schema.ToolArgument, opts ...ToolOption) (*schema.ToolResult, error)

// EnhancedStreamableToolEndpoint 增强工具流式调用签名。
type EnhancedStreamableToolEndpoint func(ctx context.Context, args *schema.ToolArgument, opts ...ToolOption) (*schema.StreamReader[*schema.ToolResult], error)

// ModelOption 配置模型调用选项。
type ModelOption interface{ applyModel() }

type modelOption = ModelOption

// ToolOption 配置工具调用选项。
type ToolOption interface{ applyTool() }

type toolOption = ToolOption

// ---- 模型接口 ----

type Model[M MessageType] interface {
	Generate(ctx context.Context, messages []M, opts ...ModelOption) (M, error)
	Stream(ctx context.Context, messages []M, opts ...ModelOption) (*schema.StreamReader[M], error)
	BindTools(tools []*schema.ToolInfo) error
}

// ---- 工具接口 ----

// Tool 基础工具接口，支持同步与流式调用。
type Tool interface {
	Name() string
	Description() string
	Invoke(ctx context.Context, argumentsInJSON string, opts ...ToolOption) (string, error)
	Stream(ctx context.Context, argumentsInJSON string, opts ...ToolOption) (*schema.StreamReader[string], error)
}

// ToolCapability 描述工具访问模式，用于并发调度。
type ToolCapability int

const (
	ToolCapReadOnly     ToolCapability = iota // 只读，可并行执行
	ToolCapWritesFiles                        // 写文件，需串行
	ToolCapExecutesCode                       // 执行代码，需串行
	ToolCapNetwork                            // 网络访问，需串行
)

// CapableTool 可选接口，声明工具能力以支持并发感知调度。
// their capability for concurrency-aware scheduling. Tools without this
// interface default to ToolCapWritesFiles (safe serialization).
type CapableTool interface {
	Tool
	Capability() ToolCapability
}

// EnhancedTool 可选接口，返回结构化 ToolResult 而非原始字符串。
// structured *schema.ToolResult instead of raw strings.
// When a Tool also satisfies EnhancedTool, the framework will call the enhanced
// methods and route through WrapEnhancedInvokableToolCall / WrapEnhancedStreamableToolCall.
type EnhancedTool interface {
	Tool
	// EnhancedInvoke invokes the tool with structured argument and returns a structured result.
	EnhancedInvoke(ctx context.Context, args *schema.ToolArgument, opts ...ToolOption) (*schema.ToolResult, error)
	// EnhancedStream invokes the tool with streaming structured results.
	EnhancedStream(ctx context.Context, args *schema.ToolArgument, opts ...ToolOption) (*schema.StreamReader[*schema.ToolResult], error)
}

// ToolInfoProvider 可选接口，提供含 JSON Schema 的工具元数据。
// provide structured metadata including the input JSON schema.
// When present, this full metadata is used when binding tools to the LLM,
// rather than the minimal Name/Description from the Tool interface.
type ToolInfoProvider interface {
	ToolInfo() *schema.ToolInfo
}

// BaseTool 由函数构造的简单 Tool 实现。
type BaseTool struct {
	name     string
	desc     string
	invokeFn func(ctx context.Context, args string) (string, error)
}

// NewBaseTool 用名称、描述与 invoke 函数创建 BaseTool
func NewBaseTool(name, desc string, fn func(ctx context.Context, args string) (string, error)) *BaseTool {
	return &BaseTool{name: name, desc: desc, invokeFn: fn}
}
func (t *BaseTool) Name() string        { return t.name }
func (t *BaseTool) Description() string { return t.desc }
func (t *BaseTool) Invoke(ctx context.Context, args string, opts ...toolOption) (string, error) {
	return t.invokeFn(ctx, args)
}
func (t *BaseTool) Stream(ctx context.Context, args string, opts ...toolOption) (*schema.StreamReader[string], error) {
	return schema.StreamReaderFromArray([]string{""}), nil
}

// ---- 模型上下文 ----

type TypedModelContext[M MessageType] struct {
	Tools               []*schema.ToolInfo
	DeferredToolInfos   []*schema.ToolInfo
	ModelRetryConfig    *TypedModelRetryConfig[M]
	ModelFailoverConfig *FailoverConfig[M]
	cancelCtx           *cancelContext
}

type ModelContext = TypedModelContext[*schema.Message]

// ---- 中间件接口 ----
//
// TypedReActMiddleware 定义 ReAct 智能体中间件接口。
// Implement *BaseMiddleware[M] to get default no-op implementations, then override only what you need.
//
// Execution order (outermost to innermost wrapper chain):
// Model call lifecycle:
//  1. BeforeAgent (can modify instruction, tools, returnDirectly)
//  2. BeforeModelRewrite (can modify state before model call)
//  3. failover -> retry -> eventSender -> WrapModel -> model.Generate
//  4. AfterModelRewrite (can modify state after model call)
//  5. AfterAgent (final state after successful completion)
// Tool call lifecycle: now handled by ToolInvokeMiddleware in ToolsNode (System C).
// Cross-cutting tool concerns (timeout, retry, cancel, event sending) are
// configured via ToolsNodeConfig.ToolInvokeMiddlewares.

type TypedReActMiddleware[M MessageType] interface {
	BeforeAgent(ctx context.Context, rc *ReActAgentContext) (context.Context, *ReActAgentContext, error)
	AfterAgent(ctx context.Context, state *TypedReActAgentState[M]) (context.Context, error)
	BeforeModelRewrite(ctx context.Context, state *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error)
	AfterModelRewrite(ctx context.Context, state *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error)
	WrapModel(ctx context.Context, m Model[M], mc *TypedModelContext[M]) (Model[M], error)
}

type ReActMiddleware = TypedReActMiddleware[*schema.Message]

// Alias names for backward compatibility.
// These allow middlewares to use the same naming convention as the ADK.
type (
	BeforeModelRewriteState[M MessageType] = TypedReActAgentState[M]
	AfterModelRewriteState[M MessageType]  = TypedReActAgentState[M]
)

// BaseMiddleware 为中间件各钩子提供空实现，按需覆写。
// Embed in custom middlewares to only override needed methods.
type BaseMiddleware[M MessageType] struct{}

func (b *BaseMiddleware[M]) BeforeAgent(ctx context.Context, rc *ReActAgentContext) (context.Context, *ReActAgentContext, error) {
	return ctx, rc, nil
}
func (b *BaseMiddleware[M]) AfterAgent(ctx context.Context, state *TypedReActAgentState[M]) (context.Context, error) {
	return ctx, nil
}
func (b *BaseMiddleware[M]) BeforeModelRewrite(ctx context.Context, state *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error) {
	return ctx, state, nil
}
func (b *BaseMiddleware[M]) AfterModelRewrite(ctx context.Context, state *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error) {
	return ctx, state, nil
}
func (b *BaseMiddleware[M]) WrapModel(_ context.Context, m Model[M], _ *TypedModelContext[M]) (Model[M], error) {
	return m, nil
}

// 模型调用链：BeforeAgent → BeforeModelRewrite → failover/retry/eventSender/WrapModel → AfterModelRewrite → AfterAgent。
