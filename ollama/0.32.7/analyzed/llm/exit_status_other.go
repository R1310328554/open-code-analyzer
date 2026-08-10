// 非 Windows 平台退出状态格式化（十进制）。
//go:build !windows

package llm

import "log/slog"

// formatExitStatus 非 Windows 直接使用十进制格式。
func formatExitStatus(s ExitStatus) string {
	return decimalExitStatus(s)
}

// logExitStatus 非 Windows 以字符串或整型记录退出码。
func logExitStatus(s ExitStatus) slog.Value {
	if s == exitStatusOK || s == exitStatusUnknown {
		return slog.StringValue(s.String())
	}

	return slog.IntValue(int(s))
}
