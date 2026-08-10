package logical

// node_project 定义投影算子 Projection：对关系做 KEEP/DROP/EXPAND 列变换。

import (
	"fmt"
	"strings"
)

// Projection 通过 All/Expand/Drop 标志组合模式，Expressions 列出参与投影的表达式。
// The Projection instruction projects (keeps/drops) columns from a relation.
// Projection implements both [Instruction] and [Value].
type Projection struct {
	b baseNode

	Relation    Value   // The input relation.
	Expressions []Value // The expressions to apply for projecting columns.

// All 类似 SELECT *；Expand 追加新列；Drop 从输入关系中移除 Expressions 指定列。
	All    bool // Marker for projecting all columns of input relation (similar to SQL `SELECT *`)
	Expand bool // Indicates that projected columns should be added to input relation
	Drop   bool // Indicates that projected columns should be dropped from input Relation
}

var (
	_ Value       = (*Projection)(nil)
	_ Instruction = (*Projection)(nil)
)

// Name returns an identifier for the Projection operation.
func (p *Projection) Name() string { return p.b.Name() }

// String returns the disassembled SSA form of the Projection instruction.
func (p *Projection) String() string {
	params := make([]string, 0, len(p.Expressions)+1)
	params = append(params, fmt.Sprintf("mode=%s", p.mode()))
	for _, expr := range p.Expressions {
		params = append(params, fmt.Sprintf("expr=%s", expr.Name()))
	}
	return fmt.Sprintf("PROJECT %s [%s]", p.Relation.Name(), strings.Join(params, ", "))
}

// mode 将 All/Expand/Drop 编码为 *、E、D 字符，写入 String 的 mode= 属性。
func (p *Projection) mode() string {
	var mode string
	if p.All {
		mode += "*"
	}
	if p.Expand {
		mode += "E"
	}
	if p.Drop {
		mode += "D"
	}
	return mode
}

// Operands 含 Relation 与各 Expression 指针，改写计划时可替换投影表达式子树。
// Operands appends the operands of p to the provided slice. The pointers may
// be modified to change operands of p.
func (p *Projection) Operands(buf []*Value) []*Value {
	buf = append(buf, &p.Relation)
	for i := range p.Expressions {
		buf = append(buf, &p.Expressions[i])
	}
	return buf
}

// Referrers returns a list of instructions that reference the Projection.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (p *Projection) Referrers() *[]Instruction { return &p.b.referrers }

func (p *Projection) base() *baseNode { return &p.b }
func (p *Projection) isInstruction()  {}
func (p *Projection) isValue()        {}
// 物理 executor 将 Projection 译为 project pipeline，模式字符串与 Builder API 一致。
