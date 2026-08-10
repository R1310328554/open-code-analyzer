package physical

// Join 物理节点表示两路输入按 timestamp 做内连接，用于指标数学表达式左右两侧均有扫描时合并时间序列。

import "github.com/oklog/ulid/v2"

// Join 当前仅支持 timestamp 内连接，后续可扩展其他连接类型与连接键。
// Join represents a join operation in the physical plan.
// For now it is only an inner join on `timestamp`. Will be expanded later.
type Join struct {
	NodeID ulid.ULID
}

// ID implements the [Node] interface.
// Returns the ULID that uniquely identifies the node in the plan.
func (f *Join) ID() ulid.ULID { return f.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
func (f *Join) Clone() Node {
	return &Join{
		NodeID: ulid.Make(),
	}
}

// Type implements the [Node] interface.
// Returns the type of the node.
func (*Join) Type() NodeType {
	return NodeTypeJoin
}
// Clone 生成新 ULID 的深拷贝，Type 返回 NodeTypeJoin 供计划遍历识别。
