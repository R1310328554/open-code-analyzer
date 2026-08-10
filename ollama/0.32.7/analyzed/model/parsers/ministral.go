// Ministral 解析器：[TOOL_CALLS]/[THINK]/[ARGS] 标签流式解析。
package parsers

import (
	"encoding/json"
	"fmt"
	"strings"
	"unicode"

	"github.com/ollama/ollama/api"
)

// ministralParserState 表示 Ministral 流式解析阶段。
type ministralParserState int

const (
	ministralCollectingContent = iota
	ministralCollectingThinkingContent
	ministralCollectingToolName
	ministralCollectingToolArgs
)

// ministralEvent 标记 Ministral 解析过程中产生的事件类型。
// ministralEvent represents an event emitted during parsing
type ministralEvent interface {
	isMinistralEvent()
}

// ministralEventContent 携带正文片段。
type ministralEventContent struct {
	content string
}

// ministralEventThinking 携带思考片段。
type ministralEventThinking struct {
	thinking string
}

// ministralEventToolCall 携带工具名与原始 JSON 参数。
type ministralEventToolCall struct {
	name string
	args string // raw JSON string
}

func (ministralEventContent) isMinistralEvent()  {}
func (ministralEventThinking) isMinistralEvent() {}
func (ministralEventToolCall) isMinistralEvent() {}

// MinistralParser 解析 Ministral 模型的 content/thinking/tool 输出。
type MinistralParser struct {
	state              ministralParserState
	buffer             strings.Builder
	tools              []api.Tool
	callIndex          int
	hasThinkingSupport bool
	pendingToolName    string // stores tool name while collecting args
}

// HasToolSupport 返回 true（Ministral 支持工具）。
func (p *MinistralParser) HasToolSupport() bool {
	return true
}

// HasThinkingSupport 返回是否启用思考模式。
func (p *MinistralParser) HasThinkingSupport() bool {
	return p.hasThinkingSupport
}

// PreservedTokens 返回解析需保留的特殊 token。
func (p *MinistralParser) PreservedTokens() []string {
	return []string{
		ministralToolCallsTag,
		ministralThinkTag,
		ministralThinkEndTag,
		ministralArgsTag,
	}
}

// setInitialState 根据预填充与思考能力决定初始收集状态。
func (p *MinistralParser) setInitialState(lastMessage *api.Message) {
	prefill := lastMessage != nil && lastMessage.Role == "assistant"
	if !p.HasThinkingSupport() {
		p.state = ministralCollectingContent
		return
	}

	if prefill && lastMessage.Content != "" {
		p.state = ministralCollectingContent
		return
	}

	p.state = ministralCollectingThinkingContent
}

// Init 初始化解析器并返回工具列表。
func (p *MinistralParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
	p.tools = tools
	p.callIndex = 0
	p.setInitialState(lastMessage)
	return tools
}

// toolByName 按函数名查找已注册工具。
func toolByName(tools []api.Tool, n string) (*api.Tool, error) {
	for i := range tools {
		if tools[i].Function.Name == n {
			return &tools[i], nil
		}
	}
	return nil, fmt.Errorf("tool '%s' not found", n)
}

const (
	ministralToolCallsTag = "[TOOL_CALLS]"
	ministralThinkTag     = "[THINK]"
	ministralThinkEndTag  = "[/THINK]"
	ministralArgsTag      = "[ARGS]"
)

