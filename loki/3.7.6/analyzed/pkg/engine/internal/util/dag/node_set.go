package dag

// nodeSet 是 DAG 内部使用的节点集合，基于 map 实现 O(1) 增删查。

// nodeSet 键为节点值、值为空 struct，迭代顺序不保证。
// nodeSet is a set of nodes in an arbitrary order.
type nodeSet[N Node] map[N]struct{}

// Add 忽略零值节点，避免将无效顶点加入图。
// Add adds the given node to the set. If node is the zero value, Add is a
// no-op.
func (s nodeSet[N]) Add(node N) {
	if isZero(node) {
		return
	}
	s[node] = struct{}{}
}

// Remove 对不存在节点为 no-op。
// Remove removes the given node from the set. If node is not in the set, Remove
// is a no-op.
func (s nodeSet[N]) Remove(node N) { delete(s, node) }

// Contains returns true if the given node is in the set.
func (s nodeSet[N]) Contains(node N) bool {
	_, ok := s[node]
	return ok
}
// Contains 用于 AddEdge 与 Walk 中的成员检测。
