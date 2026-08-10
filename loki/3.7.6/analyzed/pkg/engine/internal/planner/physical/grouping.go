package physical

// grouping 描述向量聚合与区间聚合的分组列及 by/without 模式，对应 PromQL 分组语义。

// Columns 为参与分组的 ColumnExpression 列表，Without 为 true 时表示 group without 语法。
// Grouping represents the grouping by/without label(s) for vector aggregators and range vector aggregators.
type Grouping struct {
	Columns []ColumnExpression // The columns for grouping
	Without bool               // The grouping mode
}
// 空 Columns 且 Without 为 true 时等价于无分组标签的全局聚合边界情况。
