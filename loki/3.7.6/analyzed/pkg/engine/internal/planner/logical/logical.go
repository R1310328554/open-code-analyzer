// logical 包以 SSA 形式表示日志查询的中间计划：Value 为表达式，Instruction 为语句。
// Package logical provides a logical query plan representation for data
// processing operations.
//
// The logical plan is represented using static single-assignment (SSA) form of
// intermediate representation (IR) for the operations performed on log data.
//
// For an introduction to SSA form, see
// https://en.wikipedia.org/wiki/Static_single_assignment_form.
//
// The primary interfaces of this package are:
//
// - [Value], an expression that yields a value.
// - [Instruction], a statement that consumes values and performs computation.
// - [Plan], a sequence of instructions that produces a result.
//
// A computation that also yields a result implements both the [Value] and
// [Instruction] interfaces. See the documentation comments on each type for
// which of those interfaces it implements.
//
// Values are representable as either:
//
// - A column value (such as in [ColumnRef]),
// - a relation (such as in [Select]), or
// - a value literal (such as in [Literal]).
//
// The SSA form forms a graph: each [Value] may appear as an operand of one or
// more [Instruction]s.
package logical

// 计划图由 Value 作操作数、Instruction 作消费者构成；可同时实现两接口的节点既定义又引用值。

import (
	"fmt"
	"strings"
)

// Node 聚合 String 与 base()，是 Value 与 Instruction 的公共超集。
// Node is a node in the logical plan. Every type that implements Node is either
// a [Value], [Instruction], or both.
//
// Node contains the common methods to Value and Instruction.
type Node interface {
	String() string

	// base returns the [baseNode] for a Node.
	base() *baseNode
}

// Instruction 通过 Operands 暴露可修改的操作数指针，支持计划改写与下推。
// An Instruction is an SSA instruction that computes a new [Value] or has some
// effect.
//
// Instructions that define a value (e.g., BinOp) also implement the Value
// interface; an Instruction that only has an effect (e.g., Return) does not.
type Instruction interface {
	Node

	// String returns the disassembled SSA form of the Instruction. This does not
	// include the name of the Value if the Instruction also implements [Value].
	String() string

	// Operands appends Instruction's operands to buf and returns the resulting
	// slice.
	//
	// Operands are represented as pointers to permit updating an Instruction to
	// use different operands.
	Operands(buf []*Value) []*Value

	// isInstruction is a marker method to prevent external implementations.
	isInstruction()
}

// Value.Name 返回 %%N 形式 SSA 标识；Referrers 维护反向引用供优化遍历。
// A Value is an SSA value that can be referenced by an [Instruction].
type Value interface {
	Node

	// Name returns an identifier for this Value (such as "%1"), which is used
	// when this Value appears as an operand of an Instruction.
	//
	// If the Value was not created by the logical planner, Name instead returns
	// the pointer address of the Value.
	Name() string

	// String returns human-readable information about the Value. If Value also
	// implements [Instruction], String returns the disassembled form of the
	// Instruction as documented by [Instruction.String].
	String() string

	// Referrers returns a list of instructions that reference this Value.
	//
	// Referrers may be mutated to update the reference list. Care should be
	// taken to only perform modifications that preserve the validity of the
	// reference list.
	Referrers() *[]Instruction

	// isValue is a marker method to prevent external implementations.
	isValue()
}

// Plan.Instructions 按发射顺序排列；首个 Return 的 Value 为计划最终输出。
// A Plan represents a sequence of [Instruction]s that ultimately produce a
// [Value].
//
// The first [Return] instruction in the plan denotes the final output.
type Plan struct {
	Instructions []Instruction // Instructions of the plan in order.
}

// String prints out the entire plan SSA.
func (p Plan) String() string {
	var sb strings.Builder

	for _, inst := range p.Instructions {
		switch inst := inst.(type) {
		case Value:
			fmt.Fprintf(&sb, "%s = %s\n", inst.Name(), inst.String())
		case Instruction:
			fmt.Fprintf(&sb, "%s\n", inst.String())
		}
	}

	return sb.String()
}

// Plan.Value 线性扫描指令表，返回 Return 节点持有的根 Value。
// Value returns the value of the RETURN instruction.
func (p Plan) Value() Value {
	for _, inst := range p.Instructions {
		switch inst := inst.(type) {
		case *Return:
			return inst.Value
		}
	}
	return nil
}
// Plan.String 对同时实现 Value 的指令打印「名 = 反汇编体」两行 SSA 文本。
