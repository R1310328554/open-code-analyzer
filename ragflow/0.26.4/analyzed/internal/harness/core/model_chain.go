package core

// model_chain.go — 模型包装链：事件发送、重试、故障转移、用户中间件与回调注入。


import (
	"context"
	"io"

	"ragflow/internal/harness/core/schema"
)

// ---- 事件发送模型包装器 ----

type eventSenderModelWrapper[M MessageType] struct {
	inner   Model[M]
	execCtx *reActExecCtx
}

func wrapModelWithEventSender[M MessageType](inner Model[M], ec *reActExecCtx) Model[M] {
	return &eventSenderModelWrapper[M]{inner: inner, execCtx: ec}
}

func (w *eventSenderModelWrapper[M]) Generate(ctx context.Context, msgs []M, opts ...ModelOption) (M, error) {
	if w.execCtx != nil && w.execCtx.suppressEventSend {
		return w.inner.Generate(ctx, msgs, opts...)
	}
	resp, err := w.inner.Generate(ctx, msgs, opts...)
	if err != nil {
		return resp, err
	}
	if w.execCtx != nil && w.execCtx.generator != nil && !isNilMessage(resp) {
		w.execCtx.send(typedModelOutputEvent(resp, nil))
	}
	return resp, nil
}

func (w *eventSenderModelWrapper[M]) Stream(ctx context.Context, msgs []M, opts ...ModelOption) (*schema.StreamReader[M], error) {
	s, err := w.inner.Stream(ctx, msgs, opts...)
	if err != nil {
		return nil, err
	}
	if w.execCtx != nil && w.execCtx.suppressEventSend {
		return s, nil
	}
	r := schema.NewStreamReader[M]()
	go func() {
		defer r.Close()
		defer s.Close()
		var chunks []M
		for {
			c, err := s.Recv()
			if err == io.EOF {
				break
			}
			if err != nil {
				r.Send(c, err)
				return
			}
			chunks = append(chunks, c)
			r.Send(c, nil)
		}
		if len(chunks) > 0 && w.execCtx != nil {
			if merged, e := mergeChunks(chunks); e == nil {
				w.execCtx.send(typedModelOutputEvent(merged, nil))
			}
		}
	}()
	return r, nil
}

func (w *eventSenderModelWrapper[M]) BindTools(tools []*schema.ToolInfo) error {
	return w.inner.BindTools(tools)
}

// ---- 回调注入模型包装器（追踪/监控）----

type callbackModelWrapper[M MessageType] struct {
	inner Model[M]
}

func (w *callbackModelWrapper[M]) Generate(ctx context.Context, msgs []M, opts ...ModelOption) (M, error) {
	msgs = injectMessageID(msgs)
	cbs := getCallbacks(ctx)
	if len(cbs) > 0 {
		input := &AgentCallbackInput{}
		if len(msgs) > 0 {
			switch any(msgs[0]).(type) {
			case *schema.Message:
				msgSlice := make([]Message, len(msgs))
				for i, m := range msgs {
					msgSlice[i] = any(m).(*schema.Message)
				}
				input.Input = &AgentInput{Messages: msgSlice}
			}
		}
		for _, cb := range cbs {
			cb.onStart(ctx, input)
		}
	}
	resp, err := w.inner.Generate(ctx, msgs, opts...)
	if len(cbs) > 0 {
		if err != nil {
			for _, cb := range cbs {
				if cb.onError != nil {
					cb.onError(ctx, err)
				}
			}
		}
		evIter, evGen := NewAsyncIteratorPair[*AgentEvent]()
		if err == nil {
			evGen.Send(&AgentEvent{
				Output: &AgentOutput{MessageOutput: &MessageVariant{Message: any(resp).(*schema.Message)}},
			})
		} else {
			evGen.Send(&AgentEvent{Err: err})
		}
		evGen.Close()
		output := &AgentCallbackOutput{Events: evIter}
		for _, cb := range cbs {
			cb.onEnd(ctx, output)
		}
	}
	return resp, err
}
func (w *callbackModelWrapper[M]) Stream(ctx context.Context, msgs []M, opts ...ModelOption) (*schema.StreamReader[M], error) {
	cbs := getCallbacks(ctx)
	if len(cbs) > 0 {
		input := &AgentCallbackInput{}
		if len(msgs) > 0 {
			switch any(msgs[0]).(type) {
			case *schema.Message:
				msgSlice := make([]Message, len(msgs))
				for i, m := range msgs {
					msgSlice[i] = any(m).(*schema.Message)
				}
				input.Input = &AgentInput{Messages: msgSlice}
			}
		}
		for _, cb := range cbs {
			cb.onStart(ctx, input)
		}
	}
	s, err := w.inner.Stream(ctx, msgs, opts...)
	if err != nil {
		if len(cbs) > 0 {
			for _, cb := range cbs {
				if cb.onError != nil {
					cb.onError(ctx, err)
				}
			}
			evIter, evGen := NewAsyncIteratorPair[*AgentEvent]()
			evGen.Send(&AgentEvent{Err: err})
			evGen.Close()
			output := &AgentCallbackOutput{Events: evIter}
			for _, cb := range cbs {
				cb.onEnd(ctx, output)
			}
		}
		return nil, err
	}
	// Wrap stream to fire OnEnd on completion
	r := schema.NewStreamReader[M]()
	go func() {
		defer r.Close()
		defer s.Close()
		var allChunks []M
		for {
			c, e := s.Recv()
			if e == io.EOF {
				break
			}
			if e != nil {
				r.Send(c, e)
				return
			}
			allChunks = append(allChunks, c)
			r.Send(c, nil)
		}
		if len(cbs) > 0 && len(allChunks) > 0 {
			merged, _ := mergeChunks(allChunks)
			evIter, evGen := NewAsyncIteratorPair[*AgentEvent]()
			evGen.Send(&AgentEvent{
				Output: &AgentOutput{MessageOutput: &MessageVariant{Message: any(merged).(*schema.Message)}},
			})
			evGen.Close()
			output := &AgentCallbackOutput{Events: evIter}
			for _, cb := range cbs {
				cb.onEnd(ctx, output)
			}
		}
	}()
	return r, nil
}
func (w *callbackModelWrapper[M]) BindTools(tools []*schema.ToolInfo) error {
	return w.inner.BindTools(tools)
}

