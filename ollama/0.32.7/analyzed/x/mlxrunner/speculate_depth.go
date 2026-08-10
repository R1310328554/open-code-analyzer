// 投机深度控制器：EV 最优 draft 深度、成本 EWMA、接受率模型与探测 cadence。
package mlxrunner

import (
	"fmt"
	"slices"
	"strings"
	"time"
)

// depthProbeInterval 为探测 cadence 基值：周期性 draft 超过当前选择深度。
// depthProbeInterval is the base cadence at which the controller drafts one past its
// selection to refresh the next position up; without it a shallow controller never
// notices a deeper draft becoming worthwhile.
const depthProbeInterval = 4

// depthProbeIntervalMax 限制探测退避上限，避免无限 plain decode。
// depthProbeIntervalMax caps the probe backoff. Probes that keep changing nothing
// double the interval up to this cap, trading slower re-engagement — worst case
// plain decode speed, never below — for fewer probes.
const depthProbeIntervalMax = 512

// depthController 在 [0,limit] 上选 EV 最优 draft 深度并周期性 probe。
// depthController drafts argmax_N EV(N) where EV(N) = committed(N) / cost(N), over
// depths from 0 (plain decode) up to one past the frontier, or the drafter's limit
// where that is shallower. The depth-0 floor lets it stop speculating when no draft
// pays; the frontier ceiling keeps it from scoring a depth on the optimistic
// inherited rate, so it climbs outward one position at a time as acceptance is
// measured rather than leaping deep.
//
// It holds the depth-selection state learned across requests — the target forward's
// per-depth cost curve, the drafts' per-position acceptance rates, the probe cadence,
// and the depth scheduled for the next round — persisted on the speculation so a fresh
// request starts at the proven-out depth instead of re-ramping from shallow.
// depthController 跨请求持久成本/接受率/探测状态。
type depthController struct {
	cost   *costModel
	acc    *acceptanceModel
	probed bool // the previous round drafted a probe

	// drafterLimit is the deepest draft this model's drafter can produce, 0 when
	// nothing bounds it. A depth the drafter never produces is never measured,
	// and an unmeasured depth would always look best, pinning the search there.
	drafterLimit int

	// scheduled is the draft depth next() chose for the upcoming round, carried
	// across requests so a new request's first round consumes it instead of
	// recomputing at the boundary. Its zero value is the depth-0 warmup start.
	scheduled int

	// Probe cadence, persisted so a backed-off request need not restart at the base.
	probeInterval int // rounds between probes; backs off while probes change nothing
	probeSince    int // rounds since the cadence was last calibrated
	lastSelected  int // the selection the cadence was calibrated against
}

// newDepthController 初始化 cost/acceptance 模型与 probe 间隔。
func newDepthController() *depthController {
	return &depthController{cost: newCostModel(), acc: newAcceptanceModel(), probeInterval: depthProbeInterval}
}

// frontier 返回 acceptance 模型 frontier。
func (c *depthController) frontier() int { return c.acc.frontier() }

// limit is the deepest depth the search considers: one past the frontier, held
// to what the drafter can produce.
// limit 返回搜索上限 min(frontier+1, drafterLimit)。
func (c *depthController) limit() int {
	limit := c.frontier() + 1
	if c.drafterLimit > 0 {
		limit = min(limit, c.drafterLimit)
	}
	return limit
}

// next 返回下轮 draft 深度：EV 最优或 probe/cost-seed。
// next returns the draft depth for the upcoming step: the EV-optimal depth (held
// to the search limit), except periodically it probes one past the selection to
// refresh the next position up. The probe stays within that limit. The cadence
// doubles toward its cap while probes change nothing and resets on any selection
// change, giving the new selection a full interval to settle. The chosen depth is
// recorded in c.scheduled for the next request's open to consume.
// next 选择下轮深度并更新 scheduled/probe 状态。
func (c *depthController) next() (depth int) {
	defer func() { c.scheduled = depth }()
	sel := c.selected()
	if sel != c.lastSelected {
		c.probeInterval = depthProbeInterval
		c.probeSince = 0
		c.lastSelected = sel
	} else if c.probed {
		c.probeInterval = min(c.probeInterval*2, depthProbeIntervalMax)
	}
	c.probed = false

	c.probeSince++
	if c.probeSince >= c.probeInterval {
		c.probeSince = 0
		if probe := min(sel+1, c.limit()); probe != sel {
			c.probed = true
			return probe
		}
	}
	// Seed a clean cost sample for every depth in [0, frontier+1] before judging by
	// EV. Cost is only recorded when a round's draft depth matches the prior round's
	// (the same-depth gate dropping batch-shape transitions), so a depth never dwelt
	// at has no clean sample; draft the shallowest such depth to dwell there. Without
	// this the controller stays at the one depth it can sit at without a transition
	// (depth 0) and never learns that drafting pays on a deep-optimum model.
	if seed := c.costSeedDepth(); seed >= 0 {
		return seed
	}
	return sel
}

