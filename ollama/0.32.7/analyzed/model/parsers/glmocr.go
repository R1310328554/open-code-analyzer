package parsers

import "github.com/ollama/ollama/api"

// GLM-OCR 解析器：GLM46 变体，禁用思考支持。
// GlmOcrParser 为禁用思考的 GLM46 解析器。
// GlmOcrParser is the GLM46 parser with thinking disabled.
// GlmOcrParser 嵌入 GLM46Parser。
type GlmOcrParser struct {
	GLM46Parser
}

// HasThinkingSupport 恒为 false（OCR 模型无思考）。
func (p *GlmOcrParser) HasThinkingSupport() bool {
	return false
}

// Init 仅保存工具列表。
func (p *GlmOcrParser) Init(tools []api.Tool, _ *api.Message, _ *api.ThinkValue) []api.Tool {
	p.tools = tools
	return tools
}
