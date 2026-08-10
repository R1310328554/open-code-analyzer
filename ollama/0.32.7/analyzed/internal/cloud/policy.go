// 云功能策略：读取环境/配置判断 Ollama Cloud 是否禁用。
package cloud

import (
	"github.com/ollama/ollama/envconfig"
)

// DisabledMessagePrefix 为云功能禁用时的错误消息前缀。
const DisabledMessagePrefix = "ollama cloud is disabled"

// Status 返回云功能是否禁用及决策来源（none/env/config/both）。
// Status returns whether cloud is disabled and the source of the decision.
// Source is one of: "none", "env", "config", "both".
func Status() (disabled bool, source string) {
	return envconfig.NoCloud(), envconfig.NoCloudSource()
}

// Disabled 仅返回云功能是否被禁用。
func Disabled() bool {
	return envconfig.NoCloud()
}

// DisabledError 构造带可选操作名的禁用错误消息。
func DisabledError(operation string) string {
	if operation == "" {
		return DisabledMessagePrefix
	}

	return DisabledMessagePrefix + ": " + operation
}
