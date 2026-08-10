// Linux/Solaris 终端 ioctl：读取与设置 termios 原始模式。
//go:build linux || solaris

package readline

import (
	"syscall"
	"unsafe"
)

// tcgets/tcsets 为 TIOCGWINSZ 族 ioctl 操作码。
const (
	tcgets = 0x5401
	tcsets = 0x5402
)

// getTermios 通过 SYS_IOCTL 读取终端属性。
func getTermios(fd uintptr) (*Termios, error) {
	termios := new(Termios)
	_, _, err := syscall.Syscall6(syscall.SYS_IOCTL, fd, tcgets, uintptr(unsafe.Pointer(termios)), 0, 0, 0)
	if err != 0 {
		return nil, err
	}
	return termios, nil
}

// setTermios 写回终端属性以恢复或切换模式。
func setTermios(fd uintptr, termios *Termios) error {
	_, _, err := syscall.Syscall6(syscall.SYS_IOCTL, fd, tcsets, uintptr(unsafe.Pointer(termios)), 0, 0, 0)
	if err != 0 {
		return err
	}
	return nil
}
