// Package events 为 Agent 执行提供仅追加（append-only）的事件溯源能力。
//
// 每次工具调用、状态迁移、记忆写入、审批、LLM 调用与检查点操作
// 均记录为不可变 Event。事件通过单调逻辑时钟因果排序，
// 支持确定性重放、分叉对比与事后分析。
package events

import (
	"crypto/sha256"
	"encoding/json"
	"fmt"
	"time"
)

// EventID 全局唯一事件标识（UUID v7，时间有序）。
type EventID string

// EventType 枚举 Agent 执行期间可记录的各类动作。
type EventType string

const (
	// 图执行生命周期
	EventGraphStart EventType = "graph.start"
	EventGraphEnd   EventType = "graph.end"
	EventStepStart  EventType = "step.start"
	EventStepEnd    EventType = "step.end"

	// 节点执行
	EventNodeStart EventType = "node.start"
	EventNodeEnd   EventType = "node.end"

	// 状态迁移
	EventStateRead  EventType = "state.read"
	EventStateWrite EventType = "state.write"

	// 工具调用
	EventToolCallStart  EventType = "tool.call.start"
	EventToolCallResult EventType = "tool.call.result"
	EventToolCallError  EventType = "tool.call.error"

	// LLM 调用
	EventLLMCallStart EventType = "llm.call.start"
	EventLLMCallChunk EventType = "llm.call.chunk"
	EventLLMCallEnd   EventType = "llm.call.end"

	// 记忆操作
	EventMemoryRead  EventType = "memory.read"
	EventMemoryWrite EventType = "memory.write"

	// 人机协同（Human-in-the-loop）
	EventApprovalRequest EventType = "approval.request"
	EventApprovalGranted EventType = "approval.granted"
	EventApprovalDenied  EventType = "approval.denied"

	// 检查点
	EventCheckpointCreated  EventType = "checkpoint.created"
	EventCheckpointRestored EventType = "checkpoint.restored"

	// 中断 / 恢复
	EventInterrupt EventType = "interrupt"
	EventResume    EventType = "resume"

	// 错误与重试
	EventError EventType = "error"
	EventRetry EventType = "retry"

	// 分叉 —— 从已有事件分支
	EventFork EventType = "fork"

	// 子 Agent 执行
	EventSubAgentCallStart EventType = "subagent.call.start"
	EventSubAgentCallEnd   EventType = "subagent.call.end"

	// 会话 / 转移
	EventSessionValueSet EventType = "session.value.set"
	EventSessionTransfer EventType = "session.transfer"
)

// Event 不可变的仅追加事件记录。
type Event struct {
	// ID 全局唯一事件标识
	ID EventID `json:"id"`
	// Type 事件类型，描述发生了什么
	Type EventType `json:"type"`
	// Timestamp 记录时的墙钟时间
	Timestamp time.Time `json:"timestamp"`
	// Clock 单调逻辑时钟值，提供全局全序
	Clock uint64 `json:"clock"`

	// TraceID 标识一次完整执行轨迹
	TraceID string `json:"trace_id"`
	// ParentID 同轨迹中直接前驱事件
	ParentID EventID `json:"parent_id,omitempty"`
	// CausedBy 前驱事件列表（分叉/汇合场景可有多个）
	CausedBy []EventID `json:"caused_by,omitempty"`

	// ThreadID 执行线程标识
	ThreadID string `json:"thread_id,omitempty"`
	// Step Pregel 超步编号
	Step int `json:"step,omitempty"`
	// Node 图节点名称
	Node string `json:"node,omitempty"`
	// TaskID 执行任务标识
	TaskID string `json:"task_id,omitempty"`

	// Payload 类型相关的 JSON 载荷
	Payload json.RawMessage `json:"payload,omitempty"`
	// Metadata 任意键值元数据
	Metadata map[string]any `json:"metadata,omitempty"`

	// Deterministic 为 false 表示含非确定性操作（LLM 输出、随机数、墙钟时间）
	Deterministic bool `json:"deterministic"`
	// Hash Payload+Metadata 的 SHA-256，用于完整性校验
	Hash string `json:"hash,omitempty"`
}

