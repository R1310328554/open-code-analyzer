package reduction

// tool_result.go — 工具结果截断与按工具名查找最近一条结果的辅助函数。


import (
	"fmt"

	"ragflow/internal/harness/core/schema"
)

// TruncateToolResult 将 tool 结果截断至 maxLen，并在 rune 边界切分。
// 避免在 UTF-8 多字节字符中间截断。
func TruncateToolResult(result string, maxLen int) string {
	if maxLen <= 0 || len(result) <= maxLen {
		return result
	}
	// 在 rune 边界截断（[:maxLen] 可能切断多字节字符）。
	runes := []rune(result)
	if maxLen > len(runes) {
		return result
	}
	truncated := string(runes[:maxLen])
	return fmt.Sprintf("%s\n...(truncated %d bytes)", truncated, len(result)-len(truncated))
}

// LastToolResult 从后向前查找指定工具名的最近一条 tool 结果。
func LastToolResult(msgs []*schema.Message, toolName string) *schema.Message {
	for i := len(msgs) - 1; i >= 0; i-- {
		if msgs[i].Role == schema.RoleTool && msgs[i].Name == toolName {
			return msgs[i]
		}
	}
	return nil
}

// 截断后缀含被截掉字节数，便于调试上下文丢失范围。
