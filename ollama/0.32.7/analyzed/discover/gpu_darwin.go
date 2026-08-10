// macOS 内存探测：通过 Metal/CoreGraphics 读取物理与可用内存。
package discover

/*
#cgo CFLAGS: -x objective-c
#cgo LDFLAGS: -framework Foundation -framework CoreGraphics -framework Metal
#include "gpu_info_darwin.h"
*/
import "C"

import (
	"github.com/ollama/ollama/format"
)

// metalMinimumMemory 为 Metal 后端建议的最小 GPU 内存阈值。
const (
	metalMinimumMemory = 512 * format.MebiByte
)

// GetCPUMem 调用 C 辅助函数获取 macOS 物理与可用内存。
func GetCPUMem() (memInfo, error) {
	return memInfo{
		TotalMemory: uint64(C.getPhysicalMemory()),
		FreeMemory:  uint64(C.getFreeMemory()),
		// Darwin 使用动态分页，不报告 FreeSwap。
		// FreeSwap omitted as Darwin uses dynamic paging
	}, nil
}

// IsNUMA macOS 上 ggml 不支持 NUMA，恒为 false。
func IsNUMA() bool {
	// numa support in ggml is linux only
	return false
}
