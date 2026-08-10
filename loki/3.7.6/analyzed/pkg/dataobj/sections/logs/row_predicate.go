package logs

// logs 段行级谓词：将查询条件表达为可组合的 RowPredicate 树，供 RowReader 下推过滤。

import (
	"time"
)

// RowPredicate 标记接口，所有日志行过滤表达式均实现 isRowPredicate。
// RowPredicate is an expression used to filter rows in a data object.
type RowPredicate interface {
	isRowPredicate()
}

// 以下为支持的谓词组合：逻辑与/或/非、时间范围、日志正文与元数据匹配。
// Supported predicates.
type (
	// An AndRowPredicate is a RowPredicate which requires both its Left and
	// Right predicate to be true.
	AndRowPredicate struct{ Left, Right RowPredicate }

	// An OrRowPredicate is a RowPredicate which requires either its Left or
	// Right predicate to be true.
	OrRowPredicate struct{ Left, Right RowPredicate }

	// A NotRowPredicate is a RowPredicate which requires its Inner predicate to
	// be false.
	NotRowPredicate struct{ Inner RowPredicate }

// TimeRangeRowPredicate 按纳秒时间戳区间过滤，IncludeStart/End 控制端点是否闭区间。
	// A TimeRangeRowPredicate is a RowPredicate which requires the timestamp of
	// the entry to be within the range of StartTime and EndTime.
	TimeRangeRowPredicate struct {
		StartTime, EndTime time.Time
		IncludeStart       bool // Whether StartTime is inclusive.
		IncludeEnd         bool // Whether EndTime is inclusive.
	}

// LogMessageFilterRowPredicate 用 Keep 回调对日志行字节切片做自定义匹配。
	// A LogMessageFilterRowPredicate is a RowPredicate that requires the log
	// message of the entry to pass a Keep function.
	LogMessageFilterRowPredicate struct {
		Keep func(line []byte) bool
	}

	// A MetadataMatcherRowPredicate is a RowPredicate that requires a metadata
	// key named Key to exist with a value of Value.
	MetadataMatcherRowPredicate struct{ Key, Value string }

// MetadataFilterRowPredicate 无法参与页级裁剪，仅用于无法用基础谓词表达的条件。
	// A MetadataFilterRowPredicate is a RowPredicate that requires that metadata
	// with the provided key pass a Keep function.
	//
	// The key is provided to the keep function to allow the same function to be
	// used for multiple filter predicates.
	//
	// Uses of MetadataFilterRowPredicate are not eligible for page filtering and
	// should only be used when a condition cannot be expressed by other basic
	// predicates.
	MetadataFilterRowPredicate struct {
		Key  string
		Keep func(key, value string) bool
	}
)

func (AndRowPredicate) isRowPredicate()              {}
func (OrRowPredicate) isRowPredicate()               {}
func (NotRowPredicate) isRowPredicate()              {}
func (TimeRangeRowPredicate) isRowPredicate()        {}
func (MetadataMatcherRowPredicate) isRowPredicate()  {}
func (MetadataFilterRowPredicate) isRowPredicate()   {}
func (LogMessageFilterRowPredicate) isRowPredicate() {}
// MetadataMatcherRowPredicate 精确匹配指定元数据键值对。
