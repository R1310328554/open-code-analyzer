// Package schema 提供智能体框架共享的消息与流类型。
package schema

import (
	"fmt"
	"io"
)

// RoleType 对话消息的角色枚举。
type RoleType string

const (
	RoleUser      RoleType = "user"
	RoleAssistant RoleType = "assistant"
	RoleSystem    RoleType = "system"
	RoleTool      RoleType = "tool"
	RoleFunction  RoleType = "function"
)

// AgenticRoleType 面向智能体的消息角色。
type AgenticRoleType string

const (
	AgenticRoleAssistant AgenticRoleType = "assistant"
	AgenticRoleUser      AgenticRoleType = "user"
	AgenticRoleSystem    AgenticRoleType = "system"
)

// ToolCallFunction 工具调用中的函数名与参数 JSON。
type ToolCallFunction struct {
	Name      string `json:"name"`
	Arguments string `json:"arguments"`
}

// ToolCall 模型发起的工具调用（含 ID 与函数信息）。
type ToolCall struct {
	ID       string           `json:"id"`
	Type     string           `json:"type,omitempty"`
	Function ToolCallFunction `json:"function"`
}

// Message 标准对话消息，含角色、内容与可选 tool_calls。
type Message struct {
	Role      RoleType       `json:"role"`
	Content   string         `json:"content"`
	Name      string         `json:"name,omitempty"`
	ToolCalls []ToolCall     `json:"tool_calls,omitempty"`
	ToolName  string         `json:"tool_name,omitempty"`
	Extra     map[string]any `json:"extra,omitempty"`
}

// ToolCallInfo Agentic 消息中工具调用的结构化信息。
type ToolCallInfo struct {
	ID        string `json:"id"`
	Name      string `json:"name"`
	Arguments string `json:"arguments"`
}

// ToolResult 工具执行结果，标准工具与增强工具共用。
// Used by both standard tools and enhanced tools.
type ToolResult struct {
	ToolCallID string         `json:"tool_call_id"`
	Name       string         `json:"name"`
	Content    string         `json:"content"`
	Error      string         `json:"error,omitempty"`
	Extra      map[string]any `json:"extra,omitempty"`
}

// ContentBlock AgenticMessage 内的结构化内容块（文本/工具调用/结果）。
type ContentBlock struct {
	Type       string        `json:"type"`
	Text       string        `json:"text,omitempty"`
	ToolCall   *ToolCallInfo `json:"tool_call,omitempty"`
	ToolResult *ToolResult   `json:"tool_result,omitempty"`
}

// AgenticMessage 面向智能体的消息，支持多块结构化内容。
type AgenticMessage struct {
	Role          AgenticRoleType `json:"role"`
	Content       string          `json:"content"`
	ContentBlocks []ContentBlock  `json:"content_blocks,omitempty"`
}

// ToolInfo 向模型暴露的工具元数据（名称、描述、输入 schema）。
type ToolInfo struct {
	Name        string `json:"name"`
	Description string `json:"description"`
	InputSchema any    `json:"input_schema,omitempty"`
}

// ToolChoice 控制模型如何使用已绑定工具。
type ToolChoice string

const (
	// ToolChoiceForbidden 禁止模型调用任何工具。
	ToolChoiceForbidden ToolChoice = "forbidden"
	// ToolChoiceAllowed 由模型自行决定是否调用工具。
	ToolChoiceAllowed ToolChoice = "allowed"
	// ToolChoiceForced 强制模型至少调用一个工具。
	ToolChoiceForced ToolChoice = "forced"
)

// AllowedTool 指定模型允许或必须调用的工具。
type AllowedTool struct {
	// FunctionName 按名称指定函数工具。
	FunctionName string `json:"function_name,omitempty"`
}

// AgenticToolChoice 细粒度控制模型可调用哪些工具。
type AgenticToolChoice struct {
	// Type 工具选择模式（forbidden/allowed/forced）。
	Type ToolChoice `json:"type"`
	// Allowed 可选：允许调用的工具列表。
	Allowed *struct {
		Tools []*AllowedTool `json:"tools,omitempty"`
	} `json:"allowed,omitempty"`
	// Forced 可选：必须调用的工具列表。
	Forced *struct {
		Tools []*AllowedTool `json:"tools,omitempty"`
	} `json:"forced,omitempty"`
}

// ToolArgument 增强工具调用的结构化参数。
type ToolArgument struct {
	// Name 被调用工具的名称。
	Name string `json:"name"`

	// Arguments 原始 JSON 参数字符串。
	Arguments string `json:"arguments"`

	// CallID 本次工具调用的唯一标识。
	CallID string `json:"call_id,omitempty"`
}

// ---- Gob 类型注册辅助（检查点/恢复序列化）----

var registeredTypes = make(map[string]func() any)

func RegisterType(name string, factory func() any) {
	registeredTypes[name] = factory
}

