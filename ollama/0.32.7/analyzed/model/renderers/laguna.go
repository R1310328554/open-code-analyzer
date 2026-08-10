// Laguna 渲染器：Poolside v2/v8 模板、thinking 与 XML 工具调用。
package renderers

import (
	"strings"
	"unicode"

	"github.com/ollama/ollama/api"
)

// Laguna 模板常量：BOS、thinking 标签与默认 system 文案。
const (
	lagunaBOS          = "〈|EOS|〉"
	lagunaThoughtOpen  = "<think>"
	lagunaThoughtClose = "</think>"

	// 无 system 消息时使用的 Laguna 默认 system 文案。
	// Default system message from the Laguna chat template, used when the
	// request supplies no system message.
	lagunaDefaultSystem = "You are a helpful, conversationally-fluent assistant made by Poolside. You are here to be helpful to users through natural language conversations."
)

// LagunaRenderer 渲染 Laguna v2 聊天模板。
type LagunaRenderer struct{}

// LeadingBOS 返回 Laguna BOS token。
func (r *LagunaRenderer) LeadingBOS() string {
	return lagunaBOS
}

// Render 组装 system/tools、user/assistant/tool 并 priming reasoning 模式。
func (r *LagunaRenderer) Render(messages []api.Message, tools []api.Tool, think *api.ThinkValue) (string, error) {
	var sb strings.Builder
	sb.WriteString(lagunaBOS)

	// 模板通过生成提示 token 区分 thinking 开/关，默认关闭。
	// The template signals thinking through the generation-prompt token
	// (<think> vs </think>), not through the system message. It defaults off.
	thinkingEnabled := think != nil && think.Bool()

	// ── 头部（system 消息与工具说明）──
	// ── header (system message) ──
	// The template seeds a default system message and lets an explicit leading
	// system message override it. The header is emitted whenever there is a
	// system message or tools to advertise.
	systemMessage := lagunaDefaultSystem
	firstMessageIsSystem := len(messages) > 0 && messages[0].Role == "system"
	if firstMessageIsSystem {
		systemMessage = messages[0].Content
	}

	if strings.TrimSpace(systemMessage) != "" || len(tools) > 0 {
		sb.WriteString("<system>\n")
		if strings.TrimSpace(systemMessage) != "" {
			sb.WriteByte('\n')
			sb.WriteString(strings.TrimRightFunc(systemMessage, unicode.IsSpace))
		}
		if len(tools) > 0 {
			sb.WriteString("\n\n### Tools\n\n")
			sb.WriteString("You may call functions to assist with the user query.\n")
			sb.WriteString("All available function signatures are listed below:\n")
			sb.WriteString("<available_tools>\n")
			for _, tool := range tools {
				if b, err := marshalWithSpaces(tool); err == nil {
					sb.Write(b)
					sb.WriteByte('\n')
				}
			}
			sb.WriteString("</available_tools>\n\n")
			if thinkingEnabled {
				sb.WriteString("Wrap your thinking in '<think>', '</think>' tags, followed by a function call. For each function call, return an unescaped XML-like object with function name and arguments within '<tool_call>' and '</tool_call>' tags, like here:\n")
				sb.WriteString("<think> your thoughts here </think>\n")
			} else {
				sb.WriteString("For each function call, return an unescaped XML-like object with function name and arguments within '<tool_call>' and '</tool_call>' tags, like here:\n")
			}
			sb.WriteString("<tool_call>function-name\n<arg_key>argument-key</arg_key>\n<arg_value>value-of-argument-key</arg_value>\n</tool_call>")
		}
		sb.WriteString("\n</system>\n")
	}

	// ── 主循环：遍历 user/assistant/tool/system ──
	// ── main loop ──
	for i, message := range messages {
		if i == 0 && firstMessageIsSystem {
			continue
		}
		content := message.Content
		switch message.Role {
		case "user":
			sb.WriteString("<user>\n")
			sb.WriteString(content)
			sb.WriteString("\n</user>\n")
		case "assistant":
			content, reasoning := lagunaV2AssistantContent(message.Content, message.Thinking)
			lastMessage := i == len(messages)-1
			prefill := lastMessage && (strings.TrimSpace(content) != "" || strings.TrimSpace(reasoning) != "" || len(message.ToolCalls) > 0)

			sb.WriteString("<assistant>\n")

			// 每条 assistant 回合以 reasoning 块开头：有内容则完整标签，否则 bare close。
			// Every assistant turn opens with the reasoning block: a full
			// <think>…</think> when there is reasoning, otherwise a bare
			// </think> marking the turn as direct.
			if reasoning := strings.TrimSpace(reasoning); reasoning != "" {
				sb.WriteString("<think>\n")
				sb.WriteString(reasoning)
				sb.WriteString("\n</think>\n")
			} else {
				sb.WriteString("</think>\n")
			}

			if strings.TrimSpace(content) != "" {
				sb.WriteString(strings.TrimSpace(content))
				sb.WriteByte('\n')
			}

			for _, toolCall := range message.ToolCalls {
				sb.WriteString("<tool_call>")
				sb.WriteString(toolCall.Function.Name)
				sb.WriteByte('\n')
				for name, value := range toolCall.Function.Arguments.All() {
					sb.WriteString("<arg_key>")
					sb.WriteString(name)
					sb.WriteString("</arg_key>\n")
					sb.WriteString("<arg_value>")
					sb.WriteString(formatLagunaToolCallArgument(value))
					sb.WriteString("</arg_value>\n")
				}
				sb.WriteString("</tool_call>\n")
			}

			if !prefill {
				sb.WriteString("</assistant>\n")
			}
		case "tool":
			sb.WriteString("<tool_response>\n")
			sb.WriteString(content)
			sb.WriteString("\n</tool_response>\n")
		case "system":
			sb.WriteString("<system>\n")
			sb.WriteString(content)
			sb.WriteString("\n</system>\n")
		}
	}

	// ── 生成提示：续写 assistant 预填充或新开 assistant 并 priming thinking ──
	// ── generation prompt ──
	// Continue an assistant prefill in place; otherwise open a fresh assistant
	// turn and prime the reasoning mode (<think> when thinking, else </think>).
	if len(messages) == 0 || messages[len(messages)-1].Role != "assistant" {
		sb.WriteString("<assistant>\n")
		if thinkingEnabled {
			sb.WriteString(lagunaThoughtOpen)
		} else {
			sb.WriteString(lagunaThoughtClose)
		}
	}

	return sb.String(), nil
}

