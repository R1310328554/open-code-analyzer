package physical

// dataobjscan 表示从对象存储读取 data object 的物理扫描算子，携带投影与谓词下推信息。

import (
	"slices"

	"github.com/oklog/ulid/v2"
)

// DataObjLocation is a string that uniquely identifies a data object location in
// object storage.
type DataObjLocation string

// DataObjScan 绑定 Location、Section、StreamIDs、Projections、Predicates 与 MaxTimeRange 元数据。
// DataObjScan represents a physical plan operation for reading data objects.
// It contains information about the object location, stream IDs, projections,
// predicates for reading data from a data object.
// StreamIDs 仅在单个 data object 内唯一；Predicates 通常含时间范围以裁剪读取行数。
type DataObjScan struct {
	NodeID ulid.ULID

	// Location is the unique name of the data object that is used as source for
	// reading streams.
	Location DataObjLocation
	// Section is the section index inside the data object to scan.
	Section int
	// StreamIDs is a set of stream IDs inside the data object. These IDs are
	// only unique in the context of a single data object.
	StreamIDs []int64
	// Projections are used to limit the columns that are read to the ones
	// provided in the column expressions to reduce the amount of data that needs
	// to be processed.
	Projections []ColumnExpression
	// Predicates are used to filter rows to reduce the amount of rows that are
	// returned. Predicates would almost always contain a time range filter to
	// only read the logs for the requested time range.
	Predicates []Expression
	// The maximum boundary of timestamps that scanning the
	// data object can possibly emit. Does not account for
	// predicates.
	// MaxTimeRange 仅作计划元数据，描述未应用谓词时扫描可能发出的最大时间边界。
// MaxTimeRange is not read when executing a scan.
	// It can be used as metadata to control physical plan execution.
	MaxTimeRange TimeRange
}

// ID implements the [Node] interface.
// Returns the ULID that uniquely identifies the node in the plan.
func (s *DataObjScan) ID() ulid.ULID { return s.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
func (s *DataObjScan) Clone() Node {
	return &DataObjScan{
		NodeID: ulid.Make(),

		Location:     s.Location,
		Section:      s.Section,
		StreamIDs:    slices.Clone(s.StreamIDs),
		Projections:  cloneExpressions(s.Projections),
		Predicates:   cloneExpressions(s.Predicates),
		MaxTimeRange: s.MaxTimeRange,
	}
}

// Type implements the [Node] interface.
// Returns the type of the node.
func (*DataObjScan) Type() NodeType {
	return NodeTypeDataObjScan
}
// Clone 深拷贝 stream、投影与谓词表达式列表，供物理计划重写时独立修改子图。
