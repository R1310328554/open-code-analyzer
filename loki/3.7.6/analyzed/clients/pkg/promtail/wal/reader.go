package wal

// WAL 全量读取工具：顺序扫描目录下所有段，解码 series 与 RefEntries，
// 重建 api.Entry 列表，主要用于测试与离线校验。

import (
	"fmt"

	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/clients/pkg/promtail/api"

	"github.com/grafana/loki/v3/pkg/ingester/wal"
	"github.com/grafana/loki/v3/pkg/util"
	walUtils "github.com/grafana/loki/v3/pkg/util/wal"
)

// NewWalReader 遍历记录，先索引 series Ref→标签，再拼接对应日志条目。
// ReadWAL will read all entries in the WAL located under dir. Mainly used for testing
func ReadWAL(dir string) ([]api.Entry, error) {
	reader, closeFn, err := walUtils.NewWalReader(dir, -1)
	if err != nil {
		return nil, err
	}
	defer func() { closeFn.Close() }()

	seenSeries := make(map[uint64]model.LabelSet)
	seenEntries := []api.Entry{}

	for reader.Next() {
		var walRec = wal.Record{}
		bytes := reader.Record()
		err = wal.DecodeRecord(bytes, &walRec)
		if err != nil {
			return nil, fmt.Errorf("error decoding wal record: %w", err)
		}

// 每条 WAL 记录先处理 Series 段，建立 Ref 到 model.LabelSet 的映射。
		// first read series
		for _, series := range walRec.Series {
			if _, ok := seenSeries[uint64(series.Ref)]; !ok {

				seenSeries[uint64(series.Ref)] = util.MapToModelLabelSet(series.Labels.Map())
			}
		}

// 遍历 RefEntries，按 Ref 查找标签并构造 api.Entry 追加到结果切片。
		for _, entries := range walRec.RefEntries {
			for _, entry := range entries.Entries {
				labels, ok := seenSeries[uint64(entries.Ref)]
				if !ok {
					return nil, fmt.Errorf("found entry without matching series")
				}
				seenEntries = append(seenEntries, api.Entry{
					Labels: labels,
					Entry:  entry,
				})
			}
		}

		// reset entry
		walRec.Series = walRec.Series[:]
		walRec.RefEntries = walRec.RefEntries[:]
	}

	return seenEntries, nil
}
