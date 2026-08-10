package graph

// message.go — 消息型图：Message/MessagesState、AddMessages reducer 与 OpenAI 格式转换。


import (
	"context"
	"fmt"
	"slices"

	"ragflow/internal/harness/graph/channels"
	"ragflow/internal/harness/graph/types"
)

// Message 对话消息（角色、内容、去重 ID、扩展元数据）。
type Message struct {
	ID      string                 // Unique identifier for deduplication
	Role    string                 // e.g., "user", "assistant", "system"
	Content string                 // The message content
	Extra   map[string]interface{} // Additional metadata
}

// NewMessage 创建无 ID 消息（不参与 ID 去重）。
func NewMessage(role, content string) *Message {
	return &Message{
		ID:      "",
		Role:    role,
		Content: content,
		Extra:   make(map[string]interface{}),
	}
}

// NewMessageWithID 创建带 ID 消息，供 reducer 更新合并。
func NewMessageWithID(id, role, content string) *Message {
	return &Message{
		ID:      id,
		Role:    role,
		Content: content,
		Extra:   make(map[string]interface{}),
	}
}

// MessagesState 消息图状态：消息列表 + 扩展字段。
// It contains a list of messages and optional additional fields.
type MessagesState struct {
	Messages []*Message
	Extra    map[string]interface{}
}

// AddMessages adds messages to the state.
func (s *MessagesState) AddMessages(msgs ...*Message) {
	s.Messages = append(s.Messages, msgs...)
}

// GetMessages returns all messages.
func (s *MessagesState) GetMessages() []*Message {
	return s.Messages
}

// GetLastMessage returns the last message.
func (s *MessagesState) GetLastMessage() *Message {
	if len(s.Messages) == 0 {
		return nil
	}
	return s.Messages[len(s.Messages)-1]
}

// GetMessagesByRole returns messages of a specific role.
func (s *MessagesState) GetMessagesByRole(role string) []*Message {
	filtered := make([]*Message, 0)
	for _, msg := range s.Messages {
		if msg.Role == role {
			filtered = append(filtered, msg)
		}
	}
	return filtered
}

// AddMessagesReducer 按 ID 去重/更新的消息合并 reducer。
// It performs deduplication based on message ID.
func AddMessagesReducer(existing interface{}, updates interface{}) (interface{}, error) {
	msgs, ok := updates.([]*Message)
	if !ok {
		// Try single message
		if msg, ok := updates.(*Message); ok {
			msgs = []*Message{msg}
		} else {
			return nil, &GraphError{Message: fmt.Sprintf("cannot add messages of type %T", updates)}
		}
	}

	if existing == nil {
		return msgs, nil
	}

	existingMsgs, ok := existing.([]*Message)
	if !ok {
		return nil, &GraphError{Message: fmt.Sprintf("existing messages is not []*Message, got %T", existing)}
	}

	// Create a map of existing messages by ID for quick lookup
	existingMap := make(map[string]*Message)
	for _, msg := range existingMsgs {
		if msg.ID != "" {
			existingMap[msg.ID] = msg
		}
	}

	// Process updates: update existing messages with same ID, append new ones
	result := make([]*Message, 0, len(existingMsgs)+len(msgs))
	// Keep track of which IDs have been processed
	processedIDs := make(map[string]bool)

	// First, add all existing messages, updating those that have updates
	for _, msg := range existingMsgs {
		if msg.ID == "" {
			// Messages without ID are always kept as-is
			result = append(result, msg)
			continue
		}
		// Check if there's an update for this ID
		var updated *Message
		for _, update := range msgs {
			if update.ID == msg.ID {
				updated = update
				break
			}
		}
		if updated != nil {
			result = append(result, updated)
			processedIDs[msg.ID] = true
		} else {
			result = append(result, msg)
		}
	}

	// Then, append new messages that don't have matching IDs in existing
	for _, msg := range msgs {
		if msg.ID == "" {
			// Messages without ID are always appended
			result = append(result, msg)
		} else if !processedIDs[msg.ID] && existingMap[msg.ID] == nil {
			// This is a new message with an ID not in existing
			result = append(result, msg)
		}
	}

	return result, nil
}

// MessageGraph 专用于聊天/消息工作流的图包装器。
// It automatically manages a messages channel with the AddMessages reducer.
type MessageGraph struct {
	graph           *stateGraph
	messagesChannel string
}

// NewMessageGraph 创建带 messages 通道的消息图。
func NewMessageGraph() *MessageGraph {
	// Create a simple state schema with messages field
	stateSchema := map[string]interface{}{
		"messages": []any{},
	}

	sg := NewStateGraph(stateSchema).(*stateGraph)

	// Register the messages channel so GetMessages works
	messagesChannel := "messages"
	sg.AddChannel(messagesChannel, channels.NewLastValue([]*Message{}))

	return &MessageGraph{
		graph:           sg,
		messagesChannel: messagesChannel,
	}
}

