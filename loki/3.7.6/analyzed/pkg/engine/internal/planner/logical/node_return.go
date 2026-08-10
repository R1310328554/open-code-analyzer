package logical

// node_return 定义计划出口 Return：仅实现 Instruction，不产生可引用 SSA 值。

import "fmt"

// Return.Value 指向计划根结果；builder_convert 将其作为 SSA 指令序列最后一条。
// The Return instruction yields a value to return from a plan. Return
// implements [Instruction].
type Return struct {
	b baseNode

	Value Value // The value to return.
}

// String 输出 RETURN 加操作数 SSA 名，Plan.String 对其不再打印「名 =」前缀。
// String returns the disassembled SSA form of r.
func (r *Return) String() string {
	return fmt.Sprintf("RETURN %s", r.Value.Name())
}

// Operands 仅含 Value 指针；Return 无 Referrers 因自身不被其他指令引用。
// Operands appends the operands of r to the provided slice. The pointers may
// be modified to change operands of r.
func (r *Return) Operands(buf []*Value) []*Value {
	return append(buf, &r.Value)
}

func (r *Return) base() *baseNode { return &r.b }
func (r *Return) isInstruction()  {}
// Plan.Value 扫描时匹配首个 Return；多条 Return 时仅第一条生效。
