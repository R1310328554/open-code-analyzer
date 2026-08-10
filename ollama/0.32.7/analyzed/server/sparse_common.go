// 非 Windows 平台：setSparse 为空操作（稀疏文件由 OS 默认行为处理）。
//go:build !windows

package server

import "os"

// setSparse 非 Windows 上无需设置 NTFS 稀疏标志。
func setSparse(*os.File) {
}
