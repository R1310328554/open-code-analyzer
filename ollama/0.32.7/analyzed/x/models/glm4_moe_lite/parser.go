// GLM4-MoE-Lite 输出解析：流式提取思考内容与 XML 工具调用。
package glm4_moe_lite

import (
	"context"
	"encoding/json"
	"encoding/xml"
	"fmt"
	"log/slog"
	"strings"
	"unicode"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/logutil"
)

type parserState int

const (
	parserState_LookingForThinkingOpen parserState = iota
	parserState_ThinkingStartedEatingWhitespace
	parserState_CollectingThinking
	parserState_ThinkingDoneEatingWhitespace
	parserState_CollectingContent
	parserState_ToolStartedEatingWhitespace
	parserState_CollectingToolContent
)

const (
	thinkingOpenTag  = "<think>"
	thinkingCloseTag = "</think>"
	toolOpenTag      = "<tool_call>"
	toolCloseTag     = "</tool_call>"
)

// Parser 解析 GLM4 输出，提取 redacted_thinking 与 tool_call XML。
// Parser parses GLM4-MoE-Lite model output to extract thinking and tool calls.
// 启用思考时 prompt 以 <think> 结尾，模型直接输出思考正文。
// GLM-4's prompt ends with <think> when thinking is enabled, so the parser
// must start in CollectingThinking state (the model outputs thinking content directly).
type Parser struct {
	state  parserState
	buffer strings.Builder
	tools  []api.Tool
}

// HasToolSupport 返回 true（支持工具调用）。
// HasToolSupport returns true as GLM4 supports tool calling.
func (p *Parser) HasToolSupport() bool {
	return true
}

// HasThinkingSupport 返回 true（支持思考模式）。
// HasThinkingSupport returns true as GLM4 supports thinking mode.
func (p *Parser) HasThinkingSupport() bool {
	return true
}

// Init 根据 thinkValue 设置初始状态机。
// Init initializes the parser with tools and thinking configuration.
func (p *Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
	p.tools = tools
	// 思考开启时 prompt 已含 opening tag，直接进入 CollectingThinking。
	// When thinking is enabled (nil or true), the prompt ends with <think>,
	// so model output starts directly with thinking content (no opening tag).
	if thinkValue == nil || thinkValue.Bool() {
		p.state = parserState_CollectingThinking
	}
	return tools
}

type parserEvent interface {
	isParserEvent()
}

type eventContent struct {
	content string
}

func (eventContent) isParserEvent() {}

type eventRawToolCall struct {
	raw string
}

func (eventRawToolCall) isParserEvent() {}

type eventThinkingContent struct {
	content string
}

func (eventThinkingContent) isParserEvent() {}

// Add 增量解析输出，返回正文、思考与工具调用。
// Add processes new output text and returns parsed content, thinking, and tool calls.
func (p *Parser) Add(s string, done bool) (content string, thinking string, calls []api.ToolCall, err error) {
	p.buffer.WriteString(s)
	events := p.parseEvents()

	var toolCalls []api.ToolCall
	var contentSb strings.Builder
	var thinkingSb strings.Builder

	for _, event := range events {
		switch event := event.(type) {
		case eventRawToolCall:
			toolCall, err := parseToolCall(event, p.tools)
			if err != nil {
				slog.Warn("glm-4 tool call parsing failed", "error", err)
				return "", "", nil, err
			}
			toolCalls = append(toolCalls, toolCall)
		case eventThinkingContent:
			thinkingSb.WriteString(event.content)
		case eventContent:
			contentSb.WriteString(event.content)
		}
	}

	return contentSb.String(), thinkingSb.String(), toolCalls, nil
}

func (p *Parser) parseEvents() []parserEvent {
	var all []parserEvent

	keepLooping := true
	for keepLooping {
		var events []parserEvent
		events, keepLooping = p.eat()
		if len(events) > 0 {
			all = append(all, events...)
		}
	}

	if len(all) > 0 {
		slog.Log(context.TODO(), logutil.LevelTrace, "glm-4 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String())
	}

	return all
}

// eatLeadingWhitespaceAndTransitionTo 吃掉前导空白并切换状态。
// eatLeadingWhitespaceAndTransitionTo consumes leading whitespace from the buffer
// and transitions to the next state. Returns (nil, false) if only whitespace remains
// in the buffer (needs more input), or (nil, true) if we successfully transitioned.
func (p *Parser) eatLeadingWhitespaceAndTransitionTo(nextState parserState) ([]parserEvent, bool) {
	trimmed := strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace)
	p.buffer.Reset()
	if trimmed == "" {
		return nil, false // Still only whitespace, keep waiting for more input
	}
	p.state = nextState
	p.buffer.WriteString(trimmed)
	return nil, true // Successfully transitioned
}

