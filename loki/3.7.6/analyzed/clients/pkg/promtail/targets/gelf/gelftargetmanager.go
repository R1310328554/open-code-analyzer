package gelf

// GELF TargetManager：为每个含 GelfConfig 的 scrape job 创建 UDP target，
// pipeline 包装 client；GELF target 仅可 Stop 不可 deactivate。

import (
	"github.com/go-kit/log"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/clients/pkg/logentry/stages"
	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// 按 jobName 维护 gelf Target 映射，reg 缺省时用 DefaultRegisterer。
// TargetManager manages a series of Gelf Targets.
type TargetManager struct {
	logger  log.Logger
	targets map[string]*Target
}

// 遍历 scrapeConfigs 构建 gelf_pipeline 并 NewTarget。
// NewTargetManager creates a new Gelf TargetManager.
func NewTargetManager(
	metrics *Metrics,
	logger log.Logger,
	client api.EntryHandler,
	scrapeConfigs []scrapeconfig.Config,
) (*TargetManager, error) {
	reg := metrics.reg
	if reg == nil {
		reg = prometheus.DefaultRegisterer
	}

	tm := &TargetManager{
		logger:  logger,
		targets: make(map[string]*Target),
	}

	for _, cfg := range scrapeConfigs {
		pipeline, err := stages.NewPipeline(log.With(logger, "component", "gelf_pipeline"), cfg.PipelineStages, &cfg.JobName, reg)
		if err != nil {
			return nil, err
		}

		t, err := NewTarget(metrics, logger, pipeline.Wrap(client), cfg.RelabelConfigs, cfg.GelfConfig)
		if err != nil {
			return nil, err
		}

		tm.targets[cfg.JobName] = t
	}

	return tm, nil
}

// Ready returns true if at least one GelfTarget is also ready.
func (tm *TargetManager) Ready() bool {
	for _, t := range tm.targets {
		if t.Ready() {
			return true
		}
	}
	return false
}

// Stop stops the GelfTargetManager and all of its GelfTargets.
func (tm *TargetManager) Stop() {
	for _, t := range tm.targets {
		t.Stop()
	}
}

// GELF 无动态启停，ActiveTargets 与 AllTargets 返回相同集合。
// ActiveTargets returns the list of GelfTargets where Gelf data
// is being read. ActiveTargets is an alias to AllTargets as
// GelfTargets cannot be deactivated, only stopped.
func (tm *TargetManager) ActiveTargets() map[string][]target.Target {
	return tm.AllTargets()
}

// AllTargets returns the list of all targets where gelf data
// is currently being read.
func (tm *TargetManager) AllTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targets))
	for k, v := range tm.targets {
		result[k] = []target.Target{v}
	}
	return result
}
