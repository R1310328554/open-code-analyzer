package tsdb

// structural_types 定义结构发现运行的配置、中间载荷、合并流与最终输出结果类型。

import (
	"time"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/loghttp"
	tsdbindex "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
)

// StructuralConfig 控制时间边界、MaxStreams 截断、Selector 过滤与进度回调。
// StructuralConfig controls one TSDB structural discovery run.
type StructuralConfig struct {
	// UserID is accepted for tsdb.Index compatibility. It may be empty for
	// single-tenant indexes.
	UserID string

	// From bounds the series/chunk scan start time. Zero means "use index
	// bounds".
	From time.Time

	// To bounds the series/chunk scan end time. Zero means "use index bounds".
	To time.Time

	// MaxStreams caps the number of canonical selectors in the output using
	// service-diversity selection. Zero means no cap (all unique streams are
	// returned). When set, selectWithDiversity is applied after enumeration.
	MaxStreams int

	// Selector is an optional LogQL-style label matcher clause (without outer
	// braces) appended to the ForSeries call to narrow the set of streams
	// enumerated from the index. Example: `namespace="loki-ops-002"`.
	// Empty means enumerate all streams.
	Selector string

	// ProgressWriter, when non-nil, receives periodic progress updates during
	// ForSeries enumeration. The function is called with (rawCount, uniqueCount)
	// approximately every ProgressInterval (or 10s by default).
	ProgressWriter func(rawCount, uniqueCount int)

	// ProgressInterval controls how often ProgressWriter is called. Zero
	// defaults to 10 seconds.
	ProgressInterval time.Duration
}

// StructuralSeriesPayload 为 ForSeries 回调快照，避免索引关闭后悬垂引用。
// StructuralSeriesPayload is the callback-owned input shape copied during
// TSDB traversal before readers are closed.
type StructuralSeriesPayload struct {
	Labels      loghttp.LabelSet
	Fingerprint model.Fingerprint
	ChunkMetas  []tsdbindex.ChunkMeta
}

// MergedStream 按 canonical selector 聚合多索引来源的 chunk 与 SourceCount。
// MergedStream is the deduplicated aggregate for one canonical selector.
// Labels and ChunkMetas are deep-copied values, never callback-owned refs.
type MergedStream struct {
	Selector    string
	Labels      loghttp.LabelSet
	ChunkMetas  []tsdbindex.ChunkMeta
	SourceCount int
}

// StructuralResult 含倒排索引、选中 selector 列表及 MergedStreams 供后续 range 推导。
// StructuralResult is the structural discovery output produced from local
// TSDB indexes only.
type StructuralResult struct {
	AllSelectors  []string
	LabelSets     map[string]loghttp.LabelSet
	ByServiceName map[string][]string
	ByLabelKey    map[string][]string
	TotalRaw      int
	TotalUnique   int
	TotalSelected int
	MergedStreams map[string]MergedStream
}
// MaxStreams 为零表示不截断，返回全部去重后的唯一流 selector。
