package logical

// UnaryOp 表示一元运算节点，对单个 Value 施加 NOT、类型转换等 unary 操作。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// UnaryOp 同时作为 Instruction 与 Value，可嵌套在 BinOp 或 Select 谓词中。
// The UnaryOp instruction yields the result of unary operation Op Value.
// UnaryOp implements both [Instruction] and [Value].
type UnaryOp struct {
	b baseNode

	Op    types.UnaryOp
	Value Value
}

var (
	_ Value       = (*UnaryOp)(nil)
	_ Instruction = (*UnaryOp)(nil)
)

// Name returns an identifier for the UnaryOp operation.
func (u *UnaryOp) Name() string { return u.b.Name() }

// String 格式为 Op(子节点名)，便于在计划树中识别取反或 cast 步骤。
// String returns the disassembled SSA form of the UnaryOp instruction.
func (u *UnaryOp) String() string {
	return fmt.Sprintf("%s(%s)", u.Op, u.Value.Name())
}

// Operands appends the operands of u to the provided slice. The pointers may
// be modified to change operands of u.
func (u *UnaryOp) Operands(buf []*Value) []*Value {
	return append(buf, &u.Value)
}

// Referrers returns a list of instructions that reference the UnaryOp.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (u *UnaryOp) Referrers() *[]Instruction { return &u.b.referrers }

func (u *UnaryOp) base() *baseNode { return &u.b }
func (u *UnaryOp) isValue()        {}
func (u *UnaryOp) isInstruction()  {}
// 删除谓词构建中常用 UnaryOpNot 对 selector 或 filters 取反以保留不匹配行。
