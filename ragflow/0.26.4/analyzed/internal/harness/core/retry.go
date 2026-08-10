package core

// retry.go — 模型调用重试：TypedModelRetryConfig、ShouldRetry 决策、流式首块校验与 WillRetryError 事件。


import (
	"context"
	"errors"
	"fmt"
	"io"
	"time"

	"ragflow/internal/harness/core/schema"
	"ragflow/internal/harness/graph/types"
)

var (
	ErrExceedMaxRetries = errors.New("exceeds max retries")
)

// RetryExhaustedError 重试次数耗尽时返回。
type RetryExhaustedError struct {
	LastErr      error
	TotalRetries int
}

func (e *RetryExhaustedError) Error() string {
	if e.LastErr != nil {
		return fmt.Sprintf("exceeds max retries: last error: %v", e.LastErr)
	}
	return "exceeds max retries"
}

func (e *RetryExhaustedError) Unwrap() error { return ErrExceedMaxRetries }

// WillRetryError 即将重试时作为事件发送给客户端。
type WillRetryError struct {
	ErrStr       string
	RetryAttempt int
	rejectReason any
	err          error
}

func (e *WillRetryError) Error() string     { return e.ErrStr }
func (e *WillRetryError) Unwrap() error     { return e.err }
func (e *WillRetryError) RejectReason() any { return e.rejectReason }

func init() {
	schema.RegisterType("agentcore_will_retry_error", func() any { return &WillRetryError{} })
}

// TypedRetryContext 传给 ShouldRetry 的重试决策上下文。
type TypedRetryContext[M MessageType] struct {
	RetryAttempt  int
	InputMessages []M
	OutputMessage M
	Err           error
}

type RetryContext = TypedRetryContext[*schema.Message]

// TypedRetryDecision ShouldRetry 的决策结果。
type TypedRetryDecision[M MessageType] struct {
	Retry                        bool
	RewriteError                 error
	ModifiedInputMessages        []M
	PersistModifiedInputMessages bool
	AdditionalOptions            []ModelOption
	Backoff                      time.Duration
	RejectReason                 any
}

type RetryDecision = TypedRetryDecision[*schema.Message]

// TypedModelRetryConfig 配置 Model 重试行为。
type TypedModelRetryConfig[M MessageType] struct {
	MaxRetries  int
	ShouldRetry func(ctx context.Context, retryCtx *TypedRetryContext[M]) *TypedRetryDecision[M]
	IsRetryAble func(ctx context.Context, err error) bool
	BackoffFunc func(ctx context.Context, attempt int) time.Duration
}

type ModelRetryConfig = TypedModelRetryConfig[*schema.Message]

func defaultIsRetryAble(_ context.Context, err error) bool { return err != nil }

func defaultBackoff(_ context.Context, attempt int) time.Duration {
	p := types.RetryPolicy{
		InitialInterval: 100 * time.Millisecond,
		BackoffFactor:   2.0,
		MaxInterval:     10 * time.Second,
		Jitter:          true,
	}
	return p.CalculateBackoff(attempt)
}

// typedRetryModelWrapper 为 Model 添加重试逻辑。
type typedRetryModelWrapper[M MessageType] struct {
	inner  Model[M]
	config *TypedModelRetryConfig[M]
}

func newTypedRetryModelWrapper[M MessageType](inner Model[M], config *TypedModelRetryConfig[M]) *typedRetryModelWrapper[M] {
	return &typedRetryModelWrapper[M]{inner: inner, config: config}
}

func (r *typedRetryModelWrapper[M]) Generate(ctx context.Context, input []M, opts ...ModelOption) (M, error) {
	if r.config.ShouldRetry != nil {
		return r.generateWithShouldRetry(ctx, input, opts...)
	}
	return r.generateLegacy(ctx, input, opts...)
}

