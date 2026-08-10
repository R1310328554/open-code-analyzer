// errors.go — Harness 图引擎错误码、上下文包装与专用异常类型。

// Package errors 定义 Agent Harness Go 错误类型与辅助函数。
package errors

import (
	"fmt"
	"runtime"
	"strings"
)

// ErrorCode 机器可读错误码枚举。
type ErrorCode string

const (
	// ErrorCodeGraphRecursionLimit 图超步/递归深度耗尽
	ErrorCodeGraphRecursionLimit ErrorCode = "GRAPH_RECURSION_LIMIT"
	// ErrorCodeInvalidConcurrentGraphUpdate 非法并发图更新
	ErrorCodeInvalidConcurrentGraphUpdate ErrorCode = "INVALID_CONCURRENT_GRAPH_UPDATE"
	// ErrorCodeInvalidGraphNodeReturnValue 节点返回值类型非法
	ErrorCodeInvalidGraphNodeReturnValue ErrorCode = "INVALID_GRAPH_NODE_RETURN_VALUE"
	// ErrorCodeMultipleSubgraphs 检测到多个子图冲突
	ErrorCodeMultipleSubgraphs ErrorCode = "MULTIPLE_SUBGRAPHS"
	// ErrorCodeInvalidChatHistory 聊天历史格式非法
	ErrorCodeInvalidChatHistory ErrorCode = "INVALID_CHAT_HISTORY"
	// ErrorCodeCheckpointConflict 检查点版本冲突
	ErrorCodeCheckpointConflict ErrorCode = "CHECKPOINT_CONFLICT"
	// ErrorCodeInvalidState 状态校验失败
	ErrorCodeInvalidState ErrorCode = "INVALID_STATE"
	// ErrorCodeNodeNotFound 节点不存在
	ErrorCodeNodeNotFound ErrorCode = "NODE_NOT_FOUND"
	// ErrorCodeChannelNotFound 通道不存在
	ErrorCodeChannelNotFound ErrorCode = "CHANNEL_NOT_FOUND"
	// ErrorCodeTimeout 执行超时
	ErrorCodeTimeout ErrorCode = "TIMEOUT"
	// ErrorCodeCancellation 执行被取消或中断
	ErrorCodeCancellation ErrorCode = "CANCELLATION"
)

// CreateErrorMessage 生成带文档链接的排障友好错误信息。
// The URL points to the Harness-Go documentation (not the Python LangGraph docs).
func CreateErrorMessage(message string, errorCode ErrorCode) string {
	return fmt.Sprintf(
		"%s\nFor troubleshooting, visit: https://ragflow/internal/harness/docs/errors/%s",
		message,
		errorCode,
	)
}

// ErrorContext 带错误码、堆栈与元数据的富错误上下文。
type ErrorContext struct {
	// ErrorCode is the specific error code.
	ErrorCode ErrorCode
	// Message is the error message.
	Message string
	// StackTrace is the stack trace at the point of error.
	StackTrace []string
	// Cause is the underlying cause of this error.
	Cause error
	// Metadata contains additional error metadata.
	Metadata map[string]interface{}
}

// NewErrorContext 捕获堆栈并包装底层 cause。
func NewErrorContext(code ErrorCode, message string, cause error) *ErrorContext {
	return &ErrorContext{
		ErrorCode:  code,
		Message:    message,
		StackTrace: captureStackTrace(2), // Skip captureStackTrace and NewErrorContext
		Cause:      cause,
		Metadata:   make(map[string]interface{}),
	}
}

// Error returns the error message with context.
func (ec *ErrorContext) Error() string {
	var sb strings.Builder

	sb.WriteString(fmt.Sprintf("[%s] %s", ec.ErrorCode, ec.Message))

	if ec.Cause != nil {
		sb.WriteString(fmt.Sprintf("\nCaused by: %s", ec.Cause.Error()))
	}

	if len(ec.StackTrace) > 0 {
		sb.WriteString("\nStack trace:")
		for _, frame := range ec.StackTrace {
			sb.WriteString(fmt.Sprintf("\n  %s", frame))
		}
	}

	if len(ec.Metadata) > 0 {
		sb.WriteString("\nMetadata:")
		for k, v := range ec.Metadata {
			sb.WriteString(fmt.Sprintf("\n  %s: %v", k, v))
		}
	}

	return sb.String()
}

