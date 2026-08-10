// 非 Windows 平台 ROCm 库路径调整（无操作桩）。
//go:build !windows

package llm

// adjustPlatformLibraryPaths 非 Windows 平台直接返回原路径。
func adjustPlatformLibraryPaths(paths, _ []string) []string {
	return paths
}
