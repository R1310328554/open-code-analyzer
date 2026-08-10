package pointers

// pointers 段行级谓词：高层查询条件，可组合为 And、Bloom 存在性与时间范围过滤。

import "time"

type (
	// RowPredicate is an expression used to filter rows in a data object.
	RowPredicate interface{ isRowPredicate() }
)

// Supported predicates.
type (
	// An AndRowPredicate is a RowPredicate which requires both its Left and
	// Right predicate to be true.
	AndRowPredicate struct{ Left, Right RowPredicate }

// BloomExistenceRowPredicate 要求指定名列的 Bloom 过滤器包含 Value。
	// A BloomExistenceRowPredicate is a RowPredicate which requires a bloom filter column named
	// Name to exist, and for the Value to pass the bloom filter.
	BloomExistenceRowPredicate struct{ Name, Value string }

// TimeRangeRowPredicate 按 Start/End 时间闭区间过滤指针行时间戳。
	// A TimeRangeRowPredicate is a RowPredicate which requires the timestamp of
	// the entry to be within the range of StartTime and EndTime.
	TimeRangeRowPredicate struct{ Start, End time.Time }
)

func (AndRowPredicate) isRowPredicate()            {}
func (BloomExistenceRowPredicate) isRowPredicate() {}
func (TimeRangeRowPredicate) isRowPredicate()      {}
// 行级谓词在更高层查询规划中与列级 Predicate 配合使用。