// AddNode adds a node to the message graph.
func (g *MessageGraph) AddNode(name string, action types.NodeFunc) *types.Node {
	return g.graph.AddNode(name, action)
}

// AddEdge adds a directed edge between nodes.
func (g *MessageGraph) AddEdge(startKey, endKey string) error {
	return g.graph.AddEdge(startKey, endKey)
}

// AddConditionalEdge adds a conditional edge.
func (g *MessageGraph) AddConditionalEdge(source string, condition types.EdgeFunc, edgeMap map[string]string) error {
	return g.graph.AddConditionalEdges(source, condition, edgeMap)
}

// SetEntryPoint sets the entry point node.
func (g *MessageGraph) SetEntryPoint(node string) error {
	return g.graph.SetEntryPoint(node)
}

// Build 编译并返回 CompiledGraph。
func (g *MessageGraph) Build() (types.CompiledGraph, error) {
	return g.graph.Compile()
}

// GetState returns the current state of the graph.
func (g *MessageGraph) GetState() map[string]interface{} {
	return map[string]interface{}{
		"messages": []any{},
	}
}

// GetMessages returns the messages channel value.
func (g *MessageGraph) GetMessages(ctx context.Context, channelRegistry *channels.Registry) ([]*Message, error) {
	if ch, ok := channelRegistry.Get(g.messagesChannel); ok {
		data, err := ch.Get()
		if err == nil && data != nil {
			if msgs, ok := data.([]*Message); ok {
				return msgs, nil
			}
		}
	}
	return []*Message{}, nil
}

// GetMessagesFromState 从状态 map 提取 []*Message。
func GetMessagesFromState(state map[string]interface{}) ([]*Message, error) {
	messages, ok := state["messages"]
	if !ok {
		return []*Message{}, nil
	}

	switch msgs := messages.(type) {
	case []*Message:
		return msgs, nil
	case []interface{}:
		result := make([]*Message, len(msgs))
		for i, m := range msgs {
			if msg, ok := m.(*Message); ok {
				result[i] = msg
			} else {
				return nil, &GraphError{Message: fmt.Sprintf("message at index %d is not *Message, got %T", i, m)}
			}
		}
		return result, nil
	default:
		return nil, &GraphError{Message: fmt.Sprintf("messages is not []*Message, got %T", messages)}
	}
}

// AddMessagesToState 向状态追加消息。
func AddMessagesToState(state map[string]interface{}, msgs ...*Message) error {
	existing, err := GetMessagesFromState(state)
	if err != nil {
		return err
	}

	result := make([]*Message, len(existing)+len(msgs))
	copy(result, existing)
	copy(result[len(existing):], msgs)
	state["messages"] = result
	return nil
}

// MessageRole 标准角色常量
const (
	MessageRoleUser      = "user"
	MessageRoleAssistant = "assistant"
	MessageRoleSystem    = "system"
	MessageRoleTool      = "tool"
	MessageRoleFunction  = "function"
)

// HumanMessage 构造 user 角色消息。
func HumanMessage(content string) *Message {
	return NewMessage(MessageRoleUser, content)
}

// AIMessage 构造 assistant 角色消息。
func AIMessage(content string) *Message {
	return NewMessage(MessageRoleAssistant, content)
}

// SystemMessage 构造 system 角色消息。
func SystemMessage(content string) *Message {
	return NewMessage(MessageRoleSystem, content)
}

// ToolMessage 构造 tool 角色消息并附加 tool_call_id。
func ToolMessage(content string, toolCallID string) *Message {
	msg := NewMessage(MessageRoleTool, content)
	if msg.Extra == nil {
		msg.Extra = make(map[string]interface{})
	}
	msg.Extra["tool_call_id"] = toolCallID
	return msg
}

// FunctionMessage 构造 function 角色消息并附加 name。
func FunctionMessage(content string, name string) *Message {
	msg := NewMessage(MessageRoleFunction, content)
	if msg.Extra == nil {
		msg.Extra = make(map[string]interface{})
	}
	msg.Extra["name"] = name
	return msg
}

// MessageHelper 消息格式化与检索工具集。
type MessageHelper struct {
}

// FormatMessages formats messages for display or logging.
func FormatMessages(msgs []*Message) []string {
	formatted := make([]string, len(msgs))
	for i, msg := range msgs {
		formatted[i] = fmt.Sprintf("%s: %s", msg.Role, msg.Content)
	}
	return formatted
}

// GetLastUserMessage returns the last user message.
func GetLastUserMessage(msgs []*Message) *Message {
	for i := len(msgs) - 1; i >= 0; i-- {
		if msgs[i].Role == MessageRoleUser {
			return msgs[i]
		}
	}
	return nil
}

