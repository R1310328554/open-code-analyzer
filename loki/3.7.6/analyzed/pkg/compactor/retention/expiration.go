package retention

// 保留策略过期判定：按租户全局与流级规则计算 chunk/series 是否过期，
// 支持索引条目提前丢弃及保留阶段生命周期回调。

import (
	"fmt"
	"time"

	"github.com/go-kit/log/level"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/labels"

	"github.com/grafana/loki/v3/pkg/util"
	"github.com/grafana/loki/v3/pkg/util/filter"
	util_log "github.com/grafana/loki/v3/pkg/util/log"
	"github.com/grafana/loki/v3/pkg/validation"
)

// IntervalFilter 封装待删除时间区间及行级 filter.Func 过滤函数。
// IntervalFilter contains the interval to delete
// and the function that filters lines. These will be
// applied to a chunk.
type IntervalFilter struct {
	Interval model.Interval
	Filter   filter.Func
}

// ExpirationChecker 定义保留压缩各阶段对 chunk/series 的过期判定接口。
type ExpirationChecker interface {
	Expired(userID []byte, chk Chunk, lbls labels.Labels, seriesID []byte, tableName string, now model.Time) (bool, filter.Func)
	IntervalMayHaveExpiredChunks(interval model.Interval, userID string) bool
	MarkPhaseStarted()
	MarkPhaseFailed()
	MarkPhaseTimedOut()
	MarkPhaseFinished()
	DropFromIndex(userID []byte, chk Chunk, labels labels.Labels, tableEndTime model.Time, now model.Time) bool
	CanSkipSeries(userID []byte, lbls labels.Labels, seriesID []byte, seriesStart model.Time, tableName string, now model.Time) bool
	MarkSeriesAsProcessed(userID, seriesID []byte, lbls labels.Labels, tableName string) error
}

// expirationChecker 基于 TenantsRetention 与 latestRetentionStartTime 判定过期。
type expirationChecker struct {
	tenantsRetention         *TenantsRetention
	latestRetentionStartTime latestRetentionStartTime
}

type Limits interface {
	RetentionPeriod(userID string) time.Duration
	StreamRetention(userID string) []validation.StreamRetention
	AllByUserID() map[string]*validation.Limits
	DefaultLimits() *validation.Limits
	PoliciesStreamMapping(userID string) validation.PolicyStreamMapping
}

func NewExpirationChecker(limits Limits) ExpirationChecker {
	return &expirationChecker{
		tenantsRetention: NewTenantsRetention(limits),
	}
}

// Expired 比较 chunk 结束时间与租户保留期，返回是否过期及行级过滤器。
// Expired tells if a ref chunk is expired based on retention rules.
func (e *expirationChecker) Expired(userID []byte, chk Chunk, lbls labels.Labels, _ []byte, _ string, now model.Time) (bool, filter.Func) {
	userIDStr := unsafeGetString(userID)
	period := e.tenantsRetention.RetentionPeriodFor(userIDStr, lbls)
	// The 0 value should disable retention
	if period <= 0 {
		return false, nil
	}
	return now.Sub(chk.Through) > period, nil
}

// DropFromIndex 判断表结束时间超出保留期时能否仅删索引不删 chunk。
// DropFromIndex tells if it is okay to drop the chunk entry from index table.
// We check if tableEndTime is out of retention period, calculated using the labels from the chunk.
// If the tableEndTime is out of retention then we can drop the chunk entry without removing the chunk from the store.
func (e *expirationChecker) DropFromIndex(userID []byte, _ Chunk, labels labels.Labels, tableEndTime model.Time, now model.Time) bool {
	userIDStr := unsafeGetString(userID)
	period := e.tenantsRetention.RetentionPeriodFor(userIDStr, labels)
	// The 0 value should disable retention
	if period <= 0 {
		return false
	}
	return now.Sub(tableEndTime) > period
}

func (e *expirationChecker) MarkPhaseStarted() {
	e.latestRetentionStartTime = findLatestRetentionStartTime(model.Now(), e.tenantsRetention.limits)
	level.Info(util_log.Logger).Log("msg", fmt.Sprintf("overall smallest retention period %v, default smallest retention period %v",
		e.latestRetentionStartTime.overall, e.latestRetentionStartTime.defaults))
}

