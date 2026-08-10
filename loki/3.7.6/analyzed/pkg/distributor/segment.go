package distributor

// 分段键与分区解析：按 service_name 分组流，结合租户限额 shuffle-shard 到 Kafka 分区。

import (
	"context"
	"errors"
	"fmt"
	"hash/fnv"

	"github.com/go-kit/log"
	"github.com/grafana/dskit/ring"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

// segmentationKey 在保持租户局部性的同时尽量均衡分区负载。
// A segmentationKey is a special partition key that attempts to equally
// distribute load while preserving stream locality for tenants.
type segmentationKey string

// Sum64 returns a 64 bit, non-cryptographic hash of the key.
func (key segmentationKey) Sum64() uint64 {
	h := fnv.New64a()
	// Use a reserved word here to avoid any possible hash conflicts with
	// streams.
	h.Write([]byte("__loki_segmentation_key__"))
	h.Write([]byte(key))
	return h.Sum64()
}

// getSegmentationKey returns the segmentation key for the stream or an error.
// getSegmentationKey 从 stream 标签解析 service_name，缺失时使用 unknown_service。
func getSegmentationKey(stream KeyedStream) (segmentationKey, error) {
	labels, err := syntax.ParseLabels(stream.Stream.Labels)
	if err != nil {
		return "", err
	}
	if serviceName := labels.Get("service_name"); serviceName != "" {
		return segmentationKey(serviceName), nil
	}
	return segmentationKey("unknown_service"), nil
}

// segmentationPartitionResolver resolves the partition for a segmentation key.
// segmentationPartitionResolver 结合 partition ring 与 perPartitionRateBytes 选择目标分区。
type segmentationPartitionResolver struct {
	perPartitionRateBytes uint64
	ringReader            ring.PartitionRingReader
	logger                log.Logger

	// Metrics.
	resolveFailed prometheus.Counter
	resolveTotal  prometheus.Counter
}

// newSegmentationPartitionResolver returns a new segmentationPartitionResolver.
func newSegmentationPartitionResolver(perPartitionRateBytes uint64, ringReader ring.PartitionRingReader, reg prometheus.Registerer, logger log.Logger) *segmentationPartitionResolver {
	return &segmentationPartitionResolver{
		perPartitionRateBytes: perPartitionRateBytes,
		ringReader:            ringReader,
		resolveFailed: promauto.With(reg).NewCounter(prometheus.CounterOpts{
			Name: "loki_distributor_segmentation_partition_resolver_keys_failed_total",
			Help: "Total number of segmentation keys that could not be resolved.",
		}),
		resolveTotal: promauto.With(reg).NewCounter(prometheus.CounterOpts{
			Name: "loki_distributor_segmentation_partition_resolver_keys_total",
			Help: "Total number of segmentation keys passed to the resolver.",
		}),
		logger: logger,
	}
}

// Resolve 先按租户速率 shuffle-shard 子环，再按分段键速率决定进一步 shuffle 或 hashKey 回退。
func (r *segmentationPartitionResolver) Resolve(ctx context.Context, tenant string, key segmentationKey, hashKey uint32, rateBytes, tenantRateBytes uint64) (int32, error) {
	r.resolveTotal.Inc()
	// We use a snapshot of the partition ring to ensure resolving the
	// partition for a segmentation key is determinstic even if the ring
	// changes.
	ring := r.ringReader.PartitionRing()
	if ring.ActivePartitionsCount() == 0 {
		// If there are no active partitions then we cannot write to any
		// partition as we do not know if the partition we chose will have a
		// consumer.
		r.resolveFailed.Inc()
		return 0, errors.New("no active partitions")
	}
	// Get a subring for the tenant based on their ingestion rate limit.
	// This ensures that streams are not only co-located within the same
	// segmentation key, but also segmentation keys for a tenant are as
	// co-located as possible.
	subring, err := r.tenantShuffleShard(ctx, ring, tenant, tenantRateBytes)
	if err != nil {
		r.resolveFailed.Inc()
		return 0, fmt.Errorf("failed to shuffle shard tenant: %w", err)
	}
	// If the rate is 0, we cannot make a decision to shuffle shard the segmentation
	// key. We fallback to choosing a partition for the hash key.
	if rateBytes == 0 {
		return subring.ActivePartitionForKey(hashKey)
	}
	numShuffleShardPartitions := numPartitionsForRate(rateBytes, r.perPartitionRateBytes, subring.ActivePartitionsCount())
	// If the segmentation key is small enough that it does not need to be sharded,
	// we can avoid doing an expensive shuffle shard.
	if numShuffleShardPartitions == 1 {
		return subring.ActivePartitionForKey(uint32(key.Sum64()))
	}
	subring, err = subring.ShuffleShard(string(key), numShuffleShardPartitions)
	if err != nil {
		r.resolveFailed.Inc()
		return 0, fmt.Errorf("failed to get segmentation key subring: %w", err)
	}
	// TODO(grobinson): We need to use a different method that does not depend on
	// stream sharding, as this information comes from the ingesters.
	return subring.ActivePartitionForKey(hashKey)
}

// tenantShuffleShard returns a subring for the tenant based on their rate limit.
func (r *segmentationPartitionResolver) tenantShuffleShard(_ context.Context, ring *ring.PartitionRing, tenant string, tenantRateBytes uint64) (*ring.PartitionRing, error) {
	// If the tenant has no limit, return the full ring.
	if tenantRateBytes == 0 {
		return ring, nil
	}
	numShuffleShardPartitions := numPartitionsForRate(tenantRateBytes, r.perPartitionRateBytes, ring.ActivePartitionsCount())
	return ring.ShuffleShard(tenant, numShuffleShardPartitions)
}

// numPartitionsForRate returns the number of partitions needed to keep within
// perPartitionRateBytes. It cannot exceed the total number of partitions.
// numPartitionsForRate 根据速率与每分区上限计算所需分区数，至少 1 且不超过总数。
func numPartitionsForRate(rateBytes, perPartitionRateBytes uint64, numPartitions int) int {
	partitions := rateBytes / perPartitionRateBytes
	// Must be at least 1 partition.
	partitions = max(partitions, 1)
	// Must not exceed the total number of partitions.
	partitions = min(partitions, uint64(numPartitions))
	// We can convert back to int here because partitions is guaranteed to be less
	// than or equal to the number of partitions, which is an int.
	return int(partitions)
}
// Sum64 在哈希前写入保留前缀 __loki_segmentation_key__ 以避免与普通流哈希冲突。
