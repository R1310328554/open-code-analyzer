// Unix 终端 raw 模式：Termios 封装与 IsTerminal 检测。
//go:build aix || darwin || dragonfly || freebsd || (linux && !appengine) || netbsd || openbsd || os400 || solaris

package readline

import (
	"syscall"
)

// Termios 为 syscall.Termios 的类型别名。
type Termios syscall.Termios

// SetRawMode 保存原 termios 并配置 raw 模式（8N1、VMIN=1）。
func SetRawMode(fd uintptr) (*Termios, error) {
	termios, err := getTermios(fd)
	if err != nil {
		return nil, err
	}

	newTermios := *termios
	newTermios.Iflag &^= syscall.IGNBRK | syscall.BRKINT | syscall.PARMRK | syscall.ISTRIP | syscall.INLCR | syscall.IGNCR | syscall.ICRNL | syscall.IXON
	newTermios.Lflag &^= syscall.ECHO | syscall.ECHONL | syscall.ICANON | syscall.ISIG | syscall.IEXTEN
	newTermios.Cflag &^= syscall.CSIZE | syscall.PARENB
	newTermios.Cflag |= syscall.CS8
	newTermios.Cc[syscall.VMIN] = 1
	newTermios.Cc[syscall.VTIME] = 0

	return termios, setTermios(fd, &newTermios)
}

// UnsetRawMode 恢复先前保存的 termios。
func UnsetRawMode(fd uintptr, termios any) error {
	t := termios.(*Termios)
	return setTermios(fd, t)
}

// IsTerminal 判断文件描述符是否为 TTY。
// IsTerminal returns true if the given file descriptor is a terminal.
func IsTerminal(fd uintptr) bool {
	_, err := getTermios(fd)
	return err == nil
}
