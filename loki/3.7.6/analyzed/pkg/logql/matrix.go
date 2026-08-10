package logql

// matrix 将 promql.Matrix 适配为 StepEvaluator，按查询参数的 step 逐步产出 Vector。

import (
	"time"

	"github.com/prometheus/prometheus/promql"
)

// MatrixStepEvaluator 按 params 的 start/end/step 推进时间，而非依赖 Matrix 自身步长，便于空结果合并。
// MatrixStepEvaluator exposes a promql.Matrix as a StepEvaluator.
// Ensure that the resulting StepEvaluator maintains
// the same shape that the parameters expect. For example,
// it's possible that a downstream query returns matches no
// log streams and thus returns an empty matrix.
// However, we still need to ensure that it can be merged effectively
// with another leg that may match series.
// Therefore, we determine our steps from the parameters
// and not the underlying Matrix.
type MatrixStepEvaluator struct {
	start, end, ts time.Time
	step           time.Duration
	m              promql.Matrix
}

// NewMatrixStepEvaluator 初始化游标；首次 Next 会将 ts 校正到 start。
func NewMatrixStepEvaluator(start, end time.Time, step time.Duration, m promql.Matrix) *MatrixStepEvaluator {
	return &MatrixStepEvaluator{
		start: start,
		end:   end,
		ts:    start.Add(-step), // will be corrected on first Next() call
		step:  step,
		m:     m,
	}
}

// Next 推进一个 step，收集各序列在当前毫秒时间戳的首个浮点样本并消费该点。
func (m *MatrixStepEvaluator) Next() (bool, int64, StepResult) {
	m.ts = m.ts.Add(m.step)
	if m.ts.After(m.end) {
		return false, 0, nil
	}

	ts := m.ts.UnixNano() / int64(time.Millisecond)
	vec := make(promql.Vector, 0, len(m.m))

	for i, series := range m.m {
		ln := len(series.Floats)

		if ln == 0 || series.Floats[0].T != ts {
			continue
		}

		vec = append(vec, promql.Sample{
			Metric: series.Metric,
			T:      series.Floats[0].T,
			F:      series.Floats[0].F,
		})
		m.m[i].Floats = m.m[i].Floats[1:]
	}

	return true, ts, SampleVector(vec)
}

func (m *MatrixStepEvaluator) Close() error { return nil }

func (m *MatrixStepEvaluator) Error() error { return nil }
// Close/Error 为空实现；Matrix 由调用方持有，不在此释放。
