// Copyright 2014 Manu Martinez-Almeida. All rights reserved.
// Use of this source code is governed by a MIT style
// license that can be found in the LICENSE file.

package gin

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"time"

	"github.com/mattn/go-isatty"
)

type consoleColorModeValue int

const (
	autoColor consoleColorModeValue = iota
	disableColor
	forceColor
)

const (
	green   = "\033[97;42m"
	white   = "\033[90;47m"
	yellow  = "\033[90;43m"
	red     = "\033[97;41m"
	blue    = "\033[97;44m"
	magenta = "\033[97;45m"
	cyan    = "\033[97;46m"
	reset   = "\033[0m"
)

var consoleColorMode = autoColor

// LoggerConfig 定义 Logger 中间件的配置。
type LoggerConfig struct {
	// 选修的。默认值为 gin.defaultLogFormatter
	Formatter LogFormatter

	// 输出是写入日志的写入器。
	//  选修的。默认值为 gin.DefaultWriter。
	Output io.Writer

	// SkipPaths是不写入日志的URL路径数组。
	//  选修的。
	SkipPaths []string

	// SkipQueryString 表示不应写入查询字符串
	//  对于通过查询字符串传递 API 密钥等情况。
	//  选修的。默认值为 false。
	SkipQueryString bool

	// Skip是一个Skipper，指示哪些日志不应该被写入。
	//  选修的。
	Skip Skipper
}

// Skipper 是一个根据提供的 Context 跳过日志的函数
type Skipper func(c *Context) bool

// LogFormatter 给出传递给 LoggerWithFormatter 的格式化函数的签名
type LogFormatter func(params LogFormatterParams) string

// LogFormatterParams 是记录时间到来时任何格式化程序都会处理的结构
type LogFormatterParams struct {
	Request *http.Request

	// 时间戳显示服务器返回响应后的时间。
	TimeStamp time.Time
	// StatusCode 是 HTTP 响应代码。
	StatusCode int
	// 延迟是服务器处理某个请求所花费的时间。
	Latency time.Duration
	// ClientIP 等于 Context 的 ClientIP 方法。
	ClientIP string
	// Method 是为请求提供的 HTTP 方法。
	Method string
	// Path是客户端请求的路径。
	Path string
	// 如果处理请求时发生错误，则设置 ErrorMessage。
	ErrorMessage string
	// isTerm 显示 gin 的输出描述符是否引用终端。
	isTerm bool
	// BodySize 是响应主体的大小
	BodySize int
	// 键是在请求上下文中设置的键。
	Keys map[any]any
}

// StatusCodeColor 是用于将 http 状态代码正确记录到终端的 ANSI 颜色。
func (p *LogFormatterParams) StatusCodeColor() string {
	code := p.StatusCode

	switch {
	case code >= http.StatusContinue && code < http.StatusOK:
		return white
	case code >= http.StatusOK && code < http.StatusMultipleChoices:
		return green
	case code >= http.StatusMultipleChoices && code < http.StatusBadRequest:
		return white
	case code >= http.StatusBadRequest && code < http.StatusInternalServerError:
		return yellow
	default:
		return red
	}
}

// LatencyColor 是表示延迟的 ANSI 颜色
func (p *LogFormatterParams) LatencyColor() string {
	latency := p.Latency
	switch {
	case latency < time.Millisecond*100:
		return white
	case latency < time.Millisecond*200:
		return green
	case latency < time.Millisecond*300:
		return cyan
	case latency < time.Millisecond*500:
		return blue
	case latency < time.Second:
		return yellow
	case latency < time.Second*2:
		return magenta
	default:
		return red
	}
}

// MethodColor 是用于将 http 方法正确记录到终端的 ANSI 颜色。
func (p *LogFormatterParams) MethodColor() string {
	method := p.Method

	switch method {
	case http.MethodGet:
		return blue
	case http.MethodPost:
		return cyan
	case http.MethodPut:
		return yellow
	case http.MethodDelete:
		return red
	case http.MethodPatch:
		return green
	case http.MethodHead:
		return magenta
	case http.MethodOptions:
		return white
	default:
		return reset
	}
}

// ResetColor 重置所有转义属性。
func (p *LogFormatterParams) ResetColor() string {
	return reset
}

// IsOutputColor表示是否可以将颜色输出到日志中。
func (p *LogFormatterParams) IsOutputColor() bool {
	return consoleColorMode == forceColor || (consoleColorMode == autoColor && p.isTerm)
}

