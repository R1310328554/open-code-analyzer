// Windows ROCm 运行时 DLL 选择与库路径注入。
//go:build windows

package llm

import (
	"errors"
	"fmt"
	"log/slog"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"unsafe"

	"golang.org/x/sys/windows"
)

// windowsROCmRuntimeDLLNames 按优先级列出 amdhip64 运行时 DLL 名称。
var (
	windowsROCmRuntimeDLLNames          = []string{"amdhip64_7.dll", "amdhip64_6.dll", "amdhip64.dll"}
	windowsROCmRuntimeCompanionDLLNames = map[string][]string{
		"amdhip64_7.dll": {"amd_comgr_3.dll"},
		"amdhip64_6.dll": {"amd_comgr_2.dll"},
		"amdhip64.dll":   {"amd_comgr.dll"},
	}
)

// windowsROCmRuntimeDLL 记录 bunded 与 system 两路 ROCm 运行时选择结果。
type windowsROCmRuntimeDLL struct {
	path         string
	source       string
	name         string
	bundledPath  string
	bundledVer   windowsFileVersion
	bundledVerOK bool
	systemPath   string
	systemVer    windowsFileVersion
	systemVerOK  bool
	systemDir    string
	bundledDir   string
}

// windowsFileVersion 封装 Windows 文件版本 MS/LS 双字。
type windowsFileVersion struct {
	ms uint32
	ls uint32
}

// windowsVersionTranslation 表示版本资源中的语言/代码页。
type windowsVersionTranslation struct {
	language uint16
	codePage uint16
}

// WindowsROCmRuntimeDLLPath 返回应使用的 amdhip64 运行时 DLL 绝对路径。
func WindowsROCmRuntimeDLLPath(libDirs []string) (string, error) {
	choice, err := windowsROCmRuntimeDLLChoice(libDirs)
	if err != nil {
		return "", err
	}
	return choice.path, nil
}

// adjustPlatformLibraryPaths 依次调整 ROCm 与 Vulkan 库搜索路径。
func adjustPlatformLibraryPaths(paths, gpuLibs []string) []string {
	paths = adjustWindowsROCmLibraryPaths(paths, gpuLibs)
	return adjustWindowsVulkanLibraryPaths(paths, gpuLibs)
}

// adjustWindowsROCmLibraryPaths 若选用 System32 运行时，将其目录插入 PATH 前缀。
func adjustWindowsROCmLibraryPaths(paths, gpuLibs []string) []string {
	rocmDir := firstWindowsROCmLibDir(gpuLibs)
	if rocmDir == "" {
		return paths
	}

	choice, err := windowsROCmRuntimeDLLChoice(gpuLibs)
	if err != nil {
		slog.Debug("windows ROCm runtime selection unavailable", "error", err)
		return paths
	}

	slog.Debug("selected windows ROCm runtime",
		"runtime_source", choice.source,
		"name", choice.name,
		"path", choice.path,
		"bundled", choice.bundledPath,
		"bundled_version", choice.bundledVer.String(),
		"system", choice.systemPath,
		"system_version", choice.systemVer.String(),
	)

	if choice.source != "system" || choice.systemDir == "" {
		return paths
	}

	before := choice.bundledDir
	if before == "" {
		before = rocmDir
	}

	return insertPathBefore(paths, choice.systemDir, before)
}

// windowsROCmRuntimeDLLChoice 在 bundled 与 System32 间选较新版本；
// 同主版本内优先较新驱动或 bundled 副本，不搜索 ROCm SDK 安装路径。
// AMD's Windows driver also ships ROCm runtime DLLs in System32. Within the
// same DLL name/major, use the newer driver or bundled copy; never search the
// ROCm SDK installation paths.
func windowsROCmRuntimeDLLChoice(libDirs []string) (windowsROCmRuntimeDLL, error) {
	systemDir, err := windows.GetSystemDirectory()
	if err != nil {
		return windowsROCmRuntimeDLL{}, err
	}
	systemDir = filepath.Clean(systemDir)

	for _, name := range windowsROCmRuntimeDLLNames {
		bundledPath := firstExistingFile(libDirs, name)
		if bundledPath == "" {
			continue
		}

		choice := windowsROCmRuntimeDLL{
			path:        bundledPath,
			source:      "bundled",
			name:        name,
			bundledPath: bundledPath,
			bundledDir:  filepath.Dir(bundledPath),
			systemPath:  filepath.Join(systemDir, name),
			systemDir:   systemDir,
		}
		choice.bundledVer, choice.bundledVerOK = readWindowsFileVersion(choice.bundledPath)
		if fileExists(choice.systemPath) {
			choice.systemVer, choice.systemVerOK = readWindowsFileVersion(choice.systemPath)
			if choice.systemVerOK && choice.bundledVerOK && choice.systemVer.Compare(choice.bundledVer) > 0 {
				choice.path = choice.systemPath
				choice.source = "system"
			}
		}
		if choice.source == "system" && !systemROCmCompanionDLLsCompatible(libDirs, choice.name, choice.systemDir) {
			choice.path = choice.bundledPath
			choice.source = "bundled"
		}
		return choice, nil
	}

	for _, name := range windowsROCmRuntimeDLLNames {
		path := filepath.Join(systemDir, name)
		if !fileExists(path) {
			continue
		}
		choice := windowsROCmRuntimeDLL{
			path:       path,
			source:     "system",
			name:       name,
			systemPath: path,
			systemDir:  systemDir,
		}
		choice.systemVer, choice.systemVerOK = readWindowsFileVersion(path)
		return choice, nil
	}

	return windowsROCmRuntimeDLL{}, errors.New("no amdhip64 runtime DLL found")
}

