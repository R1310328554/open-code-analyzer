package physical

// VectorAggregation 是物理计划中的 instant 向量聚合节点，在每个时间戳上对时序数据按维度分组计算 sum/max/min 等聚合结果。

import (
	"github.com/oklog/ulid/v2"

	"github.com/grafana/loki/v3/pkg/engine/internal/types"
)

// VectorAggregation 对应 PromQL/LogQL 的 instant 向量聚合算子，与 RangeAggregation 不同，它不滑动时间窗口。
// VectorAggregation represents a physical plan node that performs vector aggregations.
// It computes aggregations over time series data at each timestamp instant,
// grouping results by specified dimensions.
// VectorAggregation 持有 ULID 节点标识、分组列、聚合算子类型及序列数上限。
type VectorAggregation struct {
	NodeID ulid.ULID

	Grouping Grouping // Grouping of the data.

	// Operation defines the type of aggregation operation to perform (e.g., sum, min, max)
	Operation types.VectorAggregationType

	// MaxQuerySeries is the maximum number of unique series allowed (0 means no limit)
	MaxQuerySeries int
}

// ID 返回节点 ULID，供 DAG 图遍历与边连接引用。
// ID implements the [Node] interface.
// Returns the ULID that uniquely identifies the node in the plan.
func (v *VectorAggregation) ID() ulid.ULID { return v.NodeID }

// Clone 深拷贝 Grouping 列表达式并分配新 ULID，用于计划改写与分支复制。
// Clone returns a deep copy of the node with a new unique ID.
func (v *VectorAggregation) Clone() Node {
	return &VectorAggregation{
		NodeID: ulid.Make(),

		Grouping: Grouping{
			Columns: cloneExpressions(v.Grouping.Columns),
			Without: v.Grouping.Without,
		},
		Operation:      v.Operation,
		MaxQuerySeries: v.MaxQuerySeries,
	}
}

// Type 返回 NodeTypeVectorAggregation，供类型分发与序列化识别。
// Type implements the [Node] interface.
// Returns the type of the node.
func (*VectorAggregation) Type() NodeType {
	return NodeTypeVectorAggregation
}
// MaxQuerySeries 为 0 表示不限制唯一序列数，非零时超出则查询失败。
