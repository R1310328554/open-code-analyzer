package syslog

// Syslog target 管理器：为每个 scrape job 创建 SyslogTarget 与 pipeline，
// 统一 Ready/Stop/ActiveTargets 生命周期接口。

import (
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/clients/pkg/logentry/stages"
	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// 按 JobName 索引各 SyslogTarget，不可单独停用只能整体 Stop。
// SyslogTargetManager manages a series of SyslogTargets.
// nolint:revive
type SyslogTargetManager struct {
	logger  log.Logger
	targets map[string]*SyslogTarget
}

// 遍历 scrapeConfigs 构建 stages.Pipeline 并 Wrap client 后实例化 target。
// NewSyslogTargetManager creates a new SyslogTargetManager.
func NewSyslogTargetManager(
	metrics *Metrics,
	logger log.Logger,
	client api.EntryHandler,
	scrapeConfigs []scrapeconfig.Config,
) (*SyslogTargetManager, error) {
	reg := metrics.reg
	if reg == nil {
		reg = prometheus.DefaultRegisterer
	}

	tm := &SyslogTargetManager{
		logger:  logger,
		targets: make(map[string]*SyslogTarget),
	}

	for _, cfg := range scrapeConfigs {
		pipeline, err := stages.NewPipeline(log.With(logger, "component", "syslog_pipeline"), cfg.PipelineStages, &cfg.JobName, reg)
		if err != nil {
			return nil, err
		}

		t, err := NewSyslogTarget(metrics, logger, pipeline.Wrap(client), cfg.RelabelConfigs, cfg.SyslogConfig)
		if err != nil {
			return nil, err
		}

		tm.targets[cfg.JobName] = t
	}

	return tm, nil
}

// Ready returns true if at least one SyslogTarget is also ready.
func (tm *SyslogTargetManager) Ready() bool {
	for _, t := range tm.targets {
		if t.Ready() {
			return true
		}
	}
	return false
}

// Stop stops the SyslogTargetManager and all of its SyslogTargets.
func (tm *SyslogTargetManager) Stop() {
	for _, t := range tm.targets {
		if err := t.Stop(); err != nil {
			level.Error(t.logger).Log("msg", "error stopping SyslogTarget", "err", err.Error())
		}
	}
}

// ActiveTargets 与 AllTargets 等价，syslog target 无 inactive 状态。
// ActiveTargets returns the list of SyslogTargets where syslog data
// is being read. ActiveTargets is an alias to AllTargets as
// SyslogTargets cannot be deactivated, only stopped.
func (tm *SyslogTargetManager) ActiveTargets() map[string][]target.Target {
	return tm.AllTargets()
}

// AllTargets returns the list of all targets where syslog data
// is currently being read.
func (tm *SyslogTargetManager) AllTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targets))
	for k, v := range tm.targets {
		result[k] = []target.Target{v}
	}
	return result
}