// lagunaV2AssistantContent 从 content 中拆分 reasoning 与正文。
func lagunaV2AssistantContent(content, reasoning string) (string, string) {
	parts := strings.Split(content, lagunaThoughtClose)
	if len(parts) == 1 {
		return content, reasoning
	}

	if reasoning == "" {
		before := strings.TrimRight(parts[0], "\n")
		if i := strings.LastIndex(before, lagunaThoughtOpen); i >= 0 {
			before = before[i+len(lagunaThoughtOpen):]
		}
		reasoning = strings.TrimLeft(before, "\n")
	}

	content = strings.TrimLeft(parts[len(parts)-1], "\n")
	return content, reasoning
}

// LagunaV8Renderer 渲染 Poolside v8（poolside-v1）模板。
type LagunaV8Renderer struct{}

// LeadingBOS 返回 Laguna BOS。
func (r *LagunaV8Renderer) LeadingBOS() string {
	return lagunaBOS
}

// Render v8 变体：assistant 回合不延续最后一条历史。
func (r *LagunaV8Renderer) Render(messages []api.Message, tools []api.Tool, think *api.ThinkValue) (string, error) {
	var sb strings.Builder
	sb.WriteString(lagunaBOS)

	thinkingEnabled := think != nil && think.Bool()

	systemMessage := lagunaDefaultSystem
	firstMessageIsSystem := len(messages) > 0 && messages[0].Role == "system"
	if firstMessageIsSystem {
		systemMessage = messages[0].Content
	}

	hasSystem := strings.TrimSpace(systemMessage) != ""
	if hasSystem || len(tools) > 0 || thinkingEnabled {
		sb.WriteString("<system>")
		if hasSystem {
			sb.WriteString(strings.TrimRightFunc(systemMessage, unicode.IsSpace))
			if len(tools) > 0 {
				sb.WriteString("\n\n")
			}
		}
		if len(tools) > 0 {
			sb.WriteString("### Tools\n\n")
			sb.WriteString("You may call functions to assist with the user query.\n")
			sb.WriteString("All available function signatures are listed below:\n")
			sb.WriteString("<available_tools>\n")
			for _, tool := range tools {
				if b, err := marshalWithSpaces(tool); err == nil {
					sb.Write(b)
					sb.WriteByte('\n')
				}
			}
			sb.WriteString("</available_tools>")
		}
		sb.WriteString("</system>\n")
	}

	for i, message := range messages {
		if i == 0 && firstMessageIsSystem {
			continue
		}
		content := message.Content
		switch message.Role {
		case "user":
			sb.WriteString("<user>")
			sb.WriteString(content)
			sb.WriteString("</user>\n")
		case "assistant":
			sb.WriteString("<assistant>")
			if thinkingEnabled {
				sb.WriteString(lagunaThoughtOpen)
				sb.WriteString(message.Thinking)
				sb.WriteString(lagunaThoughtClose)
			} else {
				sb.WriteString(lagunaThoughtClose)
			}
			if content != "" {
				sb.WriteString(content)
			}
			for _, toolCall := range message.ToolCalls {
				sb.WriteString("<tool_call>")
				sb.WriteString(toolCall.Function.Name)
				for name, value := range toolCall.Function.Arguments.All() {
					sb.WriteString("<arg_key>")
					sb.WriteString(name)
					sb.WriteString("</arg_key>")
					sb.WriteString("<arg_value>")
					sb.WriteString(formatLagunaToolCallArgument(value))
					sb.WriteString("</arg_value>")
				}
				sb.WriteString("</tool_call>")
			}
			sb.WriteString("</assistant>\n")
		case "tool":
			sb.WriteString("<tool_response>")
			sb.WriteString(content)
			sb.WriteString("</tool_response>\n")
		case "system":
			sb.WriteString("<system>")
			sb.WriteString(content)
			sb.WriteString("</system>\n")
		}
	}

	sb.WriteString("<assistant>")
	if thinkingEnabled {
		sb.WriteString(lagunaThoughtOpen)
	} else {
		sb.WriteString(lagunaThoughtClose)
	}

	return sb.String(), nil
}

// formatLagunaToolCallArgument 格式化工具参数值（字符串或带空格 JSON）。
func formatLagunaToolCallArgument(value any) string {
	switch v := value.(type) {
	case string:
		return v
	case []byte:
		return string(v)
	}

	if b, err := marshalWithSpaces(value); err == nil {
		return string(b)
	}

	return formatToolCallArgument(value)
}
