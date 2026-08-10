package cache

import (
	"fmt"

	"github.com/ollama/ollama/x/mlxrunner/batch"
	"github.com/ollama/ollama/x/mlxrunner/mlx"
	"github.com/ollama/ollama/x/models/nn"
)

// RecurrentCache 存储线性 recurrent 层状态。
// Conv 状态 dtype 来自首次 Get；delta 恒为 float32 以保证精度。
// RecurrentCache stores state for linear-recurrent layers.
//
// Conv state takes its dtype from the first Get call (the activation dtype).
// Delta state is always float32: the gated-delta recurrent accumulator runs
// for the full sequence length and needs the extra precision regardless of
// activation dtype.
//
// Conv state shape: [B, convTail, convDim]
// Delta state shape: [B, numVHeads, headVDim, headKDim]
type RecurrentCache struct {
	convState  *mlx.Array
	deltaState *mlx.Array
	offset     int

	convTail  int
	convDim   int
	numVHeads int
	headVDim  int
	headKDim  int

	snapshots pendingSnapshots
}

// PrepareSnapshots 调度快照；recurrent 状态累积，内部 offset 需分段 kernel。
// PrepareSnapshots schedules snapshot capture. Recurrent state is cumulative;
// an interior offset within a forward has no state unless the recurrent kernel
// is run in segments cut at that offset (see SnapshotSplits + Put). The
// current offset is a boundary now (the pre-forward state) and is captured
// immediately. Interior offsets are captured when Put receives the matching
// per-boundary state; the end offset is captured by Put's final state.
func (c *RecurrentCache) PrepareSnapshots(offsets []int) {
	c.snapshots.prepare(c.offset, offsets)
	// 当前 offset 已是有效边界，立即捕获。
	// The current offset is a valid boundary right now, so capture it.
	c.captureBoundary(c.offset, c.convState, c.deltaState)
}

func (c *RecurrentCache) TakeSnapshots() []Snapshot { return c.snapshots.take() }

// SnapshotSplits 返回即将前向 [offset,offset+len) 内严格内部的调度点（相对前向起点）。
// SnapshotSplits returns the scheduled offsets strictly interior to the upcoming
// forward [offset, offset+forwardLen), expressed relative to the forward
// start — the points at which the caller must segment the recurrent kernel so
// each interior state can be captured. Empty when nothing is scheduled or no
// interior offsets fall in range.
func (c *RecurrentCache) SnapshotSplits(forwardLen int) []int {
	start := c.offset
	end := start + forwardLen
	var splits []int
	for _, o := range c.snapshots.offsets {
		if o > start && o < end {
			splits = append(splits, o-start)
		}
	}
	return splits
}

// captureBoundary 在 reached 为调度 offset 时快照边界 (conv,delta) 状态。
// captureBoundary snapshots the boundary state (conv, delta) at reached if
// reached is a scheduled offset — the live state at a forward boundary, or a
// kernel segment state at an interior split.
func (c *RecurrentCache) captureBoundary(reached int, conv, delta *mlx.Array) {
	c.snapshots.captureReached(reached, func(int) Snapshot {
		// 尚无可换出数据；零宽捕获为 nil。
		// Nothing exists to page out yet; a zero-width capture is a nil entry.
		if conv == nil && delta == nil {
			return nil
		}
		return newRecurrentSnapshot(conv, delta, reached)
	})
}

func (c *RecurrentCache) setState(old, v *mlx.Array) *mlx.Array {
	v = v.Clone()
	mlx.Pin(v)
	mlx.Unpin(old)
	return v
}

// NewRecurrentCache 按 SSM 维度构造 recurrent 缓存。
func NewRecurrentCache(convTail, convDim, numVHeads, headVDim, headKDim int32) *RecurrentCache {
	return &RecurrentCache{
		convTail:  int(convTail),
		convDim:   int(convDim),
		numVHeads: int(numVHeads),
		headVDim:  int(headVDim),
		headKDim:  int(headKDim),
	}
}

