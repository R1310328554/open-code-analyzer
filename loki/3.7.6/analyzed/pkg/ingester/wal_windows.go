//go:build windows

package ingester

// Windows 平台 WAL 磁盘检测桩：当前固定返回 0% 使用率，即不在 Windows 上启用磁盘满节流。

import (
	"syscall"
)

var (
	kernel32               = syscall.NewLazyDLL("kernel32.dll")
	procGetDiskFreeSpaceEx = kernel32.NewProc("GetDiskFreeSpaceExW")
)

// checkDiskUsage Windows 桩实现，暂不调用 GetDiskFreeSpaceEx，始终返回 0。
// checkDiskUsage returns the disk usage percentage (0.0 to 1.0) for the WAL directory.
func (w *walWrapper) checkDiskUsage() (float64, error) {
	// Disable this for Windows for now
	return 0.0, nil
}
// kernel32 GetDiskFreeSpaceEx 预留供后续完善 Windows WAL 磁盘保护。
