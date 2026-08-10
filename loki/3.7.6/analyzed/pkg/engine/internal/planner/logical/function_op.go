package logical

// function_op 定义变参函数调用节点 FunctionOp，同时实现 Instruction 与 Value。

import (
	"fmt"
	"strings"

	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// FunctionOp 对 Values 应用 types.VariadicOp（如 parse/json），结果可被后续引用。
// The FunctionOp instruction yields the result of function operation Op Value.
// UnaryOp implements both [Instruction] and [Value].
type FunctionOp struct {
	b baseNode

	Op     types.VariadicOp
	Values []Value
}

var (
	_ Value       = (*FunctionOp)(nil)
	_ Instruction = (*FunctionOp)(nil)
)

// Name returns an identifier for the UnaryOp operation.
func (u *FunctionOp) Name() string { return u.b.Name() }

// String 输出 Op(arg1, arg2, …) 形式，便于与 Plan.String 对齐阅读。
// String returns the disassembled SSA form of the FunctionOp instruction.
func (u *FunctionOp) String() string {
	values := make([]string, len(u.Values))
	for i, v := range u.Values {
		values[i] = v.String()
	}
	return fmt.Sprintf("%s(%s)", u.Op, strings.Join(values, ", "))
}

// Operands 返回 Values 各元素的指针切片，优化器可原地替换操作数。
// Operands appends the operands of u to the provided slice. The pointers may
// be modified to change operands of u.
func (u *FunctionOp) Operands(buf []*Value) []*Value {
	for i := range u.Values {
		buf = append(buf, &u.Values[i])
	}
	return buf
}

// Referrers returns a list of instructions that reference the FunctionOp.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (u *FunctionOp) Referrers() *[]Instruction { return &u.b.referrers }

func (u *FunctionOp) base() *baseNode { return &u.b }
func (u *FunctionOp) isValue()        {}
func (u *FunctionOp) isInstruction()  {}
// Name 委托 baseNode；isValue/isInstruction 为包内类型约束标记方法。
