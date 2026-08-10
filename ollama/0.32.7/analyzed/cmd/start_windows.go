// Windows 平台：通过 ollama app.exe 隐藏启动 GUI 并等待 API 就绪。
package cmd

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"strings"
	"syscall"
	"unsafe"

	"github.com/ollama/ollama/api"
	"golang.org/x/sys/windows"
)

// Installer 为升级安装程序进程名，运行中则拒绝并发启动。
const (
	Installer = "OllamaSetup.exe"
)

// startApp 定位 ollama app.exe、无窗口启动并等待服务。
func startApp(ctx context.Context, client *api.Client) error {
	if len(isProcRunning(Installer)) > 0 {
		return fmt.Errorf("upgrade in progress...")
	}
	AppName := "ollama app.exe"
	exe, err := os.Executable()
	if err != nil {
		return err
	}
	appExe := filepath.Join(filepath.Dir(exe), AppName)
	_, err = os.Stat(appExe)
	if errors.Is(err, os.ErrNotExist) {
		// 可执行文件旁不存在时尝试 %LOCALAPPDATA%\Ollama
		// Try the standard install location
		localAppData := os.Getenv("LOCALAPPDATA")
		appExe = filepath.Join(localAppData, "Ollama", AppName)
		_, err := os.Stat(appExe)
		if errors.Is(err, os.ErrNotExist) {
			// 仍找不到则在 PATH 中搜索
			// Finally look in the path
			appExe, err = exec.LookPath(AppName)
			if err != nil {
				return errors.New("could not locate ollama app")
			}
		}
	}

	cmd_path := "c:\\Windows\\system32\\cmd.exe"
	cmd := exec.Command(cmd_path, "/c", appExe, "--hide", "--fast-startup")
	cmd.SysProcAttr = &syscall.SysProcAttr{CreationFlags: 0x08000000, HideWindow: true}

	cmd.Stdin = strings.NewReader("")
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Start(); err != nil {
		return fmt.Errorf("unable to start ollama app %w", err)
	}

	if cmd.Process != nil {
		defer cmd.Process.Release() //nolint:errcheck
	}
	return waitForServer(ctx, client)
}

// isProcRunning 枚举进程模块基名，返回匹配 procName 的 PID 列表。
func isProcRunning(procName string) []uint32 {
	pids := make([]uint32, 2048)
	var ret uint32
	if err := windows.EnumProcesses(pids, &ret); err != nil || ret == 0 {
		slog.Debug("failed to check for running installers", "error", err)
		return nil
	}
	if ret > uint32(len(pids)) {
		pids = make([]uint32, ret+10)
		if err := windows.EnumProcesses(pids, &ret); err != nil || ret == 0 {
			slog.Debug("failed to check for running installers", "error", err)
			return nil
		}
	}
	if ret < uint32(len(pids)) {
		pids = pids[:ret]
	}
	var matches []uint32
	for _, pid := range pids {
		if pid == 0 {
			continue
		}
		hProcess, err := windows.OpenProcess(windows.PROCESS_QUERY_INFORMATION|windows.PROCESS_VM_READ, false, pid)
		if err != nil {
			continue
		}
		defer windows.CloseHandle(hProcess)
		var module windows.Handle
		var cbNeeded uint32
		cb := (uint32)(unsafe.Sizeof(module))
		if err := windows.EnumProcessModules(hProcess, &module, cb, &cbNeeded); err != nil {
			continue
		}
		var sz uint32 = 1024 * 8
		moduleName := make([]uint16, sz)
		cb = uint32(len(moduleName)) * (uint32)(unsafe.Sizeof(uint16(0)))
		if err := windows.GetModuleBaseName(hProcess, module, &moduleName[0], cb); err != nil && err != syscall.ERROR_INSUFFICIENT_BUFFER {
			continue
		}
		exeFile := path.Base(strings.ToLower(syscall.UTF16ToString(moduleName)))
		if strings.EqualFold(exeFile, procName) {
			matches = append(matches, pid)
		}
	}
	return matches
}
