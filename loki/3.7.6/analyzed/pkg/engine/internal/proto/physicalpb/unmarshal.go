package physicalpb

// unmarshal 将 physical.Plan DAG 图序列化为 physicalpb.Plan protobuf 消息。

import (
	"github.com/grafana/loki/v3/pkg/engine/internal/planner/physical"
	"github.com/grafana/loki/v3/pkg/engine/internal/proto/ulid"
)

// UnmarshalPhysical reads from into p. Returns an error if the conversion fails
// or is unsupported.
// UnmarshalPhysical 遍历图节点写入 Nodes，再按 parent→child 关系生成 Edges。
func (p *Plan) UnmarshalPhysical(from *physical.Plan) error {
	graph := from.Graph()

	*p = Plan{
		Nodes: make([]*Node, 0, graph.Len()),
		Edges: make([]*PlanEdge, 0),
	}

	for node := range graph.Nodes() {
		protoNode := &Node{}
		if err := protoNode.UnmarshalPhysical(node); err != nil {
			return err
		}
		p.Nodes = append(p.Nodes, protoNode)
	}

	for node := range graph.Nodes() {
		for _, child := range graph.Children(node) {
			edge := &PlanEdge{
				Parent: NodeID{Value: ulid.ULID(node.ID())},
				Child:  NodeID{Value: ulid.ULID(child.ID())},
			}
			p.Edges = append(p.Edges, edge)
		}
	}

	return nil
}
// 每条边 Parent/Child 使用 ulid.ULID 包装，与 Node.Id 字段保持一致。
