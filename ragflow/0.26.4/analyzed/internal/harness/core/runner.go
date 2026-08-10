package core

// runner.go — 智能体执行入口：TypedRunner 封装 Run/Query/Resume 与检查点迭代器。


import (
	"context"
	"errors"
	"fmt"

	"ragflow/internal/harness/core/schema"
)

// TypedRunner 智能体执行的主入口，封装 Run/Query/Resume 与流式事件迭代。
type TypedRunner[M MessageType] struct {
	a               TypedAgent[M]
	enableStreaming bool
	store           CheckPointStore
}

// Runner 默认 Message 类型的 Runner 别名。
type Runner = TypedRunner[*schema.Message]

// RunnerConfig 创建 Runner 时的配置。
type RunnerConfig[M MessageType] struct {
	Agent           TypedAgent[M]
	EnableStreaming bool
	CheckPointStore CheckPointStore
}

// ResumeParams 恢复执行时传入的目标数据。
type ResumeParams struct{ Targets map[string]any }

// NewRunner 创建默认 Message 类型的 Runner。
func NewRunner(ctx context.Context, conf RunnerConfig[*schema.Message]) *Runner {
	return NewTypedRunner[*schema.Message](conf)
}

// NewTypedRunner 创建泛型 TypedRunner。
func NewTypedRunner[M MessageType](conf RunnerConfig[M]) *TypedRunner[M] {
	return &TypedRunner[M]{a: conf.Agent, enableStreaming: conf.EnableStreaming, store: conf.CheckPointStore}
}

// Run 以给定消息启动智能体并返回异步事件迭代器。
func (r *TypedRunner[M]) Run(ctx context.Context, msgs []M, opts ...RunOption) *AsyncIterator[*TypedAgentEvent[M]] {
	return runImpl(r.a, r.enableStreaming, r.store, ctx, msgs, opts...)
}

// Query 将用户查询包装为单条消息后执行 Run。
func (r *TypedRunner[M]) Query(ctx context.Context, query string, opts ...RunOption) *AsyncIterator[*TypedAgentEvent[M]] {
	msgs, err := newUserMsg[M](query)
	if err != nil {
		return errorIter[M](err)
	}
	return r.Run(ctx, []M{msgs}, opts...)
}

// Resume 从检查点 ID 恢复中断的执行。
func (r *TypedRunner[M]) Resume(ctx context.Context, cid string, opts ...RunOption) (*AsyncIterator[*TypedAgentEvent[M]], error) {
	return resumeInternal(r.a, r.store, ctx, cid, nil, opts...)
}

// ResumeWithParams 恢复时附带自定义目标数据。
func (r *TypedRunner[M]) ResumeWithParams(ctx context.Context, cid string, params *ResumeParams, opts ...RunOption) (*AsyncIterator[*TypedAgentEvent[M]], error) {
	return resumeInternal(r.a, r.store, ctx, cid, params.Targets, opts...)
}

// ---- 内部实现 ----

func errorIter[M MessageType](err error) *AsyncIterator[*TypedAgentEvent[M]] {
	it, gen := NewAsyncIteratorPair[*TypedAgentEvent[M]]()
	gen.Send(&TypedAgentEvent[M]{Err: err})
	gen.Close()
	return it
}

func newUserMsg[M MessageType](query string) (M, error) {
	var zero M
	switch any(zero).(type) {
	case *schema.Message:
		return any(schema.UserMessage(query)).(M), nil
	case *schema.AgenticMessage:
		return any(schema.UserAgenticMessage(query)).(M), nil
	default:
		return zero, fmt.Errorf("unsupported message type %T", zero)
	}
}

func runImpl[M MessageType](a TypedAgent[M], streaming bool, store CheckPointStore, ctx context.Context, msgs []M, opts ...RunOption) *AsyncIterator[*TypedAgentEvent[M]] {
	o := getCommonOptions(nil, opts...)
	input := &TypedAgentInput[M]{Messages: msgs, EnableStreaming: streaming}

	var zero M
	if _, ok := any(zero).(*schema.Message); ok {
		ca, ok := any(a).(Agent)
		if !ok || ca == nil {
			return errorIter[M](fmt.Errorf("agent does not implement Agent interface"))
		}
		fa := toFlowAgent(ctx, ca)
		if store != nil {
			fa.checkPointStore = store
		}
		ci, ok := any(input).(*AgentInput)
		if !ok {
			return errorIter[M](fmt.Errorf("input type assertion failed: expected *AgentInput, got %T", input))
		}
		ctx = setupRunContext(ctx, input, o)
		return wrapIterForStore(streaming, store, ctx, any(fa.Run(ctx, ci, opts...)).(*AsyncIterator[*TypedAgentEvent[M]]), o)
	}

	tfa := toTypedFlowAgent(a)
	if store != nil {
		tfa.checkPointStore = store
	}
	ctx = setupRunContext(ctx, input, o)
	return wrapIterForStore(streaming, store, ctx, tfa.Run(ctx, input, opts...), o)
}

