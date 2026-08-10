package queryrange

// ingester_query_window 根据查询是否落入 ingester 窗口选择拆分间隔：窗口内优先使用 IngesterQuerySplitDuration，否则用默认 split。

import (
	"time"

	"github.com/grafana/loki/v3/pkg/util"
	"github.com/grafana/loki/v3/pkg/util/validation"
)

// SplitIntervalForTimeRange 以 ref 为基准计算 ingester 窗口起点，upperBound 部分重叠即启用 ingester split。
// SplitIntervalForTimeRange returns the correct split interval to use. It accounts for the given upperBound value being
// within the ingester query window, in which case it returns the ingester query split (unless it's not set, then the default
// split interval will be used).
func SplitIntervalForTimeRange(iqo util.IngesterQueryOptions, limits Limits, defaultSplitFn func(string) time.Duration, tenantIDs []string, ref, upperBound time.Time) time.Duration {
	split := validation.SmallestPositiveNonZeroDurationPerTenant(tenantIDs, defaultSplitFn)

	if iqo == nil {
		return split
	}

	// if the query is within the ingester query window, choose the ingester split duration (if configured), otherwise
	// revert to the default split duration
	ingesterQueryWindowStart := ref.Add(-iqo.QueryIngestersWithin())

	// query is (even partially) within the ingester query window
	if upperBound.After(ingesterQueryWindowStart) {
		ingesterSplit := validation.MaxDurationOrZeroPerTenant(tenantIDs, limits.IngesterQuerySplitDuration)
		if !iqo.QueryStoreOnly() && ingesterSplit > 0 {
			split = ingesterSplit
		}
	}

	return split
}
// QueryStoreOnly 为 true 或 ingester split 未配置时回退至租户默认 QuerySplitDuration。