func (e *expirationChecker) MarkPhaseFailed()   {}
func (e *expirationChecker) MarkPhaseTimedOut() {}
func (e *expirationChecker) MarkPhaseFinished() {}
func (e *expirationChecker) CanSkipSeries(userID []byte, lbls labels.Labels, _ []byte, seriesStart model.Time, _ string, now model.Time) bool {
	userIDStr := unsafeGetString(userID)
	period := e.tenantsRetention.RetentionPeriodFor(userIDStr, lbls)
	// The 0 value should disable retention
	if period <= 0 {
		return true
	}

	return now.Sub(seriesStart) < period
}

func (e *expirationChecker) MarkSeriesAsProcessed(_, _ []byte, _ labels.Labels, _ string) error {
	return nil
}

func (e *expirationChecker) IntervalMayHaveExpiredChunks(interval model.Interval, userID string) bool {
	// when userID is empty, it means we are checking for common index table. In this case we use e.overallLatestRetentionStartTime.
	latestRetentionStartTime := e.latestRetentionStartTime.overall
	if userID != "" {
		// when userID is not empty, it means we are checking for user index table.
		latestRetentionStartTimeForUser, ok := e.latestRetentionStartTime.byUser[userID]
		if ok {
			// user has custom retention config, let us use user specific latest retention start time.
			latestRetentionStartTime = latestRetentionStartTimeForUser
		} else {
			// user does not have custom retention config, let us use default latest retention start time.
			latestRetentionStartTime = e.latestRetentionStartTime.defaults
		}
	}
	return interval.Start.Before(latestRetentionStartTime)
}

// NeverExpiringExpirationChecker 返回永不过期的空实现，用于禁用保留。
// NeverExpiringExpirationChecker returns an expiration checker that never expires anything
func NeverExpiringExpirationChecker(_ Limits) ExpirationChecker {
	return &neverExpiringExpirationChecker{}
}

type neverExpiringExpirationChecker struct{}

func (e *neverExpiringExpirationChecker) Expired(_ []byte, _ Chunk, _ labels.Labels, _ []byte, _ string, _ model.Time) (bool, filter.Func) {
	return false, nil
}
func (e *neverExpiringExpirationChecker) IntervalMayHaveExpiredChunks(_ model.Interval, _ string) bool {
	return false
}
func (e *neverExpiringExpirationChecker) MarkPhaseStarted()  {}
func (e *neverExpiringExpirationChecker) MarkPhaseFailed()   {}
func (e *neverExpiringExpirationChecker) MarkPhaseTimedOut() {}
func (e *neverExpiringExpirationChecker) MarkPhaseFinished() {}
func (e *neverExpiringExpirationChecker) DropFromIndex(_ []byte, _ Chunk, _ labels.Labels, _ model.Time, _ model.Time) bool {
	return false
}
func (e *neverExpiringExpirationChecker) CanSkipSeries(_ []byte, _ labels.Labels, _ []byte, _ model.Time, _ string, _ model.Time) bool {
	return true
}
func (e *neverExpiringExpirationChecker) MarkSeriesAsProcessed(_, _ []byte, _ labels.Labels, _ string) error {
	return nil
}

type TenantsRetention struct {
	limits Limits
}

func NewTenantsRetention(l Limits) *TenantsRetention {
	return &TenantsRetention{
		limits: l,
	}
}

func (tr *TenantsRetention) RetentionHoursFor(userID string, lbs labels.Labels) string {
	return NewTenantRetentionSnapshot(tr.limits, userID).RetentionHoursFor(lbs)
}

func (tr *TenantsRetention) RetentionPeriodFor(userID string, lbs labels.Labels) time.Duration {
	return NewTenantRetentionSnapshot(tr.limits, userID).RetentionPeriodFor(lbs)
}

