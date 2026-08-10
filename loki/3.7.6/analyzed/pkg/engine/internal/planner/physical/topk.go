package physical

// TopK 物理节点按 SortBy 列保留前 K 行，不保证输出顺序；逻辑 Sort 也映射为此节点。

import "github.com/oklog/ulid/v2"

// K 为 0 表示全量排序；limitPushdown 可增大 K 以匹配 Limit.Fetch 减少扫描量。
// TopK represents a physical plan node that performs topK operation.
// It ranks rows based on sort expressions and limits the result to the top K rows.
// Implementations may not guarantee the topK rows to be in sorted order.
// SortBy/Ascending/NullsFirst 控制比较语义；parallelPushdown 会复制 TopK 到各并行分支做局部 TopK。
type TopK struct {
	NodeID ulid.ULID

	// SortBy is the column to sort by.
	SortBy     ColumnExpression
	Ascending  bool // Sort lines in ascending order if true.
	NullsFirst bool // When true, considers NULLs < non-NULLs when sorting.
	K          int  // Number of top rows to return.
}

// ID implements the [Node] interface.
// Returns the ULID that uniquely identifies the node in the plan.
func (t *TopK) ID() ulid.ULID { return t.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
func (t *TopK) Clone() Node {
	return &TopK{
		NodeID: ulid.Make(),

		SortBy:     t.SortBy.Clone().(ColumnExpression),
		Ascending:  t.Ascending,
		NullsFirst: t.NullsFirst,
		K:          t.K,
	}
}

// Type implements the [Node] interface.
// Returns the type of the node.
func (*TopK) Type() NodeType {
	return NodeTypeTopK
}
// Clone 深拷贝 SortBy 列表达式，Type 返回 NodeTypeTopK。
