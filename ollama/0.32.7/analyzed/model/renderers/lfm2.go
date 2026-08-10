// LFM2 渲染器：Liquid 风格 im 块、thinking 与 Python 风格工具调用。
package renderers

import (
	"bytes"
	"encoding/json"
	"sort"
	"strings"

	"github.com/ollama/ollama/api"
)

// LFM2Renderer 渲染 LFM2 多模态/工具聊天模板。
type LFM2Renderer struct {
	IsThinking bool   // 是否为 thinking 变体
	useImgTags bool   // 为 true 时用 [img] 表示图像
}

// lfm2BOSToken LFM2 序列起始 token。
const lfm2BOSToken = "<|startoftext|>"

// LFM2 模板特殊标签：thinking、工具列表与工具调用/响应。
const (
	lfm2ThinkingOpenTag      = "<think>"
	lfm2ThinkingCloseTag     = "</think>"
	lfm2ToolListStartTag     = "<|tool_list_start|>"
	lfm2ToolListEndTag       = "<|tool_list_end|>"
	lfm2ToolCallStartTag     = "<|tool_call_start|>"
	lfm2ToolCallEndTag       = "<|tool_call_end|>"
	lfm2ToolResponseStartTag = "<|tool_response_start|>"
	lfm2ToolResponseEndTag   = "<|tool_response_end|>"
)

// LeadingBOS 返回 <|startoftext|>。
func (r *LFM2Renderer) LeadingBOS() string {
	return lfm2BOSToken
}

// lfm2RenderSystemContent 从多段 content 中提取 system 文本。
func lfm2RenderSystemContent(content any) string {
	switch v := content.(type) {
	case string:
		return v
	case []any:
		var sb strings.Builder
		for _, item := range v {
			obj, ok := item.(map[string]any)
			if !ok {
				continue
			}

			if itemType, _ := obj["type"].(string); itemType == "text" {
				if text, ok := obj["text"].(string); ok {
					sb.WriteString(text)
				}
			}
		}
		return sb.String()
	default:
		return ""
	}
}

// lfm2JSON 编码 JSON 并在逗号/冒号后加空格（对齐 HF tojson）。
func lfm2JSON(v any) string {
	var buf bytes.Buffer
	enc := json.NewEncoder(&buf)
	enc.SetEscapeHTML(false)
	if err := enc.Encode(v); err != nil {
		fallback, _ := json.Marshal(v)
		return string(fallback)
	}

	encoded := bytes.TrimSuffix(buf.Bytes(), []byte{'\n'})

	// HF tojson 默认在逗号/冒号后插入空格。
	// HF `tojson` defaults to `json.dumps(..., separators=None)`, which inserts
	// a space after commas and colons.
	var out strings.Builder
	out.Grow(len(encoded) + len(encoded)/8)

	inString := false
	escaped := false
	for i, b := range encoded {
		out.WriteByte(b)

		if inString {
			if escaped {
				escaped = false
				continue
			}
			if b == '\\' {
				escaped = true
				continue
			}
			if b == '"' {
				inString = false
			}
			continue
		}

		if b == '"' {
			inString = true
			continue
		}

		if (b == ':' || b == ',') && i+1 < len(encoded) {
			next := encoded[i+1]
			if next != ' ' && next != '\n' && next != '\r' && next != '\t' {
				out.WriteByte(' ')
			}
		}
	}

	return out.String()
}

// lfm2ImagePlaceholder 按配置返回 [img] 或 <image> 占位符。
func lfm2ImagePlaceholder(useImgTags bool) string {
	if useImgTags {
		return "[img]"
	}

	return "<image>"
}

