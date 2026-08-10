package ingester

// recalculate_owned_streams 定时检测 ring 变化并重新评估 stream 所有权，支持经典 ingester ring 与 partition ring 两种策略。

import (
	"context"
	"fmt"
	"slices"
	"sync"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/ring"
	"github.com/grafana/dskit/services"

	lokiring "github.com/grafana/loki/v3/pkg/util/ring"
)

// ownershipStrategy 抽象 ring 变更检测与单 stream 所有权判定。
type ownershipStrategy interface {
	checkRingForChanges() (bool, error)
	isOwnedStream(*stream) (bool, error)
}

// recalculateOwnedStreamsSvc 类型封装该模块的状态与行为。
type recalculateOwnedStreamsSvc struct {
	services.Service

	logger log.Logger

	ownershipStrategy ownershipStrategy
	instancesSupplier func() []*instance
	ticker            *time.Ticker
}

// newRecalculateOwnedStreamsSvc 实现该路径上的核心处理逻辑。
func newRecalculateOwnedStreamsSvc(instancesSupplier func() []*instance, ownershipStrategy ownershipStrategy, ringPollInterval time.Duration, logger log.Logger) *recalculateOwnedStreamsSvc {
	svc := &recalculateOwnedStreamsSvc{
		instancesSupplier: instancesSupplier,
		logger:            logger,
		ownershipStrategy: ownershipStrategy,
	}
	svc.Service = services.NewTimerService(ringPollInterval, nil, svc.iteration, nil)
	return svc
}

// iteration 实现该路径上的核心处理逻辑。
func (s *recalculateOwnedStreamsSvc) iteration(_ context.Context) error {
	s.recalculate()
	return nil
}

// recalculate 实现该路径上的核心处理逻辑。
func (s *recalculateOwnedStreamsSvc) recalculate() {
	level.Info(s.logger).Log("msg", "starting recalculate owned streams job")
	defer func() {
		s.updateFixedLimitForAll()
		level.Info(s.logger).Log("msg", "completed recalculate owned streams job")
	}()
	ringChanged, err := s.ownershipStrategy.checkRingForChanges()
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to check ring for changes", "err", err)
		return
	}
	if !ringChanged {
		level.Debug(s.logger).Log("msg", "ring is not changed, skipping the job")
		return
	}
	level.Info(s.logger).Log("msg", "detected ring changes, re-evaluating streams ownership")

	for _, instance := range s.instancesSupplier() {
		if !instance.limiter.limits.UseOwnedStreamCount(instance.instanceID) {
			continue
		}

		level.Info(s.logger).Log("msg", "updating streams ownership", "tenant", instance.instanceID)
		err := instance.updateOwnedStreams(s.ownershipStrategy.isOwnedStream)
		if err != nil {
			level.Error(s.logger).Log("msg", "failed to re-evaluate streams ownership", "tenant", instance.instanceID, "err", err)
		}
	}
}

// updateFixedLimitForAll 实现该路径上的核心处理逻辑。
func (s *recalculateOwnedStreamsSvc) updateFixedLimitForAll() {
	for _, instance := range s.instancesSupplier() {
		oldLimit, newLimit := instance.ownedStreamsSvc.updateFixedLimit()
		if oldLimit != newLimit {
			level.Info(s.logger).Log("msg", "fixed limit has been updated", "tenant", instance.instanceID, "old", oldLimit, "new", newLimit)
		}
	}
}

// ownedStreamsIngesterStrategy 类型封装该模块的状态与行为。
type ownedStreamsIngesterStrategy struct {
	logger log.Logger

	ingesterID    string
	previousRing  ring.ReplicationSet
	ingestersRing ring.ReadRing

	descsBufPool sync.Pool
	hostsBufPool sync.Pool
	zoneBufPool  sync.Pool
}

// newOwnedStreamsIngesterStrategy 实现该路径上的核心处理逻辑。
func newOwnedStreamsIngesterStrategy(ingesterID string, ingestersRing ring.ReadRing, logger log.Logger) *ownedStreamsIngesterStrategy {
	return &ownedStreamsIngesterStrategy{
		ingesterID:    ingesterID,
		ingestersRing: ingestersRing,
		logger:        logger,
		descsBufPool: sync.Pool{New: func() interface{} {
			return make([]ring.InstanceDesc, ingestersRing.ReplicationFactor()+1)
		}},
		hostsBufPool: sync.Pool{New: func() interface{} {
			return make([]string, ingestersRing.ReplicationFactor()+1)
		}},
		zoneBufPool: sync.Pool{New: func() interface{} {
			return make([]string, ingestersRing.ZonesCount()+1)
		}},
	}
}

