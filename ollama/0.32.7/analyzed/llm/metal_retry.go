// macOS Metal 张量 API 初始化失败时的保守重试策略。
package llm

import (
	"runtime"
	"strings"
)

// ShouldRetryWithMetalTensorDisabled 检测 Metal 张量 API 初始化失败，
// 若 ggml 探测通过但内核编译或上下文创建失败，应设 GGML_METAL_TENSOR_DISABLE=1 重试。
// ShouldRetryWithMetalTensorDisabled detects Metal tensor API initialization
// failures where ggml's probe can pass but real kernel compilation or context
// creation fails. Retrying with GGML_METAL_TENSOR_DISABLE=1 keeps discovery and
// later runner launches on the conservative Metal path.
func ShouldRetryWithMetalTensorDisabled(err error, status *StatusWriter) bool {
	if runtime.GOOS != "darwin" {
		return false
	}

	var msg strings.Builder
	if err != nil {
		msg.WriteString(strings.ToLower(err.Error()))
	}
	if status != nil && status.LastError() != "" {
		msg.WriteByte(' ')
		msg.WriteString(strings.ToLower(status.LastError()))
	}
	text := msg.String()

	for _, needle := range []string{
		"failed to initialize ggml backend device: metal",
		"failed to initialize metal backend",
		"failed to initialize the metal library",
		"failed to allocate context",
		"unable to create llama context",
		"signal arrived during cgo execution",
		"input types must match cooperative tensor types",
	} {
		if strings.Contains(text, needle) {
			return true
		}
	}

	return false
}
