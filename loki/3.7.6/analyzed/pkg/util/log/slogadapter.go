// SPDX-License-Identifier: AGPL-3.0-only

package log

import (
	"log/slog"

	"github.com/go-kit/log"
	slgk "github.com/tjhop/slog-gokit"
)

// SlogFromGoKit 将 go-kit Logger 转为 slog.Logger，级别映射自全局 logLevel 字符串。
// SlogFromGoKit returns slog adapter for logger.
func SlogFromGoKit(logger log.Logger) *slog.Logger {
	var sl slog.Level
	switch logLevel.String() {
	case "info":
		sl = slog.LevelInfo
	case "warn":
		sl = slog.LevelWarn
	case "error":
		sl = slog.LevelError
	default:
		sl = slog.LevelDebug
	}

	lvl := slog.LevelVar{}
	lvl.Set(sl)
	return slog.New(slgk.NewGoKitHandler(logger, &lvl))
}
// 未识别的 logLevel 字符串默认映射为 Debug，便于开发环境 verbose 输出。