// costSeedDepth 返回最浅未采样成本的深度，或 -1。
// costSeedDepth is the shallowest depth within the search limit with no clean
// cost sample, or -1 if all are sampled; the bound keeps cost-seeding from
// outrunning the acceptance frontier or the drafter.
// costSeedDepth 找最浅未采样成本的深度。
func (c *depthController) costSeedDepth() int {
	limit := c.limit()
	for n := 0; n <= limit; n++ {
		if !c.cost.sampled(n) {
			return n
		}
	}
	return -1
}

// selected 返回 EV 最优深度（不修改 probe 状态）。
// selected returns the EV-optimal draft depth without mutating probe state, the
// argmax within the search limit. The frontier bound keeps the inherited optimistic
// rate from making ever-deeper depths look best; the depth-0 floor lets it stop
// speculating. Returns 0 until the cost model can compare depths.
// selected 计算 EV 最优 draft 深度。
func (c *depthController) selected() int {
	if !c.cost.ready() {
		return 0
	}
	limit := c.limit()
	best, bestEV := 0, c.acc.expectedCommitted(0)/c.cost.cost(0)
	for n := 1; n <= limit; n++ {
		if ev := c.acc.expectedCommitted(n) / c.cost.cost(n); ev > bestEV {
			best, bestEV = n, ev
		}
	}
	return best
}

// costEWMAAlpha weights the newest cost sample. Fixed-depth cost is low-variance,
// so a responsive alpha converges in a few visits while smoothing scheduler jitter.
const costEWMAAlpha = 0.3

// costModel 用 EWMA 估计各 draft 深度的 target forward 耗时。
// costModel is the target-forward cost for validating N drafts — a fused batch of
// 1 current token plus N drafts — as an EWMA per visited draft depth read by
// piecewise-linear interpolation between samples (flat beyond the extremes).
// Interpolation assumes no curve shape, so a steep compute-bound or flat
// bandwidth-bound forward is represented as measured. Cost is static within a run,
// learned from decode steps that already sync the forward, so there is no startup
// probe.
// costModel 保存各深度 EWMA 毫秒成本与采样深度列表。
type costModel struct {
	ewma   map[int]float64 // EWMA of measured ms by draft depth
	depths []int           // sampled draft depths, sorted; updated as new depths arrive
}

// newCostModel 创建空 cost EWMA 模型。
func newCostModel() *costModel {
	return &costModel{ewma: map[int]float64{}}
}

// costClampFraction bounds one sample's EWMA innovation. A host stall (cache trim,
// backpressure) can inflate a sample severalfold; unclamped it can flip the EV
// comparison against plain decode, and once the controller stops parking it stops
// resampling depth 0, so the error never heals. A genuine cost change still
// converges because every sample keeps pushing toward it.
const costClampFraction = 0.25

// observe folds one forward's wall time into the draft depth's EWMA, clamping the
// innovation so one stall-inflated sample cannot move it far.
// observe 将 wall time fold 进指定深度的 EWMA（带 clamp）。
func (m *costModel) observe(drafts int, dt time.Duration) {
	if drafts < 0 || dt <= 0 {
		return
	}
	ms := float64(dt) / float64(time.Millisecond)
	if v, ok := m.ewma[drafts]; ok {
		limit := costClampFraction * v
		m.ewma[drafts] = v + costEWMAAlpha*max(-limit, min(limit, ms-v))
	} else {
		m.ewma[drafts] = ms
		i, _ := slices.BinarySearch(m.depths, drafts)
		m.depths = slices.Insert(m.depths, i, drafts)
	}
}

// ready reports whether two distinct depths have been sampled, so a slope exists.
// ready 报告是否已有至少两个深度样本。
func (m *costModel) ready() bool { return len(m.ewma) >= 2 }

// sampled 检查深度是否已有 EWMA 样本。
func (m *costModel) sampled(drafts int) bool {
	_, ok := m.ewma[drafts]
	return ok
}

// cost 对 draft 深度做分段线性插值估计 forward 毫秒成本。
// cost returns the estimated forward wall time (ms) for validating drafts tokens:
// a piecewise-linear interpolation of the per-depth EWMAs, clamping to the nearest
// sample outside the sampled range (the curve beyond is unknown).
// cost 分段线性插值估计 draft 深度成本。
func (m *costModel) cost(drafts int) float64 {
	ds := m.depths
	if len(ds) == 0 {
		return 0
	}
	if drafts <= ds[0] {
		return m.ewma[ds[0]]
	}
	if drafts >= ds[len(ds)-1] {
		return m.ewma[ds[len(ds)-1]]
	}
	for i := 1; i < len(ds); i++ {
		if drafts <= ds[i] {
			lo, hi := ds[i-1], ds[i]
			t := float64(drafts-lo) / float64(hi-lo)
			return m.ewma[lo] + t*(m.ewma[hi]-m.ewma[lo])
		}
	}
	return m.ewma[ds[len(ds)-1]]
}

