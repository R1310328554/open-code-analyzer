// Package chat 实现 Ollama Agent 终端聊天界面（Bubble Tea）：会话渲染、输入、工具审批、云端认证与上下文压缩。
package chat

import (
	"context"
	"errors"
	"fmt"
	"os/exec"
	"runtime"
	"strings"

	tea "github.com/charmbracelet/bubbletea"
)

// chatClipboardErrorMsg 表示复制到系统剪贴板失败。
type chatClipboardErrorMsg struct {
	err error
}

// writeClipboard 为测试可替换的剪贴板写入入口。
var writeClipboard = writeSystemClipboard

// copyTextCmd 返回异步将 text 写入系统剪贴板的 Bubble Tea 命令。
func copyTextCmd(ctx context.Context, text string) tea.Cmd {
	return func() tea.Msg {
		if err := writeClipboard(ctx, text); err != nil {
			return chatClipboardErrorMsg{err: err}
		}
		return nil
	}
}

// writeSystemClipboard 按 GOOS 选择 pbcopy/clip/wl-copy/xclip/xsel。
func writeSystemClipboard(ctx context.Context, text string) error {
	if ctx == nil {
		ctx = context.Background()
	}
	switch runtime.GOOS {
	case "darwin":
		return runClipboardCommand(ctx, text, "pbcopy")
	case "windows":
		return runClipboardCommand(ctx, text, "clip")
	default:
		for _, candidate := range []struct {
			name string
			args []string
		}{
			{name: "wl-copy"},
			{name: "xclip", args: []string{"-selection", "clipboard"}},
			{name: "xsel", args: []string{"--clipboard", "--input"}},
		} {
			if _, err := exec.LookPath(candidate.name); err != nil {
				continue
			}
			return runClipboardCommand(ctx, text, candidate.name, candidate.args...)
		}
		return errors.New("no clipboard command found")
	}
}

// runClipboardCommand 通过 stdin 管道将文本传给外部剪贴板命令。
func runClipboardCommand(ctx context.Context, text, name string, args ...string) error {
	cmd := exec.CommandContext(ctx, name, args...)
	cmd.Stdin = strings.NewReader(text)
	if output, err := cmd.CombinedOutput(); err != nil {
		if len(output) > 0 {
			return fmt.Errorf("%s: %w: %s", name, err, strings.TrimSpace(string(output)))
		}
		return fmt.Errorf("%s: %w", name, err)
	}
	return nil
}
