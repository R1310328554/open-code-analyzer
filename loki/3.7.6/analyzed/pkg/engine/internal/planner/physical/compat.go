package physical

// compat 定义 ColumnCompat 物理节点：解决 metadata 列与 label 列同名冲突，生成 _extracted 列。

import (
	"slices"

	"github.com/oklog/ulid/v2"

	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// ColumnCompat 指定 Source 列类型、Destination 生成列类型及可能冲突的 Collisions 类型列表。
// ColumnCompat represents a compactibilty operation in the physical plan that
// moves a values from a conflicting metadata column with a label column into a new column suffixed with `_extracted`.
// Source/Destination/Collisions 字段命名待改进，逻辑计划 Compat 模式会插入此类节点。
type ColumnCompat struct {
	NodeID ulid.ULID

	// TODO(chaudum): These fields are poorly named. Come up with more descriptive names.
	Source      types.ColumnType   // column type of the column that may colide with columns of the same name but with collision type
	Destination types.ColumnType   // column type of the generated _extracted column (should be same as source)
	Collisions  []types.ColumnType // column types of the columns that a source type column may collide with
}

// ID implements the [Node] interface.
// Returns the ULID that uniquely identifies the node in the plan.
func (m *ColumnCompat) ID() ulid.ULID { return m.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
func (m *ColumnCompat) Clone() Node {
	return &ColumnCompat{
		NodeID: ulid.Make(),

		Source:      m.Source,
		Destination: m.Destination,
		Collisions:  slices.Clone(m.Collisions),
	}
}

// Type implements the [Node] interface.
// Returns the type of the node.
func (m *ColumnCompat) Type() NodeType {
	return NodeTypeCompat
}
// Clone 复制碰撞类型切片并分配新 ULID，Type 返回 NodeTypeCompat。
