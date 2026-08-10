package physical

// Merge 将多个并行分支的输出合并为单一流，不保证顺序，常用于分布式任务聚合点。

import "github.com/oklog/ulid/v2"

// Merge 作为父任务读取多个分区子任务结果的汇聚节点，输出顺序未定义。
// Merge combines multiple input streams into a single stream with no
// guaranteed ordering.
//
// Merge is primarily used as an aggregation point for distributed task
// execution where a parent task needs to read from many partitioned tasks.
// Merge 通常位于 Parallelize 之上，将各 worker 局部结果汇总为全局流。
type Merge struct {
	NodeID ulid.ULID
}

func (m *Merge) ID() ulid.ULID { return m.NodeID }

func (m *Merge) Clone() Node {
	return &Merge{
		NodeID: ulid.Make(),
	}
}

func (m *Merge) Type() NodeType { return NodeTypeMerge }
// MetastorePlanner 构建的计划以 Merge 为根，经 Parallelize 连接 ScanSet 扫描索引。