// splitAtTag 在标签处切分 buffer，更新剩余内容。
// splitAtTag splits the buffer at the given tag, returns the content before (trimmed of trailing whitespace),
// the content after (optionally trimmed of leading whitespace), and updates the buffer
func (p *Parser) splitAtTag(tag string, trimAfter bool) (string, string) {
	split := strings.SplitN(p.buffer.String(), tag, 2)
	before := split[0]
	before = strings.TrimRightFunc(before, unicode.IsSpace)
	after := split[1]
	if trimAfter {
		after = strings.TrimLeftFunc(after, unicode.IsSpace)
	}
	p.buffer.Reset()
	p.buffer.WriteString(after)
	return before, after
}

func (p *Parser) eat() ([]parserEvent, bool) {
	var events []parserEvent

	switch p.state {
	case parserState_LookingForThinkingOpen:
		trimmed := strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace)
		if strings.HasPrefix(trimmed, thinkingOpenTag) {
			// 找到思考 opening tag。
			// Found <think> opening tag
			after := strings.TrimPrefix(trimmed, thinkingOpenTag)
			after = strings.TrimLeftFunc(after, unicode.IsSpace)
			p.buffer.Reset()
			p.buffer.WriteString(after)
			if after == "" {
				p.state = parserState_ThinkingStartedEatingWhitespace
			} else {
				p.state = parserState_CollectingThinking
			}
			return events, true
		} else if strings.HasPrefix(thinkingOpenTag, trimmed) {
			// 部分 opening tag，继续缓冲。
			// Partial opening tag seen, keep accumulating
			return events, false
		} else if trimmed == "" {
			// 仅空白，等待更多输入。
			// Only whitespace, keep accumulating
			return events, false
		} else {
			// 无思考 tag，转入正文收集。
			// No thinking tag found, skip to content collection
			p.state = parserState_CollectingContent
			// Don't trim - we want to keep the original content
			return events, true
		}

	case parserState_ThinkingStartedEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(parserState_CollectingThinking)

	case parserState_CollectingThinking:
		acc := p.buffer.String()
		if strings.Contains(acc, thinkingCloseTag) {
			thinking, remaining := p.splitAtTag(thinkingCloseTag, true)
			if len(thinking) > 0 {
				events = append(events, eventThinkingContent{content: thinking})
			}
			if remaining == "" {
				p.state = parserState_ThinkingDoneEatingWhitespace
			} else {
				p.state = parserState_CollectingContent
			}
			return events, true
		} else if overlapLen := overlap(acc, thinkingCloseTag); overlapLen > 0 {
			// 部分 closing tag： withhold 尾部空白与部分 tag。
			// Partial closing tag - withhold it along with any trailing whitespace before it
			beforePartialTag := acc[:len(acc)-overlapLen]
			trailingWsLen := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingWsLen

			unambiguous := acc[:ambiguousStart]
			ambiguous := acc[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, eventThinkingContent{content: unambiguous})
			}
			return events, false
		} else {
			// 纯思考内容： withhold 尾部空白以防 precede closing tag。
			// Pure thinking content - withhold trailing whitespace (might precede closing tag)
			whitespaceLen := trailingWhitespaceLen(acc)
			ambiguousStart := len(acc) - whitespaceLen

			unambiguous := acc[:ambiguousStart]
			ambiguous := acc[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, eventThinkingContent{content: unambiguous})
			}
			return events, false
		}

	case parserState_ThinkingDoneEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(parserState_CollectingContent)

	case parserState_CollectingContent:
		if strings.Contains(p.buffer.String(), toolOpenTag) {
			before, after := p.splitAtTag(toolOpenTag, true)
			if len(before) > 0 {
				events = append(events, eventContent{content: before})
			}
			if after == "" {
				p.state = parserState_ToolStartedEatingWhitespace
			} else {
				p.state = parserState_CollectingToolContent
			}
			return events, true
		} else if overlapLen := overlap(p.buffer.String(), toolOpenTag); overlapLen > 0 {
			beforePartialTag := p.buffer.String()[:len(p.buffer.String())-overlapLen]
			trailingWsLen := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingWsLen

			unambiguous := p.buffer.String()[:ambiguousStart]
			ambiguous := p.buffer.String()[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, eventContent{content: unambiguous})
			}
			return events, false
		} else {
			whitespaceLen := trailingWhitespaceLen(p.buffer.String())
			ambiguousStart := len(p.buffer.String()) - whitespaceLen

			unambiguous := p.buffer.String()[:ambiguousStart]
			ambiguous := p.buffer.String()[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, eventContent{content: unambiguous})
			}
			return events, false
		}

	case parserState_ToolStartedEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(parserState_CollectingToolContent)

	case parserState_CollectingToolContent:
		acc := p.buffer.String()
		if strings.Contains(acc, toolCloseTag) {
			toolContent, _ := p.splitAtTag(toolCloseTag, true)
			if len(toolContent) == 0 {
				slog.Warn("glm4 tool call closing tag found but no content before it")
			}
			events = append(events, eventRawToolCall{raw: toolContent})
			p.state = parserState_CollectingContent
			return events, true
		} else {
			// 工具调用不流式输出，等待 closing tag。
			// Keep accumulating - tool calls are not streamed
			// We just wait for the closing tag
			return events, false
		}

	default:
		panic("unreachable")
	}
}