func (r *typedRetryModelWrapper[M]) generateLegacy(ctx context.Context, input []M, opts ...ModelOption) (zero M, _ error) {
	isRetryAble := r.config.IsRetryAble
	if isRetryAble == nil {
		isRetryAble = defaultIsRetryAble
	}
	backoff := r.config.BackoffFunc
	if backoff == nil {
		backoff = defaultBackoff
	}

	var lastErr error
	for attempt := 0; attempt <= r.config.MaxRetries; attempt++ {
		out, err := r.inner.Generate(ctx, input, opts...)
		if err == nil {
			return out, nil
		}
		if errors.Is(err, ErrStreamCanceled) {
			return zero, err
		}
		if !isRetryAble(ctx, err) {
			return zero, err
		}
		lastErr = err
		if attempt < r.config.MaxRetries {
			if err := contextAwareSleep(ctx, backoff(ctx, attempt+1)); err != nil {
				return zero, err
			}
		}
	}
	return zero, &RetryExhaustedError{LastErr: lastErr, TotalRetries: r.config.MaxRetries}
}

func (r *typedRetryModelWrapper[M]) generateWithShouldRetry(ctx context.Context, input []M, opts ...ModelOption) (M, error) {
	backoff := r.config.BackoffFunc
	if backoff == nil {
		backoff = defaultBackoff
	}
	execCtx := getReActExecCtx[M](ctx)
	currentInput := input
	currentOpts := opts
	var lastErr error
	var zero M

	for attempt := 0; attempt <= r.config.MaxRetries; attempt++ {
		if execCtx != nil {
			execCtx.suppressEventSend = true
		}
		out, err := r.inner.Generate(ctx, currentInput, currentOpts...)
		if execCtx != nil {
			execCtx.suppressEventSend = false
		}

		if errors.Is(err, ErrStreamCanceled) {
			return zero, err
		}

		retryCtx := &TypedRetryContext[M]{
			RetryAttempt: attempt + 1, InputMessages: currentInput,
			OutputMessage: out, Err: err,
		}
		decision := r.config.ShouldRetry(ctx, retryCtx)
		if decision == nil {
			decision = &TypedRetryDecision[M]{}
		}

		if !decision.Retry {
			if decision.RewriteError != nil {
				return zero, decision.RewriteError
			}
			if err != nil {
				return zero, err
			}
			if execCtx != nil && execCtx.generator != nil && !isNilMessage(out) {
				execCtx.send(typedModelOutputEvent(out, nil))
			}
			return out, nil
		}

		lastErr = err
		if lastErr == nil {
			lastErr = fmt.Errorf("model output rejected by ShouldRetry at attempt %d", attempt+1)
		}
		if attempt >= r.config.MaxRetries {
			break
		}

		// 休眠重试前发送 WillRetryError 事件
		if execCtx != nil && execCtx.generator != nil {
			willRetry := &WillRetryError{ErrStr: lastErr.Error(), RetryAttempt: attempt + 1, rejectReason: decision.RejectReason, err: lastErr}
			execCtx.send(&TypedAgentEvent[M]{Err: any(willRetry).(error)})
		}
		applyRetryDecision(&currentInput, &currentOpts, decision)
		delay := decision.Backoff
		if delay == 0 {
			delay = backoff(ctx, attempt+1)
		}
		if err := contextAwareSleep(ctx, delay); err != nil {
			return zero, err
		}
	}
	return zero, &RetryExhaustedError{LastErr: lastErr, TotalRetries: r.config.MaxRetries}
}

func (r *typedRetryModelWrapper[M]) Stream(ctx context.Context, input []M, opts ...ModelOption) (*schema.StreamReader[M], error) {
	if r.config.ShouldRetry != nil {
		return r.streamWithShouldRetry(ctx, input, opts...)
	}
	return r.streamLegacy(ctx, input, opts...)
}

