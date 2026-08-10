// MLX 内存：活跃/缓存/峰值统计、PrettyBytes 与 Metal wired limit。
package mlx

// #include "generated.h"
// #include <stdlib.h>
import "C"

import (
	"fmt"
	"log/slog"
	"strconv"
	"unsafe"
)

func (b Byte) String() string {
	return strconv.FormatInt(int64(b), 10) + " B"
}

func (b KibiByte) String() string {
	return strconv.FormatFloat(float64(b)/(1<<10), 'f', 2, 64) + " KiB"
}

func (b MebiByte) String() string {
	return strconv.FormatFloat(float64(b)/(1<<(2*10)), 'f', 2, 64) + " MiB"
}

func (b GibiByte) String() string {
	return strconv.FormatFloat(float64(b)/(1<<(3*10)), 'f', 2, 64) + " GiB"
}

func (b TebiByte) String() string {
	return strconv.FormatFloat(float64(b)/(1<<(4*10)), 'f', 2, 64) + " TiB"
}

// PrettyBytes 将字节数格式化为 B/KiB/MiB/GiB/TiB。
func PrettyBytes(n int) fmt.Stringer {
	switch {
	case n < 1<<10:
		return Byte(n)
	case n < 1<<(2*10):
		return KibiByte(n)
	case n < 1<<(3*10):
		return MebiByte(n)
	case n < 1<<(4*10):
		return GibiByte(n)
	default:
		return TebiByte(n)
	}
}

// ActiveMemory 返回当前活跃分配字节数。
func ActiveMemory() int {
	var active C.size_t
	C.mlx_get_active_memory(&active)
	return int(active)
}

// CacheMemory 返回 MLX 缓存占用字节数。
func CacheMemory() int {
	var cache C.size_t
	C.mlx_get_cache_memory(&cache)
	return int(cache)
}

// PeakMemory 返回峰值内存字节数。
func PeakMemory() int {
	var peak C.size_t
	C.mlx_get_peak_memory(&peak)
	return int(peak)
}

// ResetPeakMemory 重置峰值计数。
func ResetPeakMemory() {
	C.mlx_reset_peak_memory()
}

// MaxRecommendedWorkingSetSize 返回设备建议的 Metal 常驻内存上限。
// MaxRecommendedWorkingSetSize returns the device's recommended upper bound
// for resident Metal allocations.
func MaxRecommendedWorkingSetSize() (int, error) {
	info := C.mlx_device_info_new()
	if err := mlxCall("get device info failed", func() C.int {
		return C.mlx_device_info_get(&info, DefaultDevice().ctx)
	}); err != nil {
		C.mlx_device_info_free(info)
		return 0, err
	}
	defer C.mlx_device_info_free(info)

	key := C.CString("max_recommended_working_set_size")
	defer C.free(unsafe.Pointer(key))

	var size C.size_t
	if err := mlxCall("max recommended working set size unavailable", func() C.int {
		return C.mlx_device_info_get_size(&size, info, key)
	}); err != nil {
		return 0, err
	}
	return int(size), nil
}

// SetWiredLimit 设置 MLX 常驻 Metal 内存上限，返回旧值。
// SetWiredLimit sets the maximum amount of Metal memory MLX keeps resident and
// returns the previous limit.
func SetWiredLimit(limit int) (int, error) {
	if limit < 0 {
		return 0, fmt.Errorf("mlx: wired limit must be non-negative")
	}

	var previous C.size_t
	if err := mlxCall("set wired limit failed", func() C.int {
		return C.mlx_set_wired_limit(&previous, C.size_t(limit))
	}); err != nil {
		return 0, err
	}
	return int(previous), nil
}

// Memory 供 slog 输出内存统计。
type Memory struct{}

func (Memory) LogValue() slog.Value {
	return slog.GroupValue(
		slog.Any("active", PrettyBytes(ActiveMemory())),
		slog.Any("cache", PrettyBytes(CacheMemory())),
		slog.Any("peak", PrettyBytes(PeakMemory())),
	)
}

type (
	Byte     int
	KibiByte int
	MebiByte int
	GibiByte int
	TebiByte int
)

// ClearCache 清空 MLX 分配缓存。
func ClearCache() {
	C.mlx_clear_cache()
}
