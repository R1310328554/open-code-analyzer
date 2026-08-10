package util //nolint:revive

// util 包 events 提供基于 go-kit logger 的采样事件接口：通过 stderr logfmt 输出，freq 控制每 N 次调用才实际写日志。

import (
	"os"

	"github.com/go-kit/log"
	"go.uber.org/atomic"
)

// Event 接口用于可观测性采样事件，当前为临时经 logger 输出到 stderr 的实现。
// Provide an "event" interface for observability

// Temporary hack implementation to go via logger to stderr

var (
	// interface{} vars to avoid allocation on every call
	key   interface{} = "level" // masquerade as a level like debug, warn
	event interface{} = "event"

	eventLogger = log.NewNopLogger()
)

// Event 返回全局 eventLogger，InitEvents(0) 时为空操作 NopLogger。
// Event is the log-like API for event sampling
func Event() log.Logger {
	return eventLogger
}

// InitEvents 设置采样频率；freq<=0 关闭事件输出以消除热路径开销。
// InitEvents initializes event sampling, with the given frequency. Zero=off.
func InitEvents(freq int) {
	if freq <= 0 {
		eventLogger = log.NewNopLogger()
	} else {
		eventLogger = newEventLogger(freq)
	}
}

func newEventLogger(freq int) log.Logger {
	l := log.NewLogfmtLogger(log.NewSyncWriter(os.Stderr))
	l = log.WithPrefix(l, key, event)
	l = log.With(l, "ts", log.DefaultTimestampUTC)
	return &samplingFilter{next: l, freq: freq}
}

// samplingFilter 用原子计数对 Log 调用取模，仅命中周期时转发到下层 logger。
type samplingFilter struct {
	next  log.Logger
	freq  int
	count atomic.Int64
}

func (e *samplingFilter) Log(keyvals ...interface{}) error {
	count := e.count.Inc()
	if count%int64(e.freq) == 0 {
		return e.next.Log(keyvals...)
	}
	return nil
}
// key/event 变量以 interface{} 存储固定字符串，减少每次 Log 的字符串分配。
