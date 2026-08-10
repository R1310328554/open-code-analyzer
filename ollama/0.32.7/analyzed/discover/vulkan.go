// Vulkan 设备发现：解析 UMA 元数据并在 Windows 上通过原生 API 细化集成/独显。
// Vulkan discovery needs a small amount of normalization around device type.
// llama-server 发现输出暂无稳定结构化后端类型字段，故依赖 UMA 与 Windows 原生查询。
// llama-server discovery output does not currently expose a stable structured
// backend type field, so we use explicit Vulkan UMA metadata when it is
// present and, on Windows, refine the result with a direct Vulkan API query.
// The goal is to preserve correct integrated-vs-discrete scheduling decisions
// without relying on device-name heuristics.
package discover

import (
	"bufio"
	"errors"
	"log/slog"
	"regexp"
	"runtime"
	"strconv"
	"strings"

	"github.com/ollama/ollama/ml"
)

// vulkanUMARegex 匹配 ggml_vulkan stderr 中的 UMA 标志行。
// vulkanUMARegex matches Vulkan debug lines like:
//
//	ggml_vulkan: 0 = Intel(R) Graphics (...) | uma: 1 | fp16: 1 |
var vulkanUMARegex = regexp.MustCompile(
	`ggml_vulkan:\s+(\d+)\s+=.*\|\s+uma:\s+([01])\s+\|`,
)

// parseVulkanUMA 从 llama-server 输出解析各设备索引的 UMA（集成 GPU）标志。
func parseVulkanUMA(output string) map[int]bool {
	integratedByIndex := make(map[int]bool)

	scanner := bufio.NewScanner(strings.NewReader(output))
	for scanner.Scan() {
		if matches := vulkanUMARegex.FindStringSubmatch(scanner.Text()); matches != nil {
			idx, _ := strconv.Atoi(matches[1])
			integratedByIndex[idx] = matches[2] == "1"
		}
	}

	return integratedByIndex
}

var errWindowsVulkanProbeUnsupported = errors.New("windows vulkan probe unsupported")

// vulkanPhysicalDevice 表示 Vulkan 物理设备名称与集成标志。
type vulkanPhysicalDevice struct {
	Name       string
	Integrated bool
}

var probeLlamaServerVulkanDevices = func(_ []string) ([]vulkanPhysicalDevice, error) {
	return nil, errWindowsVulkanProbeUnsupported
}

// refineLlamaServerDevices 细化 ROCm（Linux）与 Vulkan（Windows）设备属性。
func refineLlamaServerDevices(devices []ml.DeviceInfo, libDirs []string) []ml.DeviceInfo {
	devices = refineLinuxROCmDevices(devices)
	return refineWindowsVulkanDevices(devices, libDirs)
}

// refineWindowsVulkanDevices 在 Windows 上用原生 Vulkan 枚举细化集成 GPU 标志。
func refineWindowsVulkanDevices(devices []ml.DeviceInfo, libDirs []string) []ml.DeviceInfo {
	if runtime.GOOS != "windows" {
		return devices
	}

	var vulkanIndexes []int
	for i, device := range devices {
		if device.Library != "Vulkan" {
			continue
		}
		vulkanIndexes = append(vulkanIndexes, i)
	}

	if len(vulkanIndexes) == 0 {
		return devices
	}

	probed, err := probeLlamaServerVulkanDevices(libDirs)
	if err != nil {
		if !errors.Is(err, errWindowsVulkanProbeUnsupported) {
			slog.Debug("windows vulkan device refinement unavailable", "error", err)
		}
		return devices
	}

	if !applyWindowsVulkanRefinement(devices, probed) {
		return devices
	}

	return devices
}

// applyWindowsVulkanRefinement 按设备名匹配 llama-server 与 Vulkan 探测结果并更新 Integrated。
func applyWindowsVulkanRefinement(devices []ml.DeviceInfo, probed []vulkanPhysicalDevice) bool {
	var vulkanIndexes []int
	for i, device := range devices {
		if device.Library == "Vulkan" {
			vulkanIndexes = append(vulkanIndexes, i)
		}
	}

	if len(probed) < len(vulkanIndexes) {
		slog.Debug("windows vulkan device refinement skipped: fewer probed devices than llama-server devices",
			"llama_server_count", len(vulkanIndexes), "vulkan_count", len(probed))
		return false
	}

	// 原生 Vulkan 枚举可能是 llama-server 的超集且顺序不同，故按名称匹配。
	// Raw Vulkan enumeration can be a superset of llama-server's device list
	// (extra ICDs, D3D12 mapping-layer devices) and the two orders can differ,
	// so match by name rather than requiring equal counts or matching indexes.
	matches := make([]int, len(vulkanIndexes))
	used := make([]bool, len(probed))
	for i, deviceIndex := range vulkanIndexes {
		matches[i] = -1
		description := devices[deviceIndex].Description
		for j, probedDevice := range probed {
			if used[j] || !sameVulkanDeviceName(description, probedDevice.Name) {
				continue
			}
			if matches[i] >= 0 {
				if probed[matches[i]].Integrated != probedDevice.Integrated {
					slog.Debug("windows vulkan device refinement skipped: ambiguous device name match",
						"index", i, "llama_server_name", description)
					return false
				}
				continue
			}
			matches[i] = j
		}
		if matches[i] < 0 {
			slog.Debug("windows vulkan device refinement skipped: device name mismatch",
				"index", i, "llama_server_name", description)
			return false
		}
		used[matches[i]] = true
	}

	for i, probedIndex := range matches {
		devices[vulkanIndexes[i]].Integrated = probed[probedIndex].Integrated
	}

	slog.Debug("windows vulkan device refinement applied", "devices", len(vulkanIndexes))
	return true
}

// sameVulkanDeviceName 判断两个 Vulkan 设备描述是否指向同一物理设备。
func sameVulkanDeviceName(a, b string) bool {
	return ml.SimilarDeviceDescription(a, b)
}
