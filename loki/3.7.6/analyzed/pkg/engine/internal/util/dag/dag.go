// Package dag 提供有向无环图（DAG）工具，用于表达查询/执行计划的节点与边关系。
// Package dag provides utilities for working with directed acyclic graphs
// (DAGs).
package dag

import (
	"errors"
	"fmt"
	"iter"
	"maps"
	"slices"

	"github.com/oklog/ulid/v2"
)

// Node 为图中顶点接口，零值表示空节点；需实现 comparable 与 ID()。
// Node represents an individual node in a Graph. The zero value of Node is
// reserved to indicate a nil node.
type Node interface {
	comparable

	ID() ulid.ULID
}

// Edge 描述父节点到子节点的有向边，AddEdge 时按添加顺序保留。
// Edge is a directed connection (parent-child relation) between two nodes.
type Edge[NodeType Node] struct {
	Parent, Child NodeType
}

// Graph 维护节点集合及双向 parent/children 邻接表，支持增删与拓扑变换。
// Graph is a directed acyclic graph (DAG).
type Graph[NodeType Node] struct {
	// nodes is a set containing all nodes in the plan.
	nodes nodeSet[NodeType]
	// parents maps each node to its parent nodes in the execution graph.
	parents map[NodeType][]NodeType
	// children maps each node to a set of its child nodes in the execution
	// graph.
	children map[NodeType][]NodeType
}

func (g *Graph[NodeType]) init() {
	if g.nodes == nil {
		g.nodes = make(nodeSet[NodeType])
	}
	if g.parents == nil {
		g.parents = make(map[NodeType][]NodeType)
	}
	if g.children == nil {
		g.children = make(map[NodeType][]NodeType)
	}
}

// Add 幂等插入节点，零值节点被忽略。
// Add adds a new node n to the graph if it doesn't already exist. For
// convenience, Add returns the input node without modification.
//
// Add is a no-op if n is the zero value.
func (g *Graph[NodeType]) Add(n NodeType) NodeType {
	g.init()
	if g.nodes.Contains(n) || isZero(n) {
		return n
	}
	g.nodes.Add(n)

	return n
}

func isZero[NodeType Node](n NodeType) bool {
	return n == zeroValue[NodeType]()
}

func zeroValue[NodeType Node]() NodeType {
	var zero NodeType
	return zero
}

// AddEdge 建立父子关系，两端节点须已存在且非零值。
// AddEdge creates a directed edge between two nodes in the graph, establishing
// a parent-child relationship between the nodes where e.Parent becomes a parent
// of e.Child.
//
// AddEdge returns an error if:
//
// * Either node is the zero value, or
// * either node doesn't exist in the graph.
//
// AddEdge preserves the order of addition of edges.
func (g *Graph[NodeType]) AddEdge(e Edge[NodeType]) error {
	if isZero(e.Parent) || isZero(e.Child) {
		return fmt.Errorf("parent and child nodes must not be zero values")
	}
	if !g.nodes.Contains(e.Parent) {
		return fmt.Errorf("parent node %s does not exist in graph", e.Parent.ID())
	}
	if !g.nodes.Contains(e.Child) {
		return fmt.Errorf("child node %s does not exist in graph", e.Child.ID())
	}

	// Uniquely add the edges.
	if !slices.Contains(g.children[e.Parent], e.Child) {
		g.children[e.Parent] = append(g.children[e.Parent], e.Child)
	}
	if !slices.Contains(g.parents[e.Child], e.Parent) {
		g.parents[e.Child] = append(g.parents[e.Child], e.Parent)
	}

	return nil
}

// Eliminate 删除中间节点并将父节点直接连到其子节点，保持图连通。
// Eliminate removes the node n from the graph and reconnects n's parents to
// n's children, maintaining connectivity across the graph.
//
// If n does not have a parent, all of its children will be promoted to root
// nodes.
func (g *Graph[NodeType]) Eliminate(n NodeType) {
	// For each parent p in the node to eliminate, push up n's children to
	// become children of p, and remove n as a child of p.
	parents := g.Parents(n)
	for _, parent := range parents {
		g.children[parent] = slices.DeleteFunc(g.children[parent], func(check NodeType) bool { return check == n })

		for _, child := range g.children[n] {
			if slices.Contains(g.children[parent], child) {
				// The new child was already a child a parent.
				continue
			}
			g.children[parent] = append(g.children[parent], child)
		}
	}

	// For each child c of n, push down n's parents to become parents of c.
	for _, child := range g.Children(n) {
		g.parents[child] = slices.DeleteFunc(g.parents[child], func(check NodeType) bool { return check == n })

		for _, newParent := range parents {
			if slices.Contains(g.parents[child], newParent) {
				continue
			}
			g.parents[child] = append(g.parents[child], newParent)
		}
	}

	delete(g.parents, n)
	delete(g.children, n)
	g.nodes.Remove(n)
}

