package physical

// filter 物理节点在运行时对输入列求值 Predicates，仅保留全部条件 AND 为真的行。

import "github.com/oklog/ulid/v2"

// Filter 持有 Expression 谓词列表，执行阶段逐条求值并丢弃不匹配记录。
// Filter represents a filtering operation in the physical plan.
// It contains a list of predicates (conditional expressions) that are later
// evaluated against the input columns and produce a result that only contains
// rows that match the given conditions. The list of expressions are chained
// with a logical AND.
// Predicates 在逻辑计划 Select 下推或优化合并后映射到此物理 Filter 节点。
type Filter struct {
	NodeID ulid.ULID

	// Predicates is a list of filter expressions that are used to discard not
	// matching rows during execution.
	Predicates []Expression
}

// ID implements the [Node] interface.
// Returns the ULID that uniquely identifies the node in the plan.
func (f *Filter) ID() ulid.ULID { return f.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
func (f *Filter) Clone() Node {
	return &Filter{
		NodeID: ulid.Make(),

		Predicates: cloneExpressions(f.Predicates),
	}
}

// Type implements the [Node] interface.
// Returns the type of the node.
func (*Filter) Type() NodeType {
	return NodeTypeFilter
}
// Clone 复制谓词表达式并生成新 NodeID，Type 返回 NodeTypeFilter。
