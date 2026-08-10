// Package server（Windows）提供 Windows 平台下的进程管理与路径常量。
package server

import (
	"context"
	"fmt"
	"log/slog"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"

	"golang.org/x/sys/windows"
)

// pidFile 与 serverLogPath 分别为 Ollama 服务 PID 与日志在 Windows 上的存放路径。
var (
	pidFile       = filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "ollama.pid")
	serverLogPath = filepath.Join(os.Getenv("LOCALAPPDATA"), "Ollama", "server.log")
)

// commandContext 创建隐藏窗口且独立进程组的 exec.Cmd。
func commandContext(ctx context.Context, name string, arg ...string) *exec.Cmd {
	cmd := exec.CommandContext(ctx, name, arg...)
	cmd.SysProcAttr = &syscall.SysProcAttr{
		HideWindow:    true,
		CreationFlags: windows.CREATE_NEW_PROCESS_GROUP,
	}

	return cmd
}

// terminate 通过 AttachConsole 与 CTRL_BREAK/CTRL_C 事件请求进程优雅退出。
func terminate(proc *os.Process) error {
	dll, err := windows.LoadDLL("kernel32.dll")
	if err != nil {
		return err
	}
	defer dll.Release()

	pid := proc.Pid

	f, err := dll.FindProc("AttachConsole")
	if err != nil {
		return err
	}

	r1, _, err := f.Call(uintptr(pid))
	if r1 == 0 && err != syscall.ERROR_ACCESS_DENIED {
		return err
	}

	f, err = dll.FindProc("SetConsoleCtrlHandler")
	if err != nil {
		return err
	}

	r1, _, err = f.Call(0, 1)
	if r1 == 0 {
		return err
	}

	f, err = dll.FindProc("GenerateConsoleCtrlEvent")
	if err != nil {
		return err
	}

	r1, _, err = f.Call(windows.CTRL_BREAK_EVENT, uintptr(pid))
	if r1 == 0 {
		return err
	}

	r1, _, err = f.Call(windows.CTRL_C_EVENT, uintptr(pid))
	if r1 == 0 {
		return err
	}

	return nil
}

// STILL_ACTIVE 为 Windows GetExitCodeProcess 表示进程仍在运行的退出码。
const STILL_ACTIVE = 259

// terminated 查询进程退出码以判断是否已结束。
func terminated(pid int) (bool, error) {
	hProcess, err := windows.OpenProcess(windows.PROCESS_QUERY_INFORMATION, false, uint32(pid))
	if err != nil {
		if errno, ok := err.(windows.Errno); ok && errno == windows.ERROR_INVALID_PARAMETER {
			return true, nil
		}
		return false, fmt.Errorf("failed to open process: %v", err)
	}
	defer windows.CloseHandle(hProcess)

	var exitCode uint32
	err = windows.GetExitCodeProcess(hProcess, &exitCode)
	if err != nil {
		return false, fmt.Errorf("failed to get exit code: %v", err)
	}

	if exitCode == STILL_ACTIVE {
		return false, nil
	}

	return true, nil
}

// ollamaServeProcess 通过 wmic 检查指定 PID 是否为 ollama serve。
func ollamaServeProcess(pid int) bool {
	cmd := exec.Command("wmic", "process", "where", fmt.Sprintf("ProcessId=%d", pid), "get", "CommandLine", "/value")
	cmd.SysProcAttr = &syscall.SysProcAttr{HideWindow: true}
	output, err := cmd.Output()
	if err != nil {
		slog.Debug("failed to inspect ollama process", "pid", pid, "err", err)
		return false
	}

	for _, line := range strings.Split(string(output), "\n") {
		line = strings.TrimSpace(line)
		commandLine, ok := strings.CutPrefix(line, "CommandLine=")
		if !ok {
			continue
		}

		return ollamaServeArgs(strings.Fields(strings.ToLower(commandLine)))
	}

	return false
}

// reapServers 使用 taskkill 终止除当前进程外的外部 ollama serve 实例。
// reapServers kills external ollama serve processes except our own.
func reapServers() error {
	// Get current process ID to avoid killing ourselves
	currentPID := os.Getpid()

	// Use wmic to find ollama processes
	cmd := exec.Command("wmic", "process", "where", "name='ollama.exe'", "get", "ProcessId")
	cmd.SysProcAttr = &syscall.SysProcAttr{HideWindow: true}
	output, err := cmd.Output()
	if err != nil {
		// No ollama processes found
		slog.Debug("no ollama processes found")
		return nil //nolint:nilerr
	}

	lines := strings.Split(string(output), "\n")
	var pids []string
	for _, line := range lines {
		line = strings.TrimSpace(line)
		if line == "" || line == "ProcessId" {
			continue
		}

		if _, err := strconv.Atoi(line); err == nil {
			pids = append(pids, line)
		}
	}

	for _, pidStr := range pids {
		pid, err := strconv.Atoi(pidStr)
		if err != nil {
			continue
		}

		if pid == currentPID {
			continue
		}
		if !ollamaServeProcess(pid) {
			continue
		}

		cmd := exec.Command("taskkill", "/F", "/T", "/PID", pidStr)
		if err := cmd.Run(); err != nil {
			slog.Warn("failed to kill ollama process", "pid", pid, "err", err)
		}
	}

	return nil
}
