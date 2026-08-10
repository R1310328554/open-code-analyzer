// Unix Ctrl+Z (SIGSTOP) 处理：暂停前恢复 canonical 模式。
//go:build !windows

package readline

import (
	"syscall"
)

// handleCharCtrlZ 恢复 termios 并向进程组发送 SIGSTOP。
func handleCharCtrlZ(fd uintptr, termios any) (string, error) {
	t := termios.(*Termios)
	if err := UnsetRawMode(fd, t); err != nil {
		return "", err
	}

	_ = syscall.Kill(0, syscall.SIGSTOP)

	// on resume...
	return "", nil
}
