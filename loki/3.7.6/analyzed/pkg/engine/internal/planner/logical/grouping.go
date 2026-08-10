package logical

// grouping 描述向量/区间聚合的分组语义：按列 group by 或 group without。

// Grouping.Columns 为分组键列引用；Without 为 true 表示排除所列标签后对其余列分组。
// Grouping represents the grouping by/without label(s) for vector aggregators and range vector aggregators.
type Grouping struct {
	Columns []ColumnRef // The columns for grouping
	Without bool        // The grouping mode
}

// NoGrouping 等价于 without 模式且无显式列，表示不按标签细分序列。
var (
	NoGrouping = Grouping{Without: true}
)
// 与 LogQL 的 by()/without() 子句对应，物理层再映射为 Arrow 分组列。
