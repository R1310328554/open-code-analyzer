package bloombuild

// Bloom 构建顶层配置：聚合 planner 与 builder 子组件开关及参数，
// 供 Loki 主进程注册命令行标志并校验 bloom-build 功能是否可用。

import (
	"flag"
	"fmt"

	"github.com/grafana/loki/v3/pkg/bloombuild/builder"
	"github.com/grafana/loki/v3/pkg/bloombuild/planner"
)

// Config 为 bloom-planner 与 bloom-builder 提供统一 YAML/flag 入口。
// Config configures the bloom-planner component.
type Config struct {
	Enabled bool `yaml:"enabled"`

	Planner planner.Config `yaml:"planner"`
	Builder builder.Config `yaml:"builder"`
}

// RegisterFlags 注册 bloom-build.enabled 及 planner/builder 前缀子标志。
// RegisterFlags registers flags for the bloom building configuration.
func (cfg *Config) RegisterFlags(f *flag.FlagSet) {
	f.BoolVar(&cfg.Enabled, "bloom-build.enabled", false, "Flag to enable or disable the usage of the bloom-planner and bloom-builder components.")
	cfg.Planner.RegisterFlagsWithPrefix("bloom-build.planner", f)
	cfg.Builder.RegisterFlagsWithPrefix("bloom-build.builder", f)
}

// Validate 在 enabled 时依次校验 planner 与 builder 配置合法性。
func (cfg *Config) Validate() error {
	if !cfg.Enabled {
		return nil
	}

	if err := cfg.Planner.Validate(); err != nil {
		return fmt.Errorf("invalid bloom planner configuration: %w", err)
	}

	if err := cfg.Builder.Validate(); err != nil {
		return fmt.Errorf("invalid bloom builder configuration: %w", err)
	}

	return nil
}
