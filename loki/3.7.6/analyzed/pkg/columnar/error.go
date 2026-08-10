package columnar

// error 定义 columnar 内部切片越界等错误的格式化输出。
// 各 Array 与 RecordBatch 的 Slice 方法在边界非法时 panic 此错误类型。

import "fmt"

type errorSliceBounds struct {
	i, j   int
	maxLen int
}

func (e errorSliceBounds) Error() string {
	return fmt.Sprintf("slice [%d:%d] out of range of max length %d", e.i, e.j, e.maxLen)
}
