package parsers

import (
	"context"
	"encoding/json"
	"log/slog"
	"strings"
	"unicode"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/logutil"
)

// Qwen3-VL 解析器：视觉模型 thinking/content 与 JSON tool_call。
// TODO: call the init function
const (
	CollectingThinkingContent qwenParserState = iota
	CollectingContent
	CollectingToolContent
	ThinkingDoneEatingWhitespace
	ToolCallDoneEatingWhitespace
)

const (
	thinkingCloseTag = "</think>"
)

// Qwen3VLParser 解析 Qwen3-VL 流式输出。
type Qwen3VLParser struct {
	state              qwenParserState
	buffer             strings.Builder
	tools              []api.Tool
	callIndex          int
	hasThinkingSupport bool
}

// HasToolSupport 返回 true。
func (p *Qwen3VLParser) HasToolSupport() bool {
	return true
}

// HasThinkingSupport 返回是否启用思考模式。
func (p *Qwen3VLParser) HasThinkingSupport() bool {
	return p.hasThinkingSupport
}

// PreservedTokens 返回 thinking/tool token。
func (p *Qwen3VLParser) PreservedTokens() []string {
	return []string{
		thinkingCloseTag,
		toolOpenTag,
		toolCloseTag,
	}
}

// setInitialState 根据预填充与思考能力选择初始状态。
func (p *Qwen3VLParser) setInitialState(lastMessage *api.Message) {
	prefill := lastMessage != nil && lastMessage.Role == "assistant"
	if !p.HasThinkingSupport() {
		p.state = CollectingContent
		return
	}

	if prefill && lastMessage.Content != "" {
		p.state = CollectingContent
		return
	}

	p.state = CollectingThinkingContent
}

// Init 初始化工具列表与 callIndex。
func (p *Qwen3VLParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
	p.tools = tools
	p.callIndex = 0
	p.setInitialState(lastMessage)
	return tools
}

type qwenEventThinkingContent struct {
	content string
}

func (qwenEventThinkingContent) isQwenEvent() {}

// Add 流式解析 thinking/content/JSON tool_call。
func (p *Qwen3VLParser) Add(s string, done bool) (content string, thinking string, calls []api.ToolCall, err error) {
	p.buffer.WriteString(s)
	events := p.parseEvents()

	var contentSb strings.Builder
	var thinkingSb strings.Builder
	for _, event := range events {
		switch event := event.(type) {
		case qwenEventRawToolCall:
			toolCall, err := parseJSONToolCall(event, p.tools)
			if err != nil {
				slog.Warn("qwen tool call parsing failed", "error", err)
				return "", "", nil, err
			}
			calls = append(calls, toolCall)
		case qwenEventThinkingContent:
			thinkingSb.WriteString(event.content)
		case qwenEventContent:
			// TODO(drifkin): if the same turn contains multiple interleaved content
			// events, we naively append them together here.
			contentSb.WriteString(event.content)
		}
	}

	for i := range calls {
		calls[i].Function.Index = p.callIndex
		p.callIndex++
	}

	return contentSb.String(), thinkingSb.String(), calls, nil
}

// parseEvents 循环 eat 收集事件。
func (p *Qwen3VLParser) parseEvents() []qwenEvent {
	var all []qwenEvent

	keepLooping := true
	for keepLooping {
		var events []qwenEvent
		events, keepLooping = p.eat()
		if len(events) > 0 {
			all = append(all, events...)
		}
	}

	if len(all) > 0 {
		slog.Log(context.TODO(), logutil.LevelTrace, "qwen events parsed", "events", all, "state", p.state, "buffer", p.buffer.String())
	}

	return all
}

// eatLeadingWhitespaceAndTransitionTo 跳过前导空白并转移状态。
func (p *Qwen3VLParser) eatLeadingWhitespaceAndTransitionTo(nextState qwenParserState) ([]qwenEvent, bool) {
	trimmed := strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace)
	p.buffer.Reset()
	if trimmed == "" {
		return nil, false
	}
	p.state = nextState
	p.buffer.WriteString(trimmed)
	return nil, true
}