// TenantRetentionSnapshot 为租户保留规则不可变快照，避免压缩过程中配置变更。
// TenantRetentionSnapshot is a snapshot of retention rules for a tenant.
// The underlying retention rules may change on the original limits object passed to
// NewTenantRetentionSnapshot, but the snapshot is immutable.
type TenantRetentionSnapshot struct {
	streamRetentions []validation.StreamRetention
	globalRetention  time.Duration
}

func NewTenantRetentionSnapshot(limits Limits, userID string) *TenantRetentionSnapshot {
	return &TenantRetentionSnapshot{
		streamRetentions: limits.StreamRetention(userID),
		globalRetention:  limits.RetentionPeriod(userID),
	}
}

func (r *TenantRetentionSnapshot) RetentionHoursFor(lbs labels.Labels) string {
	period := r.RetentionPeriodFor(lbs)
	return util.RetentionHours(period)
}

func (r *TenantRetentionSnapshot) RetentionPeriodFor(lbs labels.Labels) time.Duration {
	var (
		matchedRule validation.StreamRetention
		found       bool
	)
Outer:
	for _, streamRetention := range r.streamRetentions {
		for _, m := range streamRetention.Matchers {
			if !m.Matches(lbs.Get(m.Name)) {
				continue Outer
			}
		}
		// the rule is matched.
		if found {
			// if the current matched rule has a higher priority we keep it.
			if matchedRule.Priority > streamRetention.Priority {
				continue
			}
			// if priority is equal we keep the lowest retention.
			if matchedRule.Priority == streamRetention.Priority && matchedRule.Period <= streamRetention.Period {
				continue
			}
		}
		found = true
		matchedRule = streamRetention
	}

	if found {
		return time.Duration(matchedRule.Period)
	}

	return r.globalRetention
}

type latestRetentionStartTime struct {
	// defaults holds latest retention start time considering only default retention config.
	// It is used to determine if user index table may have any expired chunks when the user does not have any custom retention config set.
	defaults model.Time
	// overall holds latest retention start time for all users considering both default and per user retention config.
	// It is used to determine if common index table may have any expired chunks.
	overall model.Time
	// byUser holds latest retention start time considering only per user retention config.
	// It is used to determine if user index table may have any expired chunks.
	byUser map[string]model.Time
}

// findLatestRetentionStartTime 计算全局/默认/各租户最早保留起点以加速区间跳过。
// findLatestRetentionStartTime returns the latest retention start time overall, just default config and by each user.
func findLatestRetentionStartTime(now model.Time, limits Limits) latestRetentionStartTime {
	// find the smallest retention period from default limits
	defaultLimits := limits.DefaultLimits()
	smallestDefaultRetentionPeriod := defaultLimits.RetentionPeriod
	for _, streamRetention := range defaultLimits.StreamRetention {
		if streamRetention.Period < smallestDefaultRetentionPeriod {
			smallestDefaultRetentionPeriod = streamRetention.Period
		}
	}

	overallSmallestRetentionPeriod := smallestDefaultRetentionPeriod

	// find the smallest retention period by user
	limitsByUserID := limits.AllByUserID()
	smallestRetentionPeriodByUser := make(map[string]model.Time, len(limitsByUserID))
	for userID, limit := range limitsByUserID {
		smallestRetentionPeriodForUser := limit.RetentionPeriod
		for _, streamRetention := range limit.StreamRetention {
			if streamRetention.Period < smallestRetentionPeriodForUser {
				smallestRetentionPeriodForUser = streamRetention.Period
			}
		}

		// update the overallSmallestRetentionPeriod if this user has smaller value
		smallestRetentionPeriodByUser[userID] = now.Add(time.Duration(-smallestRetentionPeriodForUser))
		if smallestRetentionPeriodForUser < overallSmallestRetentionPeriod {
			overallSmallestRetentionPeriod = smallestRetentionPeriodForUser
		}
	}

	return latestRetentionStartTime{
		defaults: now.Add(time.Duration(-smallestDefaultRetentionPeriod)),
		overall:  now.Add(time.Duration(-overallSmallestRetentionPeriod)),
		byUser:   smallestRetentionPeriodByUser,
	}
}
