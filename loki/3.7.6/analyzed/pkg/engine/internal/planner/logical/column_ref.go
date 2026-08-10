package logical

// column_ref 表示逻辑计划中对表列的引用，仅实现 Value 接口供表达式与算子共享。

import (
	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// ColumnRef 内嵌 types.ColumnRef，Name/String 均委托 Ref.String 输出类型化列名。
// A ColumnRef referenes a column within a table relation. ColumnRef only
// implements [Value].
type ColumnRef struct {
	b baseNode

	Ref types.ColumnRef
}

var (
	_ Value = (*ColumnRef)(nil)
)

// Name returns the identifier of the ColumnRef, which combines the column type
// and column name being referenced.
func (c *ColumnRef) Name() string {
	return c.Ref.String()
}

// String returns [ColumnRef.Name].
func (c *ColumnRef) String() string {
	return c.Ref.String()
}

// Referrers 返回可变的引用者切片指针，计划改写时需同步更新双向链接。
// Referrers returns a list of instructions that reference c.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (c *ColumnRef) Referrers() *[]Instruction { return &c.b.referrers }

func (c *ColumnRef) base() *baseNode { return &c.b }
func (c *ColumnRef) isValue()        {}

// NewColumnRef 构造指定列名与 ColumnType 的列引用 Value 节点。
func NewColumnRef(name string, ty types.ColumnType) *ColumnRef {
	return &ColumnRef{
		Ref: types.ColumnRef{
			Column: name,
			Type:   ty,
		},
	}
}
// baseNode 承载 SSA ID 与 referrers，isValue 为标记方法满足 Value 类型约束。
