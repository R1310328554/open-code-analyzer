package agent

import (
	"context"
	"errors"

	"github.com/ollama/ollama/api"
)

// EventType 表示智能体运行期间发出的流式事件类型。
type EventType string

const (
	// 事件类型常量：消息增量、工具生命周期、压缩与运行结束等。
	EventMessageDelta       EventType = "message_delta"
	EventThinkingDelta      EventType = "thinking_delta"
	EventToolCallDetected   EventType = "tool_call_detected"
	EventToolStarted        EventType = "tool_started"
	EventToolFinished       EventType = "tool_finished"
	EventCompactionStarted  EventType = "compaction_started"
	EventCompactionProgress EventType = "compaction_progress"
	EventCompacted          EventType = "compacted"
	EventCompactionSkipped  EventType = "compaction_skipped"
	EventRunFinished        EventType = "run_finished"
	EventError              EventType = "error"
)

// ToolStatus is the typed lifecycle state for a tool call, carried on
// Event.ToolStatus for tool events.
type ToolStatus string

// ToolStatus 表示工具调用的生命周期状态，出现在工具相关事件的 ToolStatus 字段。

const (
	ToolStatusRunning  ToolStatus = "running"
	ToolStatusDone     ToolStatus = "done"
	ToolStatusFailed   ToolStatus = "failed"
	ToolStatusDenied   ToolStatus = "denied"
	ToolStatusDisabled ToolStatus = "disabled"
	ToolStatusSkipped  ToolStatus = "skipped"
)

// RunStatus is the typed terminal outcome of a run, carried on Event.Status for
// run_finished events.
type RunStatus string

// RunStatus 表示运行的终态结果，出现在 run_finished 事件的 Status 字段。

const (
	RunStatusDone     RunStatus = "done"
	RunStatusDenied   RunStatus = "denied"
	RunStatusCanceled RunStatus = "canceled"
)

// CompactionTrigger is the typed reason a compaction ran or was attempted,
// carried on Event.CompactionTrigger for compaction events.
type CompactionTrigger string

// CompactionTrigger 表示触发压缩的原因，出现在压缩相关事件。

const (
	CompactionTriggerForce      CompactionTrigger = "force"
	CompactionTriggerPromptEval CompactionTrigger = "prompt_eval"
	CompactionTriggerEstimate   CompactionTrigger = "estimate"
	CompactionTriggerToolOutput CompactionTrigger = "tool_output"
	CompactionTriggerError      CompactionTrigger = "error"
	CompactionTriggerDue        CompactionTrigger = "due"
)

// Event 是面向客户端的结构化流式事件载荷。
type Event struct {
	Type              EventType         `json:"type"`
	RunID             string            `json:"runId,omitempty"`
	ChatID            string            `json:"chatId,omitempty"`
	Model             string            `json:"model,omitempty"`
	Status            RunStatus         `json:"status,omitempty"`
	ToolStatus        ToolStatus        `json:"toolStatus,omitempty"`
	CompactionTrigger CompactionTrigger `json:"compactionTrigger,omitempty"`
	ToolCallID        string            `json:"toolCallId,omitempty"`
	ToolName          string            `json:"toolName,omitempty"`
	WorkingDir        string            `json:"workingDir,omitempty"`
	Content           string            `json:"content,omitempty"`
	Thinking          string            `json:"thinking,omitempty"`
	ToolCalls         []api.ToolCall    `json:"toolCalls,omitempty"`
	Messages          []api.Message     `json:"messages,omitempty"`
	Args              map[string]any    `json:"args,omitempty"`
	Tokens            int               `json:"tokens,omitempty"`
	Error             string            `json:"error,omitempty"`
}

// EventSink 接收并处理智能体事件。
type EventSink interface {
	Emit(Event) error
}

// EventSinkFunc 使函数类型满足 EventSink 接口。
type EventSinkFunc func(Event) error

func (fn EventSinkFunc) Emit(event Event) error {
	if fn == nil {
		return nil
	}
	return fn(event)
}

