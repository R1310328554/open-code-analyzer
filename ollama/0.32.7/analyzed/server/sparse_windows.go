// Windows 平台：通过 FSCTL_SET_SPARSE 标记稀疏文件（exFAT 等失败时忽略）。
package server

import (
	"os"

	"golang.org/x/sys/windows"
)

// setSparse 对支持 NTFS 稀疏的文件句柄调用 DeviceIoControl；错误忽略。
func setSparse(file *os.File) {
	// exFat (and other FS types) don't support sparse files, so ignore errors
	windows.DeviceIoControl( //nolint:errcheck
		windows.Handle(file.Fd()), windows.FSCTL_SET_SPARSE,
		nil, 0,
		nil, 0,
		nil, nil,
	)
}
