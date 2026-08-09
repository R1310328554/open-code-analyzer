// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"fmt"
	"reflect"
	"strings"

	"github.com/gin-gonic/gin/codec/json"
)

// ErrorType 是 gin 规范定义的 64 位无符号错误码。
type ErrorType uint64

const (
	// ErrorTypeBind 在 Context.Bind() 失败时使用。
	ErrorTypeBind ErrorType = 1 << 63
	// ErrorTypeRender 在 Context.Render() 失败时使用。
	ErrorTypeRender ErrorType = 1 << 62
	// ErrorTypePrivate 表示私有错误。
	ErrorTypePrivate ErrorType = 1 << 0
	// ErrorTypePublic 表示公开错误。
	ErrorTypePublic ErrorType = 1 << 1
	// ErrorTypeAny 表示任意类型的错误。
	ErrorTypeAny ErrorType = 1<<64 - 1
)

// Error 表示一条错误的完整描述。
type Error struct {
	Err  error
	Type ErrorType
	Meta any
}

type errorMsgs []*Error

var _ error = (*Error)(nil)

// SetType 设置错误类型。
func (msg *Error) SetType(flags ErrorType) *Error {
	msg.Type = flags
	return msg
}

// SetMeta 设置错误的元数据。
func (msg *Error) SetMeta(data any) *Error {
	msg.Meta = data
	return msg
}

// JSON 生成格式正确的 JSON 表示。
func (msg *Error) JSON() any {
	jsonData := H{}
	if msg.Meta != nil {
		value := reflect.ValueOf(msg.Meta)
		switch value.Kind() {
		case reflect.Struct:
			return msg.Meta
		case reflect.Map:
			for _, key := range value.MapKeys() {
				jsonData[key.String()] = value.MapIndex(key).Interface()
			}
		default:
			jsonData["meta"] = msg.Meta
		}
	}
	if _, ok := jsonData["error"]; !ok {
		jsonData["error"] = msg.Error()
	}
	return jsonData
}

// MarshalJSON 实现 json.Marshaller 接口。
func (msg *Error) MarshalJSON() ([]byte, error) {
	return json.API.Marshal(msg.JSON())
}

// Error 实现 error 接口。
func (msg Error) Error() string {
	return msg.Err.Error()
}

// IsType 判断错误是否属于指定类型。
func (msg *Error) IsType(flags ErrorType) bool {
	return (msg.Type & flags) > 0
}

// Unwrap 返回被包装的错误，以便与 errors.Is()、errors.As() 和 errors.Unwrap() 互操作。
func (msg Error) Unwrap() error {
	return msg.Err
}

// ByType 返回按类型过滤后的只读副本。
// 例如 ByType(gin.ErrorTypePublic) 返回类型为 ErrorTypePublic 的错误切片。
func (a errorMsgs) ByType(typ ErrorType) errorMsgs {
	if len(a) == 0 {
		return nil
	}
	if typ == ErrorTypeAny {
		return a
	}
	var result errorMsgs
	for _, msg := range a {
		if msg.IsType(typ) {
			result = append(result, msg)
		}
	}
	return result
}

// Last 返回切片中的最后一条错误；若切片为空则返回 nil。
// 等价于 errors[len(errors)-1] 的快捷写法。
func (a errorMsgs) Last() *Error {
	if length := len(a); length > 0 {
		return a[length-1]
	}
	return nil
}

// Errors 返回所有错误消息的字符串数组。
// 示例：
//
//	c.Error(errors.New("first"))
//	c.Error(errors.New("second"))
//	c.Error(errors.New("third"))
//	c.Errors.Errors() // == []string{"first", "second", "third"}
func (a errorMsgs) Errors() []string {
	if len(a) == 0 {
		return nil
	}
	errorStrings := make([]string, len(a))
	for i, err := range a {
		errorStrings[i] = err.Error()
	}
	return errorStrings
}

func (a errorMsgs) JSON() any {
	switch length := len(a); length {
	case 0:
		return nil
	case 1:
		return a.Last().JSON()
	default:
		jsonData := make([]any, length)
		for i, err := range a {
			jsonData[i] = err.JSON()
		}
		return jsonData
	}
}

// MarshalJSON 实现 json.Marshaller 接口。
func (a errorMsgs) MarshalJSON() ([]byte, error) {
	return json.API.Marshal(a.JSON())
}

func (a errorMsgs) String() string {
	if len(a) == 0 {
		return ""
	}
	var buffer strings.Builder
	for i, msg := range a {
		fmt.Fprintf(&buffer, "Error #%02d: %s\n", i+1, msg.Err)
		if msg.Meta != nil {
			fmt.Fprintf(&buffer, "     Meta: %v\n", msg.Meta)
		}
	}
	return buffer.String()
}
