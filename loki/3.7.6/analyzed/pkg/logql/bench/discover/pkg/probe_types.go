package discover

// 内容探测配置与结果类型：ProbeConfig 控制并发、时间范围与 broad selector。

import "time"

// ProbeConfig 指定并行度、查询时间窗及关键词 broad 匹配用的 label selector。
// ProbeConfig controls a single RunContentProbes invocation.
type ProbeConfig struct {
	// Parallelism bounds concurrent API calls. Defaults to DefaultParallelism.
	Parallelism int

	// From/To bound the API query time range.
	From time.Time
	To   time.Time

	// BroadSelector is the label matcher clause used for keyword probing.
	// Instead of issuing one query per stream×keyword (N×K queries),
	// the prober issues one query per keyword using this broad selector
	// (K queries), then matches returned stream labels against the known
	// stream set client-side.
	//
	// Example: `{namespace=~"loki-ops-002|mimir-ops-03"}`
	//
	// This field is required and corresponds to the --selector CLI flag.
	BroadSelector string

	// BroadKeywordLimit controls the maximum number of log lines returned per
	// broad keyword query. Higher values increase the chance of covering all
	// 500 target streams but cost more Loki resources. Defaults to 1000.
	BroadKeywordLimit int
}

// effectiveParallelism 为零时使用 DefaultParallelism（5）。
// effectiveParallelism returns the resolved Parallelism, defaulting to
// DefaultParallelism when the field is zero.
func (c ProbeConfig) effectiveParallelism() int {
	if c.Parallelism > 0 {
		return c.Parallelism
	}
	return DefaultParallelism
}

// effectiveBroadKeywordLimit 默认 1000 条日志，平衡覆盖流数与 Loki 负载。
// effectiveBroadKeywordLimit returns the resolved limit for broad keyword
// queries, defaulting to 1000 when the field is zero.
func (c ProbeConfig) effectiveBroadKeywordLimit() int {
	if c.BroadKeywordLimit > 0 {
		return c.BroadKeywordLimit
	}
	return 1000
}

// ContentProbeResult 打包分类与关键词结果，供 AssembleMetadata 消费。
// ContentProbeResult bundles the classify and keyword probe outputs for
// consumption by AssembleMetadata.
type ContentProbeResult struct {
	Classify *ClassifyResult
	Keywords *KeywordResult
}
// BroadSelector 对应 CLI --selector，必填；用于减少 per-stream×keyword 查询量。
