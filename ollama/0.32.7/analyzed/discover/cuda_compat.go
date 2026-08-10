// CUDA 驱动兼容性：按运行时版本过滤过旧 NVIDIA 驱动。
package discover

import (
	"context"
	"log/slog"

	"github.com/ollama/ollama/ml"
)

const (
	cudaV12RuntimeMajor = 12

	minFatbinCompressionCUDARuntimeMinor  = 4
	minFatbinCompressionNVIDIADriverMajor = 550

	minLegacyComputeJITCUDARuntimeMinor = 8
	// 较旧计算能力从 PTX JIT 时需要更高版本驱动。
	// Older CUDA compute targets need newer drivers when they are JITed from PTX.
	minLegacyComputeJITNVIDIADriverMajor = 570
)

// filterOldCUDADriver 依据 CUDA 运行时与驱动主版本丢弃不兼容 CUDA 设备。
func filterOldCUDADriver(_ context.Context, devices []ml.DeviceInfo) []ml.DeviceInfo {
	oldCUDA := func(dev ml.DeviceInfo) bool {
		return dev.Library == "CUDA" && dev.ComputeMajor > 0 && dev.ComputeMajor < 7
	}

	hasCUDA := false
	for _, dev := range devices {
		if dev.Library == "CUDA" {
			hasCUDA = true
			break
		}
	}
	if !hasCUDA {
		return devices
	}

	driver := nvidiaDriverMajorFromDevices(devices)
	if driver == 0 {
		slog.Warn("could not verify NVIDIA driver compatibility for CUDA")
		return devices
	}

	// 驱动下限与即将加载的 CUDA 运行时匹配，源码构建可兼容旧驱动。
	// Match the driver floor to the CUDA runtime we are about to load, so source
	// builds with older CUDA runtimes can still run on matching older drivers.
	runtimeMajor, runtimeMinor, hasRuntime := cudaRuntimeVersionFromDevices(devices)
	runtimeMayUseCompressedFatbins := hasRuntime &&
		runtimeMajor == cudaV12RuntimeMajor &&
		runtimeMinor >= minFatbinCompressionCUDARuntimeMinor
	// CUDA 12.8+ 源码构建依赖 PTX 打包或对旧计算能力的 JIT 支持。
	// CUDA v12.8+ source builds are expected to either use Ollama's PTX packaging
	// for older compute targets or be built against a matching local driver/toolkit.
	runtimeMayJITLegacyCompute := hasRuntime &&
		runtimeMajor == cudaV12RuntimeMajor &&
		runtimeMinor >= minLegacyComputeJITCUDARuntimeMinor
	if driver >= minLegacyComputeJITNVIDIADriverMajor || (!runtimeMayUseCompressedFatbins && !runtimeMayJITLegacyCompute) {
		return devices
	}

	filtered := devices[:0]
	for _, dev := range devices {
		if dev.Library != "CUDA" {
			filtered = append(filtered, dev)
			continue
		}
		if runtimeMayUseCompressedFatbins && driver < minFatbinCompressionNVIDIADriverMajor {
			slog.Warn("NVIDIA driver too old",
				"device", dev.Description, "compute", dev.Compute(), "driver", driver, "required_driver", "550 or newer")
			continue
		}
		if runtimeMayJITLegacyCompute && oldCUDA(dev) {
			slog.Warn("NVIDIA driver too old",
				"device", dev.Description, "compute", dev.Compute(), "driver", driver, "required_driver", "570 or newer")
			continue
		}
		filtered = append(filtered, dev)
	}
	return filtered
}

// nvidiaDriverMajorFromDevices 从设备列表取首个 CUDA 设备的 NVIDIA 驱动主版本。
func nvidiaDriverMajorFromDevices(devices []ml.DeviceInfo) int {
	for _, dev := range devices {
		if dev.Library == "CUDA" && dev.NVIDIADriverMajor > 0 {
			return dev.NVIDIADriverMajor
		}
	}
	return 0
}

// cudaRuntimeVersionFromDevices 从库目录推断 CUDA 运行时主次版本。
func cudaRuntimeVersionFromDevices(devices []ml.DeviceInfo) (int, int, bool) {
	for _, dev := range devices {
		if dev.Library == "CUDA" {
			return cudaRuntimeVersion(dev.LibraryPath)
		}
	}
	return 0, 0, false
}
