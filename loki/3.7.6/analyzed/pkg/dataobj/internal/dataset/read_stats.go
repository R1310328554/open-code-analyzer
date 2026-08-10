package dataset

// read_stats 注册 dataset 读取路径的 xcap 指标：主/次列、页下载、行读取与压缩字节等可观测统计项。

import (
	"github.com/grafana/loki/v3/pkg/xcap"
)

// 下列 Stat* 变量在 RowReader 与 downloader 中 Observe，聚合类型多为 Sum。
// xcap statistics for dataset reader operations.
var (
	// Column statistics
	StatPrimaryColumns   = xcap.NewStatisticInt64("primary.columns", xcap.AggregationTypeSum)
	StatSecondaryColumns = xcap.NewStatisticInt64("secondary.columns", xcap.AggregationTypeSum)

	// Page statistics
	StatPrimaryColumnPages   = xcap.NewStatisticInt64("primary.column.pages", xcap.AggregationTypeSum)
	StatSecondaryColumnPages = xcap.NewStatisticInt64("secondary.column.pages", xcap.AggregationTypeSum)

	// Row statistics
	StatMaxRows           = xcap.NewStatisticInt64("row.max", xcap.AggregationTypeSum)
	StatRowsAfterPruning  = xcap.NewStatisticInt64("rows.after.pruning", xcap.AggregationTypeSum)
	StatPrimaryRowsRead   = xcap.NewStatisticInt64("primary.rows.read", xcap.AggregationTypeSum)
	StatSecondaryRowsRead = xcap.NewStatisticInt64("secondary.rows.read", xcap.AggregationTypeSum)
	StatPrimaryRowBytes   = xcap.NewStatisticInt64("primary.row.read.bytes", xcap.AggregationTypeSum)
	StatSecondaryRowBytes = xcap.NewStatisticInt64("secondary.row.read.bytes", xcap.AggregationTypeSum)

	// Download/Page scan statistics
	StatPagesScanned         = xcap.NewStatisticInt64("pages.scanned", xcap.AggregationTypeSum)
	StatPagesFoundInCache    = xcap.NewStatisticInt64("pages.cache.hit", xcap.AggregationTypeSum)
	StatPageDownloadRequests = xcap.NewStatisticInt64("pages.download.requests", xcap.AggregationTypeSum)
	StatPageDownloadTime     = xcap.NewStatisticInt64("pages.download.duration.ns", xcap.AggregationTypeSum)

	// Page download byte statistics
	StatPrimaryPagesDownloaded           = xcap.NewStatisticInt64("primary.pages.downloaded", xcap.AggregationTypeSum)
	StatSecondaryPagesDownloaded         = xcap.NewStatisticInt64("secondary.pages.downloaded", xcap.AggregationTypeSum)
	StatPrimaryColumnBytes               = xcap.NewStatisticInt64("primary.pages.compressed.bytes", xcap.AggregationTypeSum)
	StatSecondaryColumnBytes             = xcap.NewStatisticInt64("secondary.pages.compressed.bytes", xcap.AggregationTypeSum)
	StatPrimaryColumnUncompressedBytes   = xcap.NewStatisticInt64("primary.pages.uncompressed.bytes", xcap.AggregationTypeSum)
	StatSecondaryColumnUncompressedBytes = xcap.NewStatisticInt64("secondary.pages.uncompressed.bytes", xcap.AggregationTypeSum)

	// Read operation statistics
	// StatReadCalls 记录 RowReader.Read 调用次数，衡量扫描轮次。
StatReadCalls = xcap.NewStatisticInt64("read.calls", xcap.AggregationTypeSum)
)
// 页缓存命中与下载耗时指标便于调优 Prefetch 与对象存储访问模式。
