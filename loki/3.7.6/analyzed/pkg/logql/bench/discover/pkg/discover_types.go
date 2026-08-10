package discover

// discover 包公共配置：默认流数上限、并发度及元数据组装/校验的时间范围 Config。

import (
	"time"
)

const (
	// DefaultMaxStreams 限制结构发现输出的最大流数量，默认 250。
// DefaultMaxStreams is the default cap on the number of streams returned by
	// RunDiscovery. Callers can override via Config.MaxStreams.
	DefaultMaxStreams = 250

	// DefaultParallelism 为关键词与范围探测的默认并发 API 调用数（5）。
// DefaultParallelism is the default number of concurrent series API calls
	// issued by RunDiscovery. Callers can override via Config.Parallelism.
	DefaultParallelism = 5
)

// Config 控制元数据组装与校验的全局时间窗口；零值 From/To 有默认解析规则。
// Config controls assembly and validation of the metadata pipeline.
// Storage discovery, content probing, and other pipeline options are configured
// directly on their respective config types (TSDBStructuralConfig, ProbeConfig).
type Config struct {
	// From is the start of the query time range. When zero-valued, defaults to
	// 24 hours before To (or before now when To is also zero).
	From time.Time

	// To is the end of the query time range. When zero-valued, defaults to the
	// current time.
	To time.Time
}

// effectiveFrom 在 From 为零时默认为 effectiveTo 前 24 小时。
// effectiveFrom returns the resolved From boundary, defaulting to 24h before
// the effective To when From is zero-valued.
func (c Config) effectiveFrom() time.Time {
	if !c.From.IsZero() {
		return c.From
	}
	return c.effectiveTo().Add(-24 * time.Hour)
}

// effectiveTo returns the resolved To boundary, defaulting to the current time
// when To is zero-valued.
func (c Config) effectiveTo() time.Time {
	if !c.To.IsZero() {
		return c.To
	}
	return time.Now()
}
// 存储发现与内容探测的细项配置在各自 TSDB/ProbeConfig 类型中单独设置。
