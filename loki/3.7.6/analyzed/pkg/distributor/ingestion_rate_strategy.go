package distributor

// 租户 ingestion 字节速率策略：本地策略直接使用限额，全局策略按 distributor 实例数均分。

import (
	"github.com/grafana/dskit/limiter"
)

// ReadLifecycler 提供 HealthyInstancesCount 供全局策略计算每实例配额。
// ReadLifecycler represents the read interface to the lifecycler.
type ReadLifecycler interface {
	HealthyInstancesCount() int
}

type localStrategy struct {
	limits Limits
}

func newLocalIngestionRateStrategy(limits Limits) limiter.RateLimiterStrategy {
	return &localStrategy{
		limits: limits,
	}
}

func (s *localStrategy) Limit(userID string) float64 {
	return s.limits.IngestionRateBytes(userID)
}

func (s *localStrategy) Burst(userID string) int {
	return s.limits.IngestionBurstSizeBytes(userID)
}

// globalStrategy 将租户字节速率除以 ring 中健康 distributor 数以实现集群级限额。
type globalStrategy struct {
	limits Limits
	ring   ReadLifecycler
}

func newGlobalIngestionRateStrategy(limits Limits, ring ReadLifecycler) limiter.RateLimiterStrategy {
	return &globalStrategy{
		limits: limits,
		ring:   ring,
	}
}

// Limit 在无健康实例时回退为完整租户限额，避免除零。
func (s *globalStrategy) Limit(userID string) float64 {
	numDistributors := s.ring.HealthyInstancesCount()

	if numDistributors == 0 {
		return s.limits.IngestionRateBytes(userID)
	}

	return s.limits.IngestionRateBytes(userID) / float64(numDistributors)
}

// Burst 在全局策略下仍返回完整租户 burst，便于运维理解突发行为。
func (s *globalStrategy) Burst(userID string) int {
	// The meaning of burst doesn't change for the global strategy, in order
	// to keep it easier to understand for users / operators.
	return s.limits.IngestionBurstSizeBytes(userID)
}
// newLocalIngestionRateStrategy 与 newGlobalIngestionRateStrategy 实现 dskit limiter 接口。
