package parsers

import "github.com/ollama/ollama/api"

// GLM-4.7 解析器：继承 GLM46，提示以 <think> 开头时直接进入思考收集。
// GLM47Parser 扩展 GLM46Parser，thinking 启用时从 CollectingThinking 起步。
// GLM47Parser extends GLM46Parser with thinking-aware initialization.
// GLM-4.7's prompt ends with <think> when thinking is enabled, so the parser
// must start in CollectingThinking state (the model outputs thinking content directly).
// GLM47Parser 嵌入 GLM46Parser 并覆盖 Init。
type GLM47Parser struct {
	GLM46Parser
}

// Init thinking 启用时将初始状态设为 CollectingThinking。
func (p *GLM47Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
	p.tools = tools
	p.callIndex = 0
	// When thinking is enabled (nil or true), the prompt ends with <think>,
	// so model output starts directly with thinking content (no opening tag).
	if thinkValue == nil || thinkValue.Bool() {
		p.state = glm46ParserState_CollectingThinking
	}
	return tools
}