// NewEvent 创建新事件，自动生成 ID 并填入当前时间戳。
func NewEvent(typ EventType, clock uint64) *Event {
	return &Event{
		ID:        EventID(fmt.Sprintf("evt-%d-%x", clock, time.Now().UnixNano())),
		Type:      typ,
		Timestamp: time.Now(),
		Clock:     clock,
		Metadata:  make(map[string]any),
	}
}

// computeHash 计算事件载荷与元数据的 SHA-256 哈希。
func (e *Event) computeHash() string {
	h := sha256.New()
	if e.Payload != nil {
		h.Write(e.Payload)
	}
	if e.Metadata != nil {
		meta, _ := json.Marshal(e.Metadata)
		h.Write(meta)
	}
	return fmt.Sprintf("%x", h.Sum(nil))
}

// Seal 终结事件：计算哈希并标记为不可变。
func (e *Event) Seal() {
	e.Hash = e.computeHash()
}

// ---- 类型化载荷 ----

// ToolCallPayload 工具调用事件的载荷。
type ToolCallPayload struct {
	ToolName   string         `json:"tool_name"`
	Arguments  map[string]any `json:"arguments,omitempty"`
	Result     any            `json:"result,omitempty"`
	DurationMs int64          `json:"duration_ms,omitempty"`
	Error      string         `json:"error,omitempty"`
	RetryCount int            `json:"retry_count,omitempty"`
}

// LLMCallPayload LLM 调用事件的载荷。
type LLMCallPayload struct {
	Model      string     `json:"model"`
	Provider   string     `json:"provider,omitempty"`
	Messages   []any      `json:"messages,omitempty"`
	Tokens     TokenUsage `json:"tokens,omitempty"`
	Content    string     `json:"content,omitempty"`
	Chunks     int        `json:"chunks,omitempty"`
	DurationMs int64      `json:"duration_ms,omitempty"`
	Cost       float64    `json:"cost,omitempty"`
}

// TokenUsage 记录 LLM 调用的 Token 消耗。
type TokenUsage struct {
	PromptTokens     int `json:"prompt_tokens"`
	CompletionTokens int `json:"completion_tokens"`
	TotalTokens      int `json:"total_tokens"`
}

// StateTransitionPayload 状态变更事件的载荷。
type StateTransitionPayload struct {
	Channel  string `json:"channel"`
	OldValue any    `json:"old_value,omitempty"`
	NewValue any    `json:"new_value"`
	Reducer  string `json:"reducer,omitempty"`
}

// MemoryWritePayload 记忆操作事件的载荷。
type MemoryWritePayload struct {
	Store     string  `json:"store"`
	Operation string  `json:"operation"`
	Key       string  `json:"key,omitempty"`
	Value     any     `json:"value,omitempty"`
	Score     float64 `json:"score,omitempty"`
}

// ApprovalPayload 审批事件的载荷。
type ApprovalPayload struct {
	RequestID string `json:"request_id"`
	Action    string `json:"action"`
	Context   any    `json:"context,omitempty"`
	Decision  string `json:"decision,omitempty"`
	LatencyMs int64  `json:"latency_ms,omitempty"`
}

// SubAgentCallPayload 子 Agent 调用事件的载荷。
type SubAgentCallPayload struct {
	SubAgentName string `json:"sub_agent_name"`
	Input        any    `json:"input,omitempty"`
	Output       any    `json:"output,omitempty"`
	Depth        int    `json:"depth,omitempty"`
	DurationMs   int64  `json:"duration_ms,omitempty"`
	Error        string `json:"error,omitempty"`
}

// SessionValuePayload 会话值事件的载荷。
type SessionValuePayload struct {
	Key   string `json:"key"`
	Value any    `json:"value,omitempty"`
}

// SessionTransferPayload 会话转移事件的载荷。
type SessionTransferPayload struct {
	FromAgent string `json:"from_agent"`
	ToAgent   string `json:"to_agent"`
	Reason    string `json:"reason,omitempty"`
	Input     any    `json:"input,omitempty"`
}
