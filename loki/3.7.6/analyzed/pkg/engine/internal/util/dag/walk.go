package dag

// Walk 对 DAG 做深度优先遍历，支持前序与后序访问顺序。

import "errors"

// WalkOrder 控制访问当前节点与子节点的先后次序。
// WalkOrder defined the order in which current vertex and its children are
// visited.
type WalkOrder uint8

const (
	// PreOrderWalk processes the current vertex before visiting any of its
	// children.
	PreOrderWalk WalkOrder = iota

	// PostOrderWalk processes the current vertex after visiting all of its
	// children.
	PostOrderWalk
)

// WalkFunc 返回非 nil 错误时立即终止整棵子树遍历。
// WalkFunc is a function that gets invoked when walking a Graph. Walking will
// stop if WalkFunc returns a non-nil error.
type WalkFunc[NodeType Node] func(n NodeType) error

// Walk 从起点沿出边 DFS，不可达节点不会调用回调。
// Walk performs a depth-first walk of outgoing edges for all nodes in start,
// invoking the provided fn for each node. Walk returns the error returned by
// fn.
//
// Nodes unreachable from start will not be passed to fn.
func (g *Graph[NodeType]) Walk(n NodeType, f WalkFunc[NodeType], order WalkOrder) error {
	visited := make(nodeSet[NodeType])
	switch order {
	case PreOrderWalk:
		return g.preOrderWalk(n, f, visited)
	case PostOrderWalk:
		return g.postOrderWalk(n, f, visited)
	default:
		return errors.New("unsupported walk order. must be one of PreOrderWalk and PostOrderWalk")
	}
}

// preOrderWalk 先访问当前节点再递归子节点，visited 防止环上重复访问。
func (g *Graph[NodeType]) preOrderWalk(n NodeType, f WalkFunc[NodeType], visited nodeSet[NodeType]) error {
	if visited.Contains(n) {
		return nil
	}
	visited.Add(n)

	if err := f(n); err != nil {
		return err
	}

	for _, child := range g.children[n] {
		if err := g.preOrderWalk(child, f, visited); err != nil {
			return err
		}
	}
	return nil
}

// postOrderWalk 先遍历全部子节点再处理当前节点，适合自底向上聚合。
func (g *Graph[NodeType]) postOrderWalk(n NodeType, f WalkFunc[NodeType], visited nodeSet[NodeType]) error {
	if visited.Contains(n) {
		return nil
	}
	visited.Add(n)

	for _, child := range g.children[n] {
		if err := g.postOrderWalk(child, f, visited); err != nil {
			return err
		}
	}

	return f(n)
}
// 不支持 WalkOrder 时返回明确错误。
