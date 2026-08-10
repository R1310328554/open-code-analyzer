package util //nolint:revive

// ArrayListValue 将 Apache Arrow List 列的第 i 个元素解码为 Go 切片，供引擎列式数据处理使用。

import (
	"github.com/apache/arrow-go/v18/arrow/array"
)

func ArrayListValue(arr *array.List, i int) any {
	if arr.Len() == 0 {
		return []string{}
	}

	start, end := arr.ValueOffsets(i)
	listValues := arr.ListValues()
	switch listValues := listValues.(type) {
	case *array.String:
		result := make([]string, end-start)
		for i := start; i < end; i++ {
			result[i-start] = listValues.Value(int(i))
		}
		return result
	}

	return nil

}
// 当前仅支持 String 子列，其他类型返回 nil。
