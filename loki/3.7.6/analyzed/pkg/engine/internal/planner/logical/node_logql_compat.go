package logical

// node_logql_compat 插入 LOGQL_COMPAT 标记，声明子计划需与 v1 引擎语义对齐。

import (
	"fmt"
)

// LogQLCompat 包装单个子 Value，物理层据此选择兼容执行路径或降级策略。
// The LOGQL_COMPAT instruction is a marker to indicate v1 engine compatibility.
// LogQLCompat implements [Instruction] and [Value].
type LogQLCompat struct {
	b baseNode

	Value Value
}

// String 输出 LOGQL_COMPAT 加操作数名，便于在 Plan 文本中定位兼容边界。
// String returns the disassembled SSA form of r.
func (c *LogQLCompat) String() string {
	return fmt.Sprintf("LOGQL_COMPAT %s", c.Value.Name())
}

func (c *LogQLCompat) Name() string { return c.b.Name() }

// Operands 仅含 Value 指针，优化遍遍历时不应剥离该兼容包装除非明确安全。
// Operands appends the operands of c to the provided slice. The pointers may
// be modified to change operands of c.
func (c *LogQLCompat) Operands(buf []*Value) []*Value {
	return append(buf, &c.Value)
}

// Referrers returns a list of instructions that reference the LogQLCompat.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (c *LogQLCompat) Referrers() *[]Instruction { return &c.b.referrers }

func (c *LogQLCompat) base() *baseNode { return &c.b }
func (c *LogQLCompat) isInstruction()  {}
func (c *LogQLCompat) isValue()        {}
// 同时实现 Instruction 与 Value，Referrers 与其他关系算子节点一致可变异。
