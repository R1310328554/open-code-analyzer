package logical

// node_literal 表示编译期已知的常量 Value，仅实现 Value 而非 Instruction。

import (
	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// Literal 零值为 NULL；inner 封装 types.Literal 的类型化存储与打印。
// A Literal represents a literal value known at plan time. Literal only
// implements [Value].
//
// The zero value of a Literal is a NULL value.
type Literal struct {
	b     baseNode
	inner types.Literal
}

var _ Value = (*Literal)(nil)

// NewLiteral(nil) 构造 NullLiteral；否则经 types.NewLiteral 推断数据类型。
func NewLiteral(value any) *Literal {
	if value == nil {
		return &Literal{inner: types.NewNullLiteral()}
	}
	return &Literal{inner: types.NewLiteral(value)}
}

// Kind 返回 Literal 的 types.DataType，供类型检查与 BinOp 简化分支判断。
// Kind returns the kind of value represented by the literal.
func (l Literal) Kind() types.DataType {
	return l.inner.Type()
}

// Name returns the string form of the literal.
func (l Literal) Name() string {
	return l.inner.String()
}

// String returns a printable form of the literal, even if lit is not a
// [ValueTypeString].
func (l Literal) String() string {
	return l.inner.String()
}

// Value 暴露底层 Go 值，regex 优化路径读取 string 模式文本。
// Value returns lit's value as untyped interface{}.
func (l Literal) Value() any {
	return l.inner.Any()
}

// Referrers returns a list of instructions that reference the Literal.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (l *Literal) Referrers() *[]Instruction { return &l.b.referrers }

func (l *Literal) base() *baseNode { return &l.b }
func (l *Literal) isValue()        {}
// Name 与 String 均委托 inner.String，常量可直接嵌入 SSA 反汇编输出。
