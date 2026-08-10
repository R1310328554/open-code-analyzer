package logical

// node_limit 定义 LIMIT 关系算子：对表关系按 Skip/Fetch 截断行数。

import (
	"fmt"
)

// Limit 包装单输入 Table Value；Fetch 为 0 表示 Skip 之后返回剩余全部行。
// The Limit instruction limits the number of rows from a table relation. Limit
// implements [Instruction] and [Value].
type Limit struct {
	b baseNode

	Table Value // Table relation to limit.

	// Skip 对应 SQL OFFSET；与 TopK 不同，Limit 不保证排序语义。
// Skip is the number of rows to skip before returning results. A value of 0
	// means no rows are skipped.
	Skip uint32

	// Fetch is the maximum number of rows to return. A value of 0 means all rows
	// are returned (after applying Skip).
	Fetch uint32
}

var (
	_ Value       = (*Limit)(nil)
	_ Instruction = (*Limit)(nil)
)

// Name returns an identifier for the Limit operation.
func (l *Limit) Name() string { return l.b.Name() }

// String 打印 LIMIT 及 skip/fetch 参数，便于日志对比物理 Limit pipeline。
// String returns the disassembled SSA form of the Limit instruction.
func (l *Limit) String() string {
	// TODO(rfratto): change the type of l.Input to [Value] so we can use
	// s.Value.Name here.
	return fmt.Sprintf("LIMIT %v [skip=%d, fetch=%d]", l.Table.Name(), l.Skip, l.Fetch)
}

// Operands appends the operands of l to the provided slice. The pointers may
// be modified to change operands of l.
func (l *Limit) Operands(buf []*Value) []*Value {
	return append(buf, &l.Table)
}

// Referrers returns a list of instructions that reference the Limit.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (l *Limit) Referrers() *[]Instruction { return &l.b.referrers }

func (l *Limit) base() *baseNode { return &l.b }
func (l *Limit) isInstruction()  {}
func (l *Limit) isValue()        {}
// 实现 Instruction 与 Value，下游 executor 将 Limit 译为带 offset 的行计数截断。
