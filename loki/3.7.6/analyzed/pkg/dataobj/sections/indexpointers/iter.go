package indexpointers

// indexpointers 迭代工具：遍历 data object 内全部 index pointer 行。

import (
	"context"
	"errors"
	"fmt"
	"io"
	"time"
	"unsafe"

	"github.com/grafana/loki/v3/pkg/dataobj"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/dataset"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/metadata/datasetmd"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/result"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/util/symbolizer"
	"github.com/grafana/loki/v3/pkg/dataobj/sections/internal/columnar"
)

// Iter 按 section 顺序打开并 yield 带租户 ID 的 TenantIndexPointer。
// Iter iterates over indexpointers in the provided decoder. All indexpointers sections are
// iterated over in order.
func Iter(ctx context.Context, obj *dataobj.Object) result.Seq[TenantIndexPointer] {
	return result.Iter(func(yield func(pointer TenantIndexPointer) bool) error {
		for i, section := range obj.Sections().Filter(CheckSection) {
			pointersSection, err := Open(ctx, section)
			if err != nil {
				return fmt.Errorf("opening section %d: %w", i, err)
			}

			for result := range IterSection(ctx, pointersSection) {
				if result.Err() != nil || !yield(TenantIndexPointer{Tenant: section.Tenant, IndexPointer: result.MustValue()}) {
					return result.Err()
				}
			}
		}

		return nil
	})
}

// IterSection 对单个 section 构建 dataset 行读取器并批量解码指针。
func IterSection(ctx context.Context, section *Section) result.Seq[IndexPointer] {
	return result.Iter(func(yield func(IndexPointer) bool) error {
		columnarSection := section.inner
		dset, err := columnar.MakeDataset(columnarSection, columnarSection.Columns())
		if err != nil {
			return fmt.Errorf("creating columns dataset: %w", err)
		}

		columns, err := result.Collect(dset.ListColumns(ctx))
		if err != nil {
			return err
		}

		r := dataset.NewRowReader(dataset.RowReaderOptions{
			Dataset:  dset,
			Columns:  columns,
			Prefetch: true,
		})
		defer r.Close()

		if err := r.Open(ctx); err != nil {
			return err
		}

		sym := symbolizer.New(128, 1024)

		var rows [1024]dataset.Row
		for {
			n, err := r.Read(ctx, rows[:])
			if err != nil && !errors.Is(err, io.EOF) {
				return err
			} else if n == 0 && errors.Is(err, io.EOF) {
				return nil
			}

			var pointer IndexPointer
			for _, row := range rows[:n] {
				if err := decodeRow(section.Columns(), row, &pointer, sym); err != nil {
					return err
				}

				if !yield(pointer) {
					return nil
				}
			}
		}
	})
}

// decodeRow 按列类型填充 IndexPointer，path 列可经 symbolizer 复用字符串。
// decodeRow decodes an indexpointer from a [dataset.Row], using the provided columns to
// determine the column type. The list of columns must match the columns used
// to create the row.
//
// The sym argument is used for reusing label values between calls to
// decodeRow. If sym is nil, label value strings are always allocated.
func decodeRow(columns []*Column, row dataset.Row, pointer *IndexPointer, sym *symbolizer.Symbolizer) error {
	for columnIndex, columnValue := range row.Values {
		column := columns[columnIndex]
		switch column.Type {
		case ColumnTypePath:
			if ty := columnValue.Type(); ty != datasetmd.PHYSICAL_TYPE_BINARY {
				return fmt.Errorf("invalid type %s for %s", ty, column.Type)
			}

			if columnValue.IsNil() || columnValue.IsZero() {
				return fmt.Errorf("nil or zero value for %s", column.Type)
			}

			if sym != nil {
				pointer.Path = sym.Get(unsafeString(columnValue.Binary()))
			} else {
				pointer.Path = string(columnValue.Binary())
			}

		case ColumnTypeMinTimestamp:
			if ty := columnValue.Type(); ty != datasetmd.PHYSICAL_TYPE_INT64 {
				return fmt.Errorf("invalid type %s for %s", ty, column.Type)
			}

			if columnValue.IsNil() || columnValue.IsZero() {
				return fmt.Errorf("nil or zero value for %s", column.Type)
			}

			pointer.StartTs = time.Unix(0, columnValue.Int64())

		case ColumnTypeMaxTimestamp:
			if ty := columnValue.Type(); ty != datasetmd.PHYSICAL_TYPE_INT64 {
				return fmt.Errorf("invalid type %s for %s", ty, column.Type)
			}

			if columnValue.IsNil() || columnValue.IsZero() {
				return fmt.Errorf("nil or zero value for %s", column.Type)
			}

			pointer.EndTs = time.Unix(0, columnValue.Int64())

		default:
			// TODO(rfratto): We probably don't want to return an error on unexpected
			// columns because it breaks forward compatibility. Should we log
			// something here?
		}
	}

	return nil
}

// unsafeString 零拷贝将 []byte 转为 string，仅供 symbolizer 热路径使用。
func unsafeString(data []byte) string {
	return unsafe.String(unsafe.SliceData(data), len(data))
}
// IterSection 预取列数据并以 1024 行批量读取提升顺序扫描吞吐。
