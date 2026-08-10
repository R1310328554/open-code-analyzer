// Package pregel 为 Pregel 节点执行提供增强重试策略。
//
// RetryExecutor 封装指数退避、抖动与可配置重试谓词。
package pregel

import (
	"context"
	"fmt"
	"time"

	"ragflow/internal/harness/graph/errors"
	"ragflow/internal/harness/graph/types"
)

// RetryExecutor 带重试逻辑的节点执行器。
type RetryExecutor struct {
	policy *types.RetryPolicy
}

// NewRetryExecutor 创建重试执行器；policy 为 nil 时用默认策略。
func NewRetryExecutor(policy *types.RetryPolicy) *RetryExecutor {
	if policy == nil {
		defaultPolicy := types.DefaultRetryPolicy()
		policy = &defaultPolicy
	}
	return &RetryExecutor{policy: policy}
}

// Execute 执行 fn，失败时按策略退避重试；GraphInterrupt 立即传播。
func (e *RetryExecutor) Execute(ctx context.Context, name string, fn func(context.Context) (any, error)) (output any, err error) {
	defer func() {
		if r := recover(); r != nil {
			output = nil
			err = fmt.Errorf("node %s panicked: %v", name, r)
		}
	}()

	var lastErr error
	var lastOutput any

	for attempt := 1; attempt <= e.policy.MaxAttempts; attempt++ {
		// Execute the function
		output, err = fn(ctx)
		if err == nil {
			return output, nil
		}

		// Check if this is a non-retryable error
		if errors.IsGraphInterrupt(err) {
			return nil, err // propagate GraphInterrupt immediately without wrapping
		}
		if e.policy.RetryOn != nil && !e.policy.RetryOn(err) {
			return nil, fmt.Errorf("node %s failed with non-retryable error: %w", name, err)
		}

		lastErr = err
		lastOutput = output

		// If we've exhausted attempts, break
		if attempt >= e.policy.MaxAttempts {
			break
		}

		// Calculate backoff with jitter
		backoff := e.calculateBackoff(attempt)

		// Wait before retry
		select {
		case <-ctx.Done():
			return nil, fmt.Errorf("node %s cancelled during retry: %w", name, ctx.Err())
		case <-time.After(backoff):
			// Continue to next attempt
		}
	}

	return nil, &RetryExhaustedError{
		NodeName:   name,
		Attempts:   e.policy.MaxAttempts,
		LastErr:    lastErr,
		LastOutput: lastOutput,
	}
}

// calculateBackoff 计算退避时长（委托 RetryPolicy.CalculateBackoff）。
// Delegates to the shared RetryPolicy.CalculateBackoff method.
func (e *RetryExecutor) calculateBackoff(attempt int) time.Duration {
	if e.policy == nil {
		defaultPolicy := types.DefaultRetryPolicy()
		return defaultPolicy.CalculateBackoff(attempt)
	}
	return e.policy.CalculateBackoff(attempt)
}

// RetryExhaustedError 重试次数耗尽时返回。
type RetryExhaustedError struct {
	NodeName   string
	Attempts   int
	LastErr    error
	LastOutput any
}

// Error 实现 error 接口。
func (e *RetryExhaustedError) Error() string {
	return fmt.Sprintf("node %s failed after %d attempts: %v", e.NodeName, e.Attempts, e.LastErr)
}

// Unwrap 返回底层错误。
func (e *RetryExhaustedError) Unwrap() error {
	return e.LastErr
}

// IsRetryExhausted 判断是否为重试耗尽错误。
func IsRetryExhausted(err error) bool {
	_, ok := err.(*RetryExhaustedError)
	return ok
}