// Get 返回 SSM 读阶段的 conv/delta 状态；首次调用 lazy 初始化零张量。
// Get returns the current conv/delta state for the SSM layer's read
// phase. On first call it lazy-initializes zero-filled state tensors
// sized from b.InputIDs and dtyped from the caller's activation dtype.
// On subsequent calls it returns the existing state; batch size and
// dtype must match the first call, since recurrent state is cumulative
// and cannot be reshaped without losing history.
func (c *RecurrentCache) Get(b *batch.Batch, dtype mlx.DType) *nn.RecurrentHistory {
	batch := b.InputIDs.Dim(0)
	if batch <= 0 {
		batch = 1
	}

	if c.convState != nil {
		if got := c.convState.Dim(0); got != batch {
			panic(fmt.Sprintf("recurrent cache: batch size changed mid-sequence (have %d, got %d)", got, batch))
		}
		if got := c.convState.DType(); got != dtype {
			panic(fmt.Sprintf("recurrent cache: conv dtype changed mid-sequence (have %v, got %v)", got, dtype))
		}
		return nn.NewRecurrentHistory(c.convState, c.deltaState)
	}

	c.convState = c.setState(nil, mlx.Zeros(dtype, batch, c.convTail, c.convDim))
	c.deltaState = c.setState(nil, mlx.Zeros(mlx.DTypeFloat32, batch, c.numVHeads, c.headVDim, c.headKDim))
	return nn.NewRecurrentHistory(c.convState, c.deltaState)
}

// Put 存储 SSM 写阶段各边界 conv/delta；末项为提交 live 状态并推进 offset。
// Put stores the conv/delta states produced by the SSM layer's write phase.
// convStates/deltaStates are the per-boundary recurrent states, one per
// boundary ending with the forward-end state. The boundaries align with this
// forward's snapshot splits plus the end: the leading entries are captured as
// snapshots at the scheduled interior offsets, and the final entry becomes the
// committed live state, advancing the cache offset by the forward's real token
// count.
//
// In the common (unsegmented) case both slices have length 1 — just the
// forward-end state.
//
// Assumes B = 1; heterogeneous batches are not supported.
func (c *RecurrentCache) Put(b *batch.Batch, convStates, deltaStates []*mlx.Array) {
	if len(convStates) != len(deltaStates) || len(convStates) == 0 {
		panic(fmt.Sprintf("recurrent cache: %d conv / %d delta boundary states", len(convStates), len(deltaStates)))
	}

	start := c.offset
	splits := c.SnapshotSplits(int(b.SeqQueryLens[0]))
	if len(splits) != len(convStates)-1 {
		panic(fmt.Sprintf("recurrent cache: %d interior splits but %d boundary states", len(splits), len(convStates)))
	}

	// 前若干项为内部分割边界，各捕获为调度 offset 快照。
	// Leading entries are the interior split boundaries; capture each as a
	// snapshot at its scheduled offset.
	for i, s := range splits {
		c.captureBoundary(start+s, convStates[i], deltaStates[i])
	}

	// 末项为前向结束状态，即提交的 live 状态。
	// The final entry is the forward-end state — the committed live state.
	last := len(convStates) - 1
	c.convState = c.setState(c.convState, convStates[last])
	c.deltaState = c.setState(c.deltaState, deltaStates[last])
	c.offset += int(b.SeqQueryLens[0])
	c.captureBoundary(c.offset, c.convState, c.deltaState)
}

func (c *RecurrentCache) State() []*mlx.Array {
	state := []*mlx.Array{c.convState, c.deltaState}
	// 已捕获快照需 eval，并入 State 批量 eval 而非逐快照 async。
	// Captured snapshots own compact copies still needing eval; fold them into
	// the caller's batched State eval instead of an async eval per snapshot per
	// layer. They drain at TakeSnapshots.
	for _, s := range c.snapshots.captured {
		if s != nil {
			rs := s.(*recurrentSnapshot)
			state = append(state, rs.convState, rs.deltaState)
		}
	}
	return state
}

