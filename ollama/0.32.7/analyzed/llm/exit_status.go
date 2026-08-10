// 子进程退出码：统一 exec.ExitError 与未知错误的表示。
package llm

import (
	"errors"
	"fmt"
	"log/slog"
	"os/exec"
)

// ExitStatus 包装子进程退出码，-1 表示未知。
type ExitStatus int

// 特殊退出码：未知与成功。
const (
	exitStatusUnknown ExitStatus = -1
	exitStatusOK      ExitStatus = 0
)

// ExitStatusFromError 从 error 提取退出码，非 ExitError 返回 unknown。
func ExitStatusFromError(err error) ExitStatus {
	if err == nil {
		return exitStatusOK
	}

	var exitErr *exec.ExitError
	if errors.As(err, &exitErr) {
		return ExitStatus(exitErr.ExitCode())
	}

	return exitStatusUnknown
}

// Known 判断退出码是否已知（非 -1）。
func (s ExitStatus) Known() bool {
	return s != exitStatusUnknown
}

// String 返回人类可读的退出状态描述。
func (s ExitStatus) String() string {
	return formatExitStatus(s)
}

// LogValue 为 slog 输出结构化退出状态。
func (s ExitStatus) LogValue() slog.Value {
	return logExitStatus(s)
}

// decimalExitStatus 将退出码格式化为十进制描述字符串。
func decimalExitStatus(s ExitStatus) string {
	switch s {
	case exitStatusOK:
		return "OK"
	case exitStatusUnknown:
		return "unknown"
	}

	return fmt.Sprintf("exit status %d", int(s))
}
