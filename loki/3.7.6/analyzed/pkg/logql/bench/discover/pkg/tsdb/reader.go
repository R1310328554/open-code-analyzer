package tsdb

// reader 打开本地 TSDB 索引文件，读取边界、标签名并保留 Index 句柄供结构发现。

import (
	"fmt"
	"strings"

	"github.com/prometheus/common/model"

	lokitsdb "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb"
	tsdbindex "github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
)

// OpenAndInspectIndexes 批量打开索引；任一失败时关闭已打开句柄再返回错误。
func OpenAndInspectIndexes(paths []string) ([]IndexReaderResult, error) {
	results := make([]IndexReaderResult, 0, len(paths))
	for _, path := range paths {
		result, err := openAndInspectIndex(path)
		if err != nil {
	// 出错路径需释放已打开的 Index，避免文件描述符泄漏。
		// Close any already-opened index handles before returning.
			for _, r := range results {
				if r.Index != nil {
					_ = r.Index.Close()
				}
			}
			return nil, err
		}
		results = append(results, result)
	}
	return results, nil
}

// openAndInspectIndex 先用 FileReader 读元数据，再 NewTSDBIndexFromFile 供 ForSeries。
func openAndInspectIndex(path string) (result IndexReaderResult, err error) {
	reader, err := tsdbindex.NewFileReader(path)
	if err != nil {
		return IndexReaderResult{}, fmt.Errorf("open tsdb index %q: %w", path, err)
	}

	defer func() {
		if closeErr := reader.Close(); err == nil && closeErr != nil {
			err = fmt.Errorf("close tsdb index %q: %w", path, closeErr)
		}
	}()

	from, through := reader.Bounds()
	labelNames, err := reader.LabelNames()
	if err != nil {
		return IndexReaderResult{}, fmt.Errorf("read label names from %q: %w", path, err)
	}

	copiedNames := make([]string, len(labelNames))
	for i, name := range labelNames {
		copiedNames[i] = strings.Clone(name)
	}

// Index 字段保持打开直至调用方 Close，供 RunStructuralDiscovery 遍历序列。
	// Full index handle for structural discovery (ForSeries traversal).
	// This stays open — the caller must close it via result.Index.Close().
	idx, _, err := lokitsdb.NewTSDBIndexFromFile(path)
	if err != nil {
		return IndexReaderResult{}, fmt.Errorf("open TSDB index for discovery %q: %w", path, err)
	}

	return IndexReaderResult{
		LocalPath: path,
		Bounds: [2]model.Time{
			model.Time(from),
			model.Time(through),
		},
		Version:    reader.Version(),
		LabelNames: copiedNames,
		Index:      idx,
	}, nil
}
// LabelNames 经 strings.Clone 深拷贝，避免底层 mmap 缓冲被意外修改。
