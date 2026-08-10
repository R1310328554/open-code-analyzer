package core

// tool_invoke.go — 统一工具调用上下文、中间件链（超时/重试/审批/限流）与事件发送。


import (
	"context"
	"fmt"
	"sync"
	"time"

	"ragflow/internal/harness/core/schema"
)

// ToolInvocationContext 单次工具调用的完整上下文，统一中间件签名。
// It replaces the separate endpoint function signatures in middleware chains
// with a single unified object, making it easier to implement cross-cutting
// concerns like timeout, retry, and approval.
type ToolInvocationContext struct {
	// Name 被调用工具名（如 get_weather）。
	Name string
	// CallID LLM 分配的本调用唯一 ID。
	CallID string
	// Arguments 结构化工具参数。
	Arguments *schema.ToolArgument
	// Result 成功执行后的工具结果（中间件可修改）。
	Result *schema.ToolResult
	// Timeout 单次调用超时，0 表示不限时。
	Timeout time.Duration
	// RetryConfig 本次调用的重试配置，nil 表示不重试。
	RetryConfig *ToolRetryConfig
	// Fallback 主调用失败时的备选函数。
	Fallback func(ctx context.Context, args *schema.ToolArgument) (*schema.ToolResult, error)

	// internal
	err     error
	skipped bool
	mu      sync.Mutex
}

// ToolRetryConfig 单次工具调用的重试策略。
type ToolRetryConfig struct {
	MaxAttempts int
	Backoff     time.Duration
	IsRetryable func(err error) bool
}

// InvokeTool 基于统一上下文的标准工具调用函数签名。
type InvokeTool func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error)

// ToolInvokeMiddleware 工具调用中间件，包装 next 处理器。
// It receives the next handler in the chain and the invocation context.
type ToolInvokeMiddleware func(next InvokeTool) InvokeTool

// ---- ToolWrapper：超时、重试与降级 ----

// NewTimeoutToolMiddleware 创建超时中间件，优先使用 ictx.Timeout。
// If the tool invocation exceeds the duration, the context is cancelled.
func NewTimeoutToolMiddleware(timeout time.Duration) ToolInvokeMiddleware {
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			d := timeout
			if ictx.Timeout > 0 {
				d = ictx.Timeout
			}
			if d <= 0 {
				return next(ctx, ictx)
			}
			ctx, cancel := context.WithTimeout(ctx, d)
			defer cancel()
			return next(ctx, ictx)
		}
	}
}

// NewRetryToolMiddleware 创建指数退避重试中间件。
func NewRetryToolMiddleware(cfg *ToolRetryConfig) ToolInvokeMiddleware {
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			rc := cfg
			if ictx.RetryConfig != nil {
				rc = ictx.RetryConfig
			}
			if rc == nil || rc.MaxAttempts <= 0 {
				return next(ctx, ictx)
			}
			backoff := rc.Backoff
			if backoff <= 0 {
				backoff = 100 * time.Millisecond
			}
			var lastErr error
			for attempt := 0; attempt <= rc.MaxAttempts; attempt++ {
				result, err := next(ctx, ictx)
				if err == nil {
					return result, nil
				}
				lastErr = err
				if rc.IsRetryable != nil && !rc.IsRetryable(err) {
					return nil, err
				}
				if attempt < rc.MaxAttempts {
					select {
					case <-ctx.Done():
						return nil, ctx.Err()
					case <-time.After(backoff):
					}
					backoff *= 2
				}
			}
			return nil, fmt.Errorf("tool retry exhausted after %d attempts: %w", rc.MaxAttempts, lastErr)
		}
	}
}

// NewFallbackToolMiddleware 主调用失败时调用备选函数。
func NewFallbackToolMiddleware(fallback func(ctx context.Context, args *schema.ToolArgument) (*schema.ToolResult, error)) ToolInvokeMiddleware {
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			result, err := next(ctx, ictx)
			if err == nil {
				return result, nil
			}
			fb := fallback
			if ictx.Fallback != nil {
				fb = ictx.Fallback
			}
			if fb == nil {
				return nil, err
			}
			return fb(ctx, ictx.Arguments)
		}
	}
}

// ---- 中间件链构建 ----

// ToolWrapperChain 从中间件与最终工具函数构建调用链（外层优先）。
func ToolWrapperChain(toolFn InvokeTool, middlewares ...ToolInvokeMiddleware) InvokeTool {
	chained := toolFn
	for i := len(middlewares) - 1; i >= 0; i-- {
		chained = middlewares[i](chained)
	}
	return chained
}

// ---- 人工审批机制 ----

// ApprovalRequest 需人工审批时返回的请求对象。
type ApprovalRequest struct {
	ToolName    string
	CallID      string
	Arguments   *schema.ToolArgument
	Description string
	// ApproveChan 接收审批结果：true 批准，false 拒绝。
	ApproveChan chan bool
}

// ApprovalMiddleware 执行前请求人工审批，拒绝则返回错误结果消息。
// tool invocation. If approval is denied or times out, the tool is skipped.
// The getApproval callback is called for every tool invocation to produce an approval request.
func ApprovalMiddleware(getApproval func(ctx context.Context, ictx *ToolInvocationContext) (*ApprovalRequest, error)) ToolInvokeMiddleware {
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			req, err := getApproval(ctx, ictx)
			if err != nil {
				return nil, fmt.Errorf("approval setup error: %w", err)
			}
			if req == nil {
				return next(ctx, ictx)
			}

			select {
			case approved := <-req.ApproveChan:
				if !approved {
					return &schema.ToolResult{
						Name:    ictx.Name,
						Content: fmt.Sprintf("Tool '%s' execution rejected by user", ictx.Name),
						Error:   "rejected",
					}, nil
				}
				return next(ctx, ictx)
			case <-ctx.Done():
				return nil, ctx.Err()
			}
		}
	}
}

