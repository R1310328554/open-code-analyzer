// 结构化日志工具：TRACE 级别、源码截断与 Trace 辅助函数。
package logutil

import (
	"context"
	"io"
	"log/slog"
	"path/filepath"
	"runtime"
	"time"
)

// LevelTrace 自定义 TRACE 日志级别（低于 DEBUG）。
const LevelTrace slog.Level = -8

// NewLogger 创建带源码文件名与 TRACE 级别映射的文本 logger。
func NewLogger(w io.Writer, level slog.Level) *slog.Logger {
	return slog.New(slog.NewTextHandler(w, &slog.HandlerOptions{
		Level:     level,
		AddSource: true,
		ReplaceAttr: func(_ []string, attr slog.Attr) slog.Attr {
			switch attr.Key {
			case slog.LevelKey:
				switch attr.Value.Any().(slog.Level) {
				case LevelTrace:
					attr.Value = slog.StringValue("TRACE")
				}
			case slog.SourceKey:
				if source, ok := attr.Value.Any().(*slog.Source); ok {
					source.File = filepath.Base(source.File)
				}
			}
			return attr
		},
	}))
}

// key 为 TraceContext 跳过栈帧计数的 context 键。
type key string

// Trace 以默认 logger 记录 TRACE 级别消息。
func Trace(msg string, args ...any) {
	TraceContext(context.WithValue(context.TODO(), key("skip"), 1), msg, args...)
}

// TraceContext 在 logger 启用 TRACE 时写入带调用栈的记录。
func TraceContext(ctx context.Context, msg string, args ...any) {
	if logger := slog.Default(); logger.Enabled(ctx, LevelTrace) {
		skip, _ := ctx.Value(key("skip")).(int)
		pc, _, _, _ := runtime.Caller(1 + skip)
		record := slog.NewRecord(time.Now(), LevelTrace, msg, pc)
		record.Add(args...)
		logger.Handler().Handle(ctx, record)
	}
}
