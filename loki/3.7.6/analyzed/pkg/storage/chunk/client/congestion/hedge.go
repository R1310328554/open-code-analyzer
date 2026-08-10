package congestion

// hedge 提供 NoopHedger 空实现：不启用请求对冲（hedging），直接返回默认 HTTP 客户端，供拥塞控制关闭 hedge 策略时使用。

import (
	"net/http"

	"github.com/go-kit/log"

	"github.com/grafana/loki/v3/pkg/storage/chunk/client/hedging"
)

type NoopHedger struct{}

// NewNoopHedger 忽略配置，始终返回共享的 noop 实例。
func NewNoopHedger(Config) *NoopHedger {
	return &NoopHedger{}
}

// HTTPClient 返回 http.DefaultClient，不注入 hedging 传输层。
func (n *NoopHedger) HTTPClient(hedging.Config) (*http.Client, error) {
	return http.DefaultClient, nil
}

func (n *NoopHedger) withLogger(log.Logger) Hedger { return n }
// withLogger 满足 Hedger 装饰链接口，直接返回自身以便 Controller 统一挂载日志。
