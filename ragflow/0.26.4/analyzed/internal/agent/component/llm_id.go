// llm_id.go — 画布 llm_id 复合命名约定解析（model@provider / model@instance@provider）。

package component

import "strings"

// splitCompositeLLMID extracts the provider driver and bare model id from the
// canvas llm_id convention:
//   - "model@provider"          -> ("model", "provider", true)
//   - "model@instance@provider" -> ("model", "provider", true)
//   - bare "model"              -> ("model", "", false)
// splitCompositeLLMID 从 llm_id 提取裸模型名与 provider driver。
func splitCompositeLLMID(s string) (modelName, driver string, hasDriver bool) {
	parts := strings.Split(strings.TrimSpace(s), "@")
	switch len(parts) {
	case 2:
		return parts[0], parts[1], true
	case 3:
		return parts[0], parts[2], true
	default:
		return s, "", false
	}
}
