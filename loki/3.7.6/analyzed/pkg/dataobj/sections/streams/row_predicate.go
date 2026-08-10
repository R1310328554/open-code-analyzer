package streams

// row_predicate 定义 streams 行级过滤表达式，供 RowReader 下推至 dataset 层。

import (
	"time"
)

type (
// RowPredicate 是过滤 data object 行的表达式接口，由具体谓词类型实现。
	// RowPredicate is an expression used to filter rows in a data object.
	RowPredicate interface{ isRowPredicate() }
)

// Supported predicates.
type (
// AndRowPredicate 要求左右子谓词同时为真。
	// An AndRowPredicate is a RowPredicate which requires both its Left and
	// Right
	// predicate to be true.
	AndRowPredicate struct{ Left, Right RowPredicate }

// OrRowPredicate 要求左右子谓词至少一个为真。
	// An OrRowPredicate is a RowPredicate which requires either its Left or
	// Right predicate to be true.
	OrRowPredicate struct{ Left, Right RowPredicate }

	// A NotRowPredicate is a RowPredicate which requires its Inner predicate to be
	// false.
	NotRowPredicate struct{ Inner RowPredicate }

// TimeRangeRowPredicate 按流的 min/max 时间戳与查询区间是否重叠过滤。
	// A TimeRangeRowPredicate is a RowPredicate which requires the timestamp of
	// the entry to be within the range of StartTime and EndTime.
	TimeRangeRowPredicate struct {
		StartTime, EndTime time.Time
		IncludeStart       bool // Whether StartTime is inclusive.
		IncludeEnd         bool // Whether EndTime is inclusive.
	}

// LabelMatcherRowPredicate 要求指定名称的标签值精确匹配。
	// A LabelMatcherRowPredicate is a RowPredicate which requires a label named
	// Name to exist with a value of Value.
	LabelMatcherRowPredicate struct{ Name, Value string }

// LabelFilterRowPredicate 用自定义 Keep 函数过滤标签，不支持页级下推。
	// A LabelFilterRowPredicate is a RowPredicate that requires that labels with
	// the provided name pass a Keep function.
	//
	// The name is provided to the keep function to allow the same function to
	// be used for multiple filter predicates.
	//
	// Uses of LabelFilterRowPredicate are not eligible for page filtering and
	// should only be used when a condition cannot be expressed by other basic
	// predicates.
	LabelFilterRowPredicate struct {
		Name string
		Keep func(name, value string) bool
	}
)

func (AndRowPredicate) isRowPredicate()          {}
func (OrRowPredicate) isRowPredicate()           {}
func (NotRowPredicate) isRowPredicate()          {}
func (TimeRangeRowPredicate) isRowPredicate()    {}
func (LabelMatcherRowPredicate) isRowPredicate() {}
func (LabelFilterRowPredicate) isRowPredicate()  {}
// RowReader 通过 translateStreamsPredicate 将行谓词翻译为 dataset.Predicate。