// sampleString 格式化各深度 EWMA 成本供调试。
// sampleString reports the EWMA ms per visited draft depth for diagnostics.
// sampleString 输出各深度 EWMA 诊断字符串。
func (m *costModel) sampleString() string {
	ds := m.depths
	parts := make([]string, 0, len(ds))
	for _, d := range ds {
		parts = append(parts, fmt.Sprintf("%d:%.0fms", d, m.ewma[d]))
	}
	return strings.Join(parts, " ")
}

// acceptanceEWMAAlpha weights the newest acceptance outcome. Acceptance drifts with
// content, so an EWMA follows the drift instead of being anchored by early tokens.
const acceptanceEWMAAlpha = 0.1

// acceptanceMinSamples is how many reaches a position needs before its rate is
// trusted; since the search reaches one past the frontier, it also gates how fast
// the frontier advances. Set near the EWMA's memory (~1/alpha); larger only slows
// the ramp, since each depth is re-measured as it is drafted.
const acceptanceMinSamples = 10

// acceptanceModel 学习各位置条件接受率 EWMA，跨请求共享。
// acceptanceModel learns the per-position conditional acceptance rate — the chance
// position i is accepted given every draft before it already was — as an EWMA, shared
// across requests so a fresh request keeps the proven-out frontier. Drift is handled
// by the EWMA forgetting, not by discarding the estimate.
// acceptanceModel 保存各位置接受率 EWMA 与样本计数。
type acceptanceModel struct {
	rate []float64 // EWMA acceptance rate given the prefix survived; index i is position i
	seen []int     // times position i was reached (warmup gate for rate)
}

// newAcceptanceModel 初始化接受率模型。
func newAcceptanceModel() *acceptanceModel {
	return &acceptanceModel{rate: []float64{0}, seen: []int{0}}
}

// grow 扩展 rate/seen 切片至索引 i。
func (a *acceptanceModel) grow(i int) {
	for len(a.seen) <= i {
		a.rate = append(a.rate, 0)
		a.seen = append(a.seen, 0)
	}
}

// observe 将本轮结果 fold 到可达位置的 EWMA。
// observe folds a step's outcome into each reached position's EWMA. A position is
// reached only when the prefix before it survived (accepted >= i-1), and is accepted
// iff accepted >= i; updating only the surviving prefix avoids diluting deeper
// positions the step never reached.
// observe 更新可达位置的条件接受率 EWMA。
func (a *acceptanceModel) observe(drafted, accepted int) {
	for i := 1; i <= drafted; i++ {
		if accepted < i-1 {
			break // prefix did not survive to position i; deeper positions unreached
		}
		a.grow(i)
		outcome := 0.0
		if accepted >= i {
			outcome = 1.0
		}
		if a.seen[i] == 0 {
			a.rate[i] = outcome
		} else {
			a.rate[i] += acceptanceEWMAAlpha * (outcome - a.rate[i])
		}
		a.seen[i]++
	}
}

// acceptance 返回位置 i 条件接受率；欠采样时继承更深可信率。
// acceptance returns the rate position i is accepted given its prefix survived. An under-sampled
// position inherits the deepest trusted rate rather than zero, so the controller
// keeps exploring deeper instead of locking shallow on noise.
// acceptance 返回位置 i 的条件接受率。
func (a *acceptanceModel) acceptance(i int) float64 {
	if i >= 1 && i < len(a.seen) && a.seen[i] >= acceptanceMinSamples {
		return a.rate[i]
	}
	// Inherit the deepest trusted rate; fall back to optimistic 1 if none yet.
	for j := i - 1; j >= 1; j-- {
		if j < len(a.seen) && a.seen[j] >= acceptanceMinSamples {
			return a.rate[j]
		}
	}
	return 1
}

// expectedCommitted 返回深度 N 的期望提交 token 数（含 current）。
// expectedCommitted returns expected committed tokens at depth N: the current token,
// which always commits, plus the expected number of accepted drafts — each draft
// position contributes the probability its whole prefix was accepted, the running
// product of the per-position acceptance rates summed over positions.
// expectedCommitted 计算深度 N 期望提交 token 数。
func (a *acceptanceModel) expectedCommitted(n int) float64 {
	total, prod := 1.0, 1.0
	for k := 1; k <= n; k++ {
		prod *= a.acceptance(k)
		total += prod
	}
	return total
}

// frontier 为最深可信接受率位置；搜索不超过 frontier+1。
// frontier is the deepest position with a trusted acceptance rate. The controller
// never selects beyond frontier+1, so the selection grows outward one position at a
// time instead of jumping deep on the inherited optimistic rate.
// frontier 返回最深可信接受率位置。
func (a *acceptanceModel) frontier() int {
	f := 0
	for i := 1; i < len(a.seen); i++ {
		if a.seen[i] >= acceptanceMinSamples {
			f = i
		} else {
			break
		}
	}
	return f
}