// eat 消费缓冲并返回当前状态下可确定的事件；第二返回值表示是否继续循环。
// eat consumes the parser's buffer, and returns a list of any unambiguous
// events from the current parser state. The second return value indicates
// whether to keep looping (true when state transitions, false when waiting
// for more data).
func (p *MinistralParser) eat() ([]ministralEvent, bool) {
	var events []ministralEvent

	switch p.state {
	case ministralCollectingContent:
		bufStr := p.buffer.String()

		// Check for [TOOL_CALLS] tag
		if strings.Contains(bufStr, ministralToolCallsTag) {
			split := strings.SplitN(bufStr, ministralToolCallsTag, 2)
			before := strings.TrimRightFunc(split[0], unicode.IsSpace)
			if len(before) > 0 {
				events = append(events, ministralEventContent{content: before})
			}
			after := split[1]
			p.buffer.Reset()
			p.buffer.WriteString(after)
			p.state = ministralCollectingToolName
			return events, true
		}

		// Check for [THINK] tag
		if strings.Contains(bufStr, ministralThinkTag) {
			split := strings.SplitN(bufStr, ministralThinkTag, 2)
			before := strings.TrimRightFunc(split[0], unicode.IsSpace)
			if len(before) > 0 {
				events = append(events, ministralEventContent{content: before})
			}
			after := split[1]
			p.buffer.Reset()
			p.buffer.WriteString(after)
			p.state = ministralCollectingThinkingContent
			return events, true
		}

		// Check for partial tag overlap with [TOOL_CALLS] or [THINK]
		overlapToolCalls := overlap(bufStr, ministralToolCallsTag)
		overlapThink := overlap(bufStr, ministralThinkTag)
		maxOverlap := max(overlapToolCalls, overlapThink)

		if maxOverlap > 0 {
			// Withhold the potential partial tag
			beforePartialTag := bufStr[:len(bufStr)-maxOverlap]
			trailingWS := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingWS
			unambiguous := bufStr[:ambiguousStart]
			ambiguous := bufStr[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, ministralEventContent{content: unambiguous})
			}
			return events, false
		}

		// No tag found: emit content but withhold trailing whitespace
		whitespaceLen := trailingWhitespaceLen(bufStr)
		ambiguousStart := len(bufStr) - whitespaceLen
		unambiguous := bufStr[:ambiguousStart]
		ambiguous := bufStr[ambiguousStart:]
		p.buffer.Reset()
		p.buffer.WriteString(ambiguous)
		if len(unambiguous) > 0 {
			events = append(events, ministralEventContent{content: unambiguous})
		}
		return events, false

	case ministralCollectingThinkingContent:
		bufStr := p.buffer.String()

		if strings.Contains(bufStr, ministralThinkEndTag) {
			split := strings.SplitN(bufStr, ministralThinkEndTag, 2)
			thinkingContent := split[0]
			after := strings.TrimLeftFunc(split[1], unicode.IsSpace)
			p.buffer.Reset()
			p.buffer.WriteString(after)
			if len(thinkingContent) > 0 {
				events = append(events, ministralEventThinking{thinking: thinkingContent})
			}
			p.state = ministralCollectingContent
			return events, true
		}

		// Check for partial overlap with [/THINK]
		if overlapLen := overlap(bufStr, ministralThinkEndTag); overlapLen > 0 {
			unambiguous := bufStr[:len(bufStr)-overlapLen]
			ambiguous := bufStr[len(bufStr)-overlapLen:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, ministralEventThinking{thinking: unambiguous})
			}
			return events, false
		}

		// No tag found: emit all thinking content
		p.buffer.Reset()
		if len(bufStr) > 0 {
			events = append(events, ministralEventThinking{thinking: bufStr})
		}
		return events, false

	case ministralCollectingToolName:
		bufStr := p.buffer.String()

		if strings.Contains(bufStr, ministralArgsTag) {
			split := strings.SplitN(bufStr, ministralArgsTag, 2)
			toolName := split[0]
			after := split[1]
			p.pendingToolName = toolName
			p.buffer.Reset()
			p.buffer.WriteString(after)
			p.state = ministralCollectingToolArgs
			return events, true
		}
		// Wait for more data
		return events, false

	case ministralCollectingToolArgs:
		bufStr := p.buffer.String()
		jsonEnd := findJSONEnd(bufStr)

		if jsonEnd != -1 {
			jsonStr := bufStr[:jsonEnd+1]
			remaining := bufStr[jsonEnd+1:]

			events = append(events, ministralEventToolCall{
				name: p.pendingToolName,
				args: jsonStr,
			})

			p.pendingToolName = ""
			p.buffer.Reset()
			p.buffer.WriteString(remaining)
			p.state = ministralCollectingContent
			return events, true
		}
		// Wait for more data
		return events, false

	default:
		panic("unexpected ministral event")
	}
}

// parseEvents loops calling eat() until it returns false
// parseEvents 循环调用 eat 直至无更多状态转移。
func (p *MinistralParser) parseEvents() []ministralEvent {
	var all []ministralEvent
	keepLooping := true
	for keepLooping {
		var events []ministralEvent
		events, keepLooping = p.eat()
		all = append(all, events...)
	}
	return all
}

// Add 追加流式片段并返回正文、思考与工具调用。
func (p *MinistralParser) Add(s string, done bool) (content string, thinking string, calls []api.ToolCall, err error) {
	p.buffer.WriteString(s)

	events := p.parseEvents()

	var contentBuilder, thinkingBuilder strings.Builder
	var toolCalls []api.ToolCall

	for _, event := range events {
		switch e := event.(type) {
		case ministralEventContent:
			contentBuilder.WriteString(e.content)
		case ministralEventThinking:
			thinkingBuilder.WriteString(e.thinking)
		case ministralEventToolCall:
			// Validate tool exists
			tool, toolErr := toolByName(p.tools, e.name)
			if toolErr != nil {
				return contentBuilder.String(), thinkingBuilder.String(), toolCalls, toolErr
			}
			// Parse JSON arguments
			var args api.ToolCallFunctionArguments
			if jsonErr := json.Unmarshal([]byte(e.args), &args); jsonErr != nil {
				return contentBuilder.String(), thinkingBuilder.String(), toolCalls, jsonErr
			}
			toolCalls = append(toolCalls, api.ToolCall{
				Function: api.ToolCallFunction{
					Name:      tool.Function.Name,
					Arguments: args,
				},
			})
		}
	}

	for i := range toolCalls {
		toolCalls[i].Function.Index = p.callIndex
		p.callIndex++
	}

	return contentBuilder.String(), thinkingBuilder.String(), toolCalls, nil
}

// findJSONEnd 查找完成 JSON 对象的闭合括号索引，支持嵌套与转义字符串。
// findJSONEnd finds the index of the closing brace that completes a JSON object.
// It properly handles nested objects, arrays, and strings (including escaped characters).
// Returns -1 if the JSON is not yet complete.
func findJSONEnd(s string) int {
	depth := 0
	inString := false
	escaped := false

	for i, r := range s {
		if inString {
			switch {
			case escaped:
				// If the previous character was a backslash, skip this character
				escaped = false
			case r == '\\':
				// Mark the next character as escaped
				escaped = true
			case r == '"':
				// End of string literal
				inString = false
			}
			continue
		}

		switch r {
		case '"':
			// Start of string literal
			inString = true
		case '{', '[':
			// Increase nesting level for objects and arrays
			depth++
		case '}', ']':
			// Decrease nesting level
			depth--
			if depth == 0 {
				// Reached the end of the root JSON structure
				return i
			}
		}
	}

	return -1
}
