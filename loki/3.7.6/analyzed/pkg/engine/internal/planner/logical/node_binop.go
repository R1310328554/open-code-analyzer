package logical

// node_binop 定义二元运算 BinOp：比较、逻辑与、正则匹配等表达式均建模为此节点。

import (
	"fmt"

	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// BinOp 同时是 Instruction 与 Value，Op 取自 types.BinaryOp 枚举。
// The BinOp instruction yields the result of binary operation Left Op Right.
// BinOp implements both [Instruction] and [Value].
type BinOp struct {
	b baseNode

	Left, Right Value
	Op          types.BinaryOp
}

var (
	_ Value       = (*BinOp)(nil)
	_ Instruction = (*BinOp)(nil)
)

// Name returns an identifier for the BinOp operation.
func (b *BinOp) Name() string { return b.b.Name() }

// String 格式为「Op 左操作数名 右操作数名」，不含自身 SSA 名前缀。
// String returns the disassembled SSA form of the BinOp instruction.
func (b *BinOp) String() string {
	return fmt.Sprintf("%s %s %s", b.Op, b.Left.Name(), b.Right.Name())
}

// Operands 返回 Left/Right 指针，logical_optimize 可原地替换为简化子图根节点。
// Operands appends the operands of b to the provided slice. The pointers may
// be modified to change operands of b.
func (b *BinOp) Operands(buf []*Value) []*Value {
	return append(buf, &b.Left, &b.Right)
}

// Referrers returns a list of instructions that reference the BinOp.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (b *BinOp) Referrers() *[]Instruction { return &b.b.referrers }

func (b *BinOp) base() *baseNode { return &b.b }
func (b *BinOp) isValue()        {}
func (b *BinOp) isInstruction()  {}
// Referrers 列表可变，计划改写时需同步维护引用完整性。
