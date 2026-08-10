// Package errtypes 定义 Ollama 自定义错误类型。
// Package errtypes contains custom error types// Package errtypes contains custom error types
package errtypes

import (
	"fmt"
	"strings"
)

// 错误消息常量。
const (
	UnknownOllamaKeyErrMsg = "unknown ollama key"
	InvalidModelNameErrMsg = "invalid model name"
)

// TODO：API 应返回结构化错误响应。
// TODO: This should have a structured response from the API
// UnknownOllamaKey 表示无效的 Ollama API 密钥。
type UnknownOllamaKey struct {
	Key string
}

// Error 返回 unauthorized 格式的错误字符串。
func (e *UnknownOllamaKey) Error() string {
	return fmt.Sprintf("unauthorized: %s %q", UnknownOllamaKeyErrMsg, strings.TrimSpace(e.Key))
}
