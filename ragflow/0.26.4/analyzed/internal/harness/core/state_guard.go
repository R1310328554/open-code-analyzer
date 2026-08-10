package core

// state_guard.go — 模型调用状态守卫：消息深拷贝、取消检查与 ID 注入。


import (
	"context"
	"encoding/json"
	"io"

	"ragflow/internal/harness/core/schema"
)

// typedStateModelWrapper 统一消息深拷贝、ID 注入、取消检查与输出事件发送，
// 作为中间件与重试链之间的核心包装层。
//
// 位于中间件与 retry/failover 链之间，提供：
// middlewares and the retry/failover chain, adding:
//   - 消息深拷贝（防止中间件链中指针共享）
//   - 消息 ID 自动赋值
//   - 模型调用前取消上下文检查
//   - 模型输出事件发送
//   - BeforeModelRewrite/AfterModelRewrite 编排（经 chatmodel 循环）
type typedStateModelWrapper[M MessageType] struct {
	inner     Model[M]
	cancelCtx *cancelContext
}

// newTypedStateModelWrapper 构造带取消守卫的模型包装器。
func newTypedStateModelWrapper[M MessageType](inner Model[M], cc *cancelContext) Model[M] {
	return &typedStateModelWrapper[M]{inner: inner, cancelCtx: cc}
}

// copyMessage 深拷贝 Message/AgenticMessage，
// 避免同一消息经多层包装器时指针共享。
//
// Extra 通过 JSON 往返深拷贝（与 checkpoint.deepCopy 一致）；
// 失败时保留原引用以免丢数据。
// If JSON round-trip fails for a value, the original reference is kept as
// a fallback to avoid data loss.
func copyMessage[M MessageType](msg M) M {
	switch v := any(msg).(type) {
	case *schema.Message:
		cp := &schema.Message{
			Role:    v.Role,
			Content: v.Content,
			Name:    v.Name,
		}
		if len(v.ToolCalls) > 0 {
			cp.ToolCalls = make([]schema.ToolCall, len(v.ToolCalls))
			copy(cp.ToolCalls, v.ToolCalls)
		}
		if v.Extra != nil {
			cp.Extra = make(map[string]any, len(v.Extra))
			for k, val := range v.Extra {
				cp.Extra[k] = deepCopyAny(val)
			}
		}
		return any(cp).(M)
	case *schema.AgenticMessage:
		cp := &schema.AgenticMessage{
			Role:    v.Role,
			Content: v.Content,
		}
		if len(v.ContentBlocks) > 0 {
			cp.ContentBlocks = make([]schema.ContentBlock, len(v.ContentBlocks))
			copy(cp.ContentBlocks, v.ContentBlocks)
		}
		return any(cp).(M)
	}
	return msg
}

// deepCopyAny 通过 JSON 往返深拷贝任意值，失败则回退原引用。
// Falls back to the original value if JSON marshal/unmarshal fails.
func deepCopyAny(v any) any {
	if v == nil {
		return nil
	}
	data, err := json.Marshal(v)
	if err != nil {
		return v // fallback: keep original reference
	}
	var result any
	if err := json.Unmarshal(data, &result); err != nil {
		return v // fallback: keep original reference
	}
	return result
}

// preprocessInput 执行取消检查、深拷贝与消息 ID 注入；取消时返回 nil。
// Returns nil if cancelled (caller should return ErrStreamCanceled immediately).
func (w *typedStateModelWrapper[M]) preprocessInput(msgs []M) []M {
	if w.cancelCtx != nil && w.cancelCtx.isImmediate() {
		return nil
	}
	copied := make([]M, len(msgs))
	for i, m := range msgs {
		copied[i] = copyMessage(m)
	}
	for _, m := range copied {
		switch v := any(m).(type) {
		case *schema.Message:
			if v.Extra == nil {
				v.Extra = make(map[string]any)
			}
			v.Extra = EnsureMessageID(v.Extra)
		}
	}
	return copied
}

// Generate 预处理输入后调用内部模型并深拷贝响应。
func (w *typedStateModelWrapper[M]) Generate(ctx context.Context, msgs []M, opts ...ModelOption) (M, error) {
	copied := w.preprocessInput(msgs)
	if copied == nil {
		var zero M
		return zero, ErrStreamCanceled
	}
	resp, err := w.inner.Generate(ctx, copied, opts...)
	if err != nil {
		return resp, err
	}
	return copyMessage(resp), nil
}

// Stream 流式调用内部模型，逐块深拷贝并监听取消。
func (w *typedStateModelWrapper[M]) Stream(ctx context.Context, msgs []M, opts ...ModelOption) (*schema.StreamReader[M], error) {
	// Cancel check before allocating any resources (returns error-embedded StreamReader)
	if w.cancelCtx != nil && w.cancelCtx.isImmediate() {
		r := schema.NewStreamReader[M]()
		var zero M
		r.Send(zero, ErrStreamCanceled)
		r.Close()
		return r, nil
	}

	copied := w.preprocessInput(msgs)
	if copied == nil {
		return nil, ErrStreamCanceled
	}

	s, err := w.inner.Stream(ctx, copied, opts...)
	if err != nil {
		return nil, err
	}

	r := schema.NewStreamReader[M]()
	go func() {
		defer r.Close()
		defer s.Close()
		for {
			if w.cancelCtx != nil && w.cancelCtx.isImmediate() {
				var zero M
				r.Send(zero, ErrStreamCanceled)
				return
			}
			c, e := s.Recv()
			if e == io.EOF {
				break
			}
			if e != nil {
				r.Send(c, e)
				return
			}
			r.Send(copyMessage(c), nil)
		}
	}()
	return r, nil
}

func (w *typedStateModelWrapper[M]) BindTools(tools []*schema.ToolInfo) error {
	return w.inner.BindTools(tools)
}

// 取消时 Stream 返回嵌入 ErrStreamCanceled 的 StreamReader。
