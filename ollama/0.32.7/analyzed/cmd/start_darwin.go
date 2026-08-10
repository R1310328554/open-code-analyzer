// macOS 平台：CLI 无法直连服务时通过 open 启动 Ollama.app 并等待就绪。
package cmd

import (
	"context"
	"errors"
	"os"
	"os/exec"
	"regexp"

	"github.com/ollama/ollama/api"
)

// errNotRunning 表示本地 Ollama 服务未运行且无法自动拉起。
var errNotRunning = errors.New("could not connect to ollama server, run 'ollama serve' to start it")

// startApp 解析 .app 路径，以 --fast-startup 启动 GUI 并阻塞至 API 可用。
func startApp(ctx context.Context, client *api.Client) error {
	exe, err := os.Executable()
	if err != nil {
		return errNotRunning
	}
	link, err := os.Readlink(exe)
	if err != nil {
		return errNotRunning
	}
	r := regexp.MustCompile(`^.*/Ollama\s?\d*.app`)
	m := r.FindStringSubmatch(link)
	if len(m) != 1 {
		return errNotRunning
	}
	if err := exec.Command("/usr/bin/open", "-j", "-a", m[0], "--args", "--fast-startup").Run(); err != nil {
		return err
	}
	return waitForServer(ctx, client)
}
