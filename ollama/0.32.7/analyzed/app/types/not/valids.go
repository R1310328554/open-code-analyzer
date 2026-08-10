//go:build windows || darwin

// not 包还提供字段级校验错误类型，便于聚合多条验证失败信息。
package not

import (
	"fmt"
)

// ValidError 表示单个字段的校验失败，含字段名与格式化消息。
type ValidError struct {
	name string
	msg  string
	args []any
}

// Valid 构造带字段名与格式化消息的校验错误。
//
// Valid returns a new validation error with the given name and message.
func Valid(name, message string, args ...any) error {
	return ValidError{name, message, args}
}

// Message 返回格式化后的校验错误消息。
//
// Message returns the formatted message for the validation error.
func (e *ValidError) Message() string {
	return fmt.Sprintf(e.msg, e.args...)
}

// Error 实现 error 接口，输出 "invalid <字段>: <消息>" 形式。
//
// Error implements the error interface.
func (e ValidError) Error() string {
	return fmt.Sprintf("invalid %s: %s", e.name, e.Message())
}

// Field 返回校验失败的字段名。
func (e ValidError) Field() string {
	return e.name
}

// Valids 用于收集多条校验错误并合并为一条 error。
//
// Valids is for building a list of validation errors.
type Valids []ValidError

// Addf 追加一条带 fmt.Sprintf 格式化消息的校验错误。
//
// Addf adds a validation error to the list with a formatted message using fmt.Sprintf.
func (b *Valids) Add(name, message string, args ...any) {
	*b = append(*b, ValidError{name, message, args})
}

// Error 将多条校验错误用分号 连接为单个字符串；无错误时返回空串。
func (b Valids) Error() string {
	if len(b) == 0 {
		return ""
	}

	var result string
	for i, err := range b {
		if i > 0 {
			result += "; "
		}
		result += err.Error()
	}
	return result
}
