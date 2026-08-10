package physical

// Limit 物理节点对结果集施加 offset 与 limit，对应 SQL 的 SKIP/FETCH 分页语义。

import "github.com/oklog/ulid/v2"

// Limit 通过 Skip 跳过前若干行、Fetch 限制返回行数，常用于日志查询分页。
// Limit represents a limiting operation in the physical plan that applies
// offset and limit to the result set. The offset specifies how many rows to
// skip before starting to return results, while limit specifies the maximum
// number of rows to return.
// Skip 与 Fetch 均为 uint32，优化器可将 Fetch 下推到 TopK 或扫描节点以减少 IO。
type Limit struct {
	NodeID ulid.ULID

	// Skip specifies how many initial rows should be skipped.
	Skip uint32
	// Fetch specifies how many rows should be returned in total.
	Fetch uint32
}

// ID implements the [Node] interface.
// Returns the ULID that uniquely identifies the node in the plan.
func (l *Limit) ID() ulid.ULID { return l.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
func (l *Limit) Clone() Node {
	return &Limit{
		NodeID: ulid.Make(),

		Skip:  l.Skip,
		Fetch: l.Fetch,
	}
}

// Type implements the [Node] interface.
// Returns the type of the node.
func (*Limit) Type() NodeType {
	return NodeTypeLimit
}
// Clone 保留 Skip/Fetch 并分配新 NodeID，Type 返回 NodeTypeLimit。