func (r *typedRetryModelWrapper[M]) streamLegacy(ctx context.Context, input []M, opts ...ModelOption) (*schema.StreamReader[M], error) {
	isRetryAble := r.config.IsRetryAble
	if isRetryAble == nil {
		isRetryAble = defaultIsRetryAble
	}
	backoff := r.config.BackoffFunc
	if backoff == nil {
		backoff = defaultBackoff
	}

	var lastErr error
	for attempt := 0; attempt <= r.config.MaxRetries; attempt++ {
		stream, err := r.inner.Stream(ctx, input, opts...)
		if err != nil {
			if errors.Is(err, ErrStreamCanceled) {
				return nil, err
			}
			if !isRetryAble(ctx, err) {
				return nil, err
			}
			lastErr = err
			if attempt < r.config.MaxRetries {
				if err := contextAwareSleep(ctx, backoff(ctx, attempt+1)); err != nil {
					return nil, err
				}
			}
			continue
		}
		// 流式路径读取首块验证流健康
		chunk, streamErr := stream.Recv()
		if streamErr == nil {
			outStream := schema.NewStreamReader[M]()
			go func() {
				outStream.Send(chunk, nil)
				for {
					c, e := stream.Recv()
					if e == io.EOF {
						break
					}
					if e != nil {
						outStream.Send(c, e)
						return
					}
					select {
					case <-ctx.Done():
						outStream.Send(c, ctx.Err())
						return
					default:
					}
					outStream.Send(c, nil)
				}
				outStream.Close()
			}()
			return outStream, nil
		}
		stream.Close()
		if errors.Is(streamErr, ErrStreamCanceled) {
			return nil, streamErr
		}
		if !isRetryAble(ctx, streamErr) {
			return nil, streamErr
		}
		lastErr = streamErr
		if attempt < r.config.MaxRetries {
			if err := contextAwareSleep(ctx, backoff(ctx, attempt+1)); err != nil {
				return nil, err
			}
		}
	}
	return nil, &RetryExhaustedError{LastErr: lastErr, TotalRetries: r.config.MaxRetries}
}

func (r *typedRetryModelWrapper[M]) streamWithShouldRetry(ctx context.Context, input []M, opts ...ModelOption) (*schema.StreamReader[M], error) {
	backoff := r.config.BackoffFunc
	if backoff == nil {
		backoff = defaultBackoff
	}
	execCtx := getReActExecCtx[M](ctx)
	currentInput := input
	currentOpts := opts
	var lastErr error

	sig := &retrySignal{ch: make(chan streamRetryVerdict, 1)}
	if execCtx != nil {
		execCtx.retrySignal = sig
	}

	for attempt := 0; attempt <= r.config.MaxRetries; attempt++ {
		stream, err := r.inner.Stream(ctx, currentInput, currentOpts...)
		if err != nil {
			if errors.Is(err, ErrStreamCanceled) {
				return nil, err
			}
			retryCtx := &TypedRetryContext[M]{
				RetryAttempt: attempt + 1, InputMessages: currentInput, Err: err,
			}
			decision := r.config.ShouldRetry(ctx, retryCtx)
			if decision == nil {
				decision = &TypedRetryDecision[M]{}
			}
			if !decision.Retry {
				if decision.RewriteError != nil {
					return nil, decision.RewriteError
				}
				return nil, err
			}
			lastErr = err
			if attempt < r.config.MaxRetries {
				if execCtx != nil && execCtx.generator != nil {
					execCtx.send(&TypedAgentEvent[M]{Err: &WillRetryError{ErrStr: lastErr.Error(), RetryAttempt: attempt + 1, rejectReason: decision.RejectReason, err: lastErr}})
				}
				applyRetryDecision(&currentInput, &currentOpts, decision)
				delay := decision.Backoff
				if delay == 0 {
					delay = backoff(ctx, attempt+1)
				}
				if err := contextAwareSleep(ctx, delay); err != nil {
					return nil, err
				}
			}
			continue
		}

		// ShouldRetry 流式路径同样读首块验证
		chunk, streamErr := stream.Recv()
		if streamErr != nil && streamErr != io.EOF {
			stream.Close()
			retryCtx := &TypedRetryContext[M]{
				RetryAttempt: attempt + 1, InputMessages: currentInput, Err: streamErr,
			}
			decision := r.config.ShouldRetry(ctx, retryCtx)
			if decision == nil {
				decision = &TypedRetryDecision[M]{}
			}
			if !decision.Retry {
				if decision.RewriteError != nil {
					return nil, decision.RewriteError
				}
				return nil, streamErr
			}
			lastErr = streamErr
			select {
			case sig.ch <- streamRetryVerdict{WillRetry: true, Err: streamErr, RejectReason: decision.RejectReason}:
			default:
			}
			if attempt < r.config.MaxRetries {
				if execCtx != nil && execCtx.generator != nil {
					execCtx.send(&TypedAgentEvent[M]{Err: &WillRetryError{ErrStr: lastErr.Error(), RetryAttempt: attempt + 1, rejectReason: decision.RejectReason, err: lastErr}})
				}
				applyRetryDecision(&currentInput, &currentOpts, decision)
				delay := decision.Backoff
				if delay == 0 {
					delay = backoff(ctx, attempt+1)
				}
				if err := contextAwareSleep(ctx, delay); err != nil {
					return nil, err
				}
			}
			continue
		}

		// 收集全部 chunk 合并后发送 output 事件并转发给调用方
		var allChunks []M
		if streamErr != io.EOF {
			allChunks = append(allChunks, chunk)
		}
		callerCh := schema.NewStreamReader[M]()
		go func() {
			if len(allChunks) > 0 {
				callerCh.Send(allChunks[0], nil)
			}
			for {
				c, e := stream.Recv()
				if e == io.EOF {
					break
				}
				if e != nil {
					callerCh.Send(c, e)
					return
				}
				allChunks = append(allChunks, c)
				callerCh.Send(c, nil)
			}
			// 流结束后发送合并后的 model output 事件
			if execCtx != nil && execCtx.generator != nil && len(allChunks) > 0 {
				if merged, err := mergeChunks(allChunks); err == nil {
					execCtx.send(typedModelOutputEvent(merged, nil))
				}
			}
			callerCh.Close()
		}()
		select {
		case sig.ch <- streamRetryVerdict{WillRetry: false}:
		default:
		}
		return callerCh, nil
	}
	return nil, &RetryExhaustedError{LastErr: lastErr, TotalRetries: r.config.MaxRetries}
}

