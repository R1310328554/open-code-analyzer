package core

// tool_interrupt.go — 工具级中断：ToolInterruptError 与恢复状态存取。


import (
	"context"
	"errors"
	"fmt"
)

// ToolInterruptError 工具执行中主动触发的中断错误。
// ToolsNode 收到后将状态写入 ToolExecutedCache 并向上传播；
// 恢复时使用缓存结果，不再重新调用工具。
// checkpointing. On resume, the cached result is used and the tool is not re-invoked.
type ToolInterruptError struct {
	// Info 面向用户的中断说明信息。
	Info any
	// State 检查点中保存的内部状态，恢复时还原。
	State any
}

func (e *ToolInterruptError) Error() string {
	return fmt.Sprintf("tool interrupt: %v", e.Info)
}

// ToolInterrupt 创建无状态工具中断错误。
// The tool should return this error from Invoke to pause execution and trigger
// a checkpoint. The interrupt info is saved and can be inspected on resume.
//
// Example:
//
//	func (t *MyTool) Invoke(ctx, args string, opts ...) (string, error) {
//	    if needsApproval(args) {
//	        return "", ToolInterrupt(ctx, "needs user approval")
//	    }
//	    return doWork(args), nil
//	}
func ToolInterrupt(ctx context.Context, info any) error {
	return &ToolInterruptError{Info: info}
}

// ToolStatefulInterrupt 创建带持久化状态的中断错误。
// The state is restored via GetToolInterruptState on resume.
func ToolStatefulInterrupt(ctx context.Context, info, state any) error {
	return &ToolInterruptError{Info: info, State: state}
}

// IsToolInterrupt 判断错误是否为工具中断并返回解析结果。
// parsed ToolInterruptError if so.
func IsToolInterrupt(err error) (*ToolInterruptError, bool) {
	var tie *ToolInterruptError
	if errors.As(err, &tie) {
		return tie, true
	}
	return nil, false
}

// toolInterruptContextKey stores ToolInterruptError state across resume.
type toolInterruptContextKey struct{}

// setToolInterruptState 将中断状态写入上下文供恢复使用。
func setToolInterruptState(ctx context.Context, tie *ToolInterruptError) context.Context {
	return context.WithValue(ctx, toolInterruptContextKey{}, tie.State)
}

// getToolInterruptState 从上下文读取恢复时的中断状态。
// Returns the saved state (nil if none) and true if this is a resume from interrupt.
func getToolInterruptState(ctx context.Context) (state any, wasInterrupted bool) {
	s := ctx.Value(toolInterruptContextKey{})
	return s, s != nil
}

// GetToolInterruptState 泛型读取类型化中断状态，便于工具检测恢复场景。
// Useful for tools to detect if they are being resumed after an interrupt.
//
// Example:
//
//	func (t *MyTool) Invoke(ctx, args string, opts ...) (string, error) {
//	    state, wasInterrupted := GetToolInterruptState[MyState](ctx)
//	    if wasInterrupted {
//	        return continueFrom(state), nil  // resume from saved state
//	    }
//	    return "", ToolStatefulInterrupt(ctx, "paused", MyState{Step: 1})
//	}
func GetToolInterruptState[T any](ctx context.Context) (state T, wasInterrupted bool) {
	s, ok := ctx.Value(toolInterruptContextKey{}).(T)
	if ok {
		return s, true
	}
	var zero T
	return zero, false
}
