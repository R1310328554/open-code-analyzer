package util //nolint:revive

// FormatTimeRFC3339Nano 统一引擎内时间戳的 UTC RFC3339Nano 字符串格式。

import "time"

// 所有引擎组件应调用此函数以保证日志与 API 时间字段格式一致。
// FormatTimeRFC3339Nano formats the given time in RFC3339Nano format in UTC.
// Use this everywhere in the engine for consistent timestamp formatting.
func FormatTimeRFC3339Nano(t time.Time) string {
	return t.UTC().Format(time.RFC3339Nano)
}
// 先转 UTC 再 Format，避免本地时区导致解析歧义。