func (r *typedRetryModelWrapper[M]) BindTools(tools []*schema.ToolInfo) error {
	return r.inner.BindTools(tools)
}

// WithModelRetry 包装 Model 启用重试。
// ShouldRetry 非 nil 但 MaxRetries 为 0 时仅尝试一次
// Retry:true 将立即 RetryExhaustedError{TotalRetries:0}
// 需 MaxRetries >= 1 才允许多次 ShouldRetry 驱动重试
// legacy 路径用 IsRetryAble + BackoffFunc。
func WithModelRetry[M MessageType](inner Model[M], cfg *TypedModelRetryConfig[M]) Model[M] {
	if cfg == nil || (cfg.MaxRetries <= 0 && cfg.ShouldRetry == nil) {
		return inner
	}
	return newTypedRetryModelWrapper(inner, cfg)
}

func applyRetryDecision[M MessageType](input *[]M, opts *[]ModelOption, decision *TypedRetryDecision[M]) {
	if decision.ModifiedInputMessages != nil && decision.PersistModifiedInputMessages {
		*input = decision.ModifiedInputMessages
	} else if decision.ModifiedInputMessages != nil {
		// 仅下次尝试使用 ModifiedInputMessages，不持久化到原始 input
		// 调用方需自行处理回滚。
		tmp := make([]M, len(decision.ModifiedInputMessages))
		copy(tmp, decision.ModifiedInputMessages)
		*input = tmp
	}
	if decision.AdditionalOptions != nil {
		*opts = append(*opts, decision.AdditionalOptions...)
	}
}

func contextAwareSleep(ctx context.Context, delay time.Duration) error {
	if delay <= 0 {
		return nil
	}
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(delay):
		return nil
	}
}

func mergeChunks[M MessageType](chunks []M) (M, error) {
	var zero M
	if len(chunks) == 0 {
		return zero, nil
	}
	switch c := any(chunks).(type) {
	case []*schema.Message:
		merged, err := schema.ConcatMessages(c)
		if err != nil {
			return zero, err
		}
		return any(merged).(M), nil
	case []*schema.AgenticMessage:
		merged, err := schema.ConcatAgenticMessages(c)
		if err != nil {
			return zero, err
		}
		return any(merged).(M), nil
	}
	return chunks[0], nil
}

// streamRetryVerdict 流式重试内部信号。
type streamRetryVerdict struct {
	WillRetry    bool
	Err          error
	RejectReason any
}

type retrySignal struct {
	ch chan streamRetryVerdict
}

func (rs *retrySignal) consume() streamRetryVerdict {
	if rs == nil {
		return streamRetryVerdict{}
	}
	select {
	case v := <-rs.ch:
		return v
	default:
		return streamRetryVerdict{}
	}
}

// WithRetry 将重试配置附加为 ModelOption。
func WithRetry[M MessageType](cfg *TypedModelRetryConfig[M]) ModelOption {
	return &typedModelOption[M]{f: func(o *modelOptions[M]) { o.RetryConfig = cfg }}
}

// generateWithShouldRetry 重试期间 suppressEventSend，成功后再补发 model output 事件。
