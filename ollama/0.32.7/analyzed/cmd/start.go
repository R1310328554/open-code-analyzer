//go:build darwin || windows

package cmd

import (
	"context"
	"errors"
	"time"

	"github.com/ollama/ollama/api"
)

// waitForServer 轮询 Heartbeat，直到本地 Ollama 服务在 5 秒内就绪。
func waitForServer(ctx context.Context, client *api.Client) error {
	// 等待后台服务进程完成启动
	// wait for the server to start
	timeout := time.After(5 * time.Second)
	tick := time.Tick(500 * time.Millisecond)
	for {
		select {
		case <-timeout:
			return errors.New("timed out waiting for server to start")
		case <-tick:
			if err := client.Heartbeat(ctx); err == nil {
				return nil // 服务已响应心跳
			// server has started
			}
		}
	}
}