func resumeInternal[M MessageType](a TypedAgent[M], store CheckPointStore, ctx context.Context, cid string, data map[string]any, opts ...RunOption) (*AsyncIterator[*TypedAgentEvent[M]], error) {
	if store == nil {
		return nil, fmt.Errorf("resume requires a checkpoint store")
	}
	ctx, rc, info, err := loadCheckpoint(store, ctx, cid)
	if err != nil {
		return nil, err
	}
	streaming := info.EnableStreaming
	o := getCommonOptions(nil, opts...)
	if o.sharedParentSession {
		if ps := getSession(ctx); ps != nil {
			rc.Session.Values = ps.Values
		}
	}
	if rc.Session.Values == nil {
		rc.Session.Values = make(map[string]any)
	}
	ctx = setRunCtx(ctx, rc)
	AddSessionValues(ctx, o.sessionValues)

	var zero M
	if _, ok := any(zero).(*schema.Message); ok {
		ca, _ := any(a).(Agent)
		fa := toFlowAgent(ctx, ca)
		ra, ok := Agent(fa).(ResumableAgent)
		if !ok {
			return nil, fmt.Errorf("agent %T does not support resume", a)
		}
		return newIterForStore(streaming, store, ctx, any(ra.Resume(ctx, info, opts...)).(*AsyncIterator[*TypedAgentEvent[M]]), &cid, o.cancelCtx), nil
	}

	tfa := toTypedFlowAgent(a)
	ra, ok := TypedAgent[M](tfa).(TypedResumableAgent[M])
	if !ok {
		return nil, fmt.Errorf("agent %T does not support resume", a)
	}
	return newIterForStore(streaming, store, ctx, ra.Resume(ctx, info, opts...), &cid, o.cancelCtx), nil
}

// setupRunContext 初始化运行上下文并为新 Run 注入 session 值。
func setupRunContext[M MessageType](ctx context.Context, input *TypedAgentInput[M], o *runOptions) context.Context {
	ctx = ctxWithNewTypedRunCtx(ctx, input, o.sharedParentSession)
	AddSessionValues(ctx, o.sessionValues)
	return ctx
}

// wrapIterForStore 在启用检查点或取消上下文时，用 handleIter 包装事件迭代器；
// 否则直接返回原始迭代器。
func wrapIterForStore[M MessageType](streaming bool, store CheckPointStore, ctx context.Context, iter *AsyncIterator[*TypedAgentEvent[M]], o *runOptions) *AsyncIterator[*TypedAgentEvent[M]] {
	if store == nil && o.cancelCtx == nil {
		return iter
	}
	return newIterForStore(streaming, store, ctx, iter, o.checkPointID, o.cancelCtx)
}

// newIterForStore 创建由 handleIter 驱动的迭代器对，负责检查点持久化与取消处理。
// and cancel handling.
func newIterForStore[M MessageType](streaming bool, store CheckPointStore, ctx context.Context, iter *AsyncIterator[*TypedAgentEvent[M]], cid *string, cc *cancelContext) *AsyncIterator[*TypedAgentEvent[M]] {
	nit, gen := NewAsyncIteratorPair[*TypedAgentEvent[M]]()
	go handleIter(streaming, store, ctx, iter, gen, cid, cc)
	return nit
}

func handleIter[M MessageType](streaming bool, store CheckPointStore, ctx context.Context, ai *AsyncIterator[*TypedAgentEvent[M]], gen *AsyncGenerator[*TypedAgentEvent[M]], cid *string, cc *cancelContext) {
	defer func() {
		if r := recover(); r != nil {
			gen.Send(&TypedAgentEvent[M]{Err: fmt.Errorf("panic: %v", r)})
		}
		gen.Close()
	}()
	var sig *InterruptSignal
	for {
		ev, ok := ai.Next()
		if !ok {
			break
		}
		if ev.Err != nil {
			var ce *CancelError
			if errors.As(ev.Err, &ce) {
				if cc != nil && cc.isRoot() && cc.shouldCancel() {
					cc.markHandled()
				}
				if ce.interruptSignal != nil && cid != nil {
					ce.InterruptContexts = nil
					saveCheckpoint(store, ctx, *cid, streaming, &InterruptInfo{}, ce.interruptSignal)
				}
				gen.Send(ev)
				break
			}
		}
		if ev.Action != nil && ev.Action.internalInterrupted != nil {
			if sig != nil {
				panic("multiple interrupt actions")
			}
			sig = ev.Action.internalInterrupted
			ev = &TypedAgentEvent[M]{
				AgentName: ev.AgentName, RunPath: ev.RunPath, Output: ev.Output,
				Action: &AgentAction{Interrupted: &InterruptInfo{Data: ev.Action.Interrupted.Data}, internalInterrupted: sig},
			}
			if cid != nil {
				saveCheckpoint(store, ctx, *cid, streaming, &InterruptInfo{Data: ev.Action.Interrupted.Data}, sig)
			}
		}
		gen.Send(ev)
	}
}

// ResumeWithData 构造带自定义恢复数据的 ResumeInfo。
// 可用于传入 ReActAgentResumeData（如 HistoryModifier）
// 以在中断恢复时修改历史等行为。
func ResumeWithData(data any) *ResumeInfo {
	return &ResumeInfo{ResumeData: data}
}

// handleIter 在后台转发事件并在中断/取消时写入检查点。
