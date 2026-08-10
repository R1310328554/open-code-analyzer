package wire

// wire.Error 表示对端 Nack 帧携带的 HTTP 语义错误，调度器据此识别 429 过载等场景。

import (
	"fmt"
)

// Error 含 Code 与 Message，实现 error 接口并支持 errors.Is 精确匹配。
// Error represents an error received in response to a message.
type Error struct {
	// Code is an HTTP status code representing the kind of error received.
	Code int32

	// Message is a human-readable description of the error.
	Message string
}

var _ error = (*Error)(nil)

// Errorf 构造带 HTTP 状态码的 wire 错误，常用于 NackFrame 响应。
// Errorf creates a new Error with the given code and formatted message.
func Errorf(code int32, format string, args ...interface{}) *Error {
	return &Error{
		Code:    code,
		Message: fmt.Sprintf(format, args...),
	}
}

// Is returns true if the target is identical to the error, providing
// functionality for [errors.Is].
func (e *Error) Is(target error) bool {
	other, ok := target.(*Error)
	if !ok {
		return false
	}
	return other.Code == e.Code && other.Message == e.Message
}

// Error returns the message of the error.
func (e *Error) Error() string { return e.Message }
// Is 同时比较 Code 与 Message 以支持 errors.Is 链式判断。