// ---- 模型包装链构建器 ----

// BuildModelWrapperChain 自内向外组装完整 Model 包装链：
//
//	base → failover → retry → eventSender → 用户中间件 → callback
//
// 自最内层（贴近 base Model）向外层依次包装。
// allToolInfos 合并 config.Tools 与各中间件 ContributeToolInfos。
// BuildModelWrapperChain 按固定顺序组装 Model 装饰器链。
func BuildModelWrapperChain[M MessageType](base Model[M], ec *reActExecCtx, cfg *ReActConfig[M], allToolInfos []*schema.ToolInfo) Model[M] {
	model := base

	// 1. 事件发送（用户中间件已有 EventSender 时跳过，避免重复）
	if !HasUserEventSenderModelWrapper(cfg.Middlewares) {
		model = wrapModelWithEventSender(model, ec)
	}

	// 2. 重试（包裹 event sender，重试时重放整条内链）
	if cfg.RetryConfig != nil {
		model = newTypedRetryModelWrapper(model, cfg.RetryConfig)
	}

	// 3. 故障转移（每次 failover 尝试仍享有 retry 行为）
	if cfg.FailoverConfig != nil && len(cfg.FailoverConfig.Models) > 0 {
		allModels := append([]Model[M]{base}, cfg.FailoverConfig.Models...)
		model = newFailoverModel(allModels, cfg.FailoverConfig)
	}

	// 4. 用户中间件 WrapModel（如 telemetry）
	for _, mw := range cfg.Middlewares {
		if mw == nil {
			continue
		}
		mc := &TypedModelContext[M]{
			Tools:               allToolInfos,
			ModelRetryConfig:    cfg.RetryConfig,
			ModelFailoverConfig: cfg.FailoverConfig,
		}
		wrapped, err := mw.WrapModel(context.Background(), model, mc)
		if err == nil && wrapped != nil {
			model = wrapped
		}
	}

	// 5. 状态包装：深拷贝消息、注入 ID、检查 cancel（防中间件副作用）
	var cancelCtx *cancelContext
	if ec != nil {
		cancelCtx = ec.cancelCtx
	}
	model = newTypedStateModelWrapper(model, cancelCtx)

	// 6. 回调注入（最外层，onStart/onEnd/onError）
	model = &callbackModelWrapper[M]{inner: model}

	return model
}

// injectMessageID 为无 ID 消息深拷贝并写入唯一 message ID。
// 在副本上操作，避免并行 goroutine 共享消息时的数据竞争。
func injectMessageID[M MessageType](msgs []M) []M {
	for i, msg := range msgs {
		switch v := any(msg).(type) {
		case *schema.Message:
			if v.Extra != nil && GetMessageID(v.Extra) != "" {
				continue // already has ID, skip
			}
			// Deep-copy so concurrent access is safe for shared messages.
			cp := copyMessage(msg)
			copied := any(cp).(*schema.Message)
			copied.Extra = EnsureMessageID(copied.Extra)
			if c2, ok := any(copied).(M); ok {
				msgs[i] = c2
			}
		}
	}
	return msgs
}

// Stream 路径合并 chunks 后发送单次 model output 事件；suppressEventSend 可抑制。
