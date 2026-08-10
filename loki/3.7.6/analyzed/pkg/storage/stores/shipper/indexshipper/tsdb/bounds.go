package tsdb

// bounds 定义 TSDB 时间边界抽象：Bounded 接口、inclusiveBounds 闭区间转换，以及 chunk 与查询范围的重叠判定 Overlap。

import (
	"math"

	"github.com/prometheus/common/model"
)

// TODO(chaudum): Replace with new v1.Interval struct
type Bounded interface {
	Bounds() (model.Time, model.Time)
}

// inclusiveBounds 将 [lower,upper) 转为闭区间 [lower,upper]，上界加一毫秒防溢出。
// InclusiveBounds will ensure the underlying Bounded implementation
// is turned into [lower,upper] inclusivity.
// Generally, we consider bounds to be `[lower,upper)` inclusive
// This helper will account for integer overflow.
// Because model.Time is millisecond-precise, but Loki uses nanosecond precision,
// be careful usage can handle an extra millisecond being added.
func inclusiveBounds(b Bounded) (model.Time, model.Time) {
	lower, upper := b.Bounds()

	if int64(upper) < math.MaxInt64 {
		upper++
	}

	return lower, upper
}

type bounds struct {
	mint, maxt model.Time
}

func newBounds(mint, maxt model.Time) bounds { return bounds{mint: mint, maxt: maxt} }

func (b bounds) Bounds() (model.Time, model.Time) { return b.mint, b.maxt }

// Overlap 判断 chunk/index 闭区间 [from,through] 是否与查询半开区间相交。
// Overlap checks whether the given chunk or index bounds
// overlap with the bounds of a query range.
// chunk/index bounds are defined as [from, through]
// query bounds are defined as [from, through)
func Overlap(chk, qry Bounded) bool {
	chkFrom, chkThrough := chk.Bounds()
	qryFrom, qryThrough := qry.Bounds()

	return chkFrom < qryThrough && chkThrough >= qryFrom
}
// bounds 结构体实现 Bounded，供 headIndexReader 构造查询时间窗口时使用。
