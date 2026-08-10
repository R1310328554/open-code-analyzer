// 非 macOS 平台：macOSMajorVersion 恒返回 0。
//go:build !darwin

package mlx

// macOSMajorVersion 非 Darwin 构建下恒为 0。
func macOSMajorVersion() int { return 0 }
