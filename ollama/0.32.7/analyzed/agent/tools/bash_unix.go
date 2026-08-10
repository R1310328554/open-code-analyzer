// bash_unix.go 提供非 Windows 平台的 bash 工具实现。
//go:build !windows

package tools

import (
	"context"
	"os/exec"
	"strings"
	"syscall"
)

// shellToolName 在 Unix 上返回 "bash"。
func shellToolName() string {
	return "bash"
}

func shellToolDescription() string {
	return "Execute a bash command on the system. Use this to inspect files, run tests, and perform development tasks."
}

func shellCommandDescription() string {
	return "The bash command to execute."
}

// newBashCommand 构造 bash -c 命令并在结束时写入 pwd。
func newBashCommand(ctx context.Context, command, cwdPath string) *exec.Cmd {
	script := command + "\n__ollama_status=$?\npwd -P > " + shellQuote(cwdPath) + "\nexit $__ollama_status"
	cmd := exec.CommandContext(ctx, "bash", "-c", script)
	configureBashCommand(cmd)
	return cmd
}

func shellQuote(value string) string {
	return "'" + strings.ReplaceAll(value, "'", "'\\''") + "'"
}

func configureBashCommand(cmd *exec.Cmd) {
	cmd.SysProcAttr = &syscall.SysProcAttr{Setpgid: true}
}

func runBashCommand(cmd *exec.Cmd) error {
	return cmd.Run()
}

// killBashCommand 向进程组发送 SIGKILL 终止命令。
func killBashCommand(cmd *exec.Cmd) error {
	if cmd == nil || cmd.Process == nil {
		return nil
	}
	_ = syscall.Kill(-cmd.Process.Pid, syscall.SIGKILL)
	return nil
}
