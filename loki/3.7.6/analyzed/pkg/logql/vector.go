package logql

// vector 提供 promql.Vector 上的堆排序别名与 VectorStepEvaluator：用于 topk/bottomk 等按样本值排序及瞬时向量一步求值。

import (
	"math"
	"time"

	"github.com/prometheus/prometheus/promql"
)

type vectorByValueHeap promql.Vector

func (s vectorByValueHeap) Len() int {
	return len(s)
}

// Less 定义 min-heap 序：NaN 在前，否则按 F 升序。
func (s vectorByValueHeap) Less(i, j int) bool {
	if math.IsNaN(s[i].F) {
		return true
	}
	return s[i].F < s[j].F
}

func (s vectorByValueHeap) Swap(i, j int) {
	s[i], s[j] = s[j], s[i]
}

func (s *vectorByValueHeap) Push(x interface{}) {
	*s = append(*s, *(x.(*promql.Sample)))
}

func (s *vectorByValueHeap) Pop() interface{} {
	old := *s
	n := len(old)
	el := old[n-1]
	*s = old[0 : n-1]
	return el
}

// vectorByReverseValueHeap 按样本值降序排列，供取最大 topk 场景。
type vectorByReverseValueHeap promql.Vector

func (s vectorByReverseValueHeap) Len() int {
	return len(s)
}

func (s vectorByReverseValueHeap) Less(i, j int) bool {
	if math.IsNaN(s[i].F) {
		return true
	}
	return s[i].F > s[j].F
}

func (s vectorByReverseValueHeap) Swap(i, j int) {
	s[i], s[j] = s[j], s[i]
}

func (s *vectorByReverseValueHeap) Push(x interface{}) {
	*s = append(*s, *(x.(*promql.Sample)))
}

func (s *vectorByReverseValueHeap) Pop() interface{} {
	old := *s
	n := len(old)
	el := old[n-1]
	*s = old[0 : n-1]
	return el
}

// VectorStepEvaluator 将静态 promql.Vector 包装为仅返回一步的 StepEvaluator。
type VectorStepEvaluator struct {
	exhausted bool
	start     time.Time
	data      promql.Vector
}

func NewVectorStepEvaluator(start time.Time, data promql.Vector) *VectorStepEvaluator {
	return &VectorStepEvaluator{
		exhausted: false,
		start:     start,
		data:      data,
	}
}

// Next 第一次返回 start 毫秒时间戳与 SampleVector，之后返回 exhausted。
func (e *VectorStepEvaluator) Next() (bool, int64, StepResult) {
	if !e.exhausted {
		e.exhausted = true
		return true, e.start.UnixNano() / int64(time.Millisecond), SampleVector(e.data)
	}
	return false, 0, nil
}

func (e *VectorStepEvaluator) Close() error {
	return nil
}

func (e *VectorStepEvaluator) Error() error {
	return nil
}
// Push/Pop 实现 container/heap 接口，对 promql.Sample 指针解引用后追加/弹出。
