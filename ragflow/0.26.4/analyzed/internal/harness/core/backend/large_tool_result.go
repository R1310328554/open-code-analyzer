// large_tool_result.go — 超大工具结果截断，避免超出上下文窗口。

package backend

import "fmt"

// LargeToolResult 处理工具结果超出上下文窗口限制的情况。
type LargeToolResult struct {
	Size    int
	Content string
}

// NewLargeToolResult 超过 maxSize 时截断并追加 ...(truncated)。
func NewLargeToolResult(content string, maxSize int) *LargeToolResult {
	if len(content) > maxSize {
		return &LargeToolResult{Size: len(content), Content: content[:maxSize] + "\n...(truncated)"}
	}
	return &LargeToolResult{Size: len(content), Content: content}
}

// String 格式化为 [Tool Result: N bytes] 加内容。
func (r *LargeToolResult) String() string {
	return fmt.Sprintf("[Tool Result: %d bytes]\n%s", r.Size, r.Content)
}
