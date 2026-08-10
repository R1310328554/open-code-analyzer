package ring

// util/ring sharding 实现租户 shuffle sharding：按 tenantID 子环判断当前实例是否负责该租户数据。

import (
	"github.com/grafana/dskit/ring"
)

type TenantSharding interface {
	OwnsTenant(tenantID string) (tenantRing ring.ReadRing, owned bool)
}

type TenantShuffleSharding struct {
	r                  ring.ReadRing
	ringLifeCycler     *ring.BasicLifecycler
	shardSizeForTenant func(tenantID string) int
}

func NewTenantShuffleSharding(
	r ring.ReadRing,
	ringLifeCycler *ring.BasicLifecycler,
	shardSizeForTenant func(tenantID string) int,
) *TenantShuffleSharding {
	return &TenantShuffleSharding{
		r:                  r,
		ringLifeCycler:     ringLifeCycler,
		shardSizeForTenant: shardSizeForTenant,
	}
}

func (s *TenantShuffleSharding) OwnsTenant(tenantID string) (ring.ReadRing, bool) {
	// A shard size of 0 means shuffle sharding is disabled for this specific user,
	shardSize := s.shardSizeForTenant(tenantID)
	if shardSize <= 0 {
		return s.r, true
	}

	subRing := s.r.ShuffleShard(tenantID, shardSize)
	if subRing.HasInstance(s.ringLifeCycler.GetInstanceID()) {
		return subRing, true
	}

	return nil, false
}

// NoopStrategy 恒返回 nil,false，用于禁用 shuffle sharding 的占位实现。
// NoopStrategy is an implementation of the ShardingStrategy that does not
// shard anything.
type NoopStrategy struct{}

// OwnsTenant implements TenantShuffleSharding.
func (s *NoopStrategy) OwnsTenant(_ string) (ring.ReadRing, bool) {
	return nil, false
}
// shardSize 为 0 表示对该租户禁用 shuffle，直接返回完整环且 owned 为 true。
