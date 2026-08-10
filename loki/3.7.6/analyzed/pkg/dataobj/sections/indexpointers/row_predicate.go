package indexpointers

// indexpointers 行级谓词：用于在读取索引指针时按条件过滤行。

import "time"

type (
// RowPredicate 是过滤 data object 行的表达式接口，由具体谓词类型实现。
	// RowPredicate is an expression used to filter rows in a data object.
	RowPredicate interface{ isRowPredicate() }
)

// Supported predicates.
type (
// TimeRangeRowPredicate 要求 min/max 时间戳列存在，且行的时间范围与查询区间重叠。
	// A TimeRangeRowPredicate is a RowPredicate which requires a start and end timestamp column to exist,
	// and for the timestamp to be within the range.
	TimeRangeRowPredicate struct{ Start, End time.Time }
)

func (TimeRangeRowPredicate) isRowPredicate() {}
// 当前仅支持时间范围谓词，后续可扩展更多索引指针过滤条件。