// systemROCmCompanionDLLsCompatible 校验 System32 配套 amd_comgr DLL 版本不低于 bundled。
func systemROCmCompanionDLLsCompatible(libDirs []string, runtimeName, systemDir string) bool {
	for _, name := range windowsROCmRuntimeCompanionDLLNames[strings.ToLower(runtimeName)] {
		bundledPath := firstExistingFile(libDirs, name)
		if bundledPath == "" {
			continue
		}

		systemPath := filepath.Join(systemDir, name)
		if !fileExists(systemPath) {
			slog.Debug("keeping bundled ROCm runtime because system companion DLL is missing",
				"name", name, "bundled", bundledPath, "system", systemPath)
			return false
		}

		bundledVer, bundledOK := readWindowsFileVersion(bundledPath)
		systemVer, systemOK := readWindowsFileVersion(systemPath)
		if !bundledOK || !systemOK {
			slog.Debug("keeping bundled ROCm runtime because companion DLL version is unavailable",
				"name", name, "bundled", bundledPath, "system", systemPath)
			return false
		}
		if systemVer.Compare(bundledVer) < 0 {
			slog.Debug("keeping bundled ROCm runtime because system companion DLL is older",
				"name", name,
				"bundled", bundledPath,
				"bundled_version", bundledVer.String(),
				"system", systemPath,
				"system_version", systemVer.String(),
			)
			return false
		}
	}
	return true
}

// firstWindowsROCmLibDir 从 GPU 库目录列表中定位首个 ROCm/HIP 目录。
func firstWindowsROCmLibDir(libDirs []string) string {
	for _, dir := range libDirs {
		if dir == "" {
			continue
		}
		base := strings.ToLower(filepath.Base(dir))
		if strings.Contains(base, "rocm") || strings.Contains(base, "hip") {
			return filepath.Clean(dir)
		}
		if fileExists(filepath.Join(dir, "ggml-hip.dll")) || fileExists(filepath.Join(dir, "libggml-hip.dll")) {
			return filepath.Clean(dir)
		}
	}
	return ""
}

// firstExistingFile 在多个目录中查找首个存在的文件名。
func firstExistingFile(dirs []string, name string) string {
	for _, dir := range dirs {
		if dir == "" {
			continue
		}
		path := filepath.Join(dir, name)
		if fileExists(path) {
			return filepath.Clean(path)
		}
	}
	return ""
}

// insertPathBefore 将 insert 目录插入 paths 中 before 之前（去重）。
func insertPathBefore(paths []string, insert, before string) []string {
	insert = filepath.Clean(insert)
	before = filepath.Clean(before)
	insertKey := strings.ToLower(insert)
	beforeKey := strings.ToLower(before)

	out := make([]string, 0, len(paths)+1)
	inserted := false
	for _, path := range paths {
		clean := filepath.Clean(path)
		key := strings.ToLower(clean)
		if key == insertKey {
			continue
		}
		if !inserted && key == beforeKey {
			out = append(out, insert)
			inserted = true
		}
		out = append(out, path)
	}
	if !inserted {
		out = append(out, insert)
	}
	return out
}

// fileExists 判断路径是否为存在的普通文件。
func fileExists(path string) bool {
	info, err := os.Stat(path)
	return err == nil && !info.IsDir()
}