// lfm2RenderContent 将多模态 content 转为文本与图像占位符。
func lfm2RenderContent(content any, useImgTags bool) string {
	switch v := content.(type) {
	case string:
		return v
	case []any:
		var sb strings.Builder
		for _, item := range v {
			obj, ok := item.(map[string]any)
			if !ok {
				sb.WriteString(lfm2JSON(item))
				continue
			}

			itemType, _ := obj["type"].(string)
			switch itemType {
			case "image":
				sb.WriteString(lfm2ImagePlaceholder(useImgTags))
			case "text":
				if text, ok := obj["text"].(string); ok {
					sb.WriteString(text)
				} else {
					sb.WriteString(lfm2JSON(item))
				}
			default:
				sb.WriteString(lfm2JSON(item))
			}
		}
		return sb.String()
	default:
		return lfm2JSON(content)
	}
}

// lfm2ToolSchema 提取 function schema 供 LFM2 工具列表使用。
func lfm2ToolSchema(tool api.Tool) any {
	if tool.Function.Name == "" {
		return tool
	}

	// LFM2 模板通常只喂 function 对象（name/description/parameters）。
	// LFM2 templates are typically fed function-schema objects (name/description/parameters).
	return tool.Function
}

// lfm2ToolCallArgument 将单参数值编码为 LFM2 JSON 字符串。
func lfm2ToolCallArgument(v any) string {
	return lfm2JSON(v)
}

// lfm2RenderToolCalls 渲染 Python 风格 func(k=v) 工具调用块。
func lfm2RenderToolCalls(calls []api.ToolCall) string {
	var sb strings.Builder

	sb.WriteString(lfm2ToolCallStartTag)
	sb.WriteString("[")
	for i, tc := range calls {
		if i > 0 {
			sb.WriteString(",")
		}

		sb.WriteString(tc.Function.Name)
		sb.WriteString("(")

		keys := make([]string, 0, tc.Function.Arguments.Len())
		for key := range tc.Function.Arguments.All() {
			keys = append(keys, key)
		}
		sort.Strings(keys)

		for j, key := range keys {
			if j > 0 {
				sb.WriteString(",")
			}
			value, _ := tc.Function.Arguments.Get(key)
			sb.WriteString(key)
			sb.WriteString("=")
			sb.WriteString(lfm2ToolCallArgument(value))
		}

		sb.WriteString(")")
	}
	sb.WriteString("]")
	sb.WriteString(lfm2ToolCallEndTag)

	return sb.String()
}

// renderMessageContent 渲染单条消息正文并处理图像占位。
func (r *LFM2Renderer) renderMessageContent(message api.Message, imageOffset int) string {
	content := lfm2RenderContent(message.Content, r.useImgTags)
	if len(message.Images) == 0 {
		return content
	}

	if r.useImgTags {
		content, _ = renderContentWithImageTags(content, len(message.Images), imageOffset)
		return content
	}

	var sb strings.Builder
	placeholder := lfm2ImagePlaceholder(false)
	if strings.Contains(content, placeholder) {
		return content
	}
	for range message.Images {
		sb.WriteString(placeholder)
	}
	sb.WriteString(content)
	return sb.String()
}

