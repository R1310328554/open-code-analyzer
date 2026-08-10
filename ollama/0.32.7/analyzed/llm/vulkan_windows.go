// Windows Vulkan 加载器选择与库路径调整。
//go:build windows

package llm

import (
	"errors"
	"log/slog"
	"os"
	"path/filepath"
	"strings"

	"golang.org/x/sys/windows"
)

// windowsVulkanRuntimeDLLName 主机 Vulkan 加载器 DLL 名称。
const windowsVulkanRuntimeDLLName = "vulkan-1.dll"

// WindowsVulkanRuntimeDLLPath 定位 vulkan-1.dll 绝对路径。
func WindowsVulkanRuntimeDLLPath(libDirs []string) (string, error) {
	systemDir, err := windows.GetSystemDirectory()
	if err != nil {
		return "", err
	}
	return windowsVulkanRuntimeDLLPath(systemDir, os.Getenv("PATH"), libDirs, fileExists)
}

// adjustWindowsVulkanLibraryPaths 将主机加载器目录插入 GPU 库路径之前。
func adjustWindowsVulkanLibraryPaths(paths, gpuLibs []string) []string {
	vulkanDir := firstWindowsVulkanLibDir(gpuLibs)
	if vulkanDir == "" {
		return paths
	}

	vulkanPath, err := WindowsVulkanRuntimeDLLPath(gpuLibs)
	if err != nil {
		slog.Debug("windows Vulkan loader selection unavailable", "error", err)
		return paths
	}

	slog.Debug("selected windows Vulkan loader", "path", vulkanPath)

	return insertPathBefore(paths, filepath.Dir(vulkanPath), vulkanDir)
}

// windowsVulkanRuntimeDLLPath 优先 System32，再搜 PATH（排除后端库目录）。
// Ollama 不再打包加载器，避免旧版 app-local 副本遮蔽主机运行时。
// Use the host Vulkan loader supplied by the installed Vulkan runtime or GPU
// driver. Ollama no longer packages the loader; exclude backend library
// directories from PATH probing so stale app-local copies from older installs
// cannot shadow the host runtime.
func windowsVulkanRuntimeDLLPath(
	systemDir string,
	pathEnv string,
	libDirs []string,
	exists func(string) bool,
) (string, error) {
	systemDir = filepath.Clean(systemDir)

	systemPath := filepath.Join(systemDir, windowsVulkanRuntimeDLLName)
	if exists(systemPath) {
		return systemPath, nil
	}

	if path := firstWindowsVulkanRuntimeDLLOnPath(pathEnv, libDirs, exists); path != "" {
		return path, nil
	}

	return "", errors.New("no host vulkan-1.dll runtime DLL found")
}

// firstWindowsVulkanRuntimeDLLOnPath 在 PATH 中查找 vulkan-1.dll，跳过 excludedDirs。
func firstWindowsVulkanRuntimeDLLOnPath(pathEnv string, excludedDirs []string, exists func(string) bool) string {
	for _, dir := range filepath.SplitList(pathEnv) {
		dir = strings.Trim(filepath.Clean(strings.Trim(dir, `"`)), `"`)
		if dir == "." || dir == "" || windowsDirInList(dir, excludedDirs) {
			continue
		}

		path := filepath.Join(dir, windowsVulkanRuntimeDLLName)
		if exists(path) {
			return filepath.Clean(path)
		}
	}
	return ""
}

// windowsDirInList 判断目录是否在排除列表或其子路径下。
func windowsDirInList(dir string, dirs []string) bool {
	dir = strings.ToLower(filepath.Clean(dir))
	for _, candidate := range dirs {
		candidate = strings.ToLower(filepath.Clean(candidate))
		if candidate == "" || candidate == "." {
			continue
		}
		if dir == candidate || strings.HasPrefix(dir, candidate+string(filepath.Separator)) {
			return true
		}
	}
	return false
}

// firstWindowsVulkanLibDir 定位首个 Vulkan/ggml-vulkan 库目录。
func firstWindowsVulkanLibDir(libDirs []string) string {
	for _, dir := range libDirs {
		if dir == "" {
			continue
		}
		base := strings.ToLower(filepath.Base(dir))
		if strings.Contains(base, "vulkan") {
			return filepath.Clean(dir)
		}
		if fileExists(filepath.Join(dir, "ggml-vulkan.dll")) || fileExists(filepath.Join(dir, "libggml-vulkan.dll")) {
			return filepath.Clean(dir)
		}
	}
	return ""
}
