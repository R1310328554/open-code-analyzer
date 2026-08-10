package core

// recorder_model.go — 模型调用事件记录：WrapModel 包装 Model，将 Generate 调用写入 context 中的 EventRecorder。


import (
	"context"
	"time"

	"ragflow/internal/harness/core/schema"
	"ragflow/internal/harness/events"
)

// ---- 模型包装器：经 context EventRecorder 记录 LLM 调用 ----

// eventRecorderModelWrapper 包装 Model，记录每次 Generate。
// 通过 events.RecorderFromContext 获取 Recorder。
type eventRecorderModelWrapper[M MessageType] struct {
	inner Model[M]
}

func wrapModelWithEventRecorder[M MessageType](inner Model[M]) Model[M] {
	return &eventRecorderModelWrapper[M]{inner: inner}
}

func (w *eventRecorderModelWrapper[M]) Generate(ctx context.Context, msgs []M, opts ...ModelOption) (M, error) {
	start := time.Now()
	resp, err := w.inner.Generate(ctx, msgs, opts...)
	durMs := time.Since(start).Milliseconds()
	rec := events.RecorderFromContext(ctx)
	if rec != nil && err == nil {
		var msgsAny []any
		for _, m := range msgs {
			msgsAny = append(msgsAny, any(m))
		}
		// 此处无法获取模型名时记为 "unknown"。
		// 模型名可通过 context 传递，后续迭代可改进。
		// 当前仅记录消息、输出内容与耗时。
		rec.RecordModelCall(ctx, "unknown", "", msgsAny, contentOf(resp), events.TokenUsage{}, durMs, 0)
	}
	return resp, err
}

func (w *eventRecorderModelWrapper[M]) Stream(ctx context.Context, msgs []M, opts ...ModelOption) (*schema.StreamReader[M], error) {
	return w.inner.Stream(ctx, msgs, opts...)
}

func (w *eventRecorderModelWrapper[M]) BindTools(tools []*schema.ToolInfo) error {
	return w.inner.BindTools(tools)
}

// ---- 经 WrapModel 注入包装器的中间件处理器 ----

type eventRecorderModelHandler[M MessageType] struct{}

// NewEventRecorderModelWrapper 创建记录模型调用的中间件。
// context 无 Recorder 时透传原 Model。
// Usage:
//
//	recorder := events.NewEventRecorder(store)
//	ctx := events.ContextWithRecorder(ctx, recorder)
//	cfg := &ReActConfig[*schema.Message]{
//	    Model: model,
//	    Handlers: []TypedReActMiddleware[*schema.Message]{
//	        NewEventRecorderModelWrapper[*schema.Message](),
//	    },
//	}
func NewEventRecorderModelWrapper[M MessageType]() *eventRecorderModelHandler[M] {
	return &eventRecorderModelHandler[M]{}
}

func (h *eventRecorderModelHandler[M]) WrapModel(ctx context.Context, m Model[M], mc *TypedModelContext[M]) (Model[M], error) {
	rec := events.RecorderFromContext(ctx)
	if rec == nil {
		return m, nil // no recorder in context — pass through
	}
	return wrapModelWithEventRecorder(m), nil
}

func (h *eventRecorderModelHandler[M]) BeforeAgent(ctx context.Context, rc *ReActAgentContext) (context.Context, *ReActAgentContext, error) {
	return ctx, rc, nil
}
func (h *eventRecorderModelHandler[M]) AfterAgent(ctx context.Context, state *TypedReActAgentState[M]) (context.Context, error) {
	return ctx, nil
}
func (h *eventRecorderModelHandler[M]) BeforeModelRewrite(ctx context.Context, st *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error) {
	return ctx, st, nil
}
func (h *eventRecorderModelHandler[M]) AfterModelRewrite(ctx context.Context, st *TypedReActAgentState[M], mc *TypedModelContext[M]) (context.Context, *TypedReActAgentState[M], error) {
	return ctx, st, nil
}

// contentOf 从响应消息提取文本内容。
func contentOf[M MessageType](resp M) string {
	if msg, ok := any(resp).(*schema.Message); ok && msg != nil {
		return msg.Content
	}
	if am, ok := any(resp).(*schema.AgenticMessage); ok && am != nil {
		return am.Content
	}
	return ""
}

// 使用前需 events.ContextWithRecorder(ctx, recorder) 注入 Recorder。
