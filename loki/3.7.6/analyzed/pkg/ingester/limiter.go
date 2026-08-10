package ingester

// limiter 计算租户 stream 数量与速率上限：结合 ring/partition 策略将全局配额折算为本地限额。

import (
	"fmt"
	"math"
	"sync"
	"time"

	"github.com/grafana/dskit/ring"
	"golang.org/x/time/rate"

	"github.com/grafana/loki/v3/pkg/compactor/retention"
	"github.com/grafana/loki/v3/pkg/distributor/shardstreams"
	"github.com/grafana/loki/v3/pkg/validation"
)

// 模块级常量定义。
const (
	errMaxStreamsPerUserLimitExceeded = "tenant '%v' per-user streams limit exceeded, streams: %d exceeds calculated limit: %d (local limit: %d, global limit: %d, local share: %d)"
)

// RingCount is the interface exposed by a ring implementation which allows
// to count members
// RingCount 类型封装该模块的状态与行为。
type RingCount interface {
	HealthyInstancesCount() int
	HealthyInstancesInZoneCount() int
	ZonesCount() int
}

// Limits 抽象租户配额：stream 上限、策略 override、分片与 retention 限制。
type Limits interface {
	UnorderedWrites(userID string) bool
	UseOwnedStreamCount(userID string) bool
	MaxLocalStreamsPerUser(userID string) int
	MaxGlobalStreamsPerUser(userID string) int
	PolicyMaxLocalStreamsPerUser(userID, policy string) int
	PolicyMaxGlobalStreamsPerUser(userID, policy string) (int, bool)
	PerStreamRateLimit(userID string) validation.RateLimit
	ShardStreams(userID string) shardstreams.Config
	IngestionPartitionsTenantShardSize(userID string) int

	retention.Limits
}

// Limiter 根据 ring/partition 策略计算租户可持有 stream 数与速率限制。
// Limiter implements primitives to get the maximum number of streams
// an ingester can handle for a specific tenant
type Limiter struct {
	limits            Limits
	ringStrategy      limiterRingStrategy
	metrics           *ingesterMetrics
	rateLimitStrategy RateLimiterStrategy

	mtx      sync.RWMutex
	disabled bool
}

// DisableForWALReplay 实现该路径上的核心处理逻辑。
func (l *Limiter) DisableForWALReplay() {
	l.mtx.Lock()
	defer l.mtx.Unlock()
	l.disabled = true
	l.metrics.limiterEnabled.Set(0)
	l.rateLimitStrategy.SetDisabled(true)
}

// Enable 实现该路径上的核心处理逻辑。
func (l *Limiter) Enable() {
	l.mtx.Lock()
	defer l.mtx.Unlock()
	l.disabled = false
	l.metrics.limiterEnabled.Set(1)
	l.rateLimitStrategy.SetDisabled(false)
}

// limiterRingStrategy 类型封装该模块的状态与行为。
type limiterRingStrategy interface {
	convertGlobalToLocalLimit(int, string) int
}

// NewLimiter makes a new limiter
// NewLimiter 创建组件实例并完成必要初始化。
func NewLimiter(limits Limits, metrics *ingesterMetrics, ingesterRingLimiterStrategy limiterRingStrategy, rateLimitStrategy RateLimiterStrategy) *Limiter {
	return &Limiter{
		limits:            limits,
		ringStrategy:      ingesterRingLimiterStrategy,
		metrics:           metrics,
		rateLimitStrategy: rateLimitStrategy,
	}
}

// UnorderedWrites 实现该路径上的核心处理逻辑。
func (l *Limiter) UnorderedWrites(userID string) bool {
	// WAL replay should not discard previously ack'd writes,
	// so allow out of order writes while the limiter is disabled.
	// This allows replaying unordered WALs into ordered configurations.
	if l.disabled {
		return true
	}
	return l.limits.UnorderedWrites(userID)
}

// 计算租户 stream 数量的本地有效上限。
func (l *Limiter) GetStreamCountLimit(tenantID string, policy string) (calculatedLimit, localLimit, globalLimit, adjustedGlobalLimit int) {
	// Start by setting the local limit either from override or default
	localLimit = l.limits.MaxLocalStreamsPerUser(tenantID)

	// We can assume that streams are evenly distributed across ingesters
	// so we do convert the global limit into a local limit
	globalLimit = l.limits.MaxGlobalStreamsPerUser(tenantID)

	// Check for policy-specific overrides if policy is specified
	// NOTE: Whereas for the regular stream limits 0 means no limit, for policy limit 0 means no per-policy limit override is specified
	if policy != noPolicy {
		policyLocalLimit := l.limits.PolicyMaxLocalStreamsPerUser(tenantID, policy)
		if policyLocalLimit > 0 {
			localLimit = policyLocalLimit
		}
		if policyGlobalLimit, exists := l.limits.PolicyMaxGlobalStreamsPerUser(tenantID, policy); exists {
			globalLimit = policyGlobalLimit
		}
	}

	adjustedGlobalLimit = l.ringStrategy.convertGlobalToLocalLimit(globalLimit, tenantID)

	// Set the calculated limit to the lesser of the local limit or the new calculated global limit
	calculatedLimit = l.minNonZero(localLimit, adjustedGlobalLimit)

	// If both the local and global limits are disabled, we just
	// use the largest int value
	if calculatedLimit == 0 {
		calculatedLimit = math.MaxInt32
	}
	return
}

