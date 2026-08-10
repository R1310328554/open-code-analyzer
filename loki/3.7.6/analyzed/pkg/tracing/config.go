package tracing

// tracing 包提供 Loki 全局分布式追踪开关配置，通过 tracing.enabled flag 控制是否初始化 OpenTelemetry 导出链路。

import (
	"flag"
)

type Config struct {
	Enabled bool `yaml:"enabled"`
}

func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	f.BoolVar(&cfg.Enabled, "tracing.enabled", true, "Set to false to disable tracing.")
}

// RegisterFlagsWithPrefix 为嵌套模块（如 ui.）注册带前缀的 tracing.enabled 开关。
func (cfg *Config) RegisterFlagsWithPrefix(prefix string, f *flag.FlagSet) {
	f.BoolVar(&cfg.Enabled, prefix+"tracing.enabled", true, "Set to false to disable tracing.")
}
// 关闭 tracing 时下游 span 创建与 OTLP 导出均被跳过，降低开发与测试环境开销。
