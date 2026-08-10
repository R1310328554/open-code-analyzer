//go:build !windows

package transfer

import "os"

// setSparse 在非 Windows 平台为空操作。
// setSparse is a no-op on non-Windows platforms.
// Windows 上通过 FSCTL_SET_SPARSE 标记稀疏文件；Unix 文件系统通常默认稀疏。
// On Windows, this sets the FSCTL_SET_SPARSE attribute which allows the OS
// to not allocate disk blocks for zero-filled regions. This is useful for
// partial downloads where not all data has been written yet. On Unix-like
// systems, filesystems typically handle this automatically (sparse by default).
func setSparse(_ *os.File) {}
