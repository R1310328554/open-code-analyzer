package tsdb

// range 定义 LogQL 基准测试用的最小查询窗口桶与阈值常量，以及范围元数据推导的结果结构。

import (
	"time"

	bench "github.com/grafana/loki/v3/pkg/logql/bench"
)

// rangeBuckets 从小到大探测；窗口锚定在时间范围末端以采样最新数据。
// rangeBuckets are probed smallest-to-largest. For each bucket d, we query the
// window [to-d, to] to determine whether enough entries exist. Anchoring at
// the END of the full time range ensures we sample the most recent data.
var rangeBuckets = []time.Duration{
	5 * time.Minute,
	10 * time.Minute,
	15 * time.Minute,
	30 * time.Minute,
	1 * time.Hour,
}

// rangeEntryThreshold 为范围查询认定 min_range 所需的最小条目数。
// rangeEntryThreshold is the minimum number of entries required in a bucket
// for that bucket to qualify as min_range (used for range queries).
const rangeEntryThreshold = 5

// instantEntryThreshold 为即时查询认定 min_instant_range 所需的最小条目数。
// instantEntryThreshold is the minimum number of entries required in a bucket
// for that bucket to qualify as min_instant_range (used for instant queries).
const instantEntryThreshold = 10

// RangeResult 汇总各 selector 的最小查询窗口及探测过程中的警告与计数。
// RangeResult holds the output of a completed range-metadata derivation.
type RangeResult struct {
	// MetadataBySelector maps each canonical selector to its probed range
	// durations. Only selectors that met at least one threshold are present.
	MetadataBySelector map[string]*bench.SerializableStreamMetadata

	// Warnings accumulates non-fatal issues encountered during probing.
	Warnings []string

	// TotalProbed is the number of streams probed.
	TotalProbed int

	// TotalSkipped is the number of streams where no bucket met any threshold.
	TotalSkipped int
}
// MetadataBySelector 仅包含至少满足一个阈值的流，跳过的流计入 TotalSkipped。
