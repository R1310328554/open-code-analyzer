package cloudflare

// Cloudflare TargetManager：为每个含 CloudflareConfig 的 job 构建 pipeline 与 Target，
// 按 job 名索引，提供 Ready/Stop/ActiveTargets/AllTargets 聚合接口。

import (
	"github.com/go-kit/log"

	"github.com/grafana/loki/v3/clients/pkg/logentry/stages"
	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/positions"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// 维护 jobName → Target 映射，生命周期与 Promtail target 接口对齐。
// TargetManager manages a series of cloudflare targets.
type TargetManager struct {
	logger  log.Logger
	targets map[string]*Target
}

// 遍历 scrapeConfigs，跳过无 Cloudflare 段的 job，pipeline.Wrap 包装 pushClient。
// NewTargetManager creates a new cloudflare target managers.
func NewTargetManager(
	metrics *Metrics,
	logger log.Logger,
	positions positions.Positions,
	pushClient api.EntryHandler,
	scrapeConfigs []scrapeconfig.Config,
) (*TargetManager, error) {
	tm := &TargetManager{
		logger:  logger,
		targets: make(map[string]*Target),
	}
	for _, cfg := range scrapeConfigs {
		if cfg.CloudflareConfig == nil {
			continue
		}
		pipeline, err := stages.NewPipeline(log.With(logger, "component", "cloudflare_pipeline"), cfg.PipelineStages, &cfg.JobName, metrics.reg)
		if err != nil {
			return nil, err
		}
		t, err := NewTarget(metrics, log.With(logger, "target", "cloudflare"), pipeline.Wrap(pushClient), positions, cfg.CloudflareConfig)
		if err != nil {
			return nil, err
		}
		tm.targets[cfg.JobName] = t
	}

	return tm, nil
}

// 任一 target.Ready() 为真即认为 manager 就绪。
// Ready returns true if at least one cloudflare target is active.
func (tm *TargetManager) Ready() bool {
	for _, t := range tm.targets {
		if t.Ready() {
			return true
		}
	}
	return false
}

func (tm *TargetManager) Stop() {
	for _, t := range tm.targets {
		t.Stop()
	}
}

// 仅返回 Ready 的 target，供 /targets API 展示活跃抓取源。
func (tm *TargetManager) ActiveTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targets))
	for k, v := range tm.targets {
		if v.Ready() {
			result[k] = []target.Target{v}
		}
	}
	return result
}

func (tm *TargetManager) AllTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targets))
	for k, v := range tm.targets {
		result[k] = []target.Target{v}
	}
	return result
}
