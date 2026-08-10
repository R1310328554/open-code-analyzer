package core

// event_sender.go — 模型/工具事件发送中间件：在 Handlers 链中控制事件发射位置，避免与内置 sender 重复。


import (
	"context"

	"ragflow/internal/harness/core/schema"
)

// ---- NewEventSenderModelWrapper 创建模型输出事件发送处理器 ----
// Place this in the Handlers chain to control WHERE events are emitted:
// - Innermost position (last in Handlers list): events contain original (unmodified) model output
// - Outermost position (first in Handlers list): events contain fully processed output
//
// When detected in Handlers, the framework skips its built-in event sender to avoid duplicates.
func NewEventSenderModelWrapper[M MessageType]() *eventSenderModelHandler[M] {
	return &eventSenderModelHandler[M]{}
}

type eventSenderModelHandler[M MessageType] struct{}

func (h *eventSenderModelHandler[M]) WrapModel(ctx context.Context, m Model[M], mc *TypedModelContext[M]) (Model[M], error) {
	ec := getReActExecCtx[M](ctx)
	if ec == nil {
		return m, nil
	}
	return wrapModelWithEventSender(m, ec), nil
}

// 其余中间件方法均为空实现
func (h *eventSenderModelHandler[M]) BeforeAgent(ctx context.Context, rc *ReActAgentContext) (context.Context, *ReActAgentContext, error) {
	return ctx, rc, nil
}
func (h *eventSenderModelHandler[M]) AfterAgent(ctx context.Context, state *TypedReActAgentState[M]) (context.Context, error) {
	return ctx, nil
}
func (h *eventSenderModelHandler[M]) BeforeModelRewrite(ctx context.Context, state *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error) {
	return ctx, state, nil
}
func (h *eventSenderModelHandler[M]) AfterModelRewrite(ctx context.Context, state *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error) {
	return ctx, state, nil
}

// HasUserEventSenderModelWrapper 检测用户是否已注册事件 sender，避免重复。
// NewEventSenderModelWrapper. When present, the framework skips its internal default
// model event sender to avoid duplicate events.
func HasUserEventSenderModelWrapper[M MessageType](handlers []TypedReActMiddleware[M]) bool {
	for _, h := range handlers {
		if _, ok := h.(*eventSenderModelHandler[M]); ok {
			return true
		}
	}
	return false
}

// ---- 工具事件构造器 ----

// TypedToolInvokeEvent 为同步工具结果构造 AgentEvent。
func TypedToolInvokeEvent(result string, tc *ToolContext) *TypedAgentEvent[*schema.Message] {
	msg := schema.ToolMessage(result, tc.CallID)
	return typedEventFromMessage(msg, nil, schema.RoleTool, tc.Name)
}

// TypedToolStreamEvent 为流式工具结果构造 AgentEvent。
func TypedToolStreamEvent(resultChunks []string, tc *ToolContext) *TypedAgentEvent[*schema.Message] {
	content := ""
	for _, ch := range resultChunks {
		content += ch
	}
	msg := schema.ToolMessage(content, tc.CallID)
	return typedEventFromMessage(msg, nil, schema.RoleTool, tc.Name)
}

// TypedEnhancedToolInvokeEvent 为增强工具结果构造事件，传播 Extra 元数据。
// Propagates Extra metadata for multimodal support.
func TypedEnhancedToolInvokeEvent(result *schema.ToolResult, tc *ToolContext) *TypedAgentEvent[*schema.Message] {
	content := result.Content
	if content == "" {
		content = result.Error
	}
	msg := schema.ToolMessage(content, tc.CallID)
	msg.Name = tc.Name
	if result.Extra != nil {
		if msg.Extra == nil {
			msg.Extra = make(map[string]any, len(result.Extra))
		}
		for k, v := range result.Extra {
			msg.Extra[k] = v
		}
	}
	return typedEventFromMessage(msg, nil, schema.RoleTool, tc.Name)
}

// TypedEnhancedToolStreamEvent 为流式增强工具结果构造事件。
// Propagates the last result's Extra metadata.
func TypedEnhancedToolStreamEvent(results []*schema.ToolResult, tc *ToolContext) *TypedAgentEvent[*schema.Message] {
	if len(results) == 0 {
		return nil
	}
	last := results[len(results)-1]
	content := last.Content
	if content == "" {
		content = last.Error
	}
	msg := schema.ToolMessage(content, tc.CallID)
	msg.Name = tc.Name
	if last.Extra != nil {
		if msg.Extra == nil {
			msg.Extra = make(map[string]any, len(last.Extra))
		}
		for k, v := range last.Extra {
			msg.Extra[k] = v
		}
	}
	return typedEventFromMessage(msg, nil, schema.RoleTool, tc.Name)
}

// Handlers 最外层：事件反映完全处理后输出；最内层：反映原始模型输出。
