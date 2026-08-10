// Qwen3-VL 渲染器：视觉 token、thinking 与 JSON tool_call。
package renderers

import (
	"strings"

	"github.com/ollama/ollama/api"
)

// Qwen3VLRenderer 渲染 Qwen3-VL instruct/thinking 模板。
type Qwen3VLRenderer struct {
	isThinking bool // 默认 thinking 模式

	emitEmptyThinkOnNoThink bool // 禁 thinking 时输出空 thinking 块
	useImgTags              bool // 为 true 时用 [img] 占位图像
}

// LeadingBOS Qwen3-VL 无额外 BOS。
func (r *Qwen3VLRenderer) LeadingBOS() string {
	return ""
}

// renderContent 渲染 vision 块或 [img] 占位符与文本。
func (r *Qwen3VLRenderer) renderContent(content api.Message, imageOffset int) (string, int) {
	if r.useImgTags {
		return renderContentWithImageTags(content.Content, len(content.Images), imageOffset)
	}

	// This assumes all images are at the front of the message - same assumption as ollama/ollama/runner.go
	var subSb strings.Builder
	for range content.Images {
		// TODO: 不同后端 vision token 渲染方式可能不同，后续应参数化。
		// TODO: (jmorganca): how to render this is different for different
		// model backends, and so we should eventually parameterize this or
		// only output a placeholder such as [img]
		subSb.WriteString("<|vision_start|><|image_pad|><|vision_end|>")
	}
	// TODO: support videos

	subSb.WriteString(content.Content)
	return subSb.String(), imageOffset
}

// Render 组装 system/tools、多步工具上下文与 JSON tool_call。
func (r *Qwen3VLRenderer) Render(messages []api.Message, tools []api.Tool, think *api.ThinkValue) (string, error) {
	var sb strings.Builder

	isThinking := r.isThinking
	if think != nil {
		isThinking = think.Bool()
	}

	if len(tools) > 0 {
		sb.WriteString(imStartTag + "system\n")
		if len(messages) > 0 && messages[0].Role == "system" {
			sb.WriteString(messages[0].Content + "\n\n")
		}
		sb.WriteString("# Tools\n\nYou may call one or more functions to assist with the user query.\n\nYou are provided with function signatures within <tools></tools> XML tags:\n<tools>")
		for _, tool := range tools {
			sb.WriteString("\n")
			if b, err := marshalWithSpaces(tool); err == nil {
				sb.Write(b)
			}
		}
		sb.WriteString("\n</tools>\n\nFor each function call, return a json object with function name and arguments within <tool_call></tool_call> XML tags:\n<tool_call>\n{\"name\": <function-name>, \"arguments\": <args-json-object>}\n</tool_call><|im_end|>\n")
	} else if len(messages) > 0 && messages[0].Role == "system" {
		sb.WriteString("<|im_start|>system\n" + messages[0].Content + "<|im_end|>\n")
	}
	multiStepTool := true
	lastQueryIndex := len(messages) - 1 // so this is the last user message

	for i := len(messages) - 1; i >= 0; i-- {
		message := messages[i]
		if multiStepTool && message.Role == "user" {
			// 检测 user 消息是否为 tool_response 包装以定位 lastQueryIndex。
			// Check if content starts with <tool_response> and ends with </tool_response>
			content, _ := r.renderContent(message, 0)
			if !(strings.HasPrefix(content, "<tool_response>") && strings.HasSuffix(content, "</tool_response>")) {
				multiStepTool = false
				lastQueryIndex = i
			}
		}
	}

	imageOffset := 0
	for i, message := range messages {
		content, nextImageOffset := r.renderContent(message, imageOffset)
		imageOffset = nextImageOffset

		lastMessage := i == len(messages)-1
		prefill := lastMessage && message.Role == "assistant"

		if message.Role == "user" || message.Role == "system" && i != 0 {
			sb.WriteString("<|im_start|>" + message.Role + "\n" + content + "<|im_end|>\n")
		} else if message.Role == "assistant" {
			contentReasoning := ""

			if isThinking {
				if message.Thinking != "" {
					contentReasoning = message.Thinking
				}
			}

			if isThinking && i > lastQueryIndex {
				if i == len(messages)-1 || contentReasoning != "" {
					sb.WriteString("<|im_start|>" + message.Role + "\n<think>\n" + strings.Trim(contentReasoning, "\n")) // 是否在末尾换行待确认
					if content != "" {
						sb.WriteString("\n</think>\n\n" + strings.TrimLeft(content, "\n"))
					}
				} else {
					sb.WriteString("<|im_start|>" + message.Role + "\n" + content)
				}
			} else {
				sb.WriteString("<|im_start|>" + message.Role + "\n" + content)
			}

			if len(message.ToolCalls) > 0 {
				for j, toolCall := range message.ToolCalls {
					if j > 0 || content != "" {
						sb.WriteString("\n")
					}

					sb.WriteString("<tool_call>\n{\"name\": \"" + toolCall.Function.Name + "\", \"arguments\": ")
					if b, err := marshalWithSpaces(toolCall.Function.Arguments); err == nil {
						sb.Write(b)
					}
					sb.WriteString("}\n</tool_call>")
				}
			}

			if !prefill {
				sb.WriteString("<|im_end|>\n")
			}
		} else if message.Role == "tool" {
			if i == 0 || messages[i-1].Role != "tool" {
				sb.WriteString("<|im_start|>user")
			}
			sb.WriteString("\n<tool_response>\n" + content + "\n</tool_response>")
			if i == len(messages)-1 || messages[i+1].Role != "tool" {
				sb.WriteString("<|im_end|>\n")
			}
		}

		// 末尾追加 assistant 生成提示。
		// prefill at the end
		if lastMessage && !prefill {
			sb.WriteString("<|im_start|>assistant\n")
			if isThinking {
				sb.WriteString("<think>\n")
			} else if r.emitEmptyThinkOnNoThink {
				sb.WriteString("<think>\n\n</think>\n\n")
			}
		}
	}

	return sb.String(), nil
}
