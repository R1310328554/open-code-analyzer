package logql

// shards 定义分片注解、解析与 ShardingStrategy：支持 2 的幂分片与按字节边界的有界分片。

import (
	"encoding/json"

	"github.com/grafana/dskit/multierror"
	"github.com/pkg/errors"
	"github.com/prometheus/common/model"

	"github.com/grafana/loki/v3/pkg/logproto"
	"github.com/grafana/loki/v3/pkg/logql/syntax"
	"github.com/grafana/loki/v3/pkg/querier/astmapper"
	v1 "github.com/grafana/loki/v3/pkg/storage/bloom/v1"
	"github.com/grafana/loki/v3/pkg/storage/stores/index/stats"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/index"
	"github.com/grafana/loki/v3/pkg/storage/stores/shipper/indexshipper/tsdb/sharding"
)

type Shards []Shard

type ShardVersion uint8

// ShardVersion 区分 PowerOfTwo 与 Bounded 两种分片模型及对应 Strategy 工厂。
// TODO(owen-d): refactor this file. There's too many layers (sharding strategies, sharding resolvers).
// Eventually we should have a single strategy (bounded) and a single resolver (dynamic).
// It's likely this could be refactored anyway -- I was in a rush writing it the first time around.
const (
	PowerOfTwoVersion ShardVersion = iota
	BoundedVersion
)

func (v ShardVersion) Strategy(resolver ShardResolver, defaultTargetShardBytes uint64) ShardingStrategy {
	switch v {
	case BoundedVersion:
		return NewDynamicBoundsStrategy(resolver, defaultTargetShardBytes)
	default:
		// TODO(owen-d): refactor, ugly, etc, but the power of two strategy already populated
		// the default target shard bytes through it's resolver
		return NewPowerOfTwoStrategy(resolver)
	}
}

func (v ShardVersion) String() string {
	switch v {
	case PowerOfTwoVersion:
		return "power_of_two"
	case BoundedVersion:
		return "bounded"
	default:
		return "unknown"
	}
}

var validStrategies = map[string]ShardVersion{
	PowerOfTwoVersion.String(): PowerOfTwoVersion,
	BoundedVersion.String():    BoundedVersion,
}

func ParseShardVersion(s string) (ShardVersion, error) {
	v, ok := validStrategies[s]
	if !ok {
		return PowerOfTwoVersion, errors.Errorf("invalid shard version %s", s)
	}
	return v, nil
}

// ShardResolver 根据表达式估算分片数、字节与可选的预计算 chunk refs。
type ShardResolver interface {
	Shards(expr syntax.Expr) (int, uint64, error)
	// ShardingRanges returns shards and optionally a set of precomputed chunk refs for each group. If present,
	// they will be used in lieu of resolving chunk refs from the index durin evaluation.
	// If chunks are present, the number of shards returned must match the number of chunk ref groups.
	ShardingRanges(expr syntax.Expr, targetBytesPerShard uint64) ([]logproto.Shard, []logproto.ChunkRefGroup, error)
	GetStats(e syntax.Expr) (stats.Stats, error)
}

type ConstantShards int

func (s ConstantShards) Shards(_ syntax.Expr) (int, uint64, error) { return int(s), 0, nil }
func (s ConstantShards) ShardingRanges(_ syntax.Expr, _ uint64) ([]logproto.Shard, []logproto.ChunkRefGroup, error) {
	return sharding.LinearShards(int(s), 0), nil, nil
}
func (s ConstantShards) GetStats(_ syntax.Expr) (stats.Stats, error) { return stats.Stats{}, nil }

// ShardingStrategy 将表达式展开为 []ShardWithChunkRefs 及 maxBytesPerShard。
type ShardingStrategy interface {
	// The chunks for each shard are optional and are used to precompute chunk refs for each group
	Shards(expr syntax.Expr) (shards []ShardWithChunkRefs, maxBytesPerShard uint64, err error)
	Resolver() ShardResolver
}

// DynamicBoundsStrategy 调用 ShardingRanges 按 targetBytesPerShard 生成有界分片列表。
type DynamicBoundsStrategy struct {
	resolver            ShardResolver
	targetBytesPerShard uint64
}

func (s DynamicBoundsStrategy) Shards(expr syntax.Expr) ([]ShardWithChunkRefs, uint64, error) {
	shards, chunks, err := s.resolver.ShardingRanges(expr, s.targetBytesPerShard)
	if err != nil {
		return nil, 0, err
	}

	var maxBytes uint64
	res := make([]ShardWithChunkRefs, 0, len(shards))
	for i, shard := range shards {
		x := ShardWithChunkRefs{
			Shard: NewBoundedShard(shard),
		}
		if shard.Stats != nil {
			maxBytes = max(maxBytes, shard.Stats.Bytes)
		}
		if len(chunks) > 0 {
			x.chunks = &chunks[i]
		}
		res = append(res, x)
	}

	return res, maxBytes, nil
}

func (s DynamicBoundsStrategy) Resolver() ShardResolver {
	return s.resolver
}

func NewDynamicBoundsStrategy(resolver ShardResolver, targetBytesPerShard uint64) DynamicBoundsStrategy {
	return DynamicBoundsStrategy{resolver: resolver, targetBytesPerShard: targetBytesPerShard}
}

