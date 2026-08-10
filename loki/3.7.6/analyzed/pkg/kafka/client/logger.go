// SPDX-License-Identifier: AGPL-3.0-only

package client

// logger 将 franz-go Kafka 客户端日志桥接到 go-kit log，固定返回 Info 级别以避免昂贵 debug 输出。

import (
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/twmb/franz-go/pkg/kgo"
)

// logger 类型封装该模块的状态与行为。
type logger struct {
	logger log.Logger
}

// newLogger 实现该路径上的核心处理逻辑。
func newLogger(l log.Logger) *logger {
	return &logger{
		logger: log.With(l, "component", "kafka_client"),
	}
}

// Level 始终返回 Info，阻止 franz-go 输出 debug 级别日志。
func (l *logger) Level() kgo.LogLevel {
	// The Kafka client calls Level() to check whether debug level is enabled or not.
	// To keep it simple, we always return Info, so the Kafka client will never try
	// to log expensive debug messages.
	return kgo.LogLevelInfo
}

// Log 实现该路径上的核心处理逻辑。
func (l *logger) Log(lev kgo.LogLevel, msg string, keyvals ...any) {
	keyvals = append([]any{"msg", msg}, keyvals...)
	switch lev {
	case kgo.LogLevelDebug:
		level.Debug(l.logger).Log(keyvals...)
	case kgo.LogLevelInfo:
		level.Info(l.logger).Log(keyvals...)
	case kgo.LogLevelWarn:
		level.Warn(l.logger).Log(keyvals...)
	case kgo.LogLevelError:
		level.Error(l.logger).Log(keyvals...)
	}
}
// Level() 恒为 Info 是刻意策略：避免 franz-go 频繁探测 debug 开关。
