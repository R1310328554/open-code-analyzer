package logical

// builder_referrers 遍历 Plan 指令图，为每个 Value 节点登记引用它的 Instruction 列表。

// buildReferrers 供优化器通过 Value.Referrers 做替换、裁剪或投影下推时使用。
// buildReferrers traverses instrs and stores referrers to values, such that
// [Value.Referrers] produces the correct result.
func buildReferrers(instrs ...Instruction) {
	seen := make(map[Instruction]struct{})
	var operandsBuf []*Value

	for _, check := range instrs {
		walkNode(check, func(n Node) bool {
			instr, ok := n.(Instruction)
			if n == nil || !ok {
				return true
			}

			// Don't process the same instruction more than once.
			if _, ok := seen[instr]; ok {
				return true
			}
			seen[instr] = struct{}{}

			operandsBuf = instr.Operands(operandsBuf[:0])

			for _, operandPointer := range operandsBuf {
				operand := *operandPointer
				if operand == nil {
					continue
				}
				operand.base().addReferrer(instr)
			}

			return true
		})
	}
}
// walkNode 深度优先访问各 Instruction 的操作数，seen 防止同一指令重复登记引用。
