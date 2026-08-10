package events

import (
	"context"
	"encoding/json"

	"ragflow/internal/harness/graph/pregel"
)

// recorder.go — 事件记录器：将图执行、LLM/工具调用等写入仅追加事件日志。

// ---- 通过 context 传递 EventRecorder ----

type recorderContextKey struct{}

// ContextWithRecorder 将 EventRecorder 存入 context，供模型包装器与工具中间件使用。
func ContextWithRecorder(ctx context.Context, r *EventRecorder) context.Context {
	return context.WithValue(ctx, recorderContextKey{}, r)
}

// RecorderFromContext 从 context 取出 EventRecorder；不存在时返回 nil。
func RecorderFromContext(ctx context.Context) *EventRecorder {
	r, _ := ctx.Value(recorderContextKey{}).(*EventRecorder)
	return r
}

// RecorderOption 配置 EventRecorder 的可选参数。
type RecorderOption func(*recorderOptions)

type recorderOptions struct {
	traceID  string
	threadID string
}

// WithTraceID 设置记录器的轨迹 ID。
func WithTraceID(traceID string) RecorderOption {
	return func(o *recorderOptions) {
		o.traceID = traceID
	}
}

// WithThreadID 设置记录器的线程 ID。
func WithThreadID(threadID string) RecorderOption {
	return func(o *recorderOptions) {
		o.threadID = threadID
	}
}

// EventRecorder 将图执行过程记录为仅追加 Event，实现 pregel.GraphCallback。
// 另提供 RecordModelCall / RecordToolCall 等细粒度 API 记录 LLM 与工具调用。
//
// 用法示例：
//
//	store := events.NewMemoryEventStore()
//	recorder := events.NewEventRecorder(store, events.WithTraceID("trace-001"))
//	cb := pregel.NewCallbackManager()
//	cb.AddCallback(recorder)
type EventRecorder struct {
	store    EventLog
	clock    *LogicalClock
	traceID  string
	threadID string
}

// NewEventRecorder 创建事件记录器。
func NewEventRecorder(store EventLog, opts ...RecorderOption) *EventRecorder {
	o := &recorderOptions{}
	for _, opt := range opts {
		opt(o)
	}
	return &EventRecorder{
		store:    store,
		clock:    NewLogicalClock(),
		traceID:  o.traceID,
		threadID: o.threadID,
	}
}

// record 创建事件并追加到存储。
func (r *EventRecorder) record(ctx context.Context, typ EventType, opts ...func(*Event)) {
	ev := NewEvent(typ, r.clock.Tick())
	ev.TraceID = r.traceID
	ev.ThreadID = r.threadID
	for _, fn := range opts {
		fn(ev)
	}
	ev.Seal()
	_ = r.store.Append(ctx, ev)
}

// ---- 基于 context 的细粒度记录（模型/工具包装器调用） ----

// RecordModelCall 记录 LLM 模型调用及结果。
func (r *EventRecorder) RecordModelCall(ctx context.Context, model, provider string, messages []any, content string, tokens TokenUsage, durationMs int64, cost float64) {
	r.record(ctx, EventLLMCallStart, func(ev *Event) {
		ev.Deterministic = false
		ev.Metadata["model"] = model
		ev.Metadata["provider"] = provider
	})
	r.record(ctx, EventLLMCallEnd, func(ev *Event) {
		ev.Deterministic = false
		pl := LLMCallPayload{
			Model:      model,
			Provider:   provider,
			Messages:   messages,
			Tokens:     tokens,
			Content:    content,
			DurationMs: durationMs,
			Cost:       cost,
		}
		ev.Payload, _ = json.Marshal(pl)
	})
}

// RecordLLMChunk 记录 LLM 流式输出的单个分块。
func (r *EventRecorder) RecordLLMChunk(ctx context.Context, model string, chunk string) {
	r.record(ctx, EventLLMCallChunk, func(ev *Event) {
		ev.Deterministic = false
		ev.Metadata["model"] = model
		ev.Metadata["chunk"] = chunk
	})
}

