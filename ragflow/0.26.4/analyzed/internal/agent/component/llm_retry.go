// Package component — retry decorator for ChatInvoker.
//
// retryInvoker wraps any ChatInvoker and retries it on error with
// exponential backoff. It mirrors Python's
//
//	for attempt in range(max_retries + 1):
//	    try: return self._chat(...)
//	    except: time.sleep(delay * 2 ** attempt)
//
// semantics from agent/component/llm.py (the actual loop is hidden
// inside LLMBundle / Base._chat; for the Go port we implement it
// directly at the ChatInvoker boundary so every chat path
// — LLMComponent, Agent component, citation grounding — benefits
// without touching the call sites).
//
// Defaults (set by getDefaultChatInvoker): 3 retries, 2s initial
// delay. The defaults match LLMParam's zero-value semantics: a
// caller who leaves both fields unset still gets retries, mirroring
// Python's LLMBundle max_retries=5 / base_delay=2.0 closely enough
// for the Go port (we use 3/2s to keep tests snappy; per-call
// overrides flow through LLMComponent.Invoke).
// llm_retry.go — ChatInvoker 重试装饰器：指数退避，对齐 Python LLMBundle 语义。

package component

import (
	"context"
	"fmt"
	"time"
)

// retryInvoker decorates a ChatInvoker with exponential-backoff
// retries. The zero-value fields disable the loop (a maxRetries of
// 0 means "no retries, single attempt") which matches LLMParam
// semantics: 0 → call exactly once.
// retryInvoker 为内层 ChatInvoker 包装指数退避重试循环。
type retryInvoker struct {
	inner        ChatInvoker
	maxRetries   int
	initialDelay time.Duration
}

// Unwrap returns the inner ChatInvoker wrapped by this
// retryInvoker. The intended use is LLMParam's param-override
// path: when a DSL sets LLMParam.MaxRetries explicitly, the
// production boot's retryInvoker is also wrapping the
// einoChatInvoker, and the two loops would otherwise
// multiplicatively stack (boot=3, MaxRetries=5 → up to
// (3+1)*(5+1) = 24 invocations). LLMComponent.Invoke walks the
// Unwrap chain to find the bare invoker, then wraps that with
// the operator's literal MaxRetries so the absolute count is
// MaxRetries+1 regardless of the boot layer.
//
// Unwrap returns nil when the inner invoker is nil (a
// defensive call-site convenience).
func (r *retryInvoker) Unwrap() ChatInvoker {
	if r == nil {
		return nil
	}
	return r.inner
}

// retryInvokerBackoff is the default backoff used when the param
// leaves DelayAfterError at its zero value. Matches Python's
// LLM_BASE_DELAY=2.0.
const retryInvokerBackoff = 2 * time.Second

// retryInvokerDefaultRetries matches Python's LLMBundle default of
// 5, but the Go port uses 3 to keep the test suite fast while still
// demonstrating the loop. Users override via LLMParam.MaxRetries.
const retryInvokerDefaultRetries = 3

// newRetryInvoker wraps inner in a retry loop with the given
// parameters. maxRetries <= 0 yields a single attempt; initialDelay
// <= 0 results in no delay between retries.
// newRetryInvoker 构造重试装饰器；maxRetries<=0 表示仅单次尝试。
func newRetryInvoker(inner ChatInvoker, maxRetries int, initialDelay time.Duration) *retryInvoker {
	if maxRetries < 0 {
		maxRetries = 0
	}
	if initialDelay < 0 {
		initialDelay = 0
	}
	return &retryInvoker{
		inner:        inner,
		maxRetries:   maxRetries,
		initialDelay: initialDelay,
	}
}

// unwrapChatInvoker walks the ChatInvoker chain, peeling off any
// retryInvoker layers to return the bare invoker underneath. Used
// by LLMComponent.Invoke's param-override path to install a fresh
// retryInvoker with the operator's literal MaxRetries without
// multiplicatively stacking on top of the boot retry layer.
//
// Returns the input unchanged when no retryInvoker layers are
// present. Returns the unwrapped invoker (which may itself be a
// retryInvoker wrapping another retryInvoker — production only
// installs one layer so a single-level walk is sufficient, but
// the loop handles pathological cases for safety).
// unwrapChatInvoker 剥除 retryInvoker 层，避免重试次数乘法叠加。
func unwrapChatInvoker(inv ChatInvoker) ChatInvoker {
	for {
		if r, ok := inv.(*retryInvoker); ok && r != nil {
			if r.Unwrap() == nil {
				return inv
			}
			inv = r.Unwrap()
			continue
		}
		return inv
	}
}

// Invoke satisfies ChatInvoker. It calls the inner invoker up to
// maxRetries+1 times (one initial attempt + maxRetries retries),
// sleeping initialDelay * 2^attempt between failures. The sleep
// honours ctx cancellation: a cancelled context aborts the backoff
// and returns ctx.Err() immediately.
// Invoke 最多调用 maxRetries+1 次，失败间按 2^attempt 退避并尊重 ctx 取消。
func (r *retryInvoker) Invoke(ctx context.Context, req ChatInvokeRequest) (*ChatInvokeResponse, error) {
	if r.inner == nil {
		return nil, fmt.Errorf("component: retryInvoker: nil inner")
	}
	delay := r.initialDelay
	var lastErr error
	for attempt := 0; attempt <= r.maxRetries; attempt++ {
		resp, err := r.inner.Invoke(ctx, req)
		if err == nil {
			return resp, nil
		}
		lastErr = err
		if attempt == r.maxRetries {
			break
		}
		// Honour ctx cancellation during backoff. A short-circuited
		// sleep avoids hanging on shutdown when a long initialDelay
		// would otherwise block the goroutine.
		select {
		case <-ctx.Done():
			return nil, ctx.Err()
		case <-time.After(delay):
		}
		// Cap the doubling at a sane upper bound (1 minute). Without
		// this a misconfigured initialDelay (e.g. 10s) plus 5 retries
		// would sleep 10+20+40+80+160 = 310s before giving up.
		if delay > 0 {
			delay *= 2
			if delay > time.Minute {
				delay = time.Minute
			}
		}
	}
	return nil, fmt.Errorf("component: LLM: chat failed after %d retries: %w", r.maxRetries, lastErr)
}
