package astmapper

// astmapper 包 subtree_folder 将尚未嵌入子查询的 AST 子树折叠为单个 embedded query，供 frontend 打包并行化边界。

import (
	"github.com/prometheus/prometheus/promql/parser"
)

/*
subtreeFolder is a NodeMapper which embeds an entire parser.Node in an embedded query
if it does not contain any previously embedded queries. This allows the frontend to "zip up" entire
subtrees of an AST that have not already been parallelized.
*/
type subtreeFolder struct{}

// NewSubtreeFolder 返回 ASTMapper，无 embedded 时整棵子树 VectorSquasher 嵌入。
// NewSubtreeFolder creates a subtreeFolder which can reduce an AST
// to one embedded query if it contains no embedded queries yet
func NewSubtreeFolder() ASTMapper {
	return NewASTNodeMapper(&subtreeFolder{})
}

// MapNode 跳过字面量；含 embedded 则不改，否则 Predicate+VectorSquasher 折叠。
// MapNode implements NodeMapper
func (f *subtreeFolder) MapNode(node parser.Node) (parser.Node, bool, error) {
	switch n := node.(type) {
	// do not attempt to fold number or string leaf nodes
	case *parser.NumberLiteral, *parser.StringLiteral:
		return n, true, nil
	}

	containsEmbedded, err := Predicate(node, isEmbedded)
	if err != nil {
		return nil, true, err
	}

	if containsEmbedded {
		return node, false, nil
	}

	expr, err := VectorSquasher(node)
	return expr, true, err
}

// isEmbedded 检测 VectorSelector 是否引用 EmbeddedQueriesMetricName 嵌入查询。
func isEmbedded(node parser.Node) (bool, error) {
	switch n := node.(type) {
	case *parser.VectorSelector:
		if n.Name == EmbeddedQueriesMetricName {
			return true, nil
		}

	case *parser.MatrixSelector:
		return isEmbedded(n.VectorSelector)
	}
	return false, nil
}

type predicate = func(parser.Node) (bool, error)

// Predicate 用 visitor 遍历子树，任一节点满足 predicate 即返回 true。
// Predicate is a helper which uses parser.Walk under the hood determine if any node in a subtree
// returns true for a specified function
func Predicate(node parser.Node, fn predicate) (bool, error) {
	v := &visitor{
		fn: fn,
	}

	if err := parser.Walk(v, node, nil); err != nil {
		return false, err
	}
	return v.result, nil
}

type visitor struct {
	fn     predicate
	result bool
}

// Visit 在首次 predicate 成功或出错时停止向下遍历。
// Visit implements parser.Visitor
func (v *visitor) Visit(node parser.Node, _ []parser.Node) (parser.Visitor, error) {
	// if the visitor has already seen a predicate success, don't overwrite
	if v.result {
		return nil, nil
	}

	var err error

	v.result, err = v.fn(node)
	if err != nil {
		return nil, err
	}
	if v.result {
		return nil, nil
	}
	return v, nil
}
// subtreeFolder 与 parallel/shard summer 配合，避免重复嵌入已并行化片段。
