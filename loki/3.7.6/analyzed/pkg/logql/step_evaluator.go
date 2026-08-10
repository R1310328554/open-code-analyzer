package logql

// step_evaluator 定义 LogQL 按步求值接口：Next 逐步推进时间戳并返回 SampleVector、分位数草图向量或 Count-Min 向量等 StepResult 变体。

import (
	"github.com/prometheus/prometheus/promql"
)

type StepResult interface {
	SampleVector() promql.Vector
	QuantileSketchVec() ProbabilisticQuantileVector
	CountMinSketchVec() CountMinSketchVector
}

// SampleVector 是 promql.Vector 的类型别名，作为最常见的 StepResult 实现。
type SampleVector promql.Vector

var _ StepResult = SampleVector{}

func (p SampleVector) SampleVector() promql.Vector {
	return promql.Vector(p)
}

func (p SampleVector) QuantileSketchVec() ProbabilisticQuantileVector {
	return ProbabilisticQuantileVector{}
}

func (SampleVector) CountMinSketchVec() CountMinSketchVector {
	return CountMinSketchVector{}
}

// StepEvaluator 逐步执行查询计划；Next 仅接受 Scalar 或 Vector 作为 promql.Value。
// StepEvaluator evaluate a single step of a query.
type StepEvaluator interface {
	// while Next returns a promql.Value, the only acceptable types are Scalar and Vector.
	Next() (ok bool, ts int64, r StepResult)
	// Close all resources used.
	Close() error
	// Reports any error
	Error() error
	// Explain returns a print of the step evaluation tree
	Explain(Node)
}

// EmptyEvaluator 为无数据占位求值器，Next 立即返回 false 并携带零值 StepResult。
type EmptyEvaluator[R StepResult] struct {
	value R
}

var _ StepEvaluator = EmptyEvaluator[SampleVector]{}

// Close implements StepEvaluator.
func (EmptyEvaluator[_]) Close() error { return nil }

// Error implements StepEvaluator.
func (EmptyEvaluator[_]) Error() error { return nil }

// Next implements StepEvaluator.
func (e EmptyEvaluator[_]) Next() (ok bool, ts int64, r StepResult) {
	return false, 0, e.value
}
// Explain 接受计划节点用于打印逐步求值树，便于调试与 EXPLAIN 输出。
