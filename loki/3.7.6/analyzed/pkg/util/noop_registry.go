package util //nolint:revive

// util 包 NoopRegistry 实现 prometheus.Registerer 空操作：测试或禁用指标导出时替换真实 Registry，避免重复注册 panic。

import "github.com/prometheus/client_golang/prometheus"

type NoopRegistry struct{}

var _ prometheus.Registerer = NoopRegistry{}

// MustRegister 丢弃所有 Collector，不触发 Describe/Collect 链路。
// MustRegister implements prometheus.Registerer.
func (n NoopRegistry) MustRegister(...prometheus.Collector) {}

// Register implements prometheus.Registerer.
func (n NoopRegistry) Register(prometheus.Collector) error {
	return nil
}

// Unregister implements prometheus.Registerer.
func (n NoopRegistry) Unregister(prometheus.Collector) bool {
	return true
}
// Unregister 恒返回 true，与真实 Registry 接口语义兼容便于 mock 替换。
