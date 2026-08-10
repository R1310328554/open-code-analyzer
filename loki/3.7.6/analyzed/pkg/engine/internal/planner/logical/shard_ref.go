package logical

// shard_ref 描述表关系的分片子集，等价于旧引擎 index.ShardAnnotation，仅实现 Value。

import (
	"fmt"
)

// ShardInfo 用 Shard/Of 表示第 shard 片共 of 片，Of 必须为 2 的幂以保证分片算法正确。
// A ShardInfo defines a subset of a table relation. ShardInfo only implements [Value].
// It is the equivalent to the [index.ShardAnnotation] in the old query engine.
type ShardInfo struct {
	b baseNode

	Shard uint32
	Of    uint32 // MUST be a power of 2 to ensure sharding logic works correctly.
}

var (
	_ Value = (*ShardInfo)(nil)
)

// Name returns the identifier of the ShardRef.
func (s *ShardInfo) Name() string {
	return fmt.Sprintf("%d_of_%d", s.Shard, s.Of)
}

// String returns [ShardInfo.Name].
func (s *ShardInfo) String() string {
	return s.Name()
}

// Referrers returns a list of instructions that reference the ShardInfo.
//
// The list of instructions can be modified to update the reference list, such
// as when modifying the plan.
func (s *ShardInfo) Referrers() *[]Instruction { return &s.b.referrers }

func (s *ShardInfo) base() *baseNode { return &s.b }
func (s *ShardInfo) isValue()        {}

// NewShard 构造分片描述；noShard 为 0_of_1 表示未分片的全量扫描。
func NewShard(shard, of uint32) *ShardInfo {
	return &ShardInfo{
		Shard: shard,
		Of:    of,
	}
}

var noShard = NewShard(0, 1)
// parseShards 从 logql.Params.Shards 解析 PowerOfTwo 变体并填入 MakeTable.Shard。
