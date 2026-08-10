// 系统内存与 Jetson 探测：调度器使用的 host 内存与 Jetpack 识别。
package discover

import (
	"log/slog"
	"os"
	"regexp"
	"runtime"
	"strconv"
	"strings"
	"time"

	"github.com/ollama/ollama/logutil"
	"github.com/ollama/ollama/ml"
)

// Jetson 设备可通过 JETSON_JETPACK 环境变量或 /etc/nv_tegra_release 识别 Jetpack。
// Jetson devices have JETSON_JETPACK="x.y.z" factory set to the Jetpack version installed.
// Included to drive logic for reducing Ollama-allocated overhead on L4T/Jetson devices.
// CudaTegra 缓存 Jetson Jetpack 版本字符串，用于降低 L4T 设备内存开销。
var CudaTegra string = os.Getenv("JETSON_JETPACK")

// GetSystemInfo 返回调度器使用的 host 总/可用内存与 swap。
// GetSystemInfo returns host memory information used by scheduling.
func GetSystemInfo() ml.SystemInfo {
	logutil.Trace("performing system memory discovery")
	startDiscovery := time.Now()
	defer func() {
		logutil.Trace("system memory discovery completed", "duration", time.Since(startDiscovery))
	}()

	memInfo, err := GetCPUMem()
	if err != nil {
		slog.Warn("error looking up system memory", "error", err)
	}

	return ml.SystemInfo{
		TotalMemory: memInfo.TotalMemory,
		FreeMemory:  memInfo.FreeMemory,
		FreeSwap:    memInfo.FreeSwap,
	}
}

// cudaJetpack 将 L4T 版本或 JETSON_JETPACK 映射为 jetpack5/jetpack6 等标签。
func cudaJetpack() string {
	if runtime.GOARCH == "arm64" && runtime.GOOS == "linux" {
		if CudaTegra != "" {
			ver := strings.Split(CudaTegra, ".")
			if len(ver) > 0 {
				return "jetpack" + ver[0]
			}
		} else if data, err := os.ReadFile("/etc/nv_tegra_release"); err == nil {
			r := regexp.MustCompile(` R(\d+) `)
			m := r.FindSubmatch(data)
			if len(m) != 2 {
				slog.Info("Unexpected format for /etc/nv_tegra_release.  Set JETSON_JETPACK to select version")
			} else {
				if l4t, err := strconv.Atoi(string(m[1])); err == nil {
					// L4T 到 Jetpack 映射非线性，需查 NVIDIA 归档表。
				// Note: mapping from L4t -> JP is inconsistent (can't just subtract 30)
					// https://developer.nvidia.com/embedded/jetpack-archive
					switch l4t {
					case 35:
						return "jetpack5"
					case 36:
						return "jetpack6"
					default:
						// 较新 Jetson 使用 SBSU 运行时。
					// Newer Jetson systems use the SBSU runtime
						slog.Debug("unrecognized L4T version", "nv_tegra_release", string(data))
					}
				}
			}
		}
	}
	return ""
}
