package tsdb

// reader_types 定义单索引打开后的检查结果：路径、时间边界、版本与 Index 句柄。

import (
	"github.com/prometheus/common/model"

	lokitsdb "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb"
)

type IndexReaderResult struct {
	LocalPath  string
	Bounds     [2]model.Time
	Version    int
	LabelNames []string
// Index 由调用方负责 Close；用于 ForSeries 遍历与 chunk 元数据收集。
	// Index is the opened TSDB index handle for structural discovery (ForSeries
	// traversal). The caller is responsible for closing it when no longer needed.
	Index lokitsdb.Index
}
// Bounds 为 [from, through] 闭区间，与 TSDB 索引文件内记录的时间范围一致。