// minNonZero 实现该路径上的核心处理逻辑。
func (l *Limiter) minNonZero(first, second int) int {
	if first == 0 || (second != 0 && first > second) {
		return second
	}

	return first
}

// ingesterRingLimiterStrategy 类型封装该模块的状态与行为。
type ingesterRingLimiterStrategy struct {
	ring              RingCount
	replicationFactor int
}

// newIngesterRingLimiterStrategy 实现该路径上的核心处理逻辑。
func newIngesterRingLimiterStrategy(ring RingCount, replicationFactor int) *ingesterRingLimiterStrategy {
	return &ingesterRingLimiterStrategy{
		ring:              ring,
		replicationFactor: replicationFactor,
	}
}

// convertGlobalToLocalLimit 实现该路径上的核心处理逻辑。
func (l *ingesterRingLimiterStrategy) convertGlobalToLocalLimit(globalLimit int, _ string) int {
	if globalLimit == 0 || l.replicationFactor == 0 {
		return 0
	}

	zonesCount := l.ring.ZonesCount()
	if zonesCount <= 1 {
		return l.calculateLimitForSingleZone(globalLimit)
	}

	return l.calculateLimitForMultipleZones(globalLimit, zonesCount)
}

// calculateLimitForSingleZone 实现该路径上的核心处理逻辑。
func (l *ingesterRingLimiterStrategy) calculateLimitForSingleZone(globalLimit int) int {
	numIngesters := l.ring.HealthyInstancesCount()
	if numIngesters > 0 {
		return int((float64(globalLimit) / float64(numIngesters)) * float64(l.replicationFactor))
	}
	return 0
}

// calculateLimitForMultipleZones 实现该路径上的核心处理逻辑。
func (l *ingesterRingLimiterStrategy) calculateLimitForMultipleZones(globalLimit, zonesCount int) int {
	ingestersInZone := l.ring.HealthyInstancesInZoneCount()
	if ingestersInZone > 0 {
		return int((float64(globalLimit) * float64(l.replicationFactor)) / float64(zonesCount) / float64(ingestersInZone))
	}
	return 0
}

// partitionRingLimiterStrategy 类型封装该模块的状态与行为。
type partitionRingLimiterStrategy struct {
	ring                  ring.PartitionRingReader
	getPartitionShardSize func(user string) int
}

// newPartitionRingLimiterStrategy 实现该路径上的核心处理逻辑。
func newPartitionRingLimiterStrategy(ring ring.PartitionRingReader, getPartitionShardSize func(user string) int) *partitionRingLimiterStrategy {
	return &partitionRingLimiterStrategy{
		ring:                  ring,
		getPartitionShardSize: getPartitionShardSize,
	}
}

// convertGlobalToLocalLimit 实现该路径上的核心处理逻辑。
func (l *partitionRingLimiterStrategy) convertGlobalToLocalLimit(globalLimit int, tenantID string) int {
	if globalLimit == 0 {
		return 0
	}

	userShardSize := l.getPartitionShardSize(tenantID)

	// ShuffleShardSize correctly handles cases when user has no shard config or more shards than number of active partitions in the ring.
	activePartitionsForUser := l.ring.PartitionRing().ShuffleShardSize(userShardSize)

	if activePartitionsForUser == 0 {
		return 0
	}
	return int(float64(globalLimit) / float64(activePartitionsForUser))
}

// supplier[T 类型封装该模块的状态与行为。
type supplier[T any] func() T

type streamCountLimiter struct {
	tenantID                   string
	limiter                    *Limiter
	defaultStreamCountSupplier supplier[int]
	ownedStreamSvc             *ownedStreamService
	delegateStreamLimits       bool
}

var noopFixedLimitSupplier = func() int {
	return 0
}

// newStreamCountLimiter 实现该路径上的核心处理逻辑。
func newStreamCountLimiter(tenantID string, defaultStreamCountSupplier supplier[int], limiter *Limiter, service *ownedStreamService, delegateStreamLimits bool) *streamCountLimiter {
	return &streamCountLimiter{
		tenantID:                   tenantID,
		limiter:                    limiter,
		defaultStreamCountSupplier: defaultStreamCountSupplier,
		ownedStreamSvc:             service,
		delegateStreamLimits:       delegateStreamLimits,
	}
}