// recurrentSnapshot 为自包含的换出 recurrent 状态。
// recurrentSnapshot holds paged-out recurrent state. Self-contained —
// does not depend on any parent state.
type recurrentSnapshot struct {
	convState, deltaState *mlx.Array
	offset                int
}

func (s *recurrentSnapshot) Size() int { return s.convState.NumBytes() + s.deltaState.NumBytes() }
func (s *recurrentSnapshot) Close()    { mlx.Unpin(s.convState, s.deltaState) }

// SetMaterializeHook 为空操作：recurrent 快照构造时已拥有副本。
// SetMaterializeHook is a no-op: recurrent snapshots own their compact copy from
// construction.
func (s *recurrentSnapshot) SetMaterializeHook(func(int)) {}

// newRecurrentSnapshot 克隆并 pin conv/delta 为 offset 处快照。
// newRecurrentSnapshot clones and pins conv/delta into an owned snapshot at
// offset. It does not schedule the eval — capture-path snapshots ride the
// cache's State into the caller's batched eval.
func newRecurrentSnapshot(conv, delta *mlx.Array, offset int) *recurrentSnapshot {
	snap := &recurrentSnapshot{
		convState:  conv.Clone(),
		deltaState: delta.Clone(),
		offset:     offset,
	}
	mlx.Pin(snap.convState, snap.deltaState)
	return snap
}

func (c *RecurrentCache) Snapshot(fromOffset int) Snapshot {
	// Recurrent state is not position-sliceable — always snapshot the full state.
	if c.convState == nil && c.deltaState == nil {
		return nil
	}

	// 换出快照直接进 trie 不经 State，故在此 AsyncEval。
	// Page-out snapshots go straight to the trie and never ride State, so
	// schedule the eval here instead of through the batched State eval.
	snap := newRecurrentSnapshot(c.convState, c.deltaState, c.offset)
	mlx.AsyncEval(snap.convState, snap.deltaState)
	return snap
}

func (c *RecurrentCache) Restore(snapshot Snapshot, target int) bool {
	if snapshot == nil {
		// recurrent 状态累积不可回退；仅 target==offset 时成功。
		// Recurrent state is cumulative and can't rewind. Only succeed
		// if we're already at the target (no-op).
		return target == c.offset
	}

	snap := snapshot.(*recurrentSnapshot)

	// recurrent 快照编码截至 snap.offset 的累积状态；target 必须匹配。
	// Recurrent snapshots encode cumulative state up to exactly
	// snap.offset. Target must match — rewinding would leave stale
	// state, and advancing isn't possible without feeding tokens.
	if target != snap.offset {
		return false
	}

	c.convState = c.setState(c.convState, snap.convState)
	c.deltaState = c.setState(c.deltaState, snap.deltaState)
	c.offset = snap.offset

	return true
}

func (c *RecurrentCache) Merge(parent, child Snapshot) Snapshot {
	// recurrent 快照自包含，子快照取代父快照。
	// Recurrent snapshots are self-contained — child supersedes parent.
	if parent != nil {
		parent.Close()
	}
	return child
}

func (c *RecurrentCache) Split(snapshot Snapshot, at int) (Snapshot, Snapshot) {
	// recurrent 状态不可按位置切分，无法在 split 点恢复中间状态。
	// Recurrent state is cumulative and not position-sliceable.
	// Cannot recover intermediate state at the split point.
	return nil, snapshot
}

func (c *RecurrentCache) Free() {
	mlx.Unpin(c.convState, c.deltaState)
	c.convState, c.deltaState = nil, nil
	c.offset = 0
	c.snapshots = pendingSnapshots{}
}

func (c *RecurrentCache) Offset() int { return c.offset }