// GetLastAIMessage returns the last assistant message.
func GetLastAIMessage(msgs []*Message) *Message {
	for i := len(msgs) - 1; i >= 0; i-- {
		if msgs[i].Role == MessageRoleAssistant {
			return msgs[i]
		}
	}
	return nil
}

// FilterMessagesByRole returns messages filtered by role.
func FilterMessagesByRole(msgs []*Message, roles ...string) []*Message {
	roleSet := make(map[string]bool)
	for _, role := range roles {
		roleSet[role] = true
	}

	filtered := make([]*Message, 0)
	for _, msg := range msgs {
		if roleSet[msg.Role] {
			filtered = append(filtered, msg)
		}
	}
	return filtered
}

// MessagesFilter 链式消息过滤器（角色/分页/谓词）。
type MessagesFilter struct {
	roles     []string
	limit     int
	offset    int
	reverse   bool
	predicate func(*Message) bool
}

// NewMessagesFilter creates a new messages filter.
func NewMessagesFilter() *MessagesFilter {
	return &MessagesFilter{}
}

// WithRole filters by message roles.
func (f *MessagesFilter) WithRole(roles ...string) *MessagesFilter {
	f.roles = roles
	return f
}

// WithLimit limits the number of messages.
func (f *MessagesFilter) WithLimit(limit int) *MessagesFilter {
	f.limit = limit
	return f
}

// WithOffset skips the first offset messages.
func (f *MessagesFilter) WithOffset(offset int) *MessagesFilter {
	f.offset = offset
	return f
}

// WithReverse reverses the message order.
func (f *MessagesFilter) WithReverse() *MessagesFilter {
	f.reverse = true
	return f
}

// WithPredicate adds a custom predicate function.
func (f *MessagesFilter) WithPredicate(predicate func(*Message) bool) *MessagesFilter {
	f.predicate = predicate
	return f
}

// Filter applies the filter to the messages.
func (f *MessagesFilter) Filter(msgs []*Message) []*Message {
	result := make([]*Message, 0)

	for _, msg := range msgs {
		// Check role filter
		if len(f.roles) > 0 {
			if !slices.Contains(f.roles, msg.Role) {
				continue
			}
		}

		// Check predicate
		if f.predicate != nil && !f.predicate(msg) {
			continue
		}

		result = append(result, msg)
	}

	// Apply offset
	if f.offset > 0 && f.offset < len(result) {
		result = result[f.offset:]
	}

	// Apply limit
	if f.limit > 0 && f.limit < len(result) {
		result = result[:f.limit]
	}

	// Apply reverse
	if f.reverse {
		for i, j := 0, len(result)-1; i < j; i, j = i+1, j-1 {
			result[i], result[j] = result[j], result[i]
		}
	}

	return result
}

// GraphError 图模块通用错误类型。
type GraphError struct {
	Message string
	Code    string
}

func (e *GraphError) Error() string {
	if e.Code != "" {
		return e.Code + ": " + e.Message
	}
	return e.Message
}

// OpenAI Chat Completions 格式互转

// OpenAIChatMessage OpenAI 聊天 API 消息结构。
type OpenAIChatMessage struct {
	Role    string `json:"role"`
	Content string `json:"content"`
	Name    string `json:"name,omitempty"`
	// Additional fields like function_call, tool_calls can be added as needed
}

// ToOpenAIChatMessage 转为 OpenAI 单条消息。
func (m *Message) ToOpenAIChatMessage() *OpenAIChatMessage {
	return &OpenAIChatMessage{
		Role:    m.Role,
		Content: m.Content,
		// Name can be extracted from Extra if needed
	}
}

// MessagesToOpenAIFormat 批量转为 OpenAI 消息数组。
func MessagesToOpenAIFormat(messages []*Message) []OpenAIChatMessage {
	result := make([]OpenAIChatMessage, len(messages))
	for i, msg := range messages {
		result[i] = OpenAIChatMessage{
			Role:    msg.Role,
			Content: msg.Content,
		}
	}
	return result
}

// OpenAIFormatToMessages 从 OpenAI 格式还原为 Message 列表。
func OpenAIFormatToMessages(openaiMessages []OpenAIChatMessage) []*Message {
	result := make([]*Message, len(openaiMessages))
	for i, msg := range openaiMessages {
		result[i] = &Message{
			ID:      "", // ID will need to be generated or preserved separately
			Role:    msg.Role,
			Content: msg.Content,
			Extra:   make(map[string]interface{}),
		}
		if msg.Name != "" {
			result[i].Extra["name"] = msg.Name
		}
	}
	return result
}

// AddMessagesReducer：同 ID 更新、无 ID 追加、保留无 ID 历史消息。
