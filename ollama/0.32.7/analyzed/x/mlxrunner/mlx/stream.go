// MLX 设备与流：默认 GPU/CPU 设备与计算流管理。
package mlx

// #include "generated.h"
import "C"

import "log/slog"

// Device 封装 MLX 设备句柄。
type Device struct {
	ctx C.mlx_device
}

// LogValue 供 slog 输出设备字符串。
func (d Device) LogValue() slog.Value {
	str := C.mlx_string_new()
	defer C.mlx_string_free(str)
	C.mlx_device_tostring(&str, d.ctx)
	return slog.StringValue(C.GoString(C.mlx_string_data(str)))
}

var (
	defaultDevice    Device
	defaultDeviceSet bool
	defaultStream    Stream
	defaultStreamSet bool
)

// resetDefaultStreamCache 切换默认设备后清空缓存。
func resetDefaultStreamCache() {
	defaultDeviceSet = false
	defaultStreamSet = false
}

// DefaultDevice 懒加载并缓存默认 MLX 设备。
func DefaultDevice() Device {
	if !defaultDeviceSet {
		d := C.mlx_device_new()
		C.mlx_get_default_device(&d)
		defaultDevice = Device{d}
		defaultDeviceSet = true
	}

	return defaultDevice
}

// GPUIsAvailable 判断 GPU 是否可用。
// GPUIsAvailable returns true if a GPU device is available.
func GPUIsAvailable() bool {
	dev := C.mlx_device_new_type(C.MLX_GPU, 0)
	defer C.mlx_device_free(dev)
	var avail C.bool
	C.mlx_device_is_available(&avail, dev)
	return bool(avail)
}

// SetDefaultDeviceGPU 将默认设备设为 GPU。
// SetDefaultDeviceGPU sets the default MLX device to GPU.
func SetDefaultDeviceGPU() {
	dev := C.mlx_device_new_type(C.MLX_GPU, 0)
	C.mlx_set_default_device(dev)
	C.mlx_device_free(dev)
	resetDefaultStreamCache()
}

// Stream 封装 MLX 计算流。
type Stream struct {
	ctx C.mlx_stream
}

// LogValue 供 slog 输出流字符串。
func (s Stream) LogValue() slog.Value {
	str := C.mlx_string_new()
	defer C.mlx_string_free(str)
	C.mlx_stream_tostring(&str, s.ctx)
	return slog.StringValue(C.GoString(C.mlx_string_data(str)))
}

// DefaultStream 返回默认设备上的默认流。
func DefaultStream() Stream {
	if !defaultStreamSet {
		s := C.mlx_stream_new()
		C.mlx_get_default_stream(&s, DefaultDevice().ctx)
		defaultStream = Stream{s}
		defaultStreamSet = true
	}

	return defaultStream
}
