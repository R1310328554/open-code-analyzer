// Windows 平台：为下载临时文件设置 FSCTL_SET_SPARSE 以节省预分配空间。
//go:build windows

package transfer

import (
	"os"

	"golang.org/x/sys/windows"
)

// setSparse 在 Windows 上为文件设置稀疏属性，避免为零区域预分配磁盘块。
// setSparse sets the FSCTL_SET_SPARSE attribute on Windows files.
// This allows the OS to not allocate disk blocks for zero-filled regions,
// which is useful for large files that may not be fully written (e.g., partial
// downloads). Without this, Windows may pre-allocate disk space for the full
// file size even if most of it is zeros.
//
// 注意：错误被忽略，因稀疏仅为优化且部分文件系统不支持。
// Note: Errors are intentionally ignored because:
// 1. 无稀疏支持时文件仍可正常使用
// 1. The file will still work correctly without sparse support
// 2. 并非所有文件系统支持稀疏（如 FAT32）
// 2. Not all Windows filesystems support sparse files (e.g., FAT32)
// 3. 仅为性能优化，非功能必需
// 3. This is an optimization, not a requirement
func setSparse(file *os.File) {
	var bytesReturned uint32
	_ = windows.DeviceIoControl(
		windows.Handle(file.Fd()),
		windows.FSCTL_SET_SPARSE,
		nil, 0,
		nil, 0,
		&bytesReturned,
		nil,
	)
}