// 创建新 stream 前校验是否超出配额。
func (l *streamCountLimiter) AssertNewStreamAllowed(tenantID string, policy string) error {
	if l.delegateStreamLimits {
		return nil
	}
	streamCountSupplier, fixedLimitSupplier := l.getSuppliers(tenantID, policy)
	calculatedLimit, localLimit, globalLimit, adjustedGlobalLimit := l.getCurrentLimit(tenantID, policy, fixedLimitSupplier)
	actualStreamsCount := streamCountSupplier()
	if actualStreamsCount < calculatedLimit {
		return nil
	}

	return fmt.Errorf(errMaxStreamsPerUserLimitExceeded, tenantID, actualStreamsCount, calculatedLimit, localLimit, globalLimit, adjustedGlobalLimit)
}

// getCurrentLimit 读取内部状态或构建查询结果。
func (l *streamCountLimiter) getCurrentLimit(tenantID, policy string, fixedLimitSupplier supplier[int]) (calculatedLimit, localLimit, globalLimit, adjustedGlobalLimit int) {
	calculatedLimit, localLimit, globalLimit, adjustedGlobalLimit = l.limiter.GetStreamCountLimit(tenantID, policy)

	// Only apply fixed limit if no policy is specified
	// Policy limits should take precedence over fixed limits
	if policy == noPolicy {
		fixedLimit := fixedLimitSupplier()
		if fixedLimit > calculatedLimit {
			calculatedLimit = fixedLimit
		}
	}

	return
}

// getSuppliers 读取内部状态或构建查询结果。
func (l *streamCountLimiter) getSuppliers(tenant string, policy string) (streamCountSupplier, fixedLimitSupplier supplier[int]) {
	if l.limiter.limits.UseOwnedStreamCount(tenant) {
		streamCountSupplier := func() int {
			return l.ownedStreamSvc.getOwnedStreamCount()
		}
		if policy != noPolicy {
			streamCountSupplier = func() int {
				return l.ownedStreamSvc.getPolicyStreamCount(policy)
			}
		}
		return streamCountSupplier, l.ownedStreamSvc.getFixedLimit
	}
	return l.defaultStreamCountSupplier, noopFixedLimitSupplier
}

// RateLimiterStrategy 类型封装该模块的状态与行为。
type RateLimiterStrategy interface {
	RateLimit(tenant string) validation.RateLimit
	SetDisabled(bool)
}

// TenantBasedStrategy 类型封装该模块的状态与行为。
type TenantBasedStrategy struct {
	disabled bool
	limits   Limits
}

// RateLimit 实现该路径上的核心处理逻辑。
func (l *TenantBasedStrategy) RateLimit(tenant string) validation.RateLimit {
	if l.disabled {
		return validation.Unlimited
	}

	return l.limits.PerStreamRateLimit(tenant)
}

// SetDisabled 更新运行时配置或内部字段。
func (l *TenantBasedStrategy) SetDisabled(disabled bool) {
	l.disabled = disabled
}

// NoLimitsStrategy 类型封装该模块的状态与行为。
type NoLimitsStrategy struct{}

func (l *NoLimitsStrategy) RateLimit(_ string) validation.RateLimit {
	return validation.Unlimited
}

// SetDisabled 更新运行时配置或内部字段。
func (l *NoLimitsStrategy) SetDisabled(_ bool) {
	// no-op
}

// StreamRateLimiter 类型封装该模块的状态与行为。
type StreamRateLimiter struct {
	recheckPeriod time.Duration
	recheckAt     time.Time
	strategy      RateLimiterStrategy
	tenant        string
	lim           *rate.Limiter
}

// NewStreamRateLimiter 创建组件实例并完成必要初始化。
func NewStreamRateLimiter(strategy RateLimiterStrategy, tenant string, recheckPeriod time.Duration) *StreamRateLimiter {
	rl := strategy.RateLimit(tenant)
	return &StreamRateLimiter{
		recheckPeriod: recheckPeriod,
		strategy:      strategy,
		tenant:        tenant,
		lim:           rate.NewLimiter(rl.Limit, rl.Burst),
	}
}

// AllowN 实现该路径上的核心处理逻辑。
func (l *StreamRateLimiter) AllowN(at time.Time, n int) bool {
	now := time.Now()
	if now.After(l.recheckAt) {
		l.recheckAt = now.Add(l.recheckPeriod)

		oldLim := l.lim.Limit()
		oldBurst := l.lim.Burst()

		next := l.strategy.RateLimit(l.tenant)

		if oldLim != next.Limit || oldBurst != next.Burst {
			// Edge case: rate.Inf doesn't advance nicely when reconfigured.
			// To simplify, we just create a new limiter after reconfiguration rather
			// than alter the existing one.
			l.lim = rate.NewLimiter(next.Limit, next.Burst)
		}
	}

	return l.lim.AllowN(at, n)
}
// WAL 重放期间可禁用 limiter，避免丢弃已 ack 的无序写入。
