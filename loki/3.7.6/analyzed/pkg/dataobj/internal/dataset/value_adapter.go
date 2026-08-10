package dataset

// value_adapter 将 columnar 列式数组批量拷贝到 dataset.Row 的 Values 槽位。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/dataobj/internal/util/slicegrow"
)

// copyArrayToRow copies an array to a dst []Row at the provided column index.
// Returns the amount of copied rows. Panics if len(dst) < src.Len().
//
// If src is nil, copyArray does nothing and returns 0.
// copyArrayToRow 按列类型分派到 UTF8/Int64/Null 拷贝函数。
func copyArrayToRow(dst []Row, columnIndex int, src columnar.Array) int {
	if src == nil {
		return 0
	}

	if len(dst) < src.Len() {
		panic(fmt.Sprintf("invariant broken: len(dst) < src.Len(): %d < %d", len(dst), src.Len()))
	}

	switch src := src.(type) {
	case *columnar.UTF8:
		return copyUTF8Array(dst, columnIndex, src)
	case *columnar.Number[int64]:
		return copyInt64Array(dst, columnIndex, src)
	case *columnar.Null:
		return copyNullArray(dst, columnIndex, src)
	default:
		panic(fmt.Sprintf("unexpected array type: %T", src))
	}
}

// copyUTF8Array copies values from src into dst. Returns the amount of copied
// rows.
//
// Should only be called from [copyArray].
// copyUTF8Array 逐行复制 UTF8 字节到 Row 的 BinaryValue 槽。
func copyUTF8Array(dst []Row, columnIndex int, src *columnar.UTF8) int {
	for i := range src.Len() {
		if src.IsNull(i) {
			dst[i].Values[columnIndex].Zero()
			continue
		}

		srcBuf := src.Get(i)
		dstBuf := slicegrow.GrowToCap(dst[i].Values[columnIndex].Buffer(), len(srcBuf))
		dstBuf = dstBuf[:len(srcBuf)]
		copy(dstBuf, srcBuf)

		dst[i].Values[columnIndex] = BinaryValue(dstBuf)
	}

	return src.Len()
}

// copyInt64Array copies values from src into dst. Returns the amount of copied
// rows.
//
// Should only be called from [copyArray].
// copyInt64Array 将非空 int64 写入 Int64Value，空值调用 Zero。
func copyInt64Array(dst []Row, columnIndex int, src *columnar.Number[int64]) int {
	for i := range src.Len() {
		if src.IsNull(i) {
			dst[i].Values[columnIndex].Zero()
			continue
		}
		dst[i].Values[columnIndex] = Int64Value(src.Get(i))
	}

	return src.Len()
}

// copyNullArray copies values from src into dst. Returns the amount of copied
// rows.
//
// Should only be called from [copyArray].
// copyNullArray 将全部行对应列置为零值表示 NULL。
func copyNullArray(dst []Row, columnIndex int, src *columnar.Null) int {
	for i := range src.Len() {
		dst[i].Values[columnIndex].Zero()
	}
	return src.Len()
}
// 拷贝过程通过 slicegrow 扩展目标缓冲以复用已有内存。
