package physical

// batching 在物理计划优化后于根节点外包一层 Batching，控制下游 RecordBatch 的输出批次大小。

import (
	"github.com/oklog/ulid/v2"

	"github.com/grafana/loki/v3/pkg/engine/internal/util/dag"
)

// Batching 节点含 NodeID 与 BatchSize，Type 为 NodeTypeBatching。
// Batching is a plan node that controls how records are grouped into output
// batches. It wraps the root of the plan and is added after optimization.
type Batching struct {
	NodeID    ulid.ULID
	BatchSize int64
}

// ID returns the ULID that uniquely identifies the node in the plan.
func (b *Batching) ID() ulid.ULID { return b.NodeID }

// Type returns [NodeTypeBatching].
func (*Batching) Type() NodeType { return NodeTypeBatching }

// Clone returns a deep copy of the node with a new unique ID.
func (b *Batching) Clone() Node {
	return &Batching{NodeID: ulid.Make(), BatchSize: b.BatchSize}
}

// WrapWithBatching inserts a [Batching] node as the new root of plan, with
// the existing root as its only child. It modifies plan in-place and returns it.
// WrapWithBatching 将原根作为 Batching 唯一子节点，原地修改 DAG 并返回 plan。
func WrapWithBatching(plan *Plan, batchSize int) (*Plan, error) {
	root, err := plan.Root()
	if err != nil {
		return nil, err
	}
	node := &Batching{NodeID: ulid.Make(), BatchSize: int64(batchSize)}
	plan.graph.Add(node)
	return plan, plan.graph.AddEdge(dag.Edge[Node]{Parent: node, Child: root})
}
// Clone 生成新 ULID 的深拷贝副本，供物理计划变换时复制子图而不共享节点 ID。
