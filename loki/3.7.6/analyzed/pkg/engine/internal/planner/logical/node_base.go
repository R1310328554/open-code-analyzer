package logical

// node_base 为所有逻辑节点嵌入 baseNode，统一 SSA 标识与引用者列表管理。

import "fmt"

// baseNode.referrers 仅对实现 Value 的节点填充；id 为空时 Name 回退为指针地址。
// baseNode holds common logic for all nodes.
type baseNode struct {
	// referrers of a node. Only populated for nodes that implement [Value].
	referrers []Instruction
	id        string // ID used for instructions
}

// Name 供 Plan.String 与 format_tree 显示 %%N 或调试地址。
// Name returns the id of the node, used when printing instructions.
func (n *baseNode) Name() string {
	if n.id != "" {
		return n.id
	}
	return fmt.Sprintf("%p", n)
}

// addReferrer 在 buildReferrers 与优化替换操作数后登记双向引用边。
func (n *baseNode) addReferrer(instr Instruction) {
	n.referrers = append(n.referrers, instr)
}
// 各具体节点通过嵌入 baseNode 并实现 base() 满足 Node 接口。