// Inject 在 parent 与其子节点之间插入新节点，用于计划重写。
// Inject injects a new node between a parent and its children:
//
// * The children of parent become children of node.
// * The child of parent becomes node.
//
// Inject panics if given a node that already exists in the plan.
//
// For convenience, Inject returns node without modification.
func (g *Graph[NodeType]) Inject(parent, node NodeType) NodeType {
	if g.nodes.Contains(node) {
		panic("injectNode: target node already exists in plan")
	}
	g.Add(node)

	// Update parent's children so that their parent is node.
	g.children[node] = g.children[parent]
	for _, child := range g.children[node] {
		g.parents[child] = slices.DeleteFunc(g.parents[child], func(check NodeType) bool { return check == parent })

		// We don't have to check for the presence of node because we guarantee
		// that it doesn't already exist in the graph with any edges.
		g.parents[child] = append(g.parents[child], node)
	}

	// Add an edge between parent and node.
	g.parents[node] = []NodeType{parent}
	g.children[parent] = []NodeType{node}
	return node
}

// Len returns the number of nodes in the graph.
func (g *Graph[NodeType]) Len() int {
	return len(g.nodes)
}

// Nodes returns all nodes in the graph in an unspecified order.
func (g *Graph[NodeType]) Nodes() iter.Seq[NodeType] {
	return func(yield func(NodeType) bool) {
		for node := range g.nodes {
			if !yield(node) {
				return
			}
		}
	}
}

// Parent returns the parent of the given node.
func (g *Graph[NodeType]) Parents(n NodeType) []NodeType {
	if _, ok := g.parents[n]; !ok {
		return nil
	}
	return g.parents[n]
}

// Children returns all child nodes of the given node.
func (g *Graph[NodeType]) Children(n NodeType) []NodeType {
	if _, ok := g.children[n]; !ok {
		return nil
	}
	return g.children[n]
}

// Roots 返回所有入度为零的根节点，可能有多棵子树。
// Roots returns all nodes that have no parents.
func (g *Graph[NodeType]) Roots() []NodeType {
	if len(g.nodes) == 0 {
		return nil
	}

	var roots []NodeType
	for node := range g.nodes {
		if len(g.parents[node]) == 0 {
			roots = append(roots, node)
		}
	}
	return roots
}

// Root 要求唯一根；无根或多根时返回错误。
// Root returns the root node that have no parents. It returns an error if the
// plan has no or multiple root nodes.
func (g *Graph[NodeType]) Root() (NodeType, error) {
	roots := g.Roots()
	if len(roots) == 0 {
		return zeroValue[NodeType](), errors.New("plan has no root node")
	} else if len(roots) > 1 {
		return zeroValue[NodeType](), errors.New("plan has multiple root nodes")
	}
	return roots[0], nil
}

// Leaves returns all nodes that have no children.
func (g *Graph[NodeType]) Leaves() []NodeType {
	if len(g.nodes) == 0 {
		return nil
	}

	var leaves []NodeType
	for node := range g.nodes {
		if len(g.children[node]) == 0 {
			leaves = append(leaves, node)
		}
	}
	return leaves
}

// Clone 浅拷贝图结构，children 切片单独克隆以避免共享底层数组。
// Clone returns a shallow clone of the graph: nodes in the graph are
// transferred using ordinary assignment.
func (g *Graph[NodeType]) Clone() *Graph[NodeType] {
	// We want to copy the children slice so we can't just use [maps.Clone] for
	// g.children.
	newChildren := make(map[NodeType][]NodeType, len(g.children))
	for node, children := range g.children {
		newChildren[node] = slices.Clone(children)
	}

	return &Graph[NodeType]{
		nodes:    maps.Clone(g.nodes),
		parents:  maps.Clone(g.parents),
		children: newChildren,
	}
}
// Leaves 返回无出边的叶节点，常用于计划末端算子。
