// Windows 退出状态：识别 NTSTATUS 严重错误并格式化十六进制。
//go:build windows

package llm

import (
	"fmt"
	"log/slog"

	"golang.org/x/sys/windows"
)

// NTSTATUS 严重级别掩码常量。
const (
	ntstatusSeverityMask  = 0xc0000000
	ntstatusSeverityError = 0xc0000000
)

// formatExitStatus Windows 上对 NTSTATUS 错误输出十六进制与系统消息。
func formatExitStatus(s ExitStatus) string {
	if s == exitStatusOK || s == exitStatusUnknown {
		return decimalExitStatus(s)
	}

	raw := uint32(s)
	if raw&ntstatusSeverityMask != ntstatusSeverityError {
		return decimalExitStatus(s)
	}

	return fmt.Sprintf("exit status 0x%08x: %s", raw, windows.NTStatus(raw).Error())
}

// logExitStatus Windows 上对 NTSTATUS 输出 code/hex/ntstatus 分组。
func logExitStatus(s ExitStatus) slog.Value {
	if s == exitStatusOK || s == exitStatusUnknown {
		return slog.StringValue(s.String())
	}

	raw := uint32(s)
	if raw&ntstatusSeverityMask != ntstatusSeverityError {
		return slog.IntValue(int(s))
	}

	return slog.GroupValue(
		slog.Int("code", int(s)),
		slog.String("hex", fmt.Sprintf("0x%08x", raw)),
		slog.String("ntstatus", windows.NTStatus(raw).Error()),
	)
}
