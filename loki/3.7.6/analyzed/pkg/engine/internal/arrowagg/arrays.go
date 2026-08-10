package arrowagg

// arrowagg.Arrays 聚合多个同类型 Arrow 数组片段，最终 Concatenate 为单一列数组。

import (
	"github.com/apache/arrow-go/v18/arrow"
	"github.com/apache/arrow-go/v18/arrow/array"
	"github.com/apache/arrow-go/v18/arrow/memory"
)

// Arrays 作为 builder 缓冲 Append 的数组，Aggregate 时一次性拼接并 Reset 复用。
// Arrays allows for aggregating a set of [arrow.Array]s together into a new,
// combined array.
type Arrays struct {
	mem memory.Allocator
	dt  arrow.DataType

	in    []arrow.Array
	nrows int // Total number of rows in the builder.
}

// NewArrays 指定 memory.Allocator 与期望 DataType，类型一致性在 Aggregate 时校验。
// NewArrays creates a new [Arrays] that aggregates a set of arrays of the same
// data type. The data type of incoming arrays is not checked until calling
// [Arrays.Aggregate].
func NewArrays(mem memory.Allocator, dt arrow.DataType) *Arrays {
	return &Arrays{mem: mem, dt: dt}
}

// Append 整列追加数组，仅递增 nrows 计数，暂不校验 dt 匹配。
// Append appends the entirety of the given array to the builder. The data type
// of arr is not checked until calling [Arrays.Aggregate].
func (a *Arrays) Append(arr arrow.Array) {
	a.nrows++
	a.in = append(a.in, arr)
}

// AppendSlice 追加 arr[i:j] 切片，内部 NewSlice 可能共享底层 buffer。
// AppendSlice appends a slice of the given array to the builder. The data type
// of arr is not checked until calling [Arrays.Aggregate].
func (a *Arrays) AppendSlice(arr arrow.Array, i, j int64) {
	a.nrows += max(0, int(j-i))
	a.in = append(a.in, array.NewSlice(arr, i, j))
}

// AppendNulls 追加 n 个指定类型的 null 占位，用于对齐稀疏列长度。
// AppendNulls appends n null values to the builer.
func (a *Arrays) AppendNulls(n int) {
	a.nrows += n
	a.in = append(a.in, array.MakeArrayOfNull(a.mem, a.dt, n))
}

// Len returns the total number of rows currently appended to the builder.
func (a *Arrays) Len() int { return a.nrows }

// Aggregate 调用 array.Concatenate 合并，无论成败均 Reset 以便下次复用 builder。
// Aggregate all appended arrays into a single array.
// If no arrays have been appended, Aggregate returns a zero-length array.
//
// Aggregate returns an error if any of the appended arrays do not match the
// data type passed to [NewArrays].
//
// After calling Aggregate, a is reset and can be reused to append more arrays.
// This reset is done even if Aggregate returns an error.
func (a *Arrays) Aggregate() (arrow.Array, error) {
	if len(a.in) == 0 {
		return array.MakeArrayOfNull(a.mem, a.dt, 0), nil
	}

	defer a.Reset()
	return array.Concatenate(a.in, a.mem)
}

// Reset 清空 in 切片与 nrows，释放对已 Append 数组的引用以便 GC。
// Reset releases all arrays currently appended to a and resets it for reuse.
func (a *Arrays) Reset() {
	clear(a.in)
	a.in = a.in[:0]
	a.nrows = 0
}
// 无输入时 Aggregate 返回零长度 null 数组，类型与 NewArrays 指定 dt 一致。