// Render 组装 system+tools、im 块消息流，并按需保留历史 thinking。
func (r *LFM2Renderer) Render(messages []api.Message, tools []api.Tool, thinkValue *api.ThinkValue) (string, error) {
	var sb strings.Builder

	// 遵循 Liquid 工具格式包装 LFM2 工具块。
	// Follow Liquid tool-use formatting for LFM2 tool wrappers.
	sb.WriteString(lfm2BOSToken)

	// 提取首条 system 并与工具列表合并。
	// Extract first system message if present (to combine with tools)
	var firstSystemContent string
	startIdx := 0
	if len(messages) > 0 && messages[0].Role == "system" {
		firstSystemContent = lfm2RenderSystemContent(messages[0].Content)
		startIdx = 1
	}

	// Append tools to first system content
	if len(tools) > 0 {
		if firstSystemContent != "" {
			firstSystemContent += "\n"
		}
		firstSystemContent += "List of tools: "
		firstSystemContent += lfm2ToolListStartTag
		firstSystemContent += "["
		for i, tool := range tools {
			firstSystemContent += lfm2JSON(lfm2ToolSchema(tool))
			if i < len(tools)-1 {
				firstSystemContent += ", "
			}
		}
		firstSystemContent += "]"
		firstSystemContent += lfm2ToolListEndTag
	}

	// Output first system block if it has content
	if firstSystemContent != "" {
		sb.WriteString("<|im_start|>system\n")
		sb.WriteString(firstSystemContent)
		sb.WriteString("<|im_end|>\n")
	}

	keepPastThinking := r.IsThinking && (thinkValue != nil && thinkValue.Bool())

	// 定位最后一条 assistant 以决定是否剥离历史 thinking。
	// Find the index of the last assistant message for thinking stripping
	lastAssistantIndex := -1
	for i := len(messages) - 1; i >= startIdx; i-- {
		if messages[i].Role == "assistant" {
			lastAssistantIndex = i
			break
		}
	}

	imageOffset := 0
	for i := range startIdx {
		imageOffset += len(messages[i].Images)
	}

	for i := startIdx; i < len(messages); i++ {
		message := messages[i]
		lastMessage := i == len(messages)-1
		prefill := lastMessage && message.Role == "assistant"

		sb.WriteString("<|im_start|>")
		sb.WriteString(message.Role)
		sb.WriteString("\n")

		content := r.renderMessageContent(message, imageOffset)
		imageOffset += len(message.Images)
		if message.Role == "assistant" && len(message.ToolCalls) > 0 && !strings.Contains(content, lfm2ToolCallStartTag) {
			if strings.TrimSpace(content) == "" {
				content = lfm2RenderToolCalls(message.ToolCalls) + content
			} else {
				content = lfm2RenderToolCalls(message.ToolCalls) + "\n" + content
			}
		}
		// 从 Thinking 字段重建内联 redacted_thinking 块，保证往返格式一致。
		// Reconstruct the inline <think>...</think> block from the separate
		// Thinking field so reasoning turns round-trip in the model's own format:
		// thinking precedes any tool calls and content. A direct answer carries no
		// Thinking, so nothing is added. Only the thinking variant emits these tags;
		// the non-thinking renderer must never send them, including for a trailing
		// assistant prefill (which is exempt from the stripping below).
		if r.IsThinking && message.Role == "assistant" && message.Thinking != "" && !strings.Contains(content, lfm2ThinkingCloseTag) {
			content = lfm2ThinkingOpenTag + message.Thinking + lfm2ThinkingCloseTag + content
		}
		// 除非保留 past thinking，否则剥离较早 assistant 的推理前缀。
		// Drop reasoning from earlier assistant turns unless thinking is kept; the
		// <think>...</think> block is a clean prefix, so everything after the close
		// tag (tool calls and content) is preserved.
		if message.Role == "assistant" && !keepPastThinking && i != lastAssistantIndex {
			if idx := strings.LastIndex(content, lfm2ThinkingCloseTag); idx >= 0 {
				content = strings.TrimSpace(content[idx+len(lfm2ThinkingCloseTag):])
			}
		}
		if message.Role == "tool" && !strings.Contains(content, lfm2ToolResponseStartTag) {
			content = lfm2ToolResponseStartTag + content + lfm2ToolResponseEndTag
		}

		sb.WriteString(content)
		if !prefill {
			sb.WriteString("<|im_end|>\n")
		}
	}

	needsGenerationPrompt := true
	if len(messages) > 0 && messages[len(messages)-1].Role == "assistant" {
		needsGenerationPrompt = false
	}

	if needsGenerationPrompt {
		// 非 assistant 预填充时需追加 assistant 生成提示。
		// RenderWithRenderer uses add_generation_prompt=true for chat rendering,
		// unless we're prefilling a trailing assistant message.
		sb.WriteString("<|im_start|>assistant\n")
	}

	return sb.String(), nil
}
