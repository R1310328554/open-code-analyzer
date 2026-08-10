package parsers

import (
	"context"
	"encoding/json"
	"fmt"
	"log/slog"
	"strings"
	"unicode"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/logutil"
)

type qwen3ParserState int

const (
	qwen3ParserStateLookingForThinkingOpen qwen3ParserState = iota
	qwen3ParserStateThinkingStartedEatingWhitespace
	qwen3ParserStateCollectingThinking
	qwen3ParserStateThinkingDoneEatingWhitespace
	qwen3ParserStateCollectingContent
	qwen3ParserStateToolStartedEatingWhitespace
	qwen3ParserStateCollectingToolContent
)

const (
	qwen3ThinkingOpenTag  = "<think>"
	qwen3ThinkingCloseTag = "</think>"
	qwen3ToolOpenTag      = "<tool_call>"
	qwen3ToolCloseTag     = "</tool_call>"
)

// Qwen3 解析器：redacted_thinking 与 <tool_call> JSON 工具块。
// Qwen3Parser 解析 Qwen3 流式输出，提取思考与工具调用。
// Qwen3 prompts end with <think> when thinking is enabled, so output begins
// with thinking content directly (without an opening tag).
// Qwen3Parser parses Qwen3 output to extract thinking and tool calls.
// Qwen3 prompts end with <think> when thinking is enabled, so output begins
// with thinking content directly (without an opening tag).
// Qwen3Parser 状态机解析 Qwen3 thinking/content/tool 标签。
type Qwen3Parser struct {
	state                  qwen3ParserState
	buffer                 strings.Builder
	tools                  []api.Tool
	callIndex              int
	hasThinkingSupport     bool
	defaultThinking        bool
	maybeThinkingOpenAtBOL bool
}

// HasToolSupport 返回 true。
func (p *Qwen3Parser) HasToolSupport() bool {
	return true
}

// HasThinkingSupport 返回是否编译时启用思考。
func (p *Qwen3Parser) HasThinkingSupport() bool {
	return p.hasThinkingSupport
}

// PreservedTokens 返回 thinking/tool 边界 token。
func (p *Qwen3Parser) PreservedTokens() []string {
	return []string{
		qwen3ThinkingOpenTag,
		qwen3ThinkingCloseTag,
		qwen3ToolOpenTag,
		qwen3ToolCloseTag,
	}
}

// Init 根据 think 偏好与 defaultThinking 设置初始状态。
func (p *Qwen3Parser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
	p.tools = tools
	p.buffer.Reset()
	p.callIndex = 0

	thinkingEnabled := thinkValue != nil && thinkValue.Bool()
	if thinkValue == nil {
		thinkingEnabled = p.defaultThinking
	}

	if p.hasThinkingSupport && thinkingEnabled {
		p.state = qwen3ParserStateCollectingThinking
		p.maybeThinkingOpenAtBOL = true
	} else {
		p.state = qwen3ParserStateCollectingContent
		p.maybeThinkingOpenAtBOL = false
	}
	return tools
}

// qwen3Event 标记 Qwen3 内部解析事件。
type qwen3Event interface {
	isQwen3Event()
}

type qwen3EventContent struct {
	content string
}

func (qwen3EventContent) isQwen3Event() {}

type qwen3EventRawToolCall struct {
	raw string
}

func (qwen3EventRawToolCall) isQwen3Event() {}

type qwen3EventThinkingContent struct {
	content string
}

func (qwen3EventThinkingContent) isQwen3Event() {}

// Add 流式解析并分配 tool call 序号。
func (p *Qwen3Parser) Add(s string, done bool) (content string, thinking string, calls []api.ToolCall, err error) {
	p.buffer.WriteString(s)
	events := p.parseEvents()

	var contentSb strings.Builder
	var thinkingSb strings.Builder
	for _, event := range events {
		switch event := event.(type) {
		case qwen3EventRawToolCall:
			toolCall, err := parseQwen3ToolCall(event, p.tools)
			if err != nil {
				slog.Warn("qwen3 tool call parsing failed", "error", err)
				return "", "", nil, err
			}
			toolCall.Function.Index = p.callIndex
			p.callIndex++
			calls = append(calls, toolCall)
		case qwen3EventThinkingContent:
			thinkingSb.WriteString(event.content)
		case qwen3EventContent:
			contentSb.WriteString(event.content)
		}
	}

	return contentSb.String(), thinkingSb.String(), calls, nil
}

