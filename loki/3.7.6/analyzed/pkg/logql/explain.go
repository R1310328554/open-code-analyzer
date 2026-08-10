package logql

// explain 为各类 StepEvaluator 实现 Explain 方法，构建可读查询计划树；MaxChildrenDisplay 限制子节点展示数量以防输出爆炸。

// MaxChildrenDisplay 控制 explain 树最多展示的直接子节点数，超出则折叠为 ...。
// MaxChildrenDisplay defines the maximum number of children that should be
// shown by explain.
const MaxChildrenDisplay = 3

func (e *LiteralStepEvaluator) Explain(parent Node) {
	b := parent.Child("Literal")
	e.nextEv.Explain(b)
}

func (e *LabelReplaceEvaluator) Explain(parent Node) {
	b := parent.Childf("%s LabelReplace", e.expr.Replacement)
	e.nextEvaluator.Explain(b)
}

func (e *VectorAggEvaluator) Explain(parent Node) {
	b := parent.Childf("[%s, %s] VectorAgg", e.expr.Operation, e.expr.Grouping)
	e.nextEvaluator.Explain(b)
}

func (m *MatrixStepEvaluator) Explain(parent Node) {
	parent.Child("MatrixStep")
}

func (e *VectorStepEvaluator) Explain(parent Node) {
	parent.Child("VectorStep")
}

// ConcatStepEvaluator 在子求值器过多时仅展示首尾与省略号。
func (e *ConcatStepEvaluator) Explain(parent Node) {
	b := parent.Child("Concat")
	if len(e.evaluators) < MaxChildrenDisplay {
		for _, child := range e.evaluators {
			child.Explain(b)
		}
	} else {
		e.evaluators[0].Explain(b)
		b.Child("...")
		e.evaluators[len(e.evaluators)-1].Explain(b)
	}
}

func (r *RangeVectorEvaluator) Explain(parent Node) {
	parent.Child("RangeVectorAgg")
}

func (e *AbsentRangeVectorEvaluator) Explain(parent Node) {
	parent.Child("Absent RangeVectorAgg")
}

// BinOpStepEvaluator 为二元运算节点分别 explain 左右子表达式。
func (e *BinOpStepEvaluator) Explain(parent Node) {
	b := parent.Childf("%s BinOp", e.expr.Op)
	e.lse.Explain(b)
	e.rse.Explain(b)
}

func (i *VectorIterator) Explain(parent Node) {
	parent.Childf("%f vectorIterator", i.val)
}

func (e *QuantileSketchVectorStepEvaluator) Explain(parent Node) {
	b := parent.Child("QuantileSketchVector")
	e.inner.Explain(b)
}

func (e *mergeOverTimeStepEvaluator) Explain(parent Node) {
	parent.Child("MergeFirstOverTime")
}

// CountMinSketchVectorStepEvaluator 在计划树中标记 CMS 向量求值步骤。
func (e *CountMinSketchVectorStepEvaluator) Explain(parent Node) {
	parent.Child("CountMinSketchVector")
}

func (EmptyEvaluator[SampleVector]) Explain(parent Node) {
	parent.Child("Empty")
}
// 各 Explain 实现递归向下遍历子 Evaluator，形成与执行顺序一致的 DAG 视图。