// RecordToolCall 记录工具调用及结果。
func (r *EventRecorder) RecordToolCall(ctx context.Context, toolName string, arguments map[string]any, result any, durationMs int64, retryCount int, errStr string) {
	r.record(ctx, EventToolCallStart, func(ev *Event) {
		ev.Metadata["tool"] = toolName
	})
	r.record(ctx, EventToolCallResult, func(ev *Event) {
		pl := ToolCallPayload{
			ToolName:   toolName,
			Arguments:  arguments,
			Result:     result,
			DurationMs: durationMs,
			RetryCount: retryCount,
			Error:      errStr,
		}
		ev.Payload, _ = json.Marshal(pl)
		if errStr != "" {
			ev.Deterministic = false
		}
	})
}

// RecordSubAgentCall 记录子 Agent 调用及结果。
func (r *EventRecorder) RecordSubAgentCall(ctx context.Context, subAgentName string, input, output any, depth int, durationMs int64, errStr string) {
	r.record(ctx, EventSubAgentCallStart, func(ev *Event) {
		pl := SubAgentCallPayload{
			SubAgentName: subAgentName,
			Input:        input,
			Depth:        depth,
		}
		ev.Payload, _ = json.Marshal(pl)
	})
	r.record(ctx, EventSubAgentCallEnd, func(ev *Event) {
		pl := SubAgentCallPayload{
			SubAgentName: subAgentName,
			Output:       output,
			Depth:        depth,
			DurationMs:   durationMs,
			Error:        errStr,
		}
		ev.Payload, _ = json.Marshal(pl)
		if errStr != "" {
			ev.Deterministic = false
		}
	})
}

// RecordSessionValue 记录会话键值变更。
func (r *EventRecorder) RecordSessionValue(ctx context.Context, key string, value any) {
	r.record(ctx, EventSessionValueSet, func(ev *Event) {
		pl := SessionValuePayload{Key: key, Value: value}
		ev.Payload, _ = json.Marshal(pl)
	})
}

// RecordSessionTransfer 记录 Agent 间会话转移。
func (r *EventRecorder) RecordSessionTransfer(ctx context.Context, fromAgent, toAgent, reason string, input any) {
	r.record(ctx, EventSessionTransfer, func(ev *Event) {
		pl := SessionTransferPayload{
			FromAgent: fromAgent,
			ToAgent:   toAgent,
			Reason:    reason,
			Input:     input,
		}
		ev.Payload, _ = json.Marshal(pl)
	})
}

// RecordStateWrite 记录状态通道写入。
func (r *EventRecorder) RecordStateWrite(ctx context.Context, channel string, oldValue, newValue any, reducer string) {
	r.record(ctx, EventStateWrite, func(ev *Event) {
		pl := StateTransitionPayload{
			Channel:  channel,
			OldValue: oldValue,
			NewValue: newValue,
			Reducer:  reducer,
		}
		ev.Payload, _ = json.Marshal(pl)
	})
}

// RecordMemoryWrite 记录记忆写入操作。
func (r *EventRecorder) RecordMemoryWrite(ctx context.Context, store, operation, key string, value any, score float64) {
	r.record(ctx, EventMemoryWrite, func(ev *Event) {
		pl := MemoryWritePayload{
			Store:     store,
			Operation: operation,
			Key:       key,
			Value:     value,
			Score:     score,
		}
		ev.Payload, _ = json.Marshal(pl)
	})
}

// RecordMemoryRead 记录记忆读取操作。
func (r *EventRecorder) RecordMemoryRead(ctx context.Context, store, key string, score float64) {
	r.record(ctx, EventMemoryRead, func(ev *Event) {
		pl := MemoryWritePayload{
			Store: store,
			Key:   key,
			Score: score,
		}
		ev.Payload, _ = json.Marshal(pl)
	})
}

// RecordApproval 记录人机协同审批事件。
func (r *EventRecorder) RecordApproval(ctx context.Context, requestID, action string, context any, decision string, latencyMs int64) {
	r.record(ctx, EventApprovalRequest, func(ev *Event) {
		pl := ApprovalPayload{
			RequestID: requestID,
			Action:    action,
			Context:   context,
			Decision:  decision,
			LatencyMs: latencyMs,
		}
		ev.Payload, _ = json.Marshal(pl)
	})
}