// Unwrap returns the underlying cause.
func (ec *ErrorContext) Unwrap() error {
	return ec.Cause
}

// AddMetadata adds metadata to the error context.
func (ec *ErrorContext) AddMetadata(key string, value interface{}) {
	if ec.Metadata == nil {
		ec.Metadata = make(map[string]interface{})
	}
	ec.Metadata[key] = value
}

// GetMetadata gets metadata from the error context.
func (ec *ErrorContext) GetMetadata(key string) (interface{}, bool) {
	if ec.Metadata == nil {
		return nil, false
	}
	val, ok := ec.Metadata[key]
	return val, ok
}

// captureStackTrace captures the current stack trace.
func captureStackTrace(skip int) []string {
	var stack []string
	pcs := make([]uintptr, 32)
	n := runtime.Callers(skip, pcs)
	if n == 0 {
		return stack
	}

	frames := runtime.CallersFrames(pcs[:n])
	for {
		frame, more := frames.Next()
		stack = append(stack, fmt.Sprintf("%s\n\t%s:%d", frame.Function, frame.File, frame.Line))
		if !more {
			break
		}
	}

	return stack
}

// WrapError 为已有错误追加 ErrorContext 层。
func WrapError(err error, code ErrorCode, message string) error {
	if err == nil {
		return nil
	}

	// If it's already an ErrorContext, just add to it
	if ec, ok := err.(*ErrorContext); ok {
		return &ErrorContext{
			ErrorCode:  code,
			Message:    message,
			StackTrace: captureStackTrace(2),
			Cause:      ec,
			Metadata:   make(map[string]interface{}),
		}
	}

	return NewErrorContext(code, message, err)
}

// GetErrorCode 从错误链提取 ErrorCode。
func GetErrorCode(err error) ErrorCode {
	if err == nil {
		return ""
	}

	// Check for ErrorContext
	if ec, ok := err.(*ErrorContext); ok {
		return ec.ErrorCode
	}

	// Check for specific error types
	if IsGraphRecursionError(err) {
		return ErrorCodeGraphRecursionLimit
	}
	if IsGraphInterrupt(err) {
		return ErrorCodeCancellation
	}
	if IsParentCommand(err) {
		return ErrorCodeInvalidConcurrentGraphUpdate
	}

	return ""
}

// GetErrorStack 提取 ErrorContext 堆栈帧。
func GetErrorStack(err error) []string {
	if err == nil {
		return nil
	}

	if ec, ok := err.(*ErrorContext); ok {
		return ec.StackTrace
	}

	return nil
}

// FormatError 格式化多层包装错误供展示。
func FormatError(err error) string {
	if err == nil {
		return ""
	}

	var sb strings.Builder

	current := err
	depth := 0
	for current != nil && depth < 10 { // Prevent infinite loops
		prefix := strings.Repeat("  ", depth)
		sb.WriteString(fmt.Sprintf("%s%s\n", prefix, current.Error()))

		// Check for wrapped error
		if unwrapped := fmt.Sprintf("%v", err); unwrapped != current.Error() {
			current = fmt.Errorf("%s", unwrapped)
		} else {
			current = nil
		}

		depth++
	}

	return sb.String()
}

// ChainError 链接两个错误形成 cause 链。
func ChainError(base error, newErr error) error {
	if newErr == nil {
		return base
	}

	if base == nil {
		return newErr
	}

	return fmt.Errorf("%s: %w", newErr, base)
}

// EmptyChannelError 通道尚未写入任何值。
type EmptyChannelError struct {
	Message string
}

func (e *EmptyChannelError) Error() string {
	if e.Message != "" {
		return e.Message
	}
	return "channel is empty"
}

// IsEmptyChannelError checks if an error is an EmptyChannelError.
func IsEmptyChannelError(err error) bool {
	_, ok := err.(*EmptyChannelError)
	return ok
}

