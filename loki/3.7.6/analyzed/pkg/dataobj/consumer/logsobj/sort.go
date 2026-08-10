package logsobj

// sort 模块实现多 logs section 的 k 路归并排序：
// 基于 loser tree 按配置的 SortOrder 合并已排序 section 中的日志记录。

import (
	"context"
	"fmt"
	"math"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/dataset"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/result"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/logs"
	"github.com/grafana/loki/v3/pkg/util/loser"
)

// sortMergeIterator 打开各 section 列式数据集，用 loser tree 做 k 路归并输出 Record。
// sortMergeIterator returns an iterator that performs a k-way merge of records from multiple logs sections.
// It requires that the input sections are sorted sorted by the same order.
func sortMergeIterator(ctx context.Context, sections []*dataobj.Section, sort logs.SortOrder) (result.Seq[logs.Record], error) {
	sequences := make([]*sectionSequence, 0, len(sections))
	for _, s := range sections {
		sec, err := logs.Open(ctx, s)
		if err != nil {
			return nil, fmt.Errorf("failed to open logs section: %w", err)
		}

		ds, err := logs.MakeColumnarDataset(sec)
		if err != nil {
			return nil, fmt.Errorf("creating columnar dataset: %w", err)
		}

		columns, err := result.Collect(ds.ListColumns(ctx))
		if err != nil {
			return nil, err
		}

		r := dataset.NewRowReader(dataset.RowReaderOptions{
			Dataset:  ds,
			Columns:  columns,
			Prefetch: true,
		})
		if err := r.Open(ctx); err != nil {
			return nil, fmt.Errorf("opening dataset row reader: %w", err)
		}

		sequences = append(sequences, &sectionSequence{
			section:         sec,
			DatasetSequence: logs.NewDatasetSequence(r, 8<<10),
		})
	}

	maxValue := result.Value(dataset.Row{
		Index: math.MaxInt,
		Values: []dataset.Value{
			dataset.Int64Value(math.MaxInt64), // StreamID
			dataset.Int64Value(math.MinInt64), // Timestamp
		},
	})

	tree := loser.New(sequences, maxValue, sectionSequenceAt, logs.CompareForSortOrder(sort), sectionSequenceClose)

	return result.Iter(
		func(yield func(logs.Record) bool) error {
			defer tree.Close()
			for tree.Next() {
				seq := tree.Winner()

				row, err := sectionSequenceAt(seq).Value()
				if err != nil {
					return err
				}

				var record logs.Record
				err = logs.DecodeRow(seq.section.Columns(), row, &record, nil)
				if err != nil || !yield(record) {
					return err
				}
			}
			return nil
		}), nil
}

// sectionSequence 包装 logs.DatasetSequence 并保留 section 引用供解码行。
type sectionSequence struct {
	logs.DatasetSequence
	section *logs.Section
}

var _ loser.Sequence = (*sectionSequence)(nil)

func sectionSequenceAt(seq *sectionSequence) result.Result[dataset.Row] { return seq.At() }
func sectionSequenceClose(seq *sectionSequence)                         { seq.Close() }
