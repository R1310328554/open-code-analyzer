package compute

// equality_numeric_kernels 定义数值比较内核实现：
// 覆盖 ==、!=、<、<=、>、>= 六种运算，int64 与 uint64 各有一套全局实例。

import (
	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/memory"
)

type numericEqualityKernel[T columnar.Numeric] interface {
	DoSS(left, right T) bool
	DoSA(out *memory.Bitmap, left T, right []T)
	DoAS(out *memory.Bitmap, left []T, right T)
	DoAA(out *memory.Bitmap, left, right []T)
}

var (
	int64EqualKernel    numericEqualityKernel[int64] = numericEqualKernelImpl[int64]{}
	int64NotEqualKernel numericEqualityKernel[int64] = numericNotEqualKernelImpl[int64]{}
	int64GTEKernel      numericEqualityKernel[int64] = numericGTEKernelImpl[int64]{}
	int64GTKernel       numericEqualityKernel[int64] = numericGTKernelImpl[int64]{}
	int64LTEKernel      numericEqualityKernel[int64] = numericLTEKernelImpl[int64]{}
	int64LTKernel       numericEqualityKernel[int64] = numericLTKernelImpl[int64]{}

	uint64EqualKernel    numericEqualityKernel[uint64] = numericEqualKernelImpl[uint64]{}
	uint64NotEqualKernel numericEqualityKernel[uint64] = numericNotEqualKernelImpl[uint64]{}
	uint64GTEKernel      numericEqualityKernel[uint64] = numericGTEKernelImpl[uint64]{}
	uint64GTKernel       numericEqualityKernel[uint64] = numericGTKernelImpl[uint64]{}
	uint64LTEKernel      numericEqualityKernel[uint64] = numericLTEKernelImpl[uint64]{}
	uint64LTKernel       numericEqualityKernel[uint64] = numericLTKernelImpl[uint64]{}
)

// numericEqualKernelImpl 实现相等（==）比较的四种操作数形态。
type numericEqualKernelImpl[T columnar.Numeric] struct{}

func (numericEqualKernelImpl[T]) DoSS(left, right T) bool {
	return left == right
}

func (numericEqualKernelImpl[T]) DoSA(out *memory.Bitmap, left T, right []T) {
	out.Resize(len(right))

	for i := range right {
		out.Set(i, left == right[i])
	}
}

func (numericEqualKernelImpl[T]) DoAS(out *memory.Bitmap, left []T, right T) {
	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] == right)
	}
}

func (numericEqualKernelImpl[T]) DoAA(out *memory.Bitmap, left, right []T) {
	if len(left) != len(right) {
		panic("invalid length")
	}

	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] == right[i])
	}
}

// numericNotEqualKernelImpl 实现不等（!=）比较。
type numericNotEqualKernelImpl[T columnar.Numeric] struct{}

func (numericNotEqualKernelImpl[T]) DoSS(left, right T) bool {
	return left != right
}

func (numericNotEqualKernelImpl[T]) DoSA(out *memory.Bitmap, left T, right []T) {
	out.Resize(len(right))

	for i := range right {
		out.Set(i, left != right[i])
	}
}

func (numericNotEqualKernelImpl[T]) DoAS(out *memory.Bitmap, left []T, right T) {
	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] != right)
	}
}

func (numericNotEqualKernelImpl[T]) DoAA(out *memory.Bitmap, left, right []T) {
	if len(left) != len(right) {
		panic("invalid length")
	}

	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] != right[i])
	}
}

// numericGTKernelImpl 实现大于（>）比较。
type numericGTKernelImpl[T columnar.Numeric] struct{}

func (numericGTKernelImpl[T]) DoSS(left, right T) bool {
	return left > right
}

func (numericGTKernelImpl[T]) DoSA(out *memory.Bitmap, left T, right []T) {
	out.Resize(len(right))

	for i := range right {
		out.Set(i, left > right[i])
	}
}

func (numericGTKernelImpl[T]) DoAS(out *memory.Bitmap, left []T, right T) {
	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] > right)
	}
}

func (numericGTKernelImpl[T]) DoAA(out *memory.Bitmap, left, right []T) {
	if len(left) != len(right) {
		panic("invalid length")
	}

	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] > right[i])
	}
}

type numericGTEKernelImpl[T columnar.Numeric] struct{}

func (numericGTEKernelImpl[T]) DoSS(left, right T) bool {
	return left >= right
}

func (numericGTEKernelImpl[T]) DoSA(out *memory.Bitmap, left T, right []T) {
	out.Resize(len(right))

	for i := range right {
		out.Set(i, left >= right[i])
	}
}

func (numericGTEKernelImpl[T]) DoAS(out *memory.Bitmap, left []T, right T) {
	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] >= right)
	}
}

func (numericGTEKernelImpl[T]) DoAA(out *memory.Bitmap, left, right []T) {
	if len(left) != len(right) {
		panic("invalid length")
	}

	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] >= right[i])
	}
}

// numericLTKernelImpl 实现小于（<）比较。
type numericLTKernelImpl[T columnar.Numeric] struct{}

func (numericLTKernelImpl[T]) DoSS(left, right T) bool {
	return left < right
}

func (numericLTKernelImpl[T]) DoSA(out *memory.Bitmap, left T, right []T) {
	out.Resize(len(right))

	for i := range right {
		out.Set(i, left < right[i])
	}
}

func (numericLTKernelImpl[T]) DoAS(out *memory.Bitmap, left []T, right T) {
	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] < right)
	}
}

func (numericLTKernelImpl[T]) DoAA(out *memory.Bitmap, left, right []T) {
	if len(left) != len(right) {
		panic("invalid length")
	}

	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] < right[i])
	}
}

type numericLTEKernelImpl[T columnar.Numeric] struct{}

func (numericLTEKernelImpl[T]) DoSS(left, right T) bool {
	return left <= right
}

func (numericLTEKernelImpl[T]) DoSA(out *memory.Bitmap, left T, right []T) {
	out.Resize(len(right))

	for i := range right {
		out.Set(i, left <= right[i])
	}
}

func (numericLTEKernelImpl[T]) DoAS(out *memory.Bitmap, left []T, right T) {
	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] <= right)
	}
}

func (numericLTEKernelImpl[T]) DoAA(out *memory.Bitmap, left, right []T) {
	if len(left) != len(right) {
		panic("invalid length")
	}

	out.Resize(len(left))

	for i := range len(left) {
		out.Set(i, left[i] <= right[i])
	}
}
