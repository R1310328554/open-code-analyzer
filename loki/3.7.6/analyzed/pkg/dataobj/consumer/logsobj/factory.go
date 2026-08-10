package logsobj

// factory 模块提供 BuilderFactory：
// 按统一配置与 scratch 存储创建 Builder，可选注册 Prometheus 指标。

import (
	"fmt"

	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/pkg/scratch"
)

// BuilderFactory 持有 BuilderConfig 与 scratch.Store，用于批量创建 Builder。
// A BuilderFactory is used to create builders.
type BuilderFactory struct {
	cfg          BuilderConfig
	scratchStore scratch.Store
}

func NewBuilderFactory(cfg BuilderConfig, scratchStore scratch.Store) *BuilderFactory {
	return &BuilderFactory{
		cfg:          cfg,
		scratchStore: scratchStore,
	}
}

// NewBuilder 创建新 Builder，registerer 非 nil 时自动注册指标。
// NewBuilder returns a new builder, or an error. The registerer is optional.
// No metrics will be registered if the registerer is nil.
func (f *BuilderFactory) NewBuilder(r prometheus.Registerer) (*Builder, error) {
	b, err := NewBuilder(f.cfg, f.scratchStore)
	if err != nil {
		return nil, err
	}
	if r != nil {
		if err = b.RegisterMetrics(r); err != nil {
			return nil, fmt.Errorf("failed to register metrics: %w", err)
		}
	}
	return b, nil
}
