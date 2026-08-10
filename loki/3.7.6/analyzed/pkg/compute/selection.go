package compute

// selection 位图应用：将未选中行在布尔数组结果中标记为 null。

import (
	"github.com/grafana/loki/v3/pkg/columnar"
	"github.com/grafana/loki/v3/pkg/memory"
)

// applySelectionToBoolArray 合并原 validity 与 selection，未选中行置 null。
// applySelectionToBoolArray applies a selection bitmap to a boolean array,
// marking unselected rows as null in the result.
func applySelectionToBoolArray(alloc *memory.Allocator, arr *columnar.Bool, selection memory.Bitmap) (*columnar.Bool, error) {
	if selection.Len() == 0 {
		return arr, nil
	}
	validity, err := computeValidityAA(alloc, arr.Validity(), selection)
	if err != nil {
		return nil, err
	}
	return columnar.NewBool(arr.Values(), validity), nil
}
// 空 selection 时直接返回原数组不做过滤。
