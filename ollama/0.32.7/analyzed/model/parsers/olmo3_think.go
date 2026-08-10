// Olmo3-Think 解析器：</think> 思考块与正文分离。
package parsers

import (
	"context"
	"log/slog"
	"strings"
	"unicode"

	"github.com/ollama/ollama/api"
	"github.com/ollama/ollama/logutil"
)

// olmo3ThinkParserState 表示 Olmo3Think 解析阶段。
type olmo3ThinkParserState int

const (
	olmo3CollectingThink olmo3ThinkParserState = iota
	olmo3CollectingContent
)

const (
	olmo3ThinkCloseTag = "</think>"
)

// Olmo3ThinkParser 解析 Olmo3 思考模型输出。
type Olmo3ThinkParser struct {
	state  olmo3ThinkParserState
	buffer strings.Builder
}

// HasToolSupport 返回 false。
func (p *Olmo3ThinkParser) HasToolSupport() bool {
	return false
}

// HasThinkingSupport 返回 true。
func (p *Olmo3ThinkParser) HasThinkingSupport() bool {
	return true
}

// PreservedTokens 返回 </think> 标签。
func (p *Olmo3ThinkParser) PreservedTokens() []string {
	return []string{
		olmo3ThinkCloseTag,
	}
}

// setInitialState 预填充正文时跳过思考收集。
func (p *Olmo3ThinkParser) setInitialState(lastMessage *api.Message) {
	prefill := lastMessage != nil && lastMessage.Role == "assistant"

	// If prefilling with content, skip to content collection
	if prefill && lastMessage.Content != "" {
		p.state = olmo3CollectingContent
		return
	}

	// Model always thinks first (the <think> tag is injected in the prompt)
	p.state = olmo3CollectingThink
}

// Init 设置初始状态并返回工具列表。
func (p *Olmo3ThinkParser) Init(tools []api.Tool, lastMessage *api.Message, thinkValue *api.ThinkValue) []api.Tool {
	p.setInitialState(lastMessage)
	return tools
}

// olmo3Event 为内部解析事件接口。
// Event types for internal parser communication
// olmo3Event 标记 Olmo3Think 事件类型。
type olmo3Event interface {
	isOlmo3Event()
}

type olmo3EventThinkContent struct {
	content string
}

type olmo3EventContent struct {
	content string
}

func (olmo3EventThinkContent) isOlmo3Event() {}
func (olmo3EventContent) isOlmo3Event()      {}

// Add 流式解析思考与正文片段。
func (p *Olmo3ThinkParser) Add(s string, done bool) (content string, thinking string, calls []api.ToolCall, err error) {
	p.buffer.WriteString(s)
	events := p.parseEvents()

	var contentSb strings.Builder
	var thinkingSb strings.Builder
	for _, event := range events {
		switch event := event.(type) {
		case olmo3EventThinkContent:
			thinkingSb.WriteString(event.content)
		case olmo3EventContent:
			contentSb.WriteString(event.content)
		}
	}

	return contentSb.String(), thinkingSb.String(), nil, nil
}

// parseEvents 循环 eat 直至无状态转移。
func (p *Olmo3ThinkParser) parseEvents() []olmo3Event {
	var all []olmo3Event

	keepLooping := true
	for keepLooping {
		var events []olmo3Event
		events, keepLooping = p.eat()
		if len(events) > 0 {
			all = append(all, events...)
		}
	}

	if len(all) > 0 {
		slog.Log(context.TODO(), logutil.LevelTrace, "olmo3 events parsed", "events", all, "state", p.state, "buffer", p.buffer.String())
	}

	return all
}

// eat 解析 </think> 边界并处理部分标签重叠。
func (p *Olmo3ThinkParser) eat() ([]olmo3Event, bool) {
	var events []olmo3Event
	bufStr := p.buffer.String()
	if bufStr == "" {
		return events, false
	}

	switch p.state {
	case olmo3CollectingThink:
		if strings.Contains(bufStr, olmo3ThinkCloseTag) {
			// Found complete </think> tag
			split := strings.SplitN(bufStr, olmo3ThinkCloseTag, 2)
			thinking := strings.TrimRightFunc(split[0], unicode.IsSpace)
			remaining := strings.TrimLeftFunc(split[1], unicode.IsSpace)

			p.buffer.Reset()
			p.buffer.WriteString(remaining)
			p.state = olmo3CollectingContent

			if len(thinking) > 0 {
				events = append(events, olmo3EventThinkContent{content: thinking})
			}
			return events, true
		} else if overlapLen := overlap(bufStr, olmo3ThinkCloseTag); overlapLen > 0 {
			// Partial </think> tag - withhold ambiguous content
			beforePartialTag := bufStr[:len(bufStr)-overlapLen]
			trailingLen := trailingWhitespaceLen(beforePartialTag)
			ambiguousStart := len(beforePartialTag) - trailingLen

			unambiguous := bufStr[:ambiguousStart]
			ambiguous := bufStr[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, olmo3EventThinkContent{content: unambiguous})
			}
			return events, false
		} else {
			// Regular thinking content - withhold trailing whitespace in case </think> follows
			whitespaceLen := trailingWhitespaceLen(bufStr)
			ambiguousStart := len(bufStr) - whitespaceLen

			unambiguous := bufStr[:ambiguousStart]
			ambiguous := bufStr[ambiguousStart:]
			p.buffer.Reset()
			p.buffer.WriteString(ambiguous)
			if len(unambiguous) > 0 {
				events = append(events, olmo3EventThinkContent{content: unambiguous})
			}
			return events, false
		}

	case olmo3CollectingContent:
		// Emit all content directly
		p.buffer.Reset()
		if len(bufStr) > 0 {
			events = append(events, olmo3EventContent{content: bufStr})
		}
		return events, false
	}

	return events, false
}
