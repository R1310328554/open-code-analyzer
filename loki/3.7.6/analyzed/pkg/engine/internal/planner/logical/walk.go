package logical

// walk 提供 logical 计划树的深度优先遍历，供优化器与分析器访问各 Instruction 节点。

// walkNode 先调用 visitor(n)，若为 Instruction 则递归访问各非 nil 操作数后再 visitor(nil)。
// walkNode traverses a plan in depth-first order: It starts by calling
// visitor(n). If visitor returns true, walkNode is called recursively for each
// of the non-nil operands of n (if any), followed by a call of visitor(nil).
// visitor 返回 false 时剪枝；末尾 nil 调用标记子树遍历完成，便于后序处理。
func walkNode(n Node, visitor func(n Node) bool) {
	if !visitor(n) {
		return
	}

	instr, ok := n.(Instruction)
	if !ok {
		return
	}

	for _, operandPointer := range instr.Operands(nil) {
		operand := *operandPointer
		if operand != nil {
			walkNode(operand, visitor)
		}
	}

	visitor(nil)
}
// 仅 Instruction 类型节点拥有 Operands，Value 叶子（如 ColumnRef）不再向下展开。
