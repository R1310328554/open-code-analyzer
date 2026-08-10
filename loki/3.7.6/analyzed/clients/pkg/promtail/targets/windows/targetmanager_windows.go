//go:build windows
// +build windows

package windows

// Windows 平台 target 管理器：为每个 scrape job 构建 pipeline 与
// Windows Target，提供 Ready/Stop/ActiveTargets 聚合视图。

import (
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/clients/pkg/logentry/stages"
	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// 按 JobName 映射 *Target，ActiveTargets 仅返回 Ready 的实例。
// TargetManager manages a series of windows event targets.
type TargetManager struct {
	logger  log.Logger
	targets map[string]*Target
}

// 遍历 scrapeConfigs，stages.NewPipeline Wrap client 后调用 New 创建 target。
// NewTargetManager creates a new Windows managers.
func NewTargetManager(
	reg prometheus.Registerer,
	logger log.Logger,
	client api.EntryHandler,
	scrapeConfigs []scrapeconfig.Config,
) (*TargetManager, error) {
	tm := &TargetManager{
		logger:  logger,
		targets: make(map[string]*Target),
	}

	for _, cfg := range scrapeConfigs {
		pipeline, err := stages.NewPipeline(log.With(logger, "component", "windows_pipeline"), cfg.PipelineStages, &cfg.JobName, reg)
		if err != nil {
			return nil, err
		}

		t, err := New(logger, pipeline.Wrap(client), cfg.RelabelConfigs, cfg.WindowsConfig)
		if err != nil {
			return nil, err
		}

		tm.targets[cfg.JobName] = t
	}

	return tm, nil
}

// Ready returns true if at least one Windows target is also ready.
func (tm *TargetManager) Ready() bool {
	for _, t := range tm.targets {
		if t.Ready() {
			return true
		}
	}
	return false
}

// Stop stops the Windows target manager and all of its targets.
func (tm *TargetManager) Stop() {
	for _, t := range tm.targets {
		if err := t.Stop(); err != nil {
			level.Error(t.logger).Log("msg", "error stopping windows target", "err", err.Error())
		}
	}
}

// 仅当 target.Ready() 为 true 时才纳入 ActiveTargets 结果。
// ActiveTargets returns the list of active Windows targets.
func (tm *TargetManager) ActiveTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targets))
	for k, v := range tm.targets {
		if v.Ready() {
			result[k] = []target.Target{v}
		}
	}
	return result
}

// AllTargets returns the list of all targets.
func (tm *TargetManager) AllTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targets))
	for k, v := range tm.targets {
		result[k] = []target.Target{v}
	}
	return result
}
