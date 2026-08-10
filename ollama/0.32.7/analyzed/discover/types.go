// 发现模块类型：内存信息与设备详情日志。
package discover

import (
	"log/slog"
	"path/filepath"
	"sort"
	"strings"

	"github.com/ollama/ollama/format"
	"github.com/ollama/ollama/ml"
)

// memInfo 表示 CPU 侧总/可用内存与 swap 余量。
type memInfo struct {
	TotalMemory uint64 `json:"total_memory,omitempty"`
	FreeMemory  uint64 `json:"free_memory,omitempty"`
	FreeSwap    uint64 `json:"free_swap,omitempty"` // TODO：将 swap 与系统内存分开报告
	// TODO split this out for system only
}

// LogDetails 按调度偏好顺序输出各 GPU 及 CPU 推理能力详情。
func LogDetails(devices []ml.DeviceInfo) {
	sort.Sort(sort.Reverse(ml.ByFreeMemory(devices))) // 按空闲显存降序，与调度偏好一致
	// Report devices in order of scheduling preference
	for _, dev := range devices {
		var libs []string
		for _, dir := range dev.LibraryPath {
			if strings.Contains(dir, filepath.Join("lib", "ollama")) {
				libs = append(libs, filepath.Base(dir))
			}
		}
		typeStr := "discrete"
		if dev.Integrated {
			typeStr = "iGPU"
		}
		slog.Info("inference compute",
			"id", dev.ID,
			"filter_id", dev.FilterID,
			"library", dev.Library,
			"compute", dev.Compute(),
			"name", dev.Name,
			"description", dev.Description,
			"libdirs", strings.Join(libs, ","),
			"driver", dev.Driver(),
			"pci_id", dev.PCIID,
			"type", typeStr,
			"total", format.HumanBytes2(dev.TotalMemory),
			"available", format.HumanBytes2(dev.FreeMemory),
		)
	}
	// 无 GPU 时记录 CPU 推理能力。
	// CPU inference
	if len(devices) == 0 {
		dev, _ := GetCPUMem()
		slog.Info("inference compute",
			"id", "cpu",
			"library", "cpu",
			"compute", "",
			"name", "cpu",
			"description", "cpu",
			"libdirs", "ollama",
			"driver", "",
			"pci_id", "",
			"type", "",
			"total", format.HumanBytes2(dev.TotalMemory),
			"available", format.HumanBytes2(dev.FreeMemory),
		)
	}
}