// AutoApprovalMiddleware 自动批准所有工具（测试或无 HITL 场景）。
// Useful for testing or when no human-in-the-loop is needed.
func AutoApprovalMiddleware() ToolInvokeMiddleware {
	return ApprovalMiddleware(func(ctx context.Context, ictx *ToolInvocationContext) (*ApprovalRequest, error) {
		return nil, nil // nil = auto-approve
	})
}

// ---- 将现有 Tool 适配为 InvokeTool ----

// ToolToInvokeFn 将标准 Tool 转为 InvokeTool，保留工具中断语义。
func ToolToInvokeFn(tool Tool) InvokeTool {
	return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
		result, err := tool.Invoke(ctx, ictx.Arguments.Arguments)
		if err != nil {
			// Preserve tool interrupts so ToolsNode can handle them.
			if _, ok := IsToolInterrupt(err); ok {
				return nil, err
			}
			return &schema.ToolResult{Name: ictx.Name, Error: err.Error(), ToolCallID: ictx.CallID}, nil
		}
		return &schema.ToolResult{Name: ictx.Name, Content: result, ToolCallID: ictx.CallID}, nil
	}
}

// EnhancedToolToInvokeFn 将 EnhancedTool 转为 InvokeTool。
func EnhancedToolToInvokeFn(tool EnhancedTool) InvokeTool {
	return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
		return tool.EnhancedInvoke(ctx, ictx.Arguments)
	}
}

// ---- 内置中间件：事件发送与取消监控 ----

// NewEventSenderToolMiddleware 工具执行后向智能体事件流发送结果。
// result events to the agent's event stream after tool execution.
func NewEventSenderToolMiddleware[M MessageType]() ToolInvokeMiddleware {
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			result, err := next(ctx, ictx)
			if err != nil {
				return nil, err
			}
			ec := getReActExecCtx[M](ctx)
			if ec != nil && ec.generator != nil && result != nil {
				content := result.Content
				if content == "" {
					content = result.Error
				}
				var msg M
				var zero M
				switch any(zero).(type) {
				case *schema.AgenticMessage:
					msg = any(&schema.AgenticMessage{
						Role:    schema.AgenticRoleUser,
						Content: content,
						ContentBlocks: []schema.ContentBlock{
							{Type: "tool_result", ToolResult: &schema.ToolResult{
								ToolCallID: ictx.CallID, Content: content,
							}},
						},
					}).(M)
				default:
					msg = any(schema.ToolMessage(content, ictx.CallID)).(M)
				}
				ev := typedEventFromMessage(msg, nil, schema.RoleTool, ictx.Name)
				ec.send(ev)
			}
			return result, nil
		}
	}
}

// NewCancelToolMiddleware 执行前检查取消上下文，立即返回 ErrStreamCanceled。
// context before tool execution. If immediate cancel is requested, it returns
// ErrStreamCanceled immediately.
func NewCancelToolMiddleware() ToolInvokeMiddleware {
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			cc := getCancelContext(ctx)
			if cc != nil && cc.isImmediate() {
				return nil, ErrStreamCanceled
			}
			return next(ctx, ictx)
		}
	}
}

// ---- 限流 ----

// rateLimiter implements a simple per-tool token bucket.
type rateLimiter struct {
	mu     sync.Mutex
	tokens map[string]*tokenBucket
}

type tokenBucket struct {
	capacity int
	tokens   float64
	rate     float64 // tokens per nanosecond
	last     time.Time
}

func (rl *rateLimiter) allow(name string) bool {
	rl.mu.Lock()
	defer rl.mu.Unlock()
	b, ok := rl.tokens[name]
	if !ok {
		return true // first use, always allow
	}
	now := time.Now()
	elapsed := now.Sub(b.last)
	b.tokens += elapsed.Seconds() * b.rate
	if b.tokens > float64(b.capacity) {
		b.tokens = float64(b.capacity)
	}
	b.last = now
	if b.tokens >= 1 {
		b.tokens--
		return true
	}
	return false
}

func (rl *rateLimiter) init(name string, rate_ float64, burst int) {
	rl.mu.Lock()
	defer rl.mu.Unlock()
	rl.tokens[name] = &tokenBucket{
		capacity: burst,
		tokens:   float64(burst),
		rate:     rate_,
		last:     time.Now(),
	}
}

// NewRateLimitToolMiddleware 按工具名令牌桶限流（rate 次/秒，burst 突发容量）。
// invocation rate per tool name using a per-token token bucket.
// rate is the number of invocations per second, burst is the maximum burst size.
//
// Example: NewRateLimitToolMiddleware(10, 5) allows up to 10 req/s with burst of 5.
func NewRateLimitToolMiddleware(rate float64, burst int) ToolInvokeMiddleware {
	rl := &rateLimiter{tokens: make(map[string]*tokenBucket)}
	return func(next InvokeTool) InvokeTool {
		return func(ctx context.Context, ictx *ToolInvocationContext) (*schema.ToolResult, error) {
			rl.initOnce(ictx.Name, rate, burst)
			if !rl.allow(ictx.Name) {
				return nil, fmt.Errorf("rate limit exceeded for tool '%s'", ictx.Name)
			}
			return next(ctx, ictx)
		}
	}
}

func (rl *rateLimiter) initOnce(name string, rate float64, burst int) {
	rl.mu.Lock()
	defer rl.mu.Unlock()
	if _, ok := rl.tokens[name]; ok {
		return
	}
	rl.tokens[name] = &tokenBucket{
		capacity: burst,
		tokens:   float64(burst),
		rate:     rate,
		last:     time.Now(),
	}
}
