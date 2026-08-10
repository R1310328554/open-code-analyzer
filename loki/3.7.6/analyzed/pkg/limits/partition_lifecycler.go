package limits

// partitionLifecycler 响应 Kafka consumer group 分区分配/回收回调，
// 根据 offset 决定分区进入 pending、replaying 或 ready 状态。

import (
	"context"
	"fmt"
	"sync"
	"time"

	"github.com/coder/quartz"
	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/twmb/franz-go/pkg/kgo"

	kafka_partition "github.com/grafana/loki/v3/pkg/kafka/partition"
)

// partitionLifecycler 桥接 franz-go 分区生命周期与 partitionManager/usageStore。
// partitionLifecycler manages assignment and revocation of partitions.
type partitionLifecycler struct {
	partitionManager *partitionManager
	offsetManager    kafka_partition.OffsetManager
	usage            *usageStore
	activeWindow     time.Duration
	logger           log.Logger

	// Used in tests.
	clock quartz.Clock
}

// newPartitionLifecycler returns a new partitionLifecycler.
func newPartitionLifecycler(
	partitionManager *partitionManager,
	offsetManager kafka_partition.OffsetManager,
	usage *usageStore,
	activeWindow time.Duration,
	logger log.Logger,
) *partitionLifecycler {
	return &partitionLifecycler{
		partitionManager: partitionManager,
		offsetManager:    offsetManager,
		usage:            usage,
		activeWindow:     activeWindow,
		logger:           logger,
		clock:            quartz.NewReal(),
	}
}

// Assign 注册新分区并并发检查 offset 以确定是否需要 replay 历史 metadata。
// Assign implements kgo.OnPartitionsAssigned.
func (l *partitionLifecycler) Assign(ctx context.Context, _ *kgo.Client, topics map[string][]int32) {
	if len(topics) > 1 {
		panic(fmt.Sprintf("expected one topic, received %d topics", len(topics)))
	}
	// We expect just one topic, and panic if topics contains more than one
	// topic. The range over topics just makes it easier to access the first
	// value in a map containing a single key.
	wg := sync.WaitGroup{}
	for _, partitions := range topics {
		l.partitionManager.Assign(partitions)
		for _, partition := range partitions {
			wg.Add(1)
			go func(partition int32) {
				defer wg.Done()
				if err := l.determineStateFromOffsets(ctx, partition); err != nil {
					level.Error(l.logger).Log(
						"msg", "failed to check offsets, partition is ready",
						"partition", partition,
						"err", err,
					)
					l.partitionManager.SetReady(partition)
				}
			}(partition)
		}
	}
	wg.Wait()
}

// Revoke 在 rebalance 正常回收时撤销分区并清空对应 usage 缓存。
// Revoke implements kgo.OnPartitionsRevoked.
func (l *partitionLifecycler) Revoke(ctx context.Context, client *kgo.Client, topics map[string][]int32) {
	l.revoke(ctx, client, topics)
}

// Lost 在分区意外丢失时与 Revoke 走相同清理路径。
// Lost implements kgo.OnPartitionsLost.
func (l *partitionLifecycler) Lost(ctx context.Context, client *kgo.Client, topics map[string][]int32) {
	l.revoke(ctx, client, topics)
}

// Revokes all partitions in topics. It expects just one topic and panics if
// topics contains more than one topic.
func (l *partitionLifecycler) revoke(_ context.Context, _ *kgo.Client, topics map[string][]int32) {
	if len(topics) > 1 {
		panic(fmt.Sprintf("expected one topic, received %d topics", len(topics)))
	}
	// The range over topics just makes it easier to access the first value
	// in a map containing a single key.
	for _, partitions := range topics {
		l.partitionManager.Revoke(partitions)
		l.usage.EvictPartitions(partitions)
	}
}

// determineStateFromOffsets 比较 start/end/next offset 判断分区是否需 replay。
func (l *partitionLifecycler) determineStateFromOffsets(ctx context.Context, partition int32) error {
	logger := log.With(l.logger, "partition", partition)
	// Get the start offset for the partition. This can be greater than zero
	// if a retention period has deleted old records.
	startOffset, err := l.offsetManager.PartitionOffset(
		ctx, partition, kafka_partition.KafkaStartOffset)
	if err != nil {
		return fmt.Errorf("failed to get last produced offset: %w", err)
	}
	// The last produced offset is the next offset after the last produced
	// record. For example, if a partition contains 1 record, then the last
	// produced offset is 1. However, the offset of the last produced record
	// is 0, as offsets start from 0.
	lastProducedOffset, err := l.offsetManager.PartitionOffset(
		ctx, partition, kafka_partition.KafkaEndOffset)
	if err != nil {
		return fmt.Errorf("failed to get last produced offset: %w", err)
	}
	// Get the first offset produced within the window. This can be the same
	// offset as the last produced offset if no records have been produced
	// within that time.
	nextOffset, err := l.offsetManager.NextOffset(ctx, partition, l.clock.Now().Add(-l.activeWindow))
	if err != nil {
		return fmt.Errorf("failed to get next offset: %w", err)
	}
	level.Debug(logger).Log(
		"msg", "fetched offsets",
		"start_offset", startOffset,
		"last_produced_offset", lastProducedOffset,
		"next_offset", nextOffset,
	)
// 分区无有效记录（从未写入或 retention 已清空）时直接标记 ready。
	if startOffset >= lastProducedOffset {
		// The partition has no records. This happens when either the
		// partition has never produced a record, or all records that have
		// been produced have been deleted due to the retention period.
		level.Debug(logger).Log("msg", "no records in partition, partition is ready")
		l.partitionManager.SetReady(partition)
		return nil
	}
// 活跃窗口内无新记录时无需 replay，分区可立即 ready。
	if nextOffset == lastProducedOffset {
		level.Debug(logger).Log("msg", "no records within window size, partition is ready")
		l.partitionManager.SetReady(partition)
		return nil
	}
	// Since we want to fetch all records up to and including the last
	// produced record, we must fetch all records up to and including the
	// last produced offset - 1.
	level.Debug(logger).Log("msg", "partition is replaying")
// 需 replay 时设置目标 offset 为最后一条已生产记录的 offset。
	l.partitionManager.SetReplaying(partition, lastProducedOffset-1)
	return nil
}
// Revoke/Lost 会 EvictPartitions，防止 rebalance 后残留旧分区的流计数。
