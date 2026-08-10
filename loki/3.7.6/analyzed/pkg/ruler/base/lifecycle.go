package base

// lifecycle 实现 ruler 在 hash ring 上的 BasicLifecycler 回调：注册时生成 token、保持 ACTIVE 状态。

import (
	"github.com/grafana/dskit/ring"
)

func (r *Ruler) OnRingInstanceRegister(_ *ring.BasicLifecycler, ringDesc ring.Desc, instanceExists bool, _ string, instanceDesc ring.InstanceDesc) (ring.InstanceState, ring.Tokens) {
// OnRingInstanceRegister 注册时将实例置 ACTIVE 并补齐 NumTokens 个随机 token。
	// When we initialize the ruler instance in the ring we want to start from
	// a clean situation, so whatever is the state we set it ACTIVE, while we keep existing
	// tokens (if any).
	var tokens []uint32
	if instanceExists {
		tokens = instanceDesc.GetTokens()
	}

	takenTokens := ringDesc.GetTokens()
	gen := ring.NewRandomTokenGenerator()
	newTokens := gen.GenerateTokens(r.cfg.Ring.NumTokens-len(tokens), takenTokens)

	// Tokens sorting will be enforced by the parent caller.
	tokens = append(tokens, newTokens...)

	return ring.ACTIVE, tokens
}

// OnRingInstanceTokens/Stopping/Heartbeat 为 lifecycler 占位回调，ruler 无需额外逻辑。
func (r *Ruler) OnRingInstanceTokens(_ *ring.BasicLifecycler, _ ring.Tokens) {}
func (r *Ruler) OnRingInstanceStopping(_ *ring.BasicLifecycler)              {}
func (r *Ruler) OnRingInstanceHeartbeat(_ *ring.BasicLifecycler, _ *ring.Desc, _ *ring.InstanceDesc) {
}
// token 排序由 lifecycler 父调用保证，避免 ring 中出现无序 token 列表。