// parseEvents 循环 eat 收集事件。
func (p *Qwen3Parser) parseEvents() []qwen3Event {
	var all []qwen3Event

	keepLooping := true
	for keepLooping {
		var events []qwen3Event
		events, keepLooping = p.eat()
		if len(events) > 0 {
			all = append(all, events...)
		}
	}

	if len(all) > 0 {
		slog.Log(context.TODO(), logutil.LevelTrace, "qwen3 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String())
	}

	return all
}

// eatLeadingWhitespaceAndTransitionTo 跳过前导空白并转移状态。
func (p *Qwen3Parser) eatLeadingWhitespaceAndTransitionTo(nextState qwen3ParserState) ([]qwen3Event, bool) {
	trimmed := strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace)
	p.buffer.Reset()
	if trimmed == "" {
		return nil, false
	}
	p.state = nextState
	p.buffer.WriteString(trimmed)
	return nil, true
}

// splitAtTag 在标签处分割内部缓冲。
func (p *Qwen3Parser) splitAtTag(tag string, trimAfter bool) (string, string) {
	return splitAtTag(&p.buffer, tag, trimAfter)
}

// eat 状态机解析 thinking/content/tool JSON 块。
func (p *Qwen3Parser) eat() ([]qwen3Event, bool) {
	var events []qwen3Event

	switch p.state {
	case qwen3ParserStateLookingForThinkingOpen:
		trimmed := strings.TrimLeftFunc(p.buffer.String(), unicode.IsSpace)
		if strings.HasPrefix(trimmed, qwen3ThinkingOpenTag) {
			after := strings.TrimPrefix(trimmed, qwen3ThinkingOpenTag)
			after = strings.TrimLeftFunc(after, unicode.IsSpace)
			p.buffer.Reset()
			p.buffer.WriteString(after)
			if after == "" {
				p.state = qwen3ParserStateThinkingStartedEatingWhitespace
			} else {
				p.state = qwen3ParserStateCollectingThinking
			}
			return events, true
		} else if strings.HasPrefix(qwen3ThinkingOpenTag, trimmed) {
			return events, false
		} else if trimmed == "" {
			return events, false
		}
		p.state = qwen3ParserStateCollectingContent
		return events, true

	case qwen3ParserStateThinkingStartedEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(qwen3ParserStateCollectingThinking)

	case qwen3ParserStateCollectingThinking:
		acc := p.buffer.String()

		// Some qwen3 checkpoints emit an explicit opening <think> tag even
		// though the prompt already ended with <think>. Strip exactly one
		// leading opening tag if present.
		if p.maybeThinkingOpenAtBOL {
			trimmed := strings.TrimLeftFunc(acc, unicode.IsSpace)
			if strings.HasPrefix(trimmed, qwen3ThinkingOpenTag) {
				after := strings.TrimPrefix(trimmed, qwen3ThinkingOpenTag)
				after = strings.TrimLeftFunc(after, unicode.IsSpace)
				p.buffer.Reset()
				p.buffer.WriteString(after)
				if after == "" {
					return events, false
				}
				p.maybeThinkingOpenAtBOL = false
				return events, true
			}
			if strings.HasPrefix(qwen3ThinkingOpenTag, trimmed) {
				return events, false
			}
			p.maybeThinkingOpenAtBOL = false
		}

		thinkingCloseIdx := strings.Index(acc, qwen3ThinkingCloseTag)
		toolOpenIdx := strings.Index(acc, qwen3ToolOpenTag)

		// If a tool call starts before </think>, treat that as the end of thinking
		// for parsing purposes and continue in tool-call mode.
		if toolOpenIdx != -1 && (thinkingCloseIdx == -1 || toolOpenIdx < thinkingCloseIdx) {
			before, after := p.splitAtTag(qwen3ToolOpenTag, true)
			if len(before) > 0 {
				events = append(events, qwen3EventThinkingContent{content: before})
			}
			if after == "" {
				p.state = qwen3ParserStateToolStartedEatingWhitespace
			} else {
				p.state = qwen3ParserStateCollectingToolContent
			}
			return events, true
		}

		if strings.Contains(acc, qwen3ThinkingCloseTag) {
			thinking, remaining := p.splitAtTag(qwen3ThinkingCloseTag, true)
			if len(thinking) > 0 {
				events = append(events, qwen3EventThinkingContent{content: thinking})
			}
			if remaining == "" {
				p.state = qwen3ParserStateThinkingDoneEatingWhitespace
			} else {
				p.state = qwen3ParserStateCollectingContent
			}
			return events, true
		} else if overlapLen := max(overlap(acc, qwen3ThinkingCloseTag), overlap(acc, qwen3ToolOpenTag)); overlapLen > 0 {
			beforePartialTag := acc[:len(acc)-overlapLen]
			trailingWsLen := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingWsLen

			unambiguous := acc[:ambiguousStart]
			ambiguous := acc[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, qwen3EventThinkingContent{content: unambiguous})
			}
			return events, false
		}

		whitespaceLen := trailingWhitespaceLen(acc)
		ambiguousStart := len(acc) - whitespaceLen
		unambiguous := acc[:ambiguousStart]
		ambiguous := acc[ambiguousStart:]
		p.buffer.Reset()
		p.buffer.WriteString(ambiguous)
		if len(unambiguous) > 0 {
			events = append(events, qwen3EventThinkingContent{content: unambiguous})
		}
		return events, false

	case qwen3ParserStateThinkingDoneEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(qwen3ParserStateCollectingContent)

	case qwen3ParserStateCollectingContent:
		acc := p.buffer.String()
		if strings.Contains(acc, qwen3ToolOpenTag) {
			before, after := p.splitAtTag(qwen3ToolOpenTag, true)
			if len(before) > 0 {
				events = append(events, qwen3EventContent{content: before})
			}
			if after == "" {
				p.state = qwen3ParserStateToolStartedEatingWhitespace
			} else {
				p.state = qwen3ParserStateCollectingToolContent
			}
			return events, true
		} else if overlapLen := overlap(acc, qwen3ToolOpenTag); overlapLen > 0 {
			beforePartialTag := acc[:len(acc)-overlapLen]
			trailingWsLen := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingWsLen

			unambiguous := acc[:ambiguousStart]
			ambiguous := acc[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, qwen3EventContent{content: unambiguous})
			}
			return events, false
		}

		whitespaceLen := trailingWhitespaceLen(acc)
		ambiguousStart := len(acc) - whitespaceLen
		unambiguous := acc[:ambiguousStart]
		ambiguous := acc[ambiguousStart:]
		p.buffer.Reset()
		p.buffer.WriteString(ambiguous)
		if len(unambiguous) > 0 {
			events = append(events, qwen3EventContent{content: unambiguous})
		}
		return events, false

	case qwen3ParserStateToolStartedEatingWhitespace:
		return p.eatLeadingWhitespaceAndTransitionTo(qwen3ParserStateCollectingToolContent)

	case qwen3ParserStateCollectingToolContent:
		acc := p.buffer.String()
		if strings.Contains(acc, qwen3ToolCloseTag) {
			toolContent, _ := p.splitAtTag(qwen3ToolCloseTag, true)
			if len(toolContent) == 0 {
				slog.Warn("qwen3 tool call closing tag found but no content before it")
			}
			events = append(events, qwen3EventRawToolCall{raw: toolContent})
			p.state = qwen3ParserStateCollectingContent
			return events, true
		}
		return events, false

	default:
		panic("unreachable")
	}
}

// parseQwen3ToolCall 将 tool_call 块内 JSON 解析为 ToolCall。
func parseQwen3ToolCall(raw qwen3EventRawToolCall, tools []api.Tool) (api.ToolCall, error) {
	var parsed struct {
		Name      string                        `json:"name"`
		Arguments api.ToolCallFunctionArguments `json:"arguments"`
	}

	if err := json.Unmarshal([]byte(raw.raw), &parsed); err != nil {
		return api.ToolCall{}, fmt.Errorf("failed to parse JSON: %w", err)
	}

	if parsed.Name == "" {
		return api.ToolCall{}, fmt.Errorf("empty function name")
	}

	_ = tools // qwen3 uses direct JSON args and does not require schema coercion here.

	toolCall := api.ToolCall{
		Function: api.ToolCallFunction{
			Name:      parsed.Name,
			Arguments: parsed.Arguments,
		},
	}

	return toolCall, nil
}
