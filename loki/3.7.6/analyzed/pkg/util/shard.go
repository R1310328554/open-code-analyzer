package util //nolint:revive

// util 包分片常量与 shuffle-sharding 辅助：ruler 规则组在 ring 上按组/按规则/随机分片，种子由租户标识 MD5 导出。

import (
	"crypto/md5"
	"encoding/binary"
	"math"
)

// ShardingStrategy* 与 ShardingAlgo* 描述 ruler 在 hash ring 上的分配策略名称。
// Sharding strategies & algorithms.
const (
	// ShardingStrategyDefault shards rule groups across available rulers in the ring.
	ShardingStrategyDefault = "default"
	// ShardingStrategyShuffle shards tenants' rule groups across available rulers in the ring using a
	// shuffle-sharding algorithm.
	ShardingStrategyShuffle = "shuffle-sharding"

	// ShardingAlgoByGroup is an alias of ShardingStrategyDefault.
	ShardingAlgoByGroup = "by-group"
	// ShardingAlgoByRule shards all rules evenly across available rules in the ring, regardless of group.
	// This can be achieved because currently Loki recording/alerting rules cannot not any inter-dependency, unlike
	// Prometheus rules, so there's really no need to shard by group. This will eventually become the new default strategy.
	ShardingAlgoByRule = "by-rule" // this will eventually become the new default strategy.
)

var (
	seedSeparator = []byte{0}
)

// ShuffleShardSeed 对 identifier 与可选 zone 做 MD5，取前 64 位作为可预测 RNG 种子。
// ShuffleShardSeed returns seed for random number generator, computed from provided identifier.
func ShuffleShardSeed(identifier, zone string) int64 {
	// Use the identifier to compute an hash we'll use to seed the random.
	hasher := md5.New()               //#nosec G401 -- This does not require collision resistance, this is an intentionally predictable value -- nosemgrep: use-of-md5
	hasher.Write(YoloBuf(identifier)) // nolint:errcheck
	if zone != "" {
		hasher.Write(seedSeparator) // nolint:errcheck
		hasher.Write(YoloBuf(zone)) // nolint:errcheck
	}
	checksum := hasher.Sum(nil)

	// Generate the seed based on the first 64 bits of the checksum.
	return int64(binary.BigEndian.Uint64(checksum))
}

// ShuffleShardExpectedInstancesPerZone 按 zone 均分 shardSize，不能整除时向上取整。
// ShuffleShardExpectedInstancesPerZone returns the number of instances that should be selected for each
// zone when zone-aware replication is enabled. The algorithm expects the shard size to be divisible
// by the number of zones, in order to have nodes balanced across zones. If it's not, we do round up.
func ShuffleShardExpectedInstancesPerZone(shardSize, numZones int) int {
	return int(math.Ceil(float64(shardSize) / float64(numZones)))
}

// ShuffleShardExpectedInstances 无 zone 感知时 numZones 传 1，返回期望选中实例总数。
// ShuffleShardExpectedInstances returns the total number of instances that should be selected for a given
// tenant. If zone-aware replication is disabled, the input numZones should be 1.
func ShuffleShardExpectedInstances(shardSize, numZones int) int {
	return ShuffleShardExpectedInstancesPerZone(shardSize, numZones) * numZones
}
// by-rule 分片忽略规则组边界，适合 Loki 无组间依赖的 recording/alerting 规则。
