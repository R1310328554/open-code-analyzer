// BSD 平台 termios ioctl：TIOCGETA/TIOCSETA。
//go:build darwin || freebsd || netbsd || openbsd

package readline

import (
	"syscall"
	"unsafe"
)

// getTermios 通过 TIOCGETA ioctl 读取 termios。
func getTermios(fd uintptr) (*Termios, error) {
	termios := new(Termios)
	_, _, err := syscall.Syscall6(syscall.SYS_IOCTL, fd, syscall.TIOCGETA, uintptr(unsafe.Pointer(termios)), 0, 0, 0)
	if err != 0 {
		return nil, err
	}
	return termios, nil
}

// setTermios 通过 TIOCSETA ioctl 写入 termios。
func setTermios(fd uintptr, termios *Termios) error {
	_, _, err := syscall.Syscall6(syscall.SYS_IOCTL, fd, syscall.TIOCSETA, uintptr(unsafe.Pointer(termios)), 0, 0, 0)
	if err != 0 {
		return err
	}
	return nil
}
