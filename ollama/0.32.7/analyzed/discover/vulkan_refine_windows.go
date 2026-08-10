// Windows Vulkan 物理设备探测：直接调用 vkCreateInstance 枚举 GPU。
package discover

import (
	"fmt"
	"unsafe"

	"github.com/ollama/ollama/llm"
)

const (
	vkSuccess                           = 0
	vkStructureTypeInstanceCreateInfo   = 1
	vkPhysicalDeviceTypeIntegratedGPU   = 1
	vkMaxPhysicalDeviceNameSize         = 256
	vkPhysicalDevicePropertiesByteCount = 4096
)

// vkInstanceCreateInfo 对应 Vulkan VkInstanceCreateInfo 头部字段。
type vkInstanceCreateInfo struct {
	SType                   uint32
	PNext                   uintptr
	Flags                   uint32
	PApplicationInfo        uintptr
	EnabledLayerCount       uint32
	PpEnabledLayerNames     uintptr
	EnabledExtensionCount   uint32
	PpEnabledExtensionNames uintptr
}

// init 将 probeLlamaServerVulkanDevices 绑定为 Windows Vulkan 枚举实现。
func init() {
	probeLlamaServerVulkanDevices = windowsVulkanPhysicalDevices
}

// windowsVulkanPhysicalDevices 创建 Vulkan 实例并枚举物理设备类型与名称。
func windowsVulkanPhysicalDevices(libDirs []string) ([]vulkanPhysicalDevice, error) {
	vulkanPath, err := llm.WindowsVulkanRuntimeDLLPath(libDirs)
	if err != nil {
		return nil, err
	}
	vulkanDLL, err := loadDLLFromPath(vulkanPath)
	if err != nil {
		return nil, err
	}
	vkCreateInstanceProc, err := findProc(vulkanDLL, "vkCreateInstance")
	if err != nil {
		return nil, fmt.Errorf("vkCreateInstance unavailable: %w", err)
	}
	vkDestroyInstanceProc, err := findProc(vulkanDLL, "vkDestroyInstance")
	if err != nil {
		return nil, fmt.Errorf("vkDestroyInstance unavailable: %w", err)
	}
	vkEnumeratePhysicalDevices, err := findProc(vulkanDLL, "vkEnumeratePhysicalDevices")
	if err != nil {
		return nil, fmt.Errorf("vkEnumeratePhysicalDevices unavailable: %w", err)
	}
	vkGetPhysicalDeviceProperties, err := findProc(vulkanDLL, "vkGetPhysicalDeviceProperties")
	if err != nil {
		return nil, fmt.Errorf("vkGetPhysicalDeviceProperties unavailable: %w", err)
	}

	createInfo := vkInstanceCreateInfo{SType: vkStructureTypeInstanceCreateInfo}
	var instance uintptr
	result, _, callErr := vkCreateInstanceProc.Call(
		uintptr(unsafe.Pointer(&createInfo)),
		0,
		uintptr(unsafe.Pointer(&instance)),
	)
	if result != vkSuccess {
		return nil, fmt.Errorf("vkCreateInstance failed: result=%d error=%w", result, callErr)
	}
	defer vkDestroyInstanceProc.Call(instance, 0)

	var count uint32
	result, _, callErr = vkEnumeratePhysicalDevices.Call(
		instance,
		uintptr(unsafe.Pointer(&count)),
		0,
	)
	if result != vkSuccess {
		return nil, fmt.Errorf("vkEnumeratePhysicalDevices count failed: result=%d error=%w", result, callErr)
	}
	if count == 0 {
		return nil, nil
	}

	physicalDevices := make([]uintptr, int(count))
	result, _, callErr = vkEnumeratePhysicalDevices.Call(
		instance,
		uintptr(unsafe.Pointer(&count)),
		uintptr(unsafe.Pointer(&physicalDevices[0])),
	)
	if result != vkSuccess {
		return nil, fmt.Errorf("vkEnumeratePhysicalDevices failed: result=%d error=%w", result, callErr)
	}

	devices := make([]vulkanPhysicalDevice, 0, count)
	for _, physicalDevice := range physicalDevices[:int(count)] {
		properties := make([]byte, vkPhysicalDevicePropertiesByteCount)
		vkGetPhysicalDeviceProperties.Call(
			physicalDevice,
			uintptr(unsafe.Pointer(&properties[0])),
		)
		deviceType := *(*uint32)(unsafe.Pointer(&properties[16]))
		deviceNameBytes := properties[20 : 20+vkMaxPhysicalDeviceNameSize]
		devices = append(devices, vulkanPhysicalDevice{
			Name:       nulTerminatedString(deviceNameBytes),
			Integrated: deviceType == vkPhysicalDeviceTypeIntegratedGPU,
		})
	}

	return devices, nil
}

// nulTerminatedString 从属性缓冲区提取 NUL 结尾的设备名字符串。
func nulTerminatedString(data []byte) string {
	for i, b := range data {
		if b == 0 {
			return string(data[:i])
		}
	}
	return string(data)
}
