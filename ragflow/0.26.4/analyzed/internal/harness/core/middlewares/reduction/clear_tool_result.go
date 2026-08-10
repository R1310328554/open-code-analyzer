package reduction

// clear_tool_result.go — 上下文缩减：BeforeModelRewrite 前清除陈旧 tool 结果，仅保留每个工具名最新一条。


import (
	"context"
	"strings"

	"ragflow/internal/harness/core"
	"ragflow/internal/harness/core/schema"
)

// ClearConfig 配置不参与清理的工具名列表。
type ClearConfig struct {
	// ExcludeTools lists tool names whose results should NOT be cleared.
	ExcludeTools []string
}

// ClearOldToolResults 在模型重写前移除旧 tool 消息，防止上下文膨胀。
// This prevents the context window from being filled with stale tool results.
func ClearOldToolResults[M core.MessageType](ctx context.Context, state *core.TypedReActAgentState[M], exclude []string) *core.TypedReActAgentState[M] {
	if state == nil || len(state.Messages) == 0 {
		return state
	}
	cleaned := make([]M, 0, len(state.Messages))
	keepCount := 0
	for _, msg := range state.Messages {
		switch v := any(msg).(type) {
		case *schema.Message:
			if v.Role == schema.RoleTool && !isExcluded(v.Name, exclude) {
				if keepToolCall(cleaned, v) {
					cleaned = append(cleaned, msg)
				}
				continue
			}
		}
		cleaned = append(cleaned, msg)
		keepCount++
	}
	state.Messages = cleaned
	return state
}

func isExcluded(name string, exclude []string) bool {
	if name == "" {
		return false
	}
	for _, e := range exclude {
		if strings.EqualFold(name, e) {
			return true
		}
	}
	return false
}

// keepToolCall 每个工具名仅保留最新一条 tool 结果。
func keepToolCall[M core.MessageType](existing []M, newMsg *schema.Message) bool {
	for i := len(existing) - 1; i >= 0; i-- {
		switch v := any(existing[i]).(type) {
		case *schema.Message:
			if v.Role == schema.RoleTool && v.Name == newMsg.Name {
				return false
			}
		}
	}
	return true
}

// ExcludeTools 中的工具名大小写不敏感，其历史结果不会被清除。
