package kafka

// Kafka TargetManager：为每个含 KafkaConfig 的 scrape job 创建 TargetSyncer，
// 按 jobName 索引；Ready 取决于是否存在 active partition target。

import (
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/prometheus/client_golang/prometheus"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"
	"github.com/grafana/loki/v3/clients/pkg/promtail/scrapeconfig"
	"github.com/grafana/loki/v3/clients/pkg/promtail/targets/target"
)

// map[jobName]*TargetSyncer，每个 job 独立 consumer group 与 pipeline。
// TargetManager manages a series of kafka targets.
type TargetManager struct {
	logger        log.Logger
	targetSyncers map[string]*TargetSyncer
}

// 遍历 scrapeConfigs 调用 NewSyncerFromScrapeConfig 构建各 job 的 syncer。
// NewTargetManager creates a new Kafka managers.
func NewTargetManager(
	reg prometheus.Registerer,
	logger log.Logger,
	pushClient api.EntryHandler,
	scrapeConfigs []scrapeconfig.Config,
) (*TargetManager, error) {
	tm := &TargetManager{
		logger:        logger,
		targetSyncers: make(map[string]*TargetSyncer),
	}
	for _, cfg := range scrapeConfigs {
		t, err := NewSyncerFromScrapeConfig(reg, logger, cfg, pushClient)
		if err != nil {
			return nil, err
		}
		tm.targetSyncers[cfg.JobName] = t
	}

	return tm, nil
}

// 任一 TargetSyncer 的 ActiveTargets 非空即视为就绪。
// Ready returns true if at least one Kafka target is active.
func (tm *TargetManager) Ready() bool {
	for _, t := range tm.targetSyncers {
		if len(t.ActiveTargets()) > 0 {
			return true
		}
	}
	return false
}

func (tm *TargetManager) Stop() {
	for _, t := range tm.targetSyncers {
		if err := t.Stop(); err != nil {
			level.Error(tm.logger).Log("msg", "error stopping kafka target", "err", err)
		}
	}
}

func (tm *TargetManager) ActiveTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targetSyncers))
	for k, v := range tm.targetSyncers {
		result[k] = v.ActiveTargets()
	}
	return result
}

// 合并 ActiveTargets 与 DroppedTargets 供 service discovery 展示。
func (tm *TargetManager) AllTargets() map[string][]target.Target {
	result := make(map[string][]target.Target, len(tm.targetSyncers))
	for k, v := range tm.targetSyncers {
		result[k] = append(v.ActiveTargets(), v.DroppedTargets()...)
	}
	return result
}
