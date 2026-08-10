// interrupt.go — 图中断/恢复：GraphInterrupt、resume 值队列与 context 管理。

// Package interrupt 提供 LangGraph Go 可恢复中断机制。
package interrupt

import (
	"context"
	"crypto/sha256"
	"fmt"
	"sync"
	"sync/atomic"

	"ragflow/internal/harness/graph/errors"
	"ragflow/internal/harness/graph/types"
)

// contextKey is the key for interrupt context in context.Context.
type contextKey struct{}

// SubGraphStateCtxKey 子图检查点状态 context 键（loop/parallel 共用）。
// (e.g. Loop iteration, currentInput). Defined here so that both the
// engine (pregel) and the sub-graph node (graph/loop.go) can access it
// without import cycles.
type SubGraphStateCtxKeyType struct{}

var SubGraphStateCtxKey = SubGraphStateCtxKeyType{}

// WithInterruptContext 注入 interruptContext 支持 resume 值消费。
func WithInterruptContext(ctx context.Context) context.Context {
	return context.WithValue(ctx, contextKey{}, &interruptContext{
		resumeValues: make([]interface{}, 0),
		index:        0,
	})
}

// GetInterruptContext 从 context 获取 interruptContext。
func GetInterruptContext(ctx context.Context) *interruptContext {
	if ic, ok := ctx.Value(contextKey{}).(*interruptContext); ok {
		return ic
	}
	return nil
}

// IsInterruptContext 判断 context 是否含中断支持。
func IsInterruptContext(ctx context.Context) bool {
	return GetInterruptContext(ctx) != nil
}

// Interrupt 在节点内触发可恢复 GraphInterrupt；
// The value is surfaced to the client and can be used to request input required to resume execution.
//
// In a given node, the first invocation of this function raises a GraphInterrupt
// exception, halting execution. The provided value is included with the exception
// and sent to the client executing the graph.
//
// A client resuming the graph must use the Command primitive to specify a value
// for the interrupt and continue execution.
// The graph resumes from the start of the node, re-executing all logic.
//
// If a node contains multiple interrupt calls, LangGraph matches resume values
// to interrupts based on their order in the node.
//
// To use an interrupt, you must enable a checkpointer, as the feature relies
// on persisting the graph state.
func Interrupt(ctx context.Context, value interface{}) (interface{}, error) {
	ic := GetInterruptContext(ctx)
	if ic == nil {
		// Fall back to global context for backward compatibility
		ic = globalContext
	}

	// Try to consume the next pending resume value under a single lock
	// (avoids TOCTOU races between separate getResumeValues/getInterruptIndex calls).
	if v, ok := ic.consumeNextResumeValue(); ok {
		return v, nil
	}

	// Check for current resume value
	v := ic.getNullResume()
	if v != nil {
		ic.setNullResume(nil) // consume it before appending
		ic.appendResumeValue(v)
		return v, nil
	}

	// No resume value found, raise interrupt
	return nil, &errors.GraphInterrupt{
		Interrupts: []interface{}{
			&types.Interrupt{
				Value: value,
				ID:    generateInterruptID(value),
			},
		},
	}
}

// interruptContext 维护 resume 值队列与消费索引。
type interruptContext struct {
	mu           sync.Mutex
	resumeValues []interface{}
	index        int
	nullResume   interface{}
}

// Global context for backward compatibility
// interruptIDCounter provides unique IDs across all interrupt points in the process.
var interruptIDCounter int64

var globalContext = &interruptContext{
	resumeValues: make([]interface{}, 0),
	index:        0,
}

