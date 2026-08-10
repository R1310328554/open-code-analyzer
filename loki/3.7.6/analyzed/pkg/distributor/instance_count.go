package distributor

// healthyInstanceDelegate：在 distributor ring 心跳回调中统计 ACTIVE 且心跳健康的实例数。

import (
	"time"

	"github.com/grafana/dskit/ring"
	"go.uber.org/atomic"
)

// healthyInstanceDelegate 将计数写入 atomic.Uint32，供全局 ingestion 限流均摊配额。
// healthyInstanceDelegate counts the number of healthy instances that are part of the ring
// and stores the count to the provided atomic integer. Used here to count the number of
// distributors in the ring to determine how to enforce rate limiting.
type healthyInstanceDelegate struct {
	count            *atomic.Uint32
	heartbeatTimeout time.Duration

	ring.BasicLifecyclerDelegate
}

// newHealthyInstanceDelegate 包装 BasicLifecyclerDelegate 以在心跳时维护实例计数。
func newHealthyInstanceDelegate(count *atomic.Uint32, heartbeatTimeout time.Duration, next ring.BasicLifecyclerDelegate) *healthyInstanceDelegate {
	return &healthyInstanceDelegate{count: count, heartbeatTimeout: heartbeatTimeout, BasicLifecyclerDelegate: next}
}

// OnRingInstanceHeartbeat 遍历 ring 描述符更新活跃成员计数并链式调用下层 delegate。
// OnRingInstanceHeartbeat implements the ring.BasicLifecyclerDelegate interface
func (d *healthyInstanceDelegate) OnRingInstanceHeartbeat(lifecycler *ring.BasicLifecycler, ringDesc *ring.Desc, instanceDesc *ring.InstanceDesc) {
	activeMembers := uint32(0)
	now := time.Now()

	for _, instance := range ringDesc.Ingesters {
		if ring.ACTIVE == instance.State && instance.IsHeartbeatHealthy(d.heartbeatTimeout, now) {
			activeMembers++
		}
	}

	d.count.Store(activeMembers)
	d.BasicLifecyclerDelegate.OnRingInstanceHeartbeat(lifecycler, ringDesc, instanceDesc)
}
// 仅统计 State 为 ACTIVE 且 IsHeartbeatHealthy 的 ingester 环成员（distributor 复用该模式）。