// eat 状态机解析 thinking/content/tool JSON 块。
func (p *Qwen3VLParser) eat() ([]qwenEvent, bool) {
	var events []qwenEvent

	switch p.state {
	case CollectingContent:
		if strings.Contains(p.buffer.String(), toolOpenTag) {
			// events = emitContentBeforeTag(p, events, toolOpenTag)
			before, _ := splitAtTag(&p.buffer, toolOpenTag, false)
			if len(before) > 0 {
				events = append(events, qwenEventContent{content: before})
			}
			p.state = CollectingToolContent
			return events, true
		} else if overlapLen := overlap(p.buffer.String(), toolOpenTag); overlapLen > 0 {
			beforePartialTag := p.buffer.String()[:len(p.buffer.String())-overlapLen]
			trailingWhitespaceLen := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingWhitespaceLen

			unambiguous := p.buffer.String()[:ambiguousStart]
			ambiguous := p.buffer.String()[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, qwenEventContent{content: unambiguous})
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
				events = append(events, qwenEventContent{content: unambiguous})
			}
			return events, false
		}
	case CollectingToolContent:
		if strings.Contains(p.buffer.String(), toolCloseTag) {
			split := strings.SplitN(p.buffer.String(), toolCloseTag, 2)
			before := split[0] // do we also need to do it to tool calls?
			if len(before) == 0 {
				slog.Warn("qwen tool call closing tag found but no content before it")
			}

			after := split[1]
			events = append(events, qwenEventRawToolCall{raw: before})
			p.buffer.Reset()
			p.buffer.WriteString(after)
			p.state = ToolCallDoneEatingWhitespace
			return events, true
		} else {
			return events, false
		}
	case CollectingThinkingContent:
		acc := p.buffer.String()
		thinkingCloseIdx := strings.Index(acc, thinkingCloseTag)
		toolOpenIdx := strings.Index(acc, toolOpenTag)

		// If a tool call starts before </think>, treat that as the end of thinking
		// for parsing purposes and continue in tool-call mode.
		if toolOpenIdx != -1 && (thinkingCloseIdx == -1 || toolOpenIdx < thinkingCloseIdx) {
			before, _ := splitAtTag(&p.buffer, toolOpenTag, false)
			if len(before) > 0 {
				events = append(events, qwenEventThinkingContent{content: before})
			}
			p.state = CollectingToolContent
			return events, true
		}

		if strings.Contains(acc, thinkingCloseTag) {
			thinking, remaining := splitAtTag(&p.buffer, thinkingCloseTag, true)
			if len(thinking) > 0 {
				events = append(events, qwenEventThinkingContent{content: thinking})
			}
			if remaining == "" {
				p.state = ThinkingDoneEatingWhitespace
			} else {
				p.state = CollectingContent
			}
			return events, true
		} else if overlapLen := max(overlap(acc, thinkingCloseTag), overlap(acc, toolOpenTag)); overlapLen > 0 {
			beforePartialTag := acc[:len(acc)-overlapLen]
			trailingWhitespaceLen := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingWhitespaceLen

			unambiguous := acc[:ambiguousStart]
			ambiguous := acc[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, qwenEventThinkingContent{content: unambiguous})
			}
			return events, false
		} else {
			whitespaceLen := trailingWhitespaceLen(acc)
			ambiguousStart := len(acc) - whitespaceLen

			unambiguous := acc[:ambiguousStart]
			ambiguous := acc[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, qwenEventThinkingContent{content: unambiguous})
			}
			return events, false
		}
	case ThinkingDoneEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(CollectingContent)
	case ToolCallDoneEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(CollectingContent)
	default:
		panic("unreachable")
	}
}

// parseJSONToolCall 将 tool_call 块 JSON 反序列化为 ToolCall。
func parseJSONToolCall(raw qwenEventRawToolCall, tools []api.Tool) (api.ToolCall, error) {
	var toolCallFunction api.ToolCallFunction
	if err := json.Unmarshal([]byte(raw.raw), &toolCallFunction); err != nil {
		return api.ToolCall{}, err
	}

	toolCall := api.ToolCall{}
	toolCall.Function = toolCallFunction

	return toolCall, nil
}