// 检测 ring 拓扑是否相对上次快照发生变化。
func (s *ownedStreamsIngesterStrategy) checkRingForChanges() (bool, error) {
	rs, err := s.ingestersRing.GetAllHealthy(ring.WriteNoExtend)
	if err != nil {
		return false, err
	}

	ringChanged := ring.HasReplicationSetChangedWithoutStateOrAddr(s.previousRing, rs)
	s.previousRing = rs
	return ringChanged, nil
}

//nolint:staticcheck
// 判断 stream 是否由本 ingester/partition 负责。
func (s *ownedStreamsIngesterStrategy) isOwnedStream(str *stream) (bool, error) {
	descsBuf := s.descsBufPool.Get().([]ring.InstanceDesc)
	hostsBuf := s.hostsBufPool.Get().([]string)
	zoneBuf := s.zoneBufPool.Get().([]string)
	defer func() {
		s.descsBufPool.Put(descsBuf[:0])
		s.hostsBufPool.Put(hostsBuf[:0])
		s.zoneBufPool.Put(zoneBuf[:0])
	}()

	replicationSet, err := s.ingestersRing.Get(lokiring.TokenFor(str.tenant, str.labelsString), ring.WriteNoExtend, descsBuf, hostsBuf, zoneBuf)
	if err != nil {
		return false, fmt.Errorf("error getting replication set for stream %s: %v", str.labelsString, err)
	}
	return s.isOwnedStreamInner(replicationSet, s.ingesterID), nil
}

// isOwnedStreamInner 执行条件判断并返回布尔结果。
func (s *ownedStreamsIngesterStrategy) isOwnedStreamInner(replicationSet ring.ReplicationSet, ingesterID string) bool {
	for _, instanceDesc := range replicationSet.Instances {
		if instanceDesc.Id == ingesterID {
			return true
		}
	}
	return false
}

// ownedStreamsPartitionStrategy 类型封装该模块的状态与行为。
type ownedStreamsPartitionStrategy struct {
	logger log.Logger

	partitionID              int32
	partitionRingWatcher     ring.PartitionRingReader
	previousActivePartitions []int32
	getPartitionShardSize    func(user string) int
}

// newOwnedStreamsPartitionStrategy 实现该路径上的核心处理逻辑。
func newOwnedStreamsPartitionStrategy(partitionID int32, ring ring.PartitionRingReader, getPartitionShardSize func(user string) int, logger log.Logger) *ownedStreamsPartitionStrategy {
	return &ownedStreamsPartitionStrategy{
		partitionID:           partitionID,
		partitionRingWatcher:  ring,
		logger:                logger,
		getPartitionShardSize: getPartitionShardSize,
	}
}

// 检测 ring 拓扑是否相对上次快照发生变化。
func (s *ownedStreamsPartitionStrategy) checkRingForChanges() (bool, error) {
	// When using partitions ring, we consider ring to be changed if active partitions have changed.
	r := s.partitionRingWatcher.PartitionRing()
	if r.PartitionsCount() == 0 {
		return false, ring.ErrEmptyRing
	}
	// todo(ctovena): We might need to consider partition shard size changes as well.
	activePartitions := r.ActivePartitionIDs()
	ringChanged := !slices.Equal(s.previousActivePartitions, activePartitions)
	s.previousActivePartitions = activePartitions
	return ringChanged, nil
}

// 判断 stream 是否由本 ingester/partition 负责。
func (s *ownedStreamsPartitionStrategy) isOwnedStream(str *stream) (bool, error) {
	subring, err := s.partitionRingWatcher.PartitionRing().ShuffleShard(str.tenant, s.getPartitionShardSize(str.tenant))
	if err != nil {
		return false, fmt.Errorf("failed to get shuffle shard for stream: %w", err)
	}
	partitionForStream, err := subring.ActivePartitionForKey(lokiring.TokenFor(str.tenant, str.labelsString))
	if err != nil {
		return false, fmt.Errorf("failed to find active partition for stream: %w", err)
	}

	return partitionForStream == s.partitionID, nil
}
// ring 未变化时跳过全量重算，降低周期性开销。