// RecordError 记录执行错误。
func (r *EventRecorder) RecordError(ctx context.Context, errMsg string) {
	r.record(ctx, EventError, func(ev *Event) {
		ev.Metadata["error"] = errMsg
	})
}

// RecordRetry 记录重试事件。
func (r *EventRecorder) RecordRetry(ctx context.Context, detail string) {
	r.record(ctx, EventRetry, func(ev *Event) {
		ev.Metadata["detail"] = detail
	})
}

// ---- GraphCallback 实现 ----

// OnRunStart 图运行开始回调。
func (r *EventRecorder) OnRunStart(ctx context.Context, graphName, threadID string) {
	r.record(ctx, EventGraphStart, func(ev *Event) {
		ev.Metadata["graph_name"] = graphName
	})
}

// OnRunEnd 图运行结束回调。
func (r *EventRecorder) OnRunEnd(ctx context.Context, graphName, threadID string, err error) {
	r.record(ctx, EventGraphEnd, func(ev *Event) {
		ev.Metadata["graph_name"] = graphName
		if err != nil {
			ev.Metadata["error"] = err.Error()
		}
	})
}

// OnStepStart Pregel 超步开始回调。
func (r *EventRecorder) OnStepStart(ctx context.Context, step, taskCount int) {
	r.record(ctx, EventStepStart, func(ev *Event) {
		ev.Step = step
		ev.Metadata["task_count"] = taskCount
	})
}

// OnStepEnd Pregel 超步结束回调。
func (r *EventRecorder) OnStepEnd(ctx context.Context, step int, err error) {
	r.record(ctx, EventStepEnd, func(ev *Event) {
		ev.Step = step
		if err != nil {
			ev.Metadata["error"] = err.Error()
		}
	})
}

// OnNodeStart 节点执行开始回调。
func (r *EventRecorder) OnNodeStart(ctx context.Context, nodeName string, step int) {
	r.record(ctx, EventNodeStart, func(ev *Event) {
		ev.Node = nodeName
		ev.Step = step
	})
}

// OnNodeEnd 节点执行结束回调。
func (r *EventRecorder) OnNodeEnd(ctx context.Context, nodeName string, step int, output interface{}, err error) {
	r.record(ctx, EventNodeEnd, func(ev *Event) {
		ev.Node = nodeName
		ev.Step = step
		if err != nil {
			ev.Metadata["error"] = err.Error()
		}
	})
}

// OnCheckpointSave 检查点保存回调。
func (r *EventRecorder) OnCheckpointSave(ctx context.Context, threadID, checkpointID string, step int) {
	r.record(ctx, EventCheckpointCreated, func(ev *Event) {
		ev.ThreadID = threadID
		ev.Step = step
		ev.Metadata["checkpoint_id"] = checkpointID
	})
}

// OnCheckpointLoad 检查点加载回调。
func (r *EventRecorder) OnCheckpointLoad(ctx context.Context, threadID, checkpointID string, step int) {
	r.record(ctx, EventCheckpointRestored, func(ev *Event) {
		ev.ThreadID = threadID
		ev.Step = step
		ev.Metadata["checkpoint_id"] = checkpointID
	})
}

// OnCheckpointUpdate 检查点更新回调。
func (r *EventRecorder) OnCheckpointUpdate(ctx context.Context, threadID, asNode string) {
	r.record(ctx, EventStateWrite, func(ev *Event) {
		ev.ThreadID = threadID
		ev.Node = asNode
	})
}

// OnInterrupt 中断回调。
func (r *EventRecorder) OnInterrupt(ctx context.Context, nodeNames []string, step int) {
	r.record(ctx, EventInterrupt, func(ev *Event) {
		ev.Step = step
		ev.Metadata["interrupt_nodes"] = nodeNames
	})
}

// OnResume 恢复执行回调。
func (r *EventRecorder) OnResume(ctx context.Context, threadID string) {
	r.record(ctx, EventResume, func(ev *Event) {
		ev.ThreadID = threadID
	})
}

// 编译期接口检查
var (
	_ pregel.GraphCallback = (*EventRecorder)(nil)
)
