//go:build !windows && !darwin
// 非 Windows/macOS 平台：不支持自动启动桌面应用，仅返回提示错误。

package cmd

import (
	"context"
	"errors"

	"github.com/ollama/ollama/api"
)

// startApp 在非桌面平台直接提示用户手动运行 ollama serve。
func startApp(ctx context.Context, client *api.Client) error {
	return errors.New("could not connect to ollama server, run 'ollama serve' to start it")
}
