package physical

// Parallelize 提示引擎对子分支分区并行执行，合并输出为无序单序列。

import "github.com/oklog/ulid/v2"

// Parallelize 是并行化边界：其下各分支可独立调度 worker，上层 Merge 再汇总。
// Parallelize represents a hint to the engine to partition and parallelize the
// children branches of the Parallelize and emit results as a single sequence
// with no guaranteed order.
type Parallelize struct {
	NodeID ulid.ULID
}

// ID returns the ULID that uniquely identifies the node in the plan.
func (p *Parallelize) ID() ulid.ULID { return p.NodeID }

// Clone returns a deep copy of the node with a new unique ID.
func (p *Parallelize) Clone() Node {
	return &Parallelize{
		NodeID: ulid.Make(),
	}
}

// Type returns [NodeTypeParallelize].
func (p *Parallelize) Type() NodeType { return NodeTypeParallelize }
// MakeTable 与 MetastorePlanner 均将 ScanSet 挂在此节点之下以并行扫描 data object。
