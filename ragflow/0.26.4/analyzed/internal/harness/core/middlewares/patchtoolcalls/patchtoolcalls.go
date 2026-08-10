// Package patchtoolcalls 修补会话历史中未完成的工具调用。
// When the model's tool call was interrupted or cut off, this middleware
// inserts placeholder tool messages so the conversation remains consistent.
package patchtoolcalls

// patchtoolcalls.go — 修补未完成工具调用：为中断或截断的 tool call 插入占位 tool 消息以保持对话一致。

import (
	"context"
	"fmt"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// PatchedContentGenerator 生成占位 tool 消息内容。
type PatchedContentGenerator func(toolName, toolCallID string) string

// Config 配置占位文案生成器与语言。
type Config struct {
	// PatchedContent overrides the default patch message content.
	PatchedContent PatchedContentGenerator
	// Language for default messages: "en" or "zh"
	Language string
}

type middleware[M core.MessageType] struct {
	core.BaseMiddleware[M]
	cfg *Config
}

func defaultPatchContent(toolName, toolCallID string) string {
	return fmt.Sprintf("[Tool call was not completed: %s(%s)]", toolName, toolCallID)
}

func zhPatchContent(toolName, toolCallID string) string {
	return fmt.Sprintf("[工具调用未完成: %s(%s)]", toolName, toolCallID)
}

func getPatchContent(cfg *Config, toolName, toolCallID string) string {
	if cfg.PatchedContent != nil {
		return cfg.PatchedContent(toolName, toolCallID)
	}
	if cfg.Language == "zh" {
		return zhPatchContent(toolName, toolCallID)
	}
	return defaultPatchContent(toolName, toolCallID)
}

// New 创建 patchtoolcalls 中间件
func New[M core.MessageType](cfg *Config) core.TypedReActMiddleware[M] {
	if cfg == nil {
		cfg = &Config{}
	}
	return &middleware[M]{cfg: cfg}
}

// buildPatchPlaceholder 按 MessageType 构造 tool 或 Agentic 占位消息
func buildPatchPlaceholder[M core.MessageType](content, callID string) M {
	var zero M
	switch any(zero).(type) {
	case *schema.AgenticMessage:
		return any(&schema.AgenticMessage{
			Role:    schema.AgenticRoleUser,
			Content: content,
			ContentBlocks: []schema.ContentBlock{
				{Type: "tool_result", ToolResult: &schema.ToolResult{
					ToolCallID: callID,
					Content:    content,
				}},
			},
		}).(M)
	default:
		return any(schema.ToolMessage(content, callID)).(M)
	}
}

// BeforeModelRewrite 扫描 assistant 工具调用并插入缺失的 tool 结果
func (m *middleware[M]) BeforeModelRewrite(ctx context.Context, state *core.TypedReActAgentState[M], mc *core.TypedModelContext[M]) (context.Context, *core.TypedReActAgentState[M], error) {
	// Build a new slice instead of mutating state.Messages in-place to avoid
	// fragility from slice reallocation mid-iteration.
	var patched []M

	// Pre-index AgenticMessage tool results by call ID for O(1) lookup.
	agenticToolResults := make(map[string]bool)
	for _, msg := range state.Messages {
		if v, ok := any(msg).(*schema.AgenticMessage); ok {
			for _, b := range v.ContentBlocks {
				if b.ToolResult != nil && b.ToolResult.ToolCallID != "" {
					agenticToolResults[b.ToolResult.ToolCallID] = true
				}
			}
		}
	}

	for i := 0; i < len(state.Messages); i++ {
		msg := state.Messages[i]
		patched = append(patched, msg)

		var toolCalls []struct{ ID, Name string }
		switch v := any(msg).(type) {
		case *schema.Message:
			if v.Role != schema.RoleAssistant || len(v.ToolCalls) == 0 {
				continue
			}
			// Next message is already a tool result — skip patching.
			if i+1 < len(state.Messages) {
				if next, ok := any(state.Messages[i+1]).(*schema.Message); ok && next.Role == schema.RoleTool {
					continue
				}
			}
			for _, tc := range v.ToolCalls {
				toolCalls = append(toolCalls, struct{ ID, Name string }{tc.ID, tc.Function.Name})
			}
		case *schema.AgenticMessage:
			for _, b := range v.ContentBlocks {
				if b.ToolCall != nil && b.ToolCall.ID != "" && !agenticToolResults[b.ToolCall.ID] {
					toolCalls = append(toolCalls, struct{ ID, Name string }{b.ToolCall.ID, b.ToolCall.Name})
				}
			}
			if len(toolCalls) == 0 {
				continue
			}
		default:
			continue
		}
		if len(toolCalls) == 0 {
			continue
		}
		for _, tc := range toolCalls {
			patchContent := getPatchContent(m.cfg, tc.Name, tc.ID)
			placeholder := buildPatchPlaceholder[M](patchContent, tc.ID)
			patched = append(patched, placeholder)
		}
	}
	state.Messages = patched
	return ctx, state, nil
}

// 下一消息已是 tool 结果则跳过；AgenticMessage 用 ContentBlocks 索引 O(1) 查重。
