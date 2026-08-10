package compute

// equality_null 处理 KindNull 类型的比较分派：
// null 与 null 比较结果恒为 null 布尔值，数组路径输出全 false 值位图。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/memory"
)

func dispatchNullEquality(alloc *memory.Allocator, left, right columnar.Datum, selection memory.Bitmap) (columnar.Datum, error) {
	_, leftScalar := left.(columnar.Scalar)
	_, rightScalar := right.(columnar.Scalar)

	switch {
	case leftScalar && rightScalar:
		return nullEqualitySS(left.(*columnar.NullScalar), right.(*columnar.NullScalar)), nil
	case leftScalar && !rightScalar:
		out := nullEqualitySA(alloc, left.(*columnar.NullScalar), right.(*columnar.Null))
		return applySelectionToBoolArray(alloc, out, selection)
	case !leftScalar && rightScalar:
		out := nullEqualityAS(alloc, left.(*columnar.Null), right.(*columnar.NullScalar))
		return applySelectionToBoolArray(alloc, out, selection)
	case !leftScalar && !rightScalar:
		out, err := nullEqualityAA(alloc, left.(*columnar.Null), right.(*columnar.Null))
		if err != nil {
			return nil, err
		}
		return applySelectionToBoolArray(alloc, out, selection)
	}

	panic("unreachable")
}

// nullEqualitySS 两 null 标量比较，结果 BoolScalar 标记为 Null。
func nullEqualitySS(_, _ *columnar.NullScalar) *columnar.BoolScalar {
	return &columnar.BoolScalar{Null: true}
}

func nullEqualitySA(alloc *memory.Allocator, _ *columnar.NullScalar, right *columnar.Null) *columnar.Bool {
	validity := computeValiditySA(alloc, true, right.Validity())

	values := memory.NewBitmap(alloc, right.Len())
	values.AppendCount(false, right.Len())

	return columnar.NewBool(values, validity)
}

func nullEqualityAS(alloc *memory.Allocator, left *columnar.Null, _ *columnar.NullScalar) *columnar.Bool {
	validity := computeValidityAS(alloc, left.Validity(), true)

	values := memory.NewBitmap(alloc, left.Len())
	values.AppendCount(false, left.Len())

	return columnar.NewBool(values, validity)
}

// nullEqualityAA 两 null 数组比较，校验长度并合并有效性位图。
func nullEqualityAA(alloc *memory.Allocator, left, right *columnar.Null) (*columnar.Bool, error) {
	if left.Len() != right.Len() {
		return nil, fmt.Errorf("array length mismatch: %d != %d", left.Len(), right.Len())
	}

	validity, err := computeValidityAA(alloc, left.Validity(), right.Validity())
	if err != nil {
		return nil, err
	}

	values := memory.NewBitmap(alloc, left.Len())
	values.AppendCount(false, left.Len())

	return columnar.NewBool(values, validity), nil
}
