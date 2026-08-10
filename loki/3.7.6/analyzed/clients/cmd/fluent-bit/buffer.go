package main

// Fluent Bit Loki 输出插件的缓冲层工厂。
// 按 bufferType 选择底层持久化队列实现（当前支持 dque）。

import (
	"fmt"

	"github.com/go-kit/log"

	"github.com/grafana/loki/v3/clients/pkg/promtail/client"
)

type bufferConfig struct {
	buffer     bool
	bufferType string
	dqueConfig dqueConfig
}

var defaultBufferConfig = bufferConfig{
	buffer:     false,
	bufferType: "dque",
	dqueConfig: defaultDqueConfig,
}

// 根据 bufferType 创建带本地持久化队列的 Loki 客户端。
// NewBuffer makes a new buffered Client.
func NewBuffer(cfg *config, logger log.Logger, metrics *client.Metrics) (client.Client, error) {
	switch cfg.bufferConfig.bufferType {
	case "dque":
		return newDque(cfg, logger, metrics)
	default:
		return nil, fmt.Errorf("failed to parse bufferType: %s", cfg.bufferConfig.bufferType)
	}
}
