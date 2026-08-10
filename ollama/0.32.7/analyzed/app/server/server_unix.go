//go:build darwin

// Package server（darwin）提供 macOS 平台下的进程管理与路径常量。
package server

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"syscall"
)

// pidFile 与 serverLogPath 分别为 Ollama 服务 PID 与日志在 macOS 上的存放路径。
var (
	pidFile       = filepath.Join(os.Getenv("HOME"), "Library", "Application Support", "Ollama", "ollama.pid")
	serverLogPath = filepath.Join(os.Getenv("HOME"), ".ollama", "logs", "server.log")
)

// commandContext 创建带上下文的 exec.Cmd。
func commandContext(ctx context.Context, name string, arg ...string) *exec.Cmd {
	return exec.CommandContext(ctx, name, arg...)
}

// terminate 向进程发送 SIGINT 请求优雅退出。
func terminate(proc *os.Process) error {
	return proc.Signal(os.Interrupt)
}

// terminated 通过 signal(0) 探测进程是否已结束。
func terminated(pid int) (bool, error) {
	proc, err := os.FindProcess(pid)
	if err != nil {
		return false, fmt.Errorf("failed to find process: %v", err)
	}

	err = proc.Signal(syscall.Signal(0))
	if err != nil {
		if errors.Is(err, os.ErrProcessDone) || errors.Is(err, syscall.ESRCH) {
			return true, nil
		}

		return false, fmt.Errorf("error signaling process: %v", err)
	}

	return false, nil
}

// ollamaServeProcess 检查指定 PID 是否正在运行 ollama serve。
func ollamaServeProcess(pid int) bool {
	output, err := exec.Command("ps", "-p", strconv.Itoa(pid), "-o", "args=").Output()
	if err != nil {
		slog.Debug("failed to inspect ollama process", "pid", pid, "err", err)
		return false
	}

	return ollamaServeArgs(strings.Fields(strings.TrimSpace(string(output))))
}

// reapServers 终止除当前进程外的所有外部 ollama serve 实例（如端口冲突时）。
// reapServers kills external ollama serve processes except our own.
func reapServers() error {
	// Get our own PID to avoid killing ourselves
	currentPID := os.Getpid()

	// Use pkill to kill ollama processes
	// -x matches the whole command name exactly
	// We'll get the list first, then kill selectively
	cmd := exec.Command("pgrep", "-x", "ollama")
	output, err := cmd.Output()
	if err != nil {
		// No ollama processes found
		slog.Debug("no ollama processes found")
		return nil //nolint:nilerr
	}

	pidsStr := strings.TrimSpace(string(output))
	if pidsStr == "" {
		return nil
	}

	pids := strings.Split(pidsStr, "\n")
	for _, pidStr := range pids {
		pidStr = strings.TrimSpace(pidStr)
		if pidStr == "" {
			continue
		}

		pid, err := strconv.Atoi(pidStr)
		if err != nil {
			slog.Debug("failed to parse PID", "pidStr", pidStr, "err", err)
			continue
		}
		if pid == currentPID {
			continue
		}
		if !ollamaServeProcess(pid) {
			continue
		}

		proc, err := os.FindProcess(pid)
		if err != nil {
			slog.Debug("failed to find process", "pid", pid, "err", err)
			continue
		}

		if err := proc.Signal(syscall.SIGTERM); err != nil {
			// Try SIGKILL if SIGTERM fails
			if err := proc.Signal(syscall.SIGKILL); err != nil {
				slog.Warn("failed to stop external ollama process", "pid", pid, "err", err)
				continue
			}
		}

		slog.Info("stopped external ollama process", "pid", pid)
	}

	return nil
}
