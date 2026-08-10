//go:build !windows

package ingester

// Unix 平台 WAL 磁盘使用率检测：通过 syscall.Statfs 读取文件系统块统计并计算占用比例。

import (
	"syscall"
)

// checkDiskUsage 对 WAL 目录所在文件系统调用 Statfs，返回已用空间占比 0.0~1.0。
// checkDiskUsage returns the disk usage percentage (0.0 to 1.0) for the WAL directory.
func (w *walWrapper) checkDiskUsage() (float64, error) {
	var stat syscall.Statfs_t
	if err := syscall.Statfs(w.cfg.Dir, &stat); err != nil {
		return 0, err
	}

// usagePercent = (total - free) / total，供 monitorDisk 与 DiskFullThreshold 比较。
	// Calculate usage percentage
	total := stat.Blocks * uint64(stat.Bsize)
	free := stat.Bfree * uint64(stat.Bsize)
	used := total - free
	usagePercent := float64(used) / float64(total)

	return usagePercent, nil
}
// 仅 Unix 构建标签生效；Windows 见 wal_windows.go 的桩实现。
