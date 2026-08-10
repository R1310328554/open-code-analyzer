package physical

// RangeAggregation 物理节点执行 PromQL 区间向量聚合，含分组、步长、区间宽度与时间边界。

import (
	"time"

	"github.com/oklog/ulid/v2"

	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// TODO: Rename based on the actual implementation.
type RangeAggregation struct {
	NodeID ulid.ULID

	Grouping       Grouping
	Operation      types.RangeAggregationType
	Start          time.Time
	End            time.Time
	Step           time.Duration // optional for instant queries
	Range          time.Duration
	MaxQuerySeries int // maximum number of unique series allowed (0 means no limit)
}

// ID returns the ULID that uniquely identifies the node in the plan.
func (r *RangeAggregation) ID() ulid.ULID { return r.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
// Clone 深拷贝 Grouping.Columns 与各时间参数字段，供 parallelPushdown 复制分片。
func (r *RangeAggregation) Clone() Node {
	return &RangeAggregation{
		NodeID: ulid.Make(),

		Grouping: Grouping{
			Columns: cloneExpressions(r.Grouping.Columns),
			Without: r.Grouping.Without,
		},
		Operation:      r.Operation,
		Start:          r.Start,
		End:            r.End,
		Step:           r.Step,
		Range:          r.Range,
		MaxQuerySeries: r.MaxQuerySeries,
	}
}

func (r *RangeAggregation) Type() NodeType {
	return NodeTypeRangeAggregation
}
// Type 返回 NodeTypeRangeAggregation，计划顶层节点定义 CalculateMaxTimeRange 的全局时间窗。