// defaultLogFormatter是Logger中间件使用的默认日志格式函数。
var defaultLogFormatter = func(param LogFormatterParams) string {
	var statusColor, methodColor, resetColor, latencyColor string
	if param.IsOutputColor() {
		statusColor = param.StatusCodeColor()
		methodColor = param.MethodColor()
		resetColor = param.ResetColor()
		latencyColor = param.LatencyColor()
	}

	switch {
	case param.Latency > time.Minute:
		param.Latency = param.Latency.Truncate(time.Second * 10)
	case param.Latency > time.Second:
		param.Latency = param.Latency.Truncate(time.Millisecond * 10)
	case param.Latency > time.Millisecond:
		param.Latency = param.Latency.Truncate(time.Microsecond * 10)
	}

	return fmt.Sprintf("[GIN] %v |%s %3d %s|%s %8v %s| %15s |%s %-7s %s %#v\n%s",
		param.TimeStamp.Format("2006/01/02 - 15:04:05"),
		statusColor, param.StatusCode, resetColor,
		latencyColor, param.Latency, resetColor,
		param.ClientIP,
		methodColor, param.Method, resetColor,
		param.Path,
		param.ErrorMessage,
	)
}

// DisableConsoleColor 禁用控制台中的颜色输出。
func DisableConsoleColor() {
	consoleColorMode = disableColor
}

// ForceConsoleColor 强制在控制台中输出颜色。
func ForceConsoleColor() {
	consoleColorMode = forceColor
}

// ErrorLogger 返回任何错误类型的 HandlerFunc。
func ErrorLogger() HandlerFunc {
	return ErrorLoggerT(ErrorTypeAny)
}

// ErrorLoggerT 返回给定错误类型的 HandlerFunc。
func ErrorLoggerT(typ ErrorType) HandlerFunc {
	return func(c *Context) {
		c.Next()
		errors := c.Errors.ByType(typ)
		if len(errors) > 0 {
			c.JSON(-1, errors)
		}
	}
}

// Logger 实例一个 Logger 中间件，它将把日志写入 gin.DefaultWriter。
//  默认情况下，gin.DefaultWriter = os.Stdout。
func Logger() HandlerFunc {
	return LoggerWithConfig(LoggerConfig{})
}

// LoggerWithFormatter实例是一个Logger中间件，具有指定日志格式的功能。
func LoggerWithFormatter(f LogFormatter) HandlerFunc {
	return LoggerWithConfig(LoggerConfig{
		Formatter: f,
	})
}

// LoggerWithWriter 实例是一个具有指定写入器缓冲区的 Logger 中间件。
//  示例：os.Stdout、以写入模式打开的文件、套接字...
func LoggerWithWriter(out io.Writer, notlogged ...string) HandlerFunc {
	return LoggerWithConfig(LoggerConfig{
		Output:    out,
		SkipPaths: notlogged,
	})
}

// LoggerWithConfig 实例一个带有配置的 Logger 中间件。
func LoggerWithConfig(conf LoggerConfig) HandlerFunc {
	formatter := conf.Formatter
	if formatter == nil {
		formatter = defaultLogFormatter
	}

	out := conf.Output
	if out == nil {
		out = DefaultWriter
	}

	notlogged := conf.SkipPaths

	isTerm := true

	if w, ok := out.(*os.File); !ok || os.Getenv("TERM") == "dumb" ||
		(!isatty.IsTerminal(w.Fd()) && !isatty.IsCygwinTerminal(w.Fd())) {
		isTerm = false
	}

	var skip map[string]struct{}

	if length := len(notlogged); length > 0 {
		skip = make(map[string]struct{}, length)

		for _, path := range notlogged {
			skip[path] = struct{}{}
		}
	}

	return func(c *Context) {
		// 启动定时器
		start := time.Now()
		path := c.Request.URL.Path
		raw := c.Request.URL.RawQuery

		// 处理请求
		c.Next()

		// 仅在不被跳过时记录
		if _, ok := skip[path]; ok || (conf.Skip != nil && conf.Skip(c)) {
			return
		}

		param := LogFormatterParams{
			Request: c.Request,
			isTerm:  isTerm,
			Keys:    c.Keys,
		}

		// 停止计时器
		param.TimeStamp = time.Now()
		param.Latency = param.TimeStamp.Sub(start)

		param.ClientIP = c.ClientIP()
		param.Method = c.Request.Method
		param.StatusCode = c.Writer.Status()
		param.ErrorMessage = c.Errors.ByType(ErrorTypePrivate).String()

		param.BodySize = c.Writer.Size()

		if raw != "" && !conf.SkipQueryString {
			path = path + "?" + raw
		}

		param.Path = path

		fmt.Fprint(out, formatter(param))
	}
}
