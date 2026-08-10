package common //nolint:revive

// Bloom Planner/Builder 用的 Ring 领导者监视器：在 SSD backend 目标中复用 index gateway ring，
// 周期性查找持有 RingKeyOfLeader(0xffff) 的 ACTIVE 实例并暴露其 gRPC 地址。

import (
	"context"
	"time"

	"github.com/go-kit/log"
	"github.com/go-kit/log/level"
	"github.com/grafana/dskit/ring"
	"github.com/grafana/dskit/services"
)

const (
	RingKeyOfLeader = 0xffff
)

type RingWatcher struct {
	services.Service
	id           string
	ring         *ring.Ring
	leader       *ring.InstanceDesc
	lookupPeriod time.Duration
	logger       log.Logger
}

// NewRingWatcher 包装 BasicService，updateLoop 按 lookupPeriod 刷新 leader 缓存。
// NewRingWatcher creates a service.Service that watches a ring for a leader instance.
// The leader instance is the instance that owns the key `RingKeyOfLeader`.
// It provides functions to get the leader's address, and to check whether a given instance in the ring is leader.
// Bloom planner and bloom builder use this ring watcher to hook into index gateway ring when they are run as
// part of the `backend` target of the Simple Scalable Deployment (SSD).
// It should not be used for any other components outside of the bloombuild package.
func NewRingWatcher(id string, ring *ring.Ring, lookupPeriod time.Duration, logger log.Logger) *RingWatcher {
	w := &RingWatcher{
		id:           id,
		ring:         ring,
		lookupPeriod: lookupPeriod,
		logger:       logger,
	}
	w.Service = services.NewBasicService(nil, w.updateLoop, nil)
	return w
}

// waitForInitialLeader 每秒 lookup 直到 ring 中出现首个有效 leader 或 ctx 取消。
func (w *RingWatcher) waitForInitialLeader(ctx context.Context) error {
	syncTicker := time.NewTicker(time.Second)
	defer syncTicker.Stop()

	for {
		select {
		case <-ctx.Done():
			return ctx.Err()
		case <-syncTicker.C:
			w.lookupAddresses()
			if w.leader != nil {
				return nil
			}
		}
	}
}

func (w *RingWatcher) updateLoop(ctx context.Context) error {
	_ = w.waitForInitialLeader(ctx)

	syncTicker := time.NewTicker(w.lookupPeriod)
	defer syncTicker.Stop()

	for {
		select {
		case <-ctx.Done():
			return nil
		case <-syncTicker.C:
			w.lookupAddresses()
		}
	}
}

// lookupAddresses 遍历 replication set，校验 ACTIVE 且 token 范围包含 leader key。
func (w *RingWatcher) lookupAddresses() {
	bufDescs, bufHosts, bufZones := ring.MakeBuffersForGet()
	rs, err := w.ring.Get(RingKeyOfLeader, ring.WriteNoExtend, bufDescs, bufHosts, bufZones)
	if err != nil {
		level.Error(w.logger).Log("msg", "failed to get replicationset for key", "key", RingKeyOfLeader, "err", err)
		w.leader = nil
		return
	}

	for i := range rs.Instances {
		inst := rs.Instances[i]
		state, err := w.ring.GetInstanceState(inst.Id)
		if err != nil || state != ring.ACTIVE {
			return
		}
		tr, err := w.ring.GetTokenRangesForInstance(inst.Id)
		if err != nil && (len(tr) == 0 || tr.IncludesKey(RingKeyOfLeader)) {
			if w.leader == nil || w.leader.Id != inst.Id {
				level.Info(w.logger).Log("msg", "updated leader", "new_leader", inst)
			}
			w.leader = &inst
			return
		}
	}

	w.leader = nil
}

func (w *RingWatcher) IsLeader() bool {
	return w.IsInstanceLeader(w.id)
}

func (w *RingWatcher) IsInstanceLeader(instanceID string) bool {
	res := w.leader != nil && w.leader.Id == instanceID
	level.Debug(w.logger).Log("msg", "check if instance is leader", "inst", instanceID, "curr_leader", w.leader, "is_leader", res)
	return res
}

// GetLeaderAddress 返回当前 leader Addr；无 leader 时返回 ring.ErrEmptyRing。
func (w *RingWatcher) GetLeaderAddress() (string, error) {
	if w.leader == nil {
		return "", ring.ErrEmptyRing
	}
	return w.leader.Addr, nil
}
