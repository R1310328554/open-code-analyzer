package lokipush

// PushTargetManager：为含 PushConfig 的 scrape job 创建 HTTP push target，
// job_name 必须唯一且非空（用于 metrics namespace 与 pipeline 命名）。

import (
	"errors"
	"fmt"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/prometheus/util/strutil"

	"github.com/grafana/loki/v3/clients/pkg/logentry/stages"
	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// 按 jobName 索引 PushTarget，每个 job 独立 HTTP 监听端口。
// PushTargetManager manages a series of PushTargets.
type PushTargetManager struct {
	logger  log.Logger
	targets map[string]*PushTarget
}

// validateJobName 后构建 push_pipeline 并 NewPushTarget。
// NewPushTargetManager creates a new PushTargetManager.
func NewPushTargetManager(
	reg prometheus.Registerer,
	logger log.Logger,
	client api.EntryHandler,
	scrapeConfigs []scrapeconfig.Config,
) (*PushTargetManager, error) {

	tm := &PushTargetManager{
		logger:  logger,
		targets: make(map[string]*PushTarget),
	}

	if err := validateJobName(scrapeConfigs); err != nil {
		return nil, err
	}

	for _, cfg := range scrapeConfigs {
		pipeline, err := stages.NewPipeline(log.With(logger, "component", "push_pipeline_"+cfg.JobName), cfg.PipelineStages, &cfg.JobName, reg)
		if err != nil {
			return nil, err
		}

		t, err := NewPushTarget(logger, pipeline.Wrap(client), cfg.RelabelConfigs, cfg.JobName, cfg.PushConfig)
		if err != nil {
			return nil, err
		}

		tm.targets[cfg.JobName] = t
	}

	return tm, nil
}

// 校验 job_name 唯一非空，SanitizeLabelName 规范化 job 名用于指标注册。
func validateJobName(scrapeConfigs []scrapeconfig.Config) error {
	jobNames := map[string]struct{}{}
	for i, cfg := range scrapeConfigs {
		if cfg.JobName == "" {
			return errors.New("`job_name` must be defined for the `push` scrape_config with a " +
				"unique name to properly register metrics, " +
				"at least one `push` scrape_config has no `job_name` defined")
		}
		if _, ok := jobNames[cfg.JobName]; ok {
			return fmt.Errorf("`job_name` must be unique for each `push` scrape_config, "+
				"a duplicate `job_name` of %s was found", cfg.JobName)
		}
		jobNames[cfg.JobName] = struct{}{}

		scrapeConfigs[i].JobName = strutil.SanitizeLabelName(cfg.JobName)
	}
	return nil
}

// Ready returns true if at least one PushTarget is also ready.
func (tm *PushTargetManager) Ready() bool {
	for _, t := range tm.targets {
		if t.Ready() {
			return true
		}
	}
	return false
}

// Stop stops the PushTargetManager and all of its PushTargets.
func (tm *PushTargetManager) Stop() {
	for _, t := range tm.targets {
		if err := t.Stop(); err != nil {
			level.Error(t.logger).Log("msg", "error stopping PushTarget", "err", err.Error())
		}
	}
}

// Push target 无 deactivate，ActiveTargets 与 AllTargets 返回相同集合。
// ActiveTargets returns the list of PushTargets where Push data
// is being read. ActiveTargets is an alias to AllTargets as
// PushTargets cannot be deactivated, only stopped.
func (tm *PushTargetManager) ActiveTargets() map[string][]target.Target {
	return tm.AllTargets()
}

// AllTargets returns the list of all targets where Push data
// is currently being read.
func (tm *PushTargetManager) AllTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targets))
	for k, v := range tm.targets {
		result[k] = []target.Target{v}
	}
	return result
}
