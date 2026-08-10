// macOS 专用：通过 sysctl 读取系统主版本号。
package mlx

import (
	"strconv"
	"strings"
	"syscall"
)

// macOSMajorVersion 读取 kern.osproductversion 的主版本号。
func macOSMajorVersion() int {
	ver, err := syscall.Sysctl("kern.osproductversion")
	if err != nil {
		return 0
	}
	parts := strings.SplitN(ver, ".", 2)
	major, _ := strconv.Atoi(parts[0])
	return major
}