// RegisterName 为 gob 序列化注册具体类型，须在 init() 中调用。
// 自定义类型通过 SetRunLocalValue 存入时需注册，
// 以便在中断/恢复检查点周期中正确序列化。
func RegisterName[T any](name string) {
	RegisterType(name, func() any { var t T; return &t })
}

// StreamReader 泛型缓冲流读取器。
type StreamReader[M any] struct {
	ch     chan streamFrame[M]
	closed bool
}

type streamFrame[M any] struct {
	Data M
	Err  error
}

// NewStreamReader 创建带 64 缓冲容量的 StreamReader。
func NewStreamReader[M any]() *StreamReader[M] {
	return &StreamReader[M]{ch: make(chan streamFrame[M], 64)}
}

// Recv 阻塞读取下一项，通道关闭时返回 io.EOF。
func (sr *StreamReader[M]) Recv() (M, error) {
	frame, ok := <-sr.ch
	if !ok {
		var zero M
		return zero, io.EOF
	}
	return frame.Data, frame.Err
}

// Send 向流推送数据项（可附带错误）。
func (sr *StreamReader[M]) Send(data M, err error) {
	if sr.closed {
		return
	}
	sr.ch <- streamFrame[M]{Data: data, Err: err}
}

// Close 关闭流并释放通道。
func (sr *StreamReader[M]) Close() {
	if !sr.closed {
		sr.closed = true
		close(sr.ch)
	}
}

// StreamReaderFromArray 由切片预填充并立即关闭的流。
func StreamReaderFromArray[M any](items []M) *StreamReader[M] {
	sr := NewStreamReader[M]()
	for _, item := range items {
		sr.Send(item, nil)
	}
	sr.Close()
	return sr
}

// ConcatMessages 将多条消息合并为一条（拼接内容与 Extra）。
func ConcatMessages(msgs []*Message) (*Message, error) {
	if len(msgs) == 0 {
		return nil, fmt.Errorf("no messages to concatenate")
	}
	result := &Message{
		Role:    msgs[0].Role,
		Content: "",
		Extra:   make(map[string]any),
	}
	for _, m := range msgs {
		result.Content += m.Content
		if m.Extra != nil {
			for k, v := range m.Extra {
				result.Extra[k] = v
			}
		}
		if len(m.ToolCalls) > 0 {
			result.ToolCalls = m.ToolCalls
		}
		if m.ToolName != "" {
			result.ToolName = m.ToolName
		}
	}
	return result, nil
}

// ConcatAgenticMessages 合并多条 AgenticMessage。
func ConcatAgenticMessages(msgs []*AgenticMessage) (*AgenticMessage, error) {
	if len(msgs) == 0 {
		return nil, fmt.Errorf("no messages to concatenate")
	}
	result := &AgenticMessage{
		Role:          msgs[0].Role,
		Content:       "",
		ContentBlocks: nil,
	}
	for _, m := range msgs {
		result.Content += m.Content
		if m.ContentBlocks != nil {
			result.ContentBlocks = append(result.ContentBlocks, m.ContentBlocks...)
		}
	}
	return result, nil
}

// ConcatMessageStream 消费流中全部消息后合并。
func ConcatMessageStream(sr *StreamReader[*Message]) (*Message, error) {
	defer sr.Close()
	var msgs []*Message
	for {
		m, err := sr.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			return nil, err
		}
		msgs = append(msgs, m)
	}
	return ConcatMessages(msgs)
}

// ---- 消息构造辅助函数 ----

// UserMessage 构造用户角色消息。
func UserMessage(content string) *Message {
	return &Message{Role: RoleUser, Content: content, Extra: make(map[string]any)}
}
// AssistantMessage 构造助手角色消息。
func AssistantMessage(content string) *Message {
	return &Message{Role: RoleAssistant, Content: content, Extra: make(map[string]any)}
}
// SystemMessage 构造系统角色消息。
func SystemMessage(content string) *Message {
	return &Message{Role: RoleSystem, Content: content, Extra: make(map[string]any)}
}
// ToolMessage 构造工具结果消息。
func ToolMessage(content, toolCallID string) *Message {
	return &Message{Role: RoleTool, Content: content, Name: toolCallID, Extra: make(map[string]any)}
}
// FunctionMessage 构造函数角色消息。
func FunctionMessage(content, name string) *Message {
	return &Message{Role: RoleFunction, Content: content, Name: name, Extra: make(map[string]any)}
}
// UserAgenticMessage 构造用户 Agentic 消息（含文本块）。
func UserAgenticMessage(content string) *AgenticMessage {
	return &AgenticMessage{
		Role: AgenticRoleUser, Content: content,
		ContentBlocks: []ContentBlock{{Type: "text", Text: content}},
	}
}

// RegisterType/RegisterName 使自定义类型可跨检查点 gob 序列化。
