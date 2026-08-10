package limits

// partitionManager 跟踪 Kafka 分区分配状态机：pending→replaying→ready，
// 并暴露 Prometheus 指标供运维观察各分区 replay 进度。

import (
	"fmt"
	"strconv"
	"sync"

	"github.com/coder/quartz"
	"github.com/prometheus/client_golang/prometheus"
)

var (
	// partitionsDesc is a gauge which tracks the state of the partitions
	// in the partitionManager. The value of the gauge is set to the value of
	// the partitionState enum.
	partitionsDesc = prometheus.NewDesc(
		"loki_ingest_limits_partitions",
		"The state of each partition.",
		[]string{"partition"},
		nil,
	)
)

// partitionState 枚举分区生命周期：未知、待处理、回放中、就绪。
type partitionState int

const (
	// partitionUnknown is the zero value.
	partitionUnknown partitionState = iota
	partitionPending
	partitionReplaying
	partitionReady
)

// String implements the [fmt.Stringer] interface.
func (s partitionState) String() string {
	switch s {
	case partitionPending:
		return "pending"
	case partitionReplaying:
		return "replaying"
	case partitionReady:
		return "ready"
	default:
		return "unknown"
	}
}

// partitionManager 维护 partition→{assignedAt,targetOffset,state} 映射。
// partitionManager keeps track of the partitions assigned and for
// each partition a timestamp of when it was assigned.
type partitionManager struct {
	partitions map[int32]partitionEntry
	mtx        sync.RWMutex

	// Used for tests.
	clock quartz.Clock
}

// partitionEntry 记录分区分配时间戳、replay 目标 offset 与当前状态。
// partitionEntry contains metadata about an assigned partition.
type partitionEntry struct {
	assignedAt   int64
	targetOffset int64
	state        partitionState
}

// newPartitionManager returns a new [PartitionManager].
func newPartitionManager(reg prometheus.Registerer) (*partitionManager, error) {
	m := partitionManager{
		partitions: make(map[int32]partitionEntry),
		clock:      quartz.NewReal(),
	}
	if err := reg.Register(&m); err != nil {
		return nil, fmt.Errorf("failed to register metrics: %w", err)
	}
	return &m, nil
}

// Assign assigns the partitions.
func (m *partitionManager) Assign(partitions []int32) {
	m.mtx.Lock()
	defer m.mtx.Unlock()
	for _, partition := range partitions {
		m.partitions[partition] = partitionEntry{
			assignedAt: m.clock.Now().UnixNano(),
			state:      partitionPending,
		}
	}
}

// CheckReady 在 CheckReady 健康检查中判定服务是否完成分区 warmup。
// CheckReady returns true if all partitions are ready.
func (m *partitionManager) CheckReady() bool {
	m.mtx.RLock()
	defer m.mtx.RUnlock()
	for _, entry := range m.partitions {
		if entry.state != partitionReady {
			return false
		}
	}
	return true
}

// Count returns the number of assigned partitions.
func (m *partitionManager) Count() int {
	m.mtx.Lock()
	defer m.mtx.Unlock()
	return len(m.partitions)
}

// GetState returns the current state of the partition. It returns false
// if the partition does not exist.
func (m *partitionManager) GetState(partition int32) (partitionState, bool) {
	m.mtx.RLock()
	defer m.mtx.RUnlock()
	entry, ok := m.partitions[partition]
	return entry.state, ok
}

// TargetOffsetReached 供 consumer 判断 replay 是否已消费到目标 offset。
// TargetOffsetReached returns true if the partition is replaying and the
// target offset has been reached.
func (m *partitionManager) TargetOffsetReached(partition int32, offset int64) bool {
	m.mtx.RLock()
	defer m.mtx.RUnlock()
	entry, ok := m.partitions[partition]
	if ok {
		return entry.state == partitionReplaying && entry.targetOffset <= offset
	}
	return false
}

// Has returns true if the partition is assigned, otherwise false.
func (m *partitionManager) Has(partition int32) bool {
	m.mtx.RLock()
	defer m.mtx.RUnlock()
	_, ok := m.partitions[partition]
	return ok
}

// List returns a map of all assigned partitions and the timestamp of when
// each partition was assigned.
func (m *partitionManager) List() map[int32]int64 {
	m.mtx.RLock()
	defer m.mtx.RUnlock()
	result := make(map[int32]int64)
	for partition, entry := range m.partitions {
		result[partition] = entry.assignedAt
	}
	return result
}

// ListByState returns all partitions with the specified state and their last
// updated timestamps.
func (m *partitionManager) ListByState(state partitionState) map[int32]int64 {
	m.mtx.RLock()
	defer m.mtx.RUnlock()
	result := make(map[int32]int64)
	for partition, entry := range m.partitions {
		if entry.state == state {
			result[partition] = entry.assignedAt
		}
	}
	return result
}

// SetReplaying 设置 replay 目标；consumer 到达该 offset 后调用 SetReady。
// SetReplaying sets the partition as replaying and the offset that must
// be consumed for it to become ready. It returns false if the partition
// does not exist.
func (m *partitionManager) SetReplaying(partition int32, offset int64) bool {
	m.mtx.Lock()
	defer m.mtx.Unlock()
	entry, ok := m.partitions[partition]
	if ok {
		entry.state = partitionReplaying
		entry.targetOffset = offset
		m.partitions[partition] = entry
	}
	return ok
}

// SetReady sets the partition as ready. It returns false if the partition
// does not exist.
func (m *partitionManager) SetReady(partition int32) bool {
	m.mtx.Lock()
	defer m.mtx.Unlock()
	entry, ok := m.partitions[partition]
	if ok {
		entry.state = partitionReady
		entry.targetOffset = 0
		m.partitions[partition] = entry
	}
	return ok
}

// Revoke deletes the partitions.
func (m *partitionManager) Revoke(partitions []int32) {
	m.mtx.Lock()
	defer m.mtx.Unlock()
	for _, partition := range partitions {
		delete(m.partitions, partition)
	}
}

// Describe implements [prometheus.Collector].
func (m *partitionManager) Describe(descs chan<- *prometheus.Desc) {
	descs <- partitionsDesc
}

// Collect implements [prometheus.Collector].
func (m *partitionManager) Collect(metrics chan<- prometheus.Metric) {
	m.mtx.RLock()
	defer m.mtx.RUnlock()
	for partition, entry := range m.partitions {
		metrics <- prometheus.MustNewConstMetric(
			partitionsDesc,
			prometheus.GaugeValue,
			float64(entry.state),
			strconv.FormatInt(int64(partition), 10),
		)
	}
}
// loki_ingest_limits_partitions 指标以 gauge 形式导出各分区 state 枚举值。