// consumeNextResumeValue 原子消费下一个 resume 值并推进索引。
// the index under a single lock (avoids TOCTOU between separate lock acquisitions).
func (ic *interruptContext) consumeNextResumeValue() (interface{}, bool) {
	if ic == nil {
		return nil, false
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	if ic.index < len(ic.resumeValues) {
		v := ic.resumeValues[ic.index]
		ic.index++
		return v, true
	}
	return nil, false
}

// getResumeValues returns a copy of the current resume values.
func (ic *interruptContext) getResumeValues() []interface{} {
	if ic == nil {
		return nil
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	result := make([]interface{}, len(ic.resumeValues))
	copy(result, ic.resumeValues)
	return result
}

// getInterruptIndex returns the current interrupt index.
func (ic *interruptContext) getInterruptIndex() int {
	if ic == nil {
		return 0
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	return ic.index
}

// getNullResume checks for a null resume value.
func (ic *interruptContext) getNullResume() interface{} {
	if ic == nil {
		return nil
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	return ic.nullResume
}

// appendResumeValue appends a resume value.
func (ic *interruptContext) appendResumeValue(v interface{}) {
	if ic == nil {
		return
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	ic.resumeValues = append(ic.resumeValues, v)
}

// setNullResume sets the null resume value.
func (ic *interruptContext) setNullResume(v interface{}) {
	if ic == nil {
		return
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	ic.nullResume = v
}

// setResumeValues replaces the resume values.
func (ic *interruptContext) setResumeValues(values []interface{}) {
	if ic == nil {
		return
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	ic.resumeValues = values
}

// reset clears all interrupt context fields.
func (ic *interruptContext) reset() {
	if ic == nil {
		return
	}
	ic.mu.Lock()
	defer ic.mu.Unlock()
	ic.resumeValues = make([]interface{}, 0)
	ic.index = 0
	ic.nullResume = nil
}

// GetResumeValues 返回 resume 值副本（loop/parallel 恢复用）。
func GetResumeValues(ctx context.Context) []interface{} {
	var ic *interruptContext
	if ctx != nil {
		ic = GetInterruptContext(ctx)
	}
	if ic == nil {
		ic = globalContext
	}
	return ic.getResumeValues()
}

// GetInterruptIndex returns the current interrupt index from context.
func GetInterruptIndex(ctx context.Context) int {
	ic := GetInterruptContext(ctx)
	if ic == nil {
		ic = globalContext
	}
	return ic.getInterruptIndex()
}

// AppendResumeValue appends a resume value to the context.
func AppendResumeValue(ctx context.Context, value interface{}) {
	var ic *interruptContext
	if ctx != nil {
		ic = GetInterruptContext(ctx)
	}
	if ic == nil {
		ic = globalContext
	}
	ic.appendResumeValue(value)
}

// GetNullResume gets the null resume value from context.
// If consume is true, the value is cleared after retrieval.
func GetNullResume(ctx context.Context, consume bool) interface{} {
	ic := GetInterruptContext(ctx)
	if ic == nil {
		ic = globalContext
	}
	v := ic.getNullResume()
	if consume {
		ic.setNullResume(nil)
	}
	return v
}

// Reset 重置中断上下文；仅重置当前请求 context，避免污染全局。
// When a per-request context is found, only that context is reset.
// The global fallback context is only reset when no per-request context
// exists, preventing concurrent requests from corrupting each other.
func Reset(ctx context.Context) {
	ic := GetInterruptContext(ctx)
	if ic != nil {
		ic.reset()
		return
	}
	globalContext.reset()
}

// generateInterruptID 基于值哈希与进程计数器生成唯一中断 ID。
// The ID combines a hash of the value with a process-unique counter so that
// two interrupts with the same value (e.g. "Please provide input") are still
// distinguishable.
func generateInterruptID(value interface{}) string {
	h := sha256.Sum256([]byte(fmt.Sprintf("%v", value)))
	n := atomic.AddInt64(&interruptIDCounter, 1)
	return fmt.Sprintf("%x_%d", h[:8], n)
}

// IsInterrupt 判断 error 是否为 GraphInterrupt。
func IsInterrupt(err error) bool {
	return errors.IsGraphInterrupt(err)
}

// GetInterruptValue 从 GraphInterrupt 解包用户传入的原始 value。
// Unlike returning the *types.Interrupt envelope directly, this unwraps to the .Value field
// so callers get the value they originally passed to Interrupt(ctx, value).
func GetInterruptValue(err error) (interface{}, bool) {
	if !errors.IsGraphInterrupt(err) {
		return nil, false
	}

	if gi, ok := err.(*errors.GraphInterrupt); ok && len(gi.Interrupts) > 0 {
		if intr, ok := gi.Interrupts[0].(*types.Interrupt); ok {
			return intr.Value, true
		}
		return gi.Interrupts[0], true
	}

	return nil, false
}

// SetResumeValues 测试用：预设 resume 值队列。
func SetResumeValues(ctx context.Context, values []interface{}) {
	ic := GetInterruptContext(ctx)
	if ic == nil {
		ic = globalContext
	}
	ic.setResumeValues(values)
}

// 同一节点多次 Interrupt 按调用顺序匹配 resume 值；须启用 checkpointer。