// PowerOfTwoStrategy 按 resolver 返回的 factor 生成 0..factor-1 的 2 的幂分片注解。
type PowerOfTwoStrategy struct {
	resolver ShardResolver
}

func NewPowerOfTwoStrategy(resolver ShardResolver) PowerOfTwoStrategy {
	return PowerOfTwoStrategy{resolver: resolver}
}

func (s PowerOfTwoStrategy) Resolver() ShardResolver {
	return s.resolver
}

// PowerOfTwo strategy does not support precomputed chunk refs
func (s PowerOfTwoStrategy) Shards(expr syntax.Expr) ([]ShardWithChunkRefs, uint64, error) {
	factor, bytesPerShard, err := s.resolver.Shards(expr)
	if err != nil {
		return nil, 0, err
	}

	if factor == 0 {
		return nil, bytesPerShard, nil
	}

	res := make([]ShardWithChunkRefs, 0, factor)
	for i := 0; i < factor; i++ {
		res = append(
			res,
			ShardWithChunkRefs{
				Shard: NewPowerOfTwoShard(index.ShardAnnotation{Of: uint32(factor), Shard: uint32(i)}),
			},
		)
	}
	return res, bytesPerShard, nil
}

// ShardWithChunkRefs 可选绑定 ChunkRefGroup，评估时跳过索引查 chunk。
// ShardWithChunkRefs is a convenience type for passing around shards with associated chunk refs.
// The chunk refs are optional as determined by their contents (zero chunks means no precomputed refs)
// and are used to precompute chunk refs for each group
type ShardWithChunkRefs struct {
	Shard
	chunks *logproto.ChunkRefGroup
}

// Shard 持有 PowerOfTwo 或 Bounded 之一，实现 Match/GetFromThrough 等指纹过滤接口。
// Shard represents a shard annotation
// It holds either a power of two shard (legacy) or a bounded shard
type Shard struct {
	PowerOfTwo *index.ShardAnnotation
	Bounded    *logproto.Shard
}

func (s *Shard) Variant() ShardVersion {
	if s.Bounded != nil {
		return BoundedVersion
	}

	return PowerOfTwoVersion
}

// implement FingerprintFilter
// Match 判断序列指纹是否落在分片边界内，供 ingester/querier 过滤 chunk。
func (s *Shard) Match(fp model.Fingerprint) bool {
	if s.Bounded != nil {
		return v1.BoundsFromProto(s.Bounded.Bounds).Match(fp)
	}

	return s.PowerOfTwo.Match(fp)
}

func (s *Shard) GetFromThrough() (model.Fingerprint, model.Fingerprint) {
	if s.Bounded != nil {
		return v1.BoundsFromProto(s.Bounded.Bounds).GetFromThrough()
	}

	return s.PowerOfTwo.GetFromThrough()
}

// convenience method for unaddressability concerns using constructors in literals (tests)
func (s Shard) Ptr() *Shard {
	return &s
}

func (s Shard) Bind(chunks *logproto.ChunkRefGroup) *ShardWithChunkRefs {
	return &ShardWithChunkRefs{
		Shard:  s,
		chunks: chunks,
	}
}

func NewBoundedShard(shard logproto.Shard) Shard {
	return Shard{Bounded: &shard}
}

func NewPowerOfTwoShard(shard index.ShardAnnotation) Shard {
	return Shard{PowerOfTwo: &shard}
}

func (s Shard) String() string {
	if s.Bounded != nil {
		b, err := json.Marshal(s.Bounded)
		if err != nil {
			panic(err)
		}
		return string(b)
	}

	return s.PowerOfTwo.String()
}

func (xs Shards) Encode() (encoded []string) {
	for _, shard := range xs {
		encoded = append(encoded, shard.String())
	}

	return encoded
}

// ParseShards parses a list of string encoded shards
// ParseShards 解析分片字符串列表，要求所有分片版本一致。
func ParseShards(strs []string) (Shards, ShardVersion, error) {
	if len(strs) == 0 {
		return nil, PowerOfTwoVersion, nil
	}
	shards := make(Shards, 0, len(strs))

	var prevVersion ShardVersion
	for i, str := range strs {
		shard, version, err := ParseShard(str)
		if err != nil {
			return nil, PowerOfTwoVersion, err
		}

		if i == 0 {
			prevVersion = version
		} else if prevVersion != version {
			return nil, PowerOfTwoVersion, errors.New("shards must be of the same version")
		}
		shards = append(shards, shard)
	}
	return shards, prevVersion, nil
}

// ParseShard 先尝试 JSON 有界分片，失败则回退 astmapper 的 2 的幂分片格式。
func ParseShard(s string) (Shard, ShardVersion, error) {

	var bounded logproto.Shard
	v2Err := json.Unmarshal([]byte(s), &bounded)
	if v2Err == nil {
		return Shard{Bounded: &bounded}, BoundedVersion, nil
	}

	old, v1Err := astmapper.ParseShard(s)
	casted := old.TSDB()
	if v1Err == nil {
		return Shard{PowerOfTwo: &casted}, PowerOfTwoVersion, nil
	}

	err := errors.Wrap(
		multierror.New(v1Err, v2Err).Err(),
		"failed to parse shard",
	)
	return Shard{}, PowerOfTwoVersion, err
}
// ConstantShards 用于测试或固定分片因子；Encode 将分片列表序列化为查询参数。