// eventMetadata carries the run identification fields shared by all events.
type eventMetadata struct {
	// eventMetadata 携带各事件共享的运行标识字段。
	runID  string
	chatID string
	model  string
}

// newEventMetadata 从运行 ID 与选项构造事件元数据。
func newEventMetadata(runID string, opts RunOptions) eventMetadata {
	return eventMetadata{runID: runID, chatID: opts.ChatID, model: opts.Model}
}

func newMessageDelta(m eventMetadata, content string) Event {
	return Event{Type: EventMessageDelta, RunID: m.runID, ChatID: m.chatID, Model: m.model, Content: content}
}

func newThinkingDelta(m eventMetadata, thinking string) Event {
	return Event{Type: EventThinkingDelta, RunID: m.runID, ChatID: m.chatID, Model: m.model, Thinking: thinking}
}

func newToolCallDetected(m eventMetadata, calls []api.ToolCall) Event {
	return Event{Type: EventToolCallDetected, RunID: m.runID, ChatID: m.chatID, Model: m.model, ToolCalls: calls}
}

func newToolStarted(m eventMetadata, callID, toolName, workingDir string, args map[string]any) Event {
	return Event{Type: EventToolStarted, RunID: m.runID, ChatID: m.chatID, Model: m.model, ToolStatus: ToolStatusRunning, ToolCallID: callID, ToolName: toolName, WorkingDir: workingDir, Args: args}
}

func newToolFinished(m eventMetadata, status ToolStatus, callID, toolName, workingDir string, args map[string]any, content, errMsg string) Event {
	ev := Event{Type: EventToolFinished, RunID: m.runID, ChatID: m.chatID, Model: m.model, ToolStatus: status, ToolCallID: callID, ToolName: toolName, WorkingDir: workingDir, Args: args, Content: content}
	if errMsg != "" {
		ev.Error = errMsg
	}
	return ev
}

func newRunFinished(m eventMetadata, status RunStatus) Event {
	return Event{Type: EventRunFinished, RunID: m.runID, ChatID: m.chatID, Model: m.model, Status: status}
}

func newErrorEvent(m eventMetadata, errMsg string) Event {
	return Event{Type: EventError, RunID: m.runID, ChatID: m.chatID, Model: m.model, Error: errMsg}
}

func newCompactionProgress(m eventMetadata, tokens int) Event {
	return Event{Type: EventCompactionProgress, RunID: m.runID, ChatID: m.chatID, Model: m.model, Tokens: tokens}
}

func newCompactionStarted(m eventMetadata, trigger CompactionTrigger) Event {
	return Event{Type: EventCompactionStarted, RunID: m.runID, ChatID: m.chatID, Model: m.model, CompactionTrigger: trigger}
}

func newCompactionSkipped(m eventMetadata, trigger CompactionTrigger, content string) Event {
	return Event{Type: EventCompactionSkipped, RunID: m.runID, ChatID: m.chatID, Model: m.model, CompactionTrigger: trigger, Content: content}
}

func newCompacted(m eventMetadata, messages []api.Message, trigger CompactionTrigger, content string) Event {
	return Event{Type: EventCompacted, RunID: m.runID, ChatID: m.chatID, Model: m.model, CompactionTrigger: trigger, Content: content, Messages: messages}
}

// emit 向所有已注册事件接收器广播事件。
func (s *Session) emit(event Event) error {
	if s == nil {
		return nil
	}
	var errs []error
	for _, sink := range s.EventSinks {
		if sink == nil {
			continue
		}
		if err := sink.Emit(event); err != nil {
			errs = append(errs, err)
		}
	}
	return errors.Join(errs...)
}

// emitIgnoringCanceled 发送事件；取消导致的 sink 关闭不算失败。
func (s *Session) emitIgnoringCanceled(ctx context.Context, event Event) error {
	err := s.emit(event)
	if err != nil && ctx != nil && ctx.Err() != nil {
		//nolint:nilerr // Event sinks may close during cancellation; cancellation is not a user-facing emit failure.
		return nil
	}
	return err
}