// overlap 返回 s 后缀与 tag 前缀的最长重叠长度。
// overlap returns the length of the overlap between the end of s and the start of tag.
func overlap(s, tag string) int {
	for i := 1; i <= len(tag) && i <= len(s); i++ {
		if strings.HasSuffix(s, tag[:i]) {
			return i
		}
	}
	return 0
}

// trailingWhitespaceLen 返回 s 尾部空白长度。
// trailingWhitespaceLen returns the length of trailing whitespace in s.
func trailingWhitespaceLen(s string) int {
	trimmed := strings.TrimRightFunc(s, unicode.IsSpace)
	return len(s) - len(trimmed)
}

// ToolCallXML 为 GLM-4 工具调用 XML 结构。
// ToolCallXML represents the structure of a GLM-4 tool call for XML parsing
type ToolCallXML struct {
	XMLName xml.Name `xml:"tool_call"`
	Content string   `xml:",chardata"` // Function name (text nodes between tags)
	Keys    []string `xml:"arg_key"`   // All arg_key elements in document order
	Values  []string `xml:"arg_value"` // All arg_value elements in document order
}

// escapeContent 转义文本中的 XML 实体，保留 arg_key/arg_value 标签。
// escapeContent escapes XML entities in text content while preserving arg_key/arg_value tags
func escapeContent(s string) string {
	var result strings.Builder
	inTag := false

	for i := range len(s) {
		ch := s[i]

		if ch == '<' {
			// 识别已知 arg 标签边界。
			// Check if this is a known tag
			if strings.HasPrefix(s[i:], "<arg_key>") ||
				strings.HasPrefix(s[i:], "</arg_key>") ||
				strings.HasPrefix(s[i:], "<arg_value>") ||
				strings.HasPrefix(s[i:], "</arg_value>") {
				inTag = true
			}
		}

		if inTag {
			result.WriteByte(ch)
			if ch == '>' {
				inTag = false
			}
		} else {
			// 非标签区转义 & < >。
			// Escape special characters in text content
			switch ch {
			case '&':
				result.WriteString("&amp;")
			case '<':
				result.WriteString("&lt;")
			case '>':
				result.WriteString("&gt;")
			default:
				result.WriteByte(ch)
			}
		}
	}

	return result.String()
}

// repairUnclosedArgValues 补全缺失的 </arg_value> 闭合标签。
// repairUnclosedArgValues inserts missing </arg_value> closing tags.
// GLM models sometimes omit the closing tag, producing XML like:
//
//	<arg_value>value</tool_call>
//
// instead of:
//
//	<arg_value>value</arg_value></tool_call>
func repairUnclosedArgValues(s string) string {
	var result strings.Builder
	for {
		openIdx := strings.Index(s, "<arg_value>")
		if openIdx == -1 {
			result.WriteString(s)
			break
		}
		afterOpen := openIdx + len("<arg_value>")
		closeIdx := strings.Index(s[afterOpen:], "</arg_value>")
		nextKeyIdx := strings.Index(s[afterOpen:], "<arg_key>")
		if closeIdx != -1 && (nextKeyIdx == -1 || closeIdx < nextKeyIdx) {
			end := afterOpen + closeIdx + len("</arg_value>")
			result.WriteString(s[:end])
			s = s[end:]
			continue
		}
		if nextKeyIdx != -1 {
			insertAt := afterOpen + nextKeyIdx
			result.WriteString(s[:insertAt])
			result.WriteString("</arg_value>")
			s = s[insertAt:]
		} else {
			result.WriteString(s)
			result.WriteString("</arg_value>")
			break
		}
	}
	return result.String()
}

