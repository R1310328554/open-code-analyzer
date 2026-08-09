// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"flag"
	"io"
	"os"
	"sync/atomic"

	"github.com/gin-gonic/gin/binding"
)

// EnvGinMode 表示 gin 模式的环境名称。
const EnvGinMode = "GIN_MODE"

const (
	// DebugMode 表示 gin 模式为 debug。
	DebugMode = "debug"
	// ReleaseMode表示gin模式已释放。
	ReleaseMode = "release"
	// TestMode 表示 gin 模式正在测试。
	TestMode = "test"
)

const (
	debugCode = iota
	releaseCode
	testCode
)

// DefaultWriter 是 Gin 用于调试输出的默认 io.Writer
//  中间件输出，如 Logger() 或 Recovery()。
//  请注意，Logger 和 Recovery 都提供了自定义方法来配置其
//  输出 io.Writer。
//  要支持 Windows 中的着色，请使用：
//
// 	import "github.com/mattn/go-colorable"
// 	gin.DefaultWriter = colorable.NewColorableStdout()
var DefaultWriter io.Writer = os.Stdout

// DefaultErrorWriter 是 Gin 用来调试错误的默认 io.Writer
var DefaultErrorWriter io.Writer = os.Stderr

var (
	ginMode  int32 = debugCode
	modeName atomic.Value
)

func init() {
	mode := os.Getenv(EnvGinMode)
	SetMode(mode)
}

// SetMode 根据输入字符串设置 gin 模式。
func SetMode(value string) {
	if value == "" {
		if flag.Lookup("test.v") != nil {
			value = TestMode
		} else {
			value = DebugMode
		}
	}

	switch value {
	case DebugMode:
		atomic.StoreInt32(&ginMode, debugCode)
	case ReleaseMode:
		atomic.StoreInt32(&ginMode, releaseCode)
	case TestMode:
		atomic.StoreInt32(&ginMode, testCode)
	default:
		panic("gin mode unknown: " + value + " (available mode: debug release test)")
	}
	modeName.Store(value)
}

// DisableBindValidation 关闭默认验证器。
func DisableBindValidation() {
	binding.Validator = nil
}

// EnableJsonDecoderUseNumber 将绑定设置为 true.EnableDecoderUseNumber 为
//  在 JSON Decoder 实例上调用 UseNumber 方法。
func EnableJsonDecoderUseNumber() {
	binding.EnableDecoderUseNumber = true
}

// EnableJsonDecoderDisallowUnknownFields 设置 true 进行绑定。EnableDecoderDisallowUnknownFields 为
//  在 JSON Decoder 实例上调用 DisallowUnknownFields 方法。
func EnableJsonDecoderDisallowUnknownFields() {
	binding.EnableDecoderDisallowUnknownFields = true
}

// 模式返回当前杜松子酒模式。
func Mode() string {
	return modeName.Load().(string)
}