// GraphRecursionError Pregel 递归/超步上限触达。
type GraphRecursionError struct {
	Limit int
}

func (e *GraphRecursionError) Error() string {
	return fmt.Sprintf(
		"Graph recursion limit of %d reached. To increase the limit, "+
			"run your graph with a config specifying a higher recursion_limit.",
		e.Limit,
	)
}

// IsGraphRecursionError checks if an error is a GraphRecursionError.
func IsGraphRecursionError(err error) bool {
	_, ok := err.(*GraphRecursionError)
	return ok
}

// InvalidUpdateError 通道更新参数非法。
type InvalidUpdateError struct {
	Message string
}

func (e *InvalidUpdateError) Error() string {
	return fmt.Sprintf("Invalid update: %s", e.Message)
}

// IsInvalidUpdateError checks if an error is an InvalidUpdateError.
func IsInvalidUpdateError(err error) bool {
	_, ok := err.(*InvalidUpdateError)
	return ok
}

// GraphBubbleUp 子图向上冒泡异常的基类。
type GraphBubbleUp struct {
	Message string
	Cause   error
}

func (e *GraphBubbleUp) Error() string {
	if e.Message != "" {
		return e.Message
	}
	if e.Cause != nil {
		return e.Cause.Error()
	}
	return "graph bubble up"
}

func (e *GraphBubbleUp) Unwrap() error {
	return e.Cause
}

// GraphInterrupt 图执行被 interrupt.Interrupt 暂停。
type GraphInterrupt struct {
	Interrupts []interface{}
}

func (e *GraphInterrupt) Error() string {
	return fmt.Sprintf("graph interrupted with %d interrupt(s)", len(e.Interrupts))
}

// IsGraphInterrupt checks if an error is a GraphInterrupt.
func IsGraphInterrupt(err error) bool {
	_, ok := err.(*GraphInterrupt)
	return ok
}

// ParentCommand 子图请求向父图发送 Command。
type ParentCommand struct {
	Command interface{}
}

func (e *ParentCommand) Error() string {
	return "parent command"
}

// IsParentCommand checks if an error is a ParentCommand.
func IsParentCommand(err error) bool {
	_, ok := err.(*ParentCommand)
	return ok
}

// EmptyInputError 图收到空输入。
type EmptyInputError struct {
	Message string
}

func (e *EmptyInputError) Error() string {
	if e.Message != "" {
		return e.Message
	}
	return "empty input"
}

// IsEmptyInputError checks if an error is an EmptyInputError.
func IsEmptyInputError(err error) bool {
	_, ok := err.(*EmptyInputError)
	return ok
}

// TaskNotFound 执行器找不到指定任务。
type TaskNotFound struct {
	TaskID string
}

func (e *TaskNotFound) Error() string {
	return fmt.Sprintf("task not found: %s", e.TaskID)
}

// IsTaskNotFound checks if an error is a TaskNotFound.
func IsTaskNotFound(err error) bool {
	_, ok := err.(*TaskNotFound)
	return ok
}

// InvalidNodeError 节点定义或配置非法。
type InvalidNodeError struct {
	NodeName string
	Message  string
}

func (e *InvalidNodeError) Error() string {
	return fmt.Sprintf("invalid node '%s': %s", e.NodeName, e.Message)
}

// InvalidEdgeError 边定义非法。
type InvalidEdgeError struct {
	From    string
	To      string
	Message string
}

func (e *InvalidEdgeError) Error() string {
	return fmt.Sprintf("invalid edge from '%s' to '%s': %s", e.From, e.To, e.Message)
}

// ChannelNotFoundError 通道名未注册。
type ChannelNotFoundError struct {
	ChannelName string
}

func (e *ChannelNotFoundError) Error() string {
	return fmt.Sprintf("channel not found: %s", e.ChannelName)
}

// NodeNotFoundError 节点名不存在于图中。
type NodeNotFoundError struct {
	NodeName string
}

func (e *NodeNotFoundError) Error() string {
	return fmt.Sprintf("node not found: %s", e.NodeName)
}

// IsGraphInterrupt/IsParentCommand 等类型断言辅助用于引擎分支处理。
