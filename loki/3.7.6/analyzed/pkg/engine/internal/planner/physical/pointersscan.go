package physical

// PointersScan 表示通过 metastore 索引路径扫描 data object 指针，携带 selector 与时间边界。

import (
	"time"

	"github.com/oklog/ulid/v2"
)

type PointersScan struct {
	NodeID ulid.ULID

	Location DataObjLocation

	Selector   Expression
	Predicates []Expression

	Start time.Time
	End   time.Time
}

// MaxTimeRange 返回 Start/End 构成的查询时间窗口，供计划级时间边界合并。
func (s *PointersScan) MaxTimeRange() TimeRange {
	return TimeRange{s.Start, s.End}
}

func (s *PointersScan) ID() ulid.ULID { return s.NodeID }

func (s *PointersScan) Clone() Node {
	var selector Expression
	if s.Selector != nil {
		selector = s.Selector.Clone()
	}
	return &PointersScan{
		NodeID:     ulid.Make(),
		Location:   s.Location,
		Selector:   selector,
		Predicates: cloneExpressions(s.Predicates),
		Start:      s.Start,
		End:        s.End,
	}
}

func (s *PointersScan) Type() NodeType {
	return NodeTypePointersScan
}
// Clone 深拷贝 Selector 与 Predicates，Type 返回 NodeTypePointersScan。