// readWindowsFileVersion 读取 PE 文件版本资源。
func readWindowsFileVersion(path string) (windowsFileVersion, bool) {
	var zero windows.Handle
	infoSize, err := windows.GetFileVersionInfoSize(path, &zero)
	if err != nil || infoSize == 0 {
		return windowsFileVersion{}, false
	}

	versionInfo := make([]byte, infoSize)
	if err := windows.GetFileVersionInfo(path, 0, infoSize, unsafe.Pointer(&versionInfo[0])); err != nil {
		return windowsFileVersion{}, false
	}

	if version, ok := readWindowsStringFileVersion(versionInfo); ok {
		return version, true
	}

	var fixedInfo *windows.VS_FIXEDFILEINFO
	var fixedInfoLen uint32
	if err := windows.VerQueryValue(unsafe.Pointer(&versionInfo[0]), `\`, unsafe.Pointer(&fixedInfo), &fixedInfoLen); err != nil {
		return windowsFileVersion{}, false
	}
	if fixedInfo == nil {
		return windowsFileVersion{}, false
	}
	return windowsFileVersion{ms: fixedInfo.FileVersionMS, ls: fixedInfo.FileVersionLS}, true
}

// readWindowsStringFileVersion 从 StringFileInfo 解析四段版本号。
func readWindowsStringFileVersion(versionInfo []byte) (windowsFileVersion, bool) {
	translations := windowsVersionTranslations(versionInfo)
	if len(translations) == 0 {
		translations = []windowsVersionTranslation{{language: 0x0409, codePage: 0x04b0}}
	}

	for _, translation := range translations {
		for _, name := range []string{"FileVersion", "ProductVersion"} {
			value, ok := windowsVersionString(versionInfo, translation, name)
			if !ok {
				continue
			}
			version, ok := parseWindowsFileVersionString(value)
			if ok {
				return version, true
			}
		}
	}

	return windowsFileVersion{}, false
}

// windowsVersionTranslations 提取版本资源中的语言/代码页列表。
func windowsVersionTranslations(versionInfo []byte) []windowsVersionTranslation {
	var translations *windowsVersionTranslation
	var translationsLen uint32
	if err := windows.VerQueryValue(unsafe.Pointer(&versionInfo[0]), `\VarFileInfo\Translation`, unsafe.Pointer(&translations), &translationsLen); err != nil {
		return nil
	}
	if translations == nil || translationsLen < uint32(unsafe.Sizeof(windowsVersionTranslation{})) {
		return nil
	}
	return unsafe.Slice(translations, int(translationsLen)/int(unsafe.Sizeof(windowsVersionTranslation{})))
}

// windowsVersionString 读取指定翻译块下的 FileVersion/ProductVersion 字符串。
func windowsVersionString(versionInfo []byte, translation windowsVersionTranslation, name string) (string, bool) {
	subBlock := fmt.Sprintf(`\StringFileInfo\%04x%04x\%s`, translation.language, translation.codePage, name)
	var value *uint16
	var valueLen uint32
	if err := windows.VerQueryValue(unsafe.Pointer(&versionInfo[0]), subBlock, unsafe.Pointer(&value), &valueLen); err != nil {
		return "", false
	}
	if value == nil || valueLen == 0 {
		return "", false
	}
	return windows.UTF16PtrToString(value), true
}

// parseWindowsFileVersionString 将 "a.b.c.d" 字符串解析为 MS/LS 版本。
func parseWindowsFileVersionString(s string) (windowsFileVersion, bool) {
	parts := strings.FieldsFunc(s, func(r rune) bool {
		return r < '0' || r > '9'
	})
	if len(parts) < 2 {
		return windowsFileVersion{}, false
	}

	var version [4]uint16
	for i := 0; i < len(version) && i < len(parts); i++ {
		part, err := strconv.ParseUint(parts[i], 10, 16)
		if err != nil {
			return windowsFileVersion{}, false
		}
		version[i] = uint16(part)
	}

	return windowsFileVersion{
		ms: uint32(version[0])<<16 | uint32(version[1]),
		ls: uint32(version[2])<<16 | uint32(version[3]),
	}, true
}

// Compare 比较两个文件版本，返回 -1/0/1。
func (v windowsFileVersion) Compare(other windowsFileVersion) int {
	if v.ms < other.ms {
		return -1
	}
	if v.ms > other.ms {
		return 1
	}
	if v.ls < other.ls {
		return -1
	}
	if v.ls > other.ls {
		return 1
	}
	return 0
}

// String 格式化为 "major.minor.build.revision"。
func (v windowsFileVersion) String() string {
	if v.ms == 0 && v.ls == 0 {
		return ""
	}
	return fmt.Sprintf("%d.%d.%d.%d",
		(v.ms>>16)&0xffff,
		v.ms&0xffff,
		(v.ls>>16)&0xffff,
		v.ls&0xffff,
	)
}