func parseToolCall(raw eventRawToolCall, tools []api.Tool) (api.ToolCall, error) {
	// 先转义文本实体。
	// Escape any unescaped entities in text content
	escaped := escapeContent(raw.raw)

	// 包装为合法 XML 根元素。
	// Wrap the content in a root element to make it valid XML
	xmlString := "<tool_call>" + escaped + "</tool_call>"

	// 解析 XML，失败时用 repair 后重试一次。
	// Parse XML into struct, retrying once with repaired XML if it fails
	var parsed ToolCallXML
	if err := xml.Unmarshal([]byte(xmlString), &parsed); err != nil {
		parsed = ToolCallXML{}
		repaired := "<tool_call>" + repairUnclosedArgValues(escaped) + "</tool_call>"
		if err2 := xml.Unmarshal([]byte(repaired), &parsed); err2 != nil {
			return api.ToolCall{}, fmt.Errorf("failed to parse XML: %w", err)
		}
	}

	// 提取并 trim 函数名。
	// Extract and trim function name
	functionName := strings.TrimSpace(parsed.Content)
	if functionName == "" {
		return api.ToolCall{}, fmt.Errorf("empty function name")
	}

	// 校验 arg_key 与 arg_value 数量配对。
	// Verify keys and values are paired correctly
	if len(parsed.Keys) != len(parsed.Values) {
		return api.ToolCall{}, fmt.Errorf("mismatched arg_key and arg_value counts: %d keys, %d values", len(parsed.Keys), len(parsed.Values))
	}

	// 在 tools 列表中匹配函数以获取参数类型。
	// Find the matching tool to get parameter types
	var matchedTool *api.Tool
	for i := range tools {
		if tools[i].Function.Name == functionName {
			matchedTool = &tools[i]
			break
		}
	}

	// 按 key/value 对构建参数字典。
	// Build arguments map by pairing keys and values
	toolCall := api.ToolCall{
		Function: api.ToolCallFunction{
			Name:      functionName,
			Arguments: api.NewToolCallFunctionArguments(),
		},
	}

	for i := range parsed.Keys {
		key := strings.TrimSpace(parsed.Keys[i])
		value := parsed.Values[i] // Don't trim here - parseValue handles it

		// Look up parameter type
		var paramType api.PropertyType
		if matchedTool != nil && matchedTool.Function.Parameters.Properties != nil {
			if prop, ok := matchedTool.Function.Parameters.Properties.Get(key); ok {
				// anyOf 时合并 union 中所有类型。
			// Handle anyOf by collecting all types from the union
				if len(prop.AnyOf) > 0 {
					for _, anyOfProp := range prop.AnyOf {
						paramType = append(paramType, anyOfProp.Type...)
					}
				} else {
					paramType = prop.Type
				}
			}
		}

		// 按 schema 类型 coercion 解析值。
		// Parse value with type coercion
		toolCall.Function.Arguments.Set(key, parseValue(value, paramType))
	}

	return toolCall, nil
}

// parseValue 将字符串按 paramType  coercion 为 bool/int/float/JSON 等。
// parseValue parses a string value and coerces it to the appropriate type based on paramType.
func parseValue(value string, paramType api.PropertyType) any {
	value = strings.TrimSpace(value)

	// 无类型信息时返回字符串。
	// If no type specified, return as string
	if len(paramType) == 0 {
		return value
	}

	// 按声明类型依次尝试解析。
	// Try to parse based on specified types
	for _, t := range paramType {
		switch t {
		case "boolean":
			if value == "true" {
				return true
			}
			if value == "false" {
				return false
			}
		case "integer":
			var i int64
			if _, err := fmt.Sscanf(value, "%d", &i); err == nil {
				return i
			}
		case "number":
			var f float64
			if _, err := fmt.Sscanf(value, "%f", &f); err == nil {
				return f
			}
		case "array", "object":
			// array/object 尝试 JSON 解析。
			// Try to parse as JSON
			var result any
			if err := json.Unmarshal([]byte(value), &result); err == nil {
				return result
			}
		}
	}

	// 默认回退为字符串。
	// Default to string
	return value
}