// RetryPredicates 常用重试条件谓词集合。
var RetryPredicates = struct {
	Always          func(error) bool
	Never           func(error) bool
	NetworkErrors   func(error) bool
	TemporaryErrors func(error) bool
}{
	Always: func(error) bool {
		return true
	},
	Never: func(error) bool {
		return false
	},
	// NetworkErrors retries on common network-related errors
	NetworkErrors: func(err error) bool {
		if err == nil {
			return false
		}
		// Check for common network error patterns
		errMsg := err.Error()
		networkKeywords := []string{
			"connection refused",
			"connection reset",
			"timeout",
			"network",
			"dns",
			"temporary failure",
			"503", // Service Unavailable
			"502", // Bad Gateway
			"504", // Gateway Timeout
		}
		for _, kw := range networkKeywords {
			if contains(errMsg, kw) {
				return true
			}
		}
		return false
	},
	// TemporaryErrors retries on errors that might be transient
	TemporaryErrors: func(err error) bool {
		if err == nil {
			return false
		}
		// Check for temporary error patterns
		errMsg := err.Error()
		tempKeywords := []string{
			"temporary",
			"transient",
			"rate limit",
			"too many requests",
			"429", // Too Many Requests
		}
		for _, kw := range tempKeywords {
			if contains(errMsg, kw) {
				return true
			}
		}
		return false
	},
}

// contains 检查字符串是否包含子串（大小写不敏感）。
func contains(s, substr string) bool {
	return len(s) >= len(substr) &&
		(s == substr ||
			len(s) > len(substr) &&
				(s[0:len(substr)] == substr ||
					containsIgnoreCase(s, substr)))
}

// containsIgnoreCase 大小写不敏感子串检查。
func containsIgnoreCase(s, substr string) bool {
	s = toLower(s)
	substr = toLower(substr)
	return len(s) >= len(substr) && s[:len(substr)] == substr
}

// toLower 将 ASCII 字符串转为小写。
func toLower(s string) string {
	result := make([]byte, len(s))
	for i := 0; i < len(s); i++ {
		c := s[i]
		if c >= 'A' && c <= 'Z' {
			c += 'a' - 'A'
		}
		result[i] = c
	}
	return string(result)
}

// RetryConfig 重试行为配置（含回调）。
type RetryConfig struct {
	// Policy is the retry policy to use
	Policy *types.RetryPolicy
	// OnRetry is called after each failed attempt
	OnRetry func(attempt int, err error)
	// OnSuccess is called on successful completion
	OnSuccess func(attempt int)
}

// NewRetryConfig 创建默认重试配置。
func NewRetryConfig() *RetryConfig {
	defaultPolicy := types.DefaultRetryPolicy()
	return &RetryConfig{
		Policy: &defaultPolicy,
	}
}

// WithRetryOn 设置重试条件谓词。
func (c *RetryConfig) WithRetryOn(predicate func(error) bool) *RetryConfig {
	c.Policy.RetryOn = predicate
	return c
}

// WithMaxAttempts 设置最大尝试次数。
func (c *RetryConfig) WithMaxAttempts(maxAttempts int) *RetryConfig {
	c.Policy.MaxAttempts = maxAttempts
	return c
}

// WithBackoff 设置退避参数（初始/最大间隔、因子）。
func (c *RetryConfig) WithBackoff(initial, max time.Duration, factor float64) *RetryConfig {
	c.Policy.InitialInterval = initial
	c.Policy.MaxInterval = max
	c.Policy.BackoffFactor = factor
	return c
}

// WithJitter 启用/禁用抖动。
func (c *RetryConfig) WithJitter(enabled bool) *RetryConfig {
	c.Policy.Jitter = enabled
	return c
}

// WithOnRetry 设置每次重试后的回调。
func (c *RetryConfig) WithOnRetry(callback func(attempt int, err error)) *RetryConfig {
	c.OnRetry = callback
	return c
}

// WithOnSuccess 设置成功完成后的回调。
func (c *RetryConfig) WithOnSuccess(callback func(attempt int)) *RetryConfig {
	c.OnSuccess = callback
	return c
}
