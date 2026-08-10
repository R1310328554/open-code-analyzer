package main

// Fluent Bit 插件 Loki 客户端工厂。
// 按配置决定使用直连 Promtail client 或带缓冲的 dque 客户端。

import (
	"github.com/go-kit/log"

	"github.com/grafana/loki/v3/clients/pkg/promtail/client"
)

// 根据 buffer 开关创建直连或缓冲型 Loki 推送客户端。
// NewClient creates a new client based on the fluentbit configuration.
func NewClient(cfg *config, logger log.Logger, metrics *client.Metrics) (client.Client, error) {
	if cfg.bufferConfig.buffer {
		return NewBuffer(cfg, logger, metrics)
	}
	return client.New(metrics, cfg.clientConfig, 0, 0, false, logger)
}
