package tsdb

// download_types 定义 TSDB 索引下载的配置、单文件元数据与批量结果类型。

import (
	"time"

	"github.com/prometheus/common/model"
)

type DownloadConfig struct {
	Tenant      string
	From        time.Time
	To          time.Time
	TmpDir      string
// TablePrefix 覆盖默认 index_ 前缀，适配非标准部署的表命名。
	TablePrefix string // Overrides default "index_" prefix for table name generation (e.g. "loki_dev_005_tsdb_index_").
}

type DownloadedIndexFile struct {
	Table      string
	ObjectName string
	LocalPath  string
	From       model.Time
	Through    model.Time
}

// DownloadResult 聚合一次发现下载任务的全部索引文件列表。
type DownloadResult struct {
	Files []DownloadedIndexFile
}
// From/Through 使用 Prometheus model.Time，与 TSDB 索引内部时间语义一致。
