package vector

// vector 子包提供按样本值排序的最小/最大堆实现，供 LogQL 聚合算子维护 topk/bottomk 候选集。

import (
	"math"

	"github.com/prometheus/prometheus/promql"
)

type HeapByMaxValue promql.Vector

func (s HeapByMaxValue) Len() int {
	return len(s)
}

// NaN 样本在两种堆中均视为边界值，保证 topk 行为与 PromQL 一致。
func (s HeapByMaxValue) Less(i, j int) bool {
	if math.IsNaN(s[i].F) {
		return true
	}
	return s[i].F < s[j].F
}

func (s HeapByMaxValue) Swap(i, j int) {
	s[i], s[j] = s[j], s[i]
}

func (s *HeapByMaxValue) Push(x interface{}) {
	*s = append(*s, *(x.(*promql.Sample)))
}

func (s *HeapByMaxValue) Pop() interface{} {
	old := *s
	n := len(old)
	el := old[n-1]
	*s = old[0 : n-1]
	return el
}

// HeapByMinValue 为降序堆（Less 用 >），Pop 得到当前向量中的最大样本。
type HeapByMinValue promql.Vector

func (s HeapByMinValue) Len() int {
	return len(s)
}

func (s HeapByMinValue) Less(i, j int) bool {
	if math.IsNaN(s[i].F) {
		return true
	}
	return s[i].F > s[j].F
}

func (s HeapByMinValue) Swap(i, j int) {
	s[i], s[j] = s[j], s[i]
}

func (s *HeapByMinValue) Push(x interface{}) {
	*s = append(*s, *(x.(*promql.Sample)))
}

func (s *HeapByMinValue) Pop() interface{} {
	old := *s
	n := len(old)
	el := old[n-1]
	*s = old[0 : n-1]
	return el
}
// HeapByMinValue/HeapByMaxValue 与 pkg/logql/vector.go 中堆类型语义互补，供不同包引用。
