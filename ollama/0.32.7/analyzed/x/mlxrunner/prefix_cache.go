// prefix_cache.go 用压缩前缀 trie 跨会话共享 KV cache：
// 仅一条 active 路径持有 live MLX 数组；切换路径分页换入换出快照。
// prefix_cache.go manages cache state shared across conversations using a// prefix_cache.go manages cache state shared across conversations using a
// 每 trie 节点存边 token 序列与各层可分页快照。
// compressed prefix trie. Each trie node stores a token sequence (edge) and
// optional per-layer snapshots that can be paged in/out of the live MLX cache
// arrays.
//
// 关键性质：
// Key properties:
//   - 同时仅一条 trie 路径 active（live MLX 数组）
//   - Only one path through the trie is "active" (backed by live MLX arrays)
//     at a time. Switching paths pages out the frontier node and pages in the
//     new path.
//   - 快照仅在 active 路径 frontier 捕获；中间节点来自 split prefill。
//   - Snapshots are only captured at the frontier (end) of the active path.
//     Intermediate node snapshots come from split prefill.
//   - 各 cache 层 token offset 必须一致。
//   - All cache layers must stay at the same token offset.
//   - 兄弟边不得共享 token 前缀（压缩 trie 不变量）。
//   - Sibling edges must not share a common token prefix (compressed trie
//     invariant).
//   - begin() 至少重算一 token 以便 pipeline 种子生成。
//   - begin() always re-evaluates at least one token so the pipeline can seed
//     generation, even on a full prefix match.

package mlxrunner

import (
	"cmp"
	"fmt"
	"log/slog"
	"slices"
	"time"

	"github.com/ollama/ollama/logutil"
	"github.com/ollama/ollama/x/mlxrunner/cache"
	"github.com/ollama/ollama/x/mlxrunner/mlx"
)

const maxPagedOutBytes int64 = 8 << 30 // 换出快照内存 8GiB 驱逐阈值
// 8 GiB eviction threshold for paged-out snapshot memory

// prefixCache 管理前缀 trie 与 live cache 数组。
type prefixCache struct {
	root          *trieNode   // 前缀 trie 根
	// root of the prefix trie
	activePath    []*trieNode // 当前带 live 数组的根到叶路径
	// current root→leaf path with live MLX arrays
	caches        []cache.Cache
	pagedOutBytes int64 // total bytes in paged-out snapshots across the trie

	// draftLookahead 为 draft cache 条目向前看的 token 数；key 打包 (t_i,t_{i+1})。
	// draftLookahead is how far the draft caches' entries reference past
	// their own slot; trie keys pack each token with its look-ahead (see key).
	draftLookahead int
}

// pendingSnapshot 为 prefill 中计划捕获的快照。
// pendingSnapshot is a snapshot scheduled to be taken during prefill.
type pendingSnapshot struct {
	offset int
	user   bool
}

// cacheSession 管理单次 pipeline 的 cache；close 时保存状态。
// cacheSession manages caches for a single pipeline run.
// Callers should append generated tokens to outputs and
// defer close to save the cache state.
type cacheSession struct {
	cache     *prefixCache
	inputs    []int32
	effInputs []uint32 // inputs' key alphabet, media folds applied
	outputs   []int32

	caches    []cache.Cache
	remaining []int32

	// pendingSnapshots lists offsets where snapshots should be captured
	// during prefill, sorted by offset. Entries are scheduled on the caches
	// before prefill and drained or discarded after.
	pendingSnapshots []pendingSnapshot
}

// newPrefixCache 为模型生命周期管理给定 cache 槽位。
// newPrefixCache manages the given cache slots for the model's life.
func newPrefixCache(caches []cache.Cache) *prefixCache {
	return &prefixCache{caches: caches}
}

func (c *prefixCache) ensureRoot() {
	if c.root == nil {
		c.root = &trieNode{
			lastUsed: time.Now(),
		}
		c.activePath = []*trieNode{c.root}
	}
}

// begin 为新请求准备 cache：匹配前缀、切换路径，至少留一 token 重算。
// begin prepares caches for a new request. It finds the nearest
// matching cache or creates new caches if none match.
func (c *prefixCache) begin(inputs []int32, items []mediaItem) *cacheSession {
	c.ensureRoot()

	effInputs := effectiveKeyTokens(inputs, items)
	keys := c.key(effInputs)
	matchPath, matched := findBestMatch(c.root, keys)
	originalMatched := matched

	// 全匹配时也保留至少一 token 重算以种子生成。
	// Always keep at least one token to re-evaluate so the
	// pipeline can seed token generation from it.
	if matched == len(inputs) && matched > 0 {
		matchPath, matched = findBestMatch(c.root, keys[:matched-1])
	}

	// 切换到匹配路径并按需分页。
	// Switch to the matched path, paging in/out as needed.
	c.switchToPath(matchPath, matched)

	// switchToPath aligns caches to a common offset
	prefix := c.minCacheOffset()
	remaining := inputs[prefix:]

	session := &cacheSession{
		cache:     c,
		inputs:    inputs,
		effInputs: effInputs,
		caches:    c.caches,
		remaining: remaining,
	}

	// 在分支点调度快照供后续 diverge 请求 restore。
	// Schedule a snapshot at the branch point during prefill so future
	// requests diverging here can restore instead of re-evaluating.
	if prefix < matched {
		session.pendingSnapshots = append(session.pendingSnapshots, pendingSnapshot{offset: matched, user: false})
	}

	msg := "cache hit"
	if prefix == 0 {
		msg = "cache miss"
	}
	slog.Info(msg, "total", len(inputs), "matched", originalMatched, "cached", prefix, "left", len(remaining))

	return session
}

// effectiveKeyTokens 返回每位置 trie key 字母表；媒体展开用 fold 替代 token ID。
// effectiveKeyTokens returns the per-position key alphabet: the token ID
// outside media expansions, the item's fold value across each expansion's
// whole range.
func effectiveKeyTokens(tokens []int32, items []mediaItem) []uint32 {
	eff := make([]uint32, len(tokens))
	for i, t := range tokens {
		eff[i] = uint32(t)
	}
	for _, item := range items {
		for i := item.pos; i < item.pos+item.length; i++ {
			eff[i] = item.fold
		}
	}
	return eff
}

// key 打包可 restore 偏移的 (token_i, token_{i+1})；draftLookahead=1 时双 token key。
// key packs (token i, token i+1) per restorable offset: draft caches
// pair each slot with the next token, so matching k keys verifies k+1
// tokens and every match is a valid restore point.
func (c *prefixCache) key(tokens []uint32) []trieKey {
	keys := make([]trieKey, max(len(tokens)-c.draftLookahead, 0))
	switch c.draftLookahead {
	case 0:
		for i, t := range tokens {
			keys[i] = trieKey(t)
		}
	case 1:
		for i := range keys {
			keys[i] = trieKey(tokens[i])<<32 | trieKey(tokens[i+1])
		}
	default:
		panic(fmt.Sprintf("prefixCache: unsupported draft look-ahead %d", c.draftLookahead))
	}
	return keys
}

// storedKeys 为已 eval 流（prompt + 生成 token）的 key 序列。
// storedKeys keys the session's evaluated stream: the prompt's effective
// tokens plus generated tokens, which are never media.
func (s *cacheSession) storedKeys() []trieKey {
	eff := s.effInputs
	if len(s.outputs) > 0 {
		eff = make([]uint32, 0, len(s.effInputs)+len(s.outputs))
		eff = append(eff, s.effInputs...)
		for _, t := range s.outputs {
			eff = append(eff, uint32(t))
		}
	}
	return s.cache.key(eff)
}

// switchToPath 切换 active 路径：换出旧叶、rewind、换入新路径快照。
// switchToPath transitions from the current active path to a new path,
// paging out diverging segments and paging in the new path.
func (c *prefixCache) switchToPath(newPath []*trieNode, matched int) {
	defer c.enforceEvictionPolicy()

	// Find common ancestor index.
	commonLen := 0
	for commonLen < len(c.activePath) && commonLen < len(newPath) {
		if c.activePath[commonLen] != newPath[commonLen] {
			break
		}
		commonLen++
	}

	ancestorOffset := 0
	if commonLen > 0 {
		ancestorOffset = c.activePath[commonLen-1].endOffset
	}

	var pageOutCount, pageInCount int

	// 仅换出旧路径叶节点 live 状态；中间节点快照已在创建时捕获。
	// Page out the leaf of the old path. Only the leaf's live cache
	// state is correct — intermediate nodes already have snapshots
	// captured during their creation (splitNode + prefill). Snapshotting
	// non-leaf nodes here would produce wrong results for non-rewindable
	// caches (e.g. RecurrentCache) whose state reflects the leaf, not
	// the intermediate boundary.
	leaf := len(c.activePath) - 1
	leafDiverges := leaf >= commonLen
	leafNeedsRewind := matched < c.activePath[leaf].endOffset
	if leafDiverges || leafNeedsRewind {
		node := c.activePath[leaf]
		if !node.hasAllSnapshots() {
			fromOffset := node.startOffset()
			snaps := make([]cache.Snapshot, len(c.caches))
			for j, kv := range c.caches {
				if kv == nil {
					continue
				}
				snaps[j] = kv.Snapshot(fromOffset)
			}
			node.setSnapshots(snaps, &c.pagedOutBytes)
			pageOutCount++
			logutil.Trace(fmt.Sprintf("page out: [%d, %d)", fromOffset, node.endOffset))
		}
	}

	// rewind 各 cache 至目标 offset，失败则 Free。
	// Rewind each cache to the target offset or free it. When matched
	// falls within the ancestor's range (same-path case), we rewind
	// directly to the match point. Otherwise we rewind to the ancestor
	// and let page-in bring us forward to matched.
	rewindTarget := min(ancestorOffset, matched)
	for _, kv := range c.caches {
		if kv == nil {
			continue
		}
		if !kv.Restore(nil, rewindTarget) {
			kv.Free()
		}
	}

	// 沿新路径换入快照；已越过 node 的 cache 跳过。
	// Page in — walk the full new path, restoring from snapshots.
	// Freed caches naturally pick up the first available snapshot.
	// Caches already past a node skip it via offset check.
pageIn:
	for _, node := range newPath {
		if !node.hasSnapshots() {
			continue
		}
		nodeTarget := min(node.endOffset, matched)
		for j, kv := range c.caches {
			if kv == nil {
				continue
			}
			if j >= len(node.snapshots) || node.snapshots[j] == nil {
				continue
			}
			if kv.Offset() >= nodeTarget {
				continue
			}
			if !kv.Restore(node.snapshots[j], nodeTarget) {
				// restore 失败则停止换入，由对齐统一到一致 offset。
			// Restore failed — stop page-in and let alignment
				// bring all caches to a consistent offset.
				break pageIn
			}
		}
		if node.endOffset > ancestorOffset {
			pageInCount++
			logutil.Trace(fmt.Sprintf("page in: [%d, %d)", node.startOffset(), nodeTarget))
		}
	}

	// 将所有 cache 对齐到最小 offset。
	// Align all caches to the minimum offset.
	c.activePath = newPath
	minOff := c.minCacheOffset()
	for _, kv := range c.caches {
		if kv != nil && kv.Offset() != minOff {
			if !kv.Restore(nil, minOff) {
				slog.Warn("failed to restore cache, freeing all caches", "offset", minOff)
				c.freeAll()
				break
			}
		}
	}
	for i := len(c.activePath) - 1; i >= 0; i-- {
		if c.activePath[i].endOffset <= minOff {
			c.activePath = c.activePath[:i+1]
			break
		}
	}

	// Update last-used time on only the final used node. For recurrent
	// caches we don't need the intermediate snapshots and for KV caches
	// we can reslice the data out of merged edges.
	if len(c.activePath) > 0 {
		c.activePath[len(c.activePath)-1].lastUsed = time.Now()
	}

	if pageOutCount > 0 || pageInCount > 0 {
		slog.Debug("switching cache path", "page_out", pageOutCount, "page_in", pageInCount)
	}
}

// schedulePrefillSnapshots 在 prefill 跨越 offset 时捕获内部快照。
// schedulePrefillSnapshots schedules every cache to capture snapshots as the
// forward pass crosses the given absolute token offsets, so a single full-size
// prefill records interior states without the caller breaking the batch. A
// passed offset names a token prefix; the capture lands at the deepest
// state that prefix alone determines (offset - draftLookahead), which is where
// a prompt sharing exactly that prefix restores. The offsets are merged with
// any snapshots begin already scheduled (e.g. a branch point), with coinciding
// offsets upgraded to user so eviction preserves them.
//
// Offsets at or before the current cache position, or past the end of the
// prompt, are dropped: callers only request offsets ahead of the prefill base,
// so this is a defensive guard.
func (s *cacheSession) schedulePrefillSnapshots(offsets []int) {
	c := s.cache
	base := c.minCacheOffset()
	for _, offset := range offsets {
		offset -= c.draftLookahead
		if offset <= base || offset > len(s.inputs) {
			continue
		}
		// Deduplicate: if this offset already exists, upgrade to user.
		found := false
		for i := range s.pendingSnapshots {
			if s.pendingSnapshots[i].offset == offset {
				s.pendingSnapshots[i].user = true
				found = true
				break
			}
		}
		if !found {
			s.pendingSnapshots = append(s.pendingSnapshots, pendingSnapshot{offset: offset, user: true})
		}
	}
	slices.SortFunc(s.pendingSnapshots, func(a, b pendingSnapshot) int {
		return a.offset - b.offset
	})

	if len(s.pendingSnapshots) == 0 {
		return
	}

	prepared := make([]int, len(s.pendingSnapshots))
	for i, p := range s.pendingSnapshots {
		prepared[i] = p.offset
	}
	for _, kv := range c.caches {
		if kv != nil {
			kv.PrepareSnapshots(prepared)
		}
	}
}

// discardPrefillSnapshots 丢弃未 attach 的 prefill 快照，防泄漏。
// discardPrefillSnapshots drains and closes the snapshots scheduled by
// schedulePrefillSnapshots without attaching them to the trie, releasing their
// pinned/lazy state. It is a no-op once attachPrefillSnapshots has drained the
// schedule, so close can call it unconditionally to clean up an abandoned
// prefill.
func (s *cacheSession) discardPrefillSnapshots() {
	if len(s.pendingSnapshots) == 0 {
		return
	}
	s.pendingSnapshots = nil

	for _, kv := range s.cache.caches {
		if kv == nil {
			continue
		}
		for _, snap := range kv.TakeSnapshots() {
			if snap != nil {
				snap.Close()
			}
		}
	}
}

// attachPrefillSnapshots 将 prefill 捕获的快照挂到 trie 并推进 frontier。
// attachPrefillSnapshots collects the snapshots captured during prefill and
// attaches them to the trie, materializing a node at each requested offset.
// Pending offsets are ascending and were scheduled in the same order, so the
// snapshots each cache returns line up with them. The trie frontier is
// advanced to each offset in turn, so its node edges [prev, offset) match the
// edge-local ranges the caches captured.
func (s *cacheSession) attachPrefillSnapshots() {
	if len(s.pendingSnapshots) == 0 {
		return
	}

	c := s.cache
	pending := s.pendingSnapshots
	s.pendingSnapshots = nil

	// Drain each cache's captures (one per pending offset, in order) into
	// per-offset rows.
	rows := make([][]cache.Snapshot, len(pending))
	for i := range rows {
		rows[i] = make([]cache.Snapshot, len(c.caches))
	}
	for j, kv := range c.caches {
		if kv == nil {
			continue
		}
		taken := kv.TakeSnapshots()
		for i := range pending {
			if i < len(taken) {
				rows[i][j] = taken[i]
			}
		}
	}

	// prefill 留末 token 给 decode，未写到的 offset 跳过。
	// Prefill leaves one token unprocessed for decode seeding, so an offset
	// at or past the live cache position was never crossed by a write and has
	// no captured state. Skip it rather than materialize a node whose edge
	// claims tokens the cache never wrote. Closing its (nil) row is a no-op.
	reached := c.minCacheOffset()
	stored := s.storedKeys()
	for i, p := range pending {
		if p.offset > reached {
			// Never crossed by a write, so the row is nil; close any entry
			// defensively in case a cache captured one anyway.
			for _, snap := range rows[i] {
				if snap != nil {
					snap.Close()
				}
			}
			continue
		}
		frontier := c.activePath[len(c.activePath)-1]
		if frontier.endOffset < p.offset {
			edgeTokens := stored[frontier.endOffset:p.offset]
			frontier = c.advancePath(frontier, edgeTokens, p.offset)
		}
		if p.user {
			frontier.user = true
		}
		s.attachCapturedSnapshots(frontier, rows[i])
	}
}

// attachCapturedSnapshots 将预捕获快照存到节点（非 live Snapshot）。
// attachCapturedSnapshots stores pre-captured snapshots on a trie node. Unlike
// taking a fresh Snapshot from the live cache, this works for an interior node
// whose offset the live cache has already advanced past: the snapshots come
// from the capture scheduled earlier, not from the cache's current state. The
// node takes ownership of the snapshots (TakeSnapshots already transferred it).
func (s *cacheSession) attachCapturedSnapshots(node *trieNode, snaps []cache.Snapshot) {
	c := s.cache
	node.setSnapshots(snaps, &c.pagedOutBytes)
	node.lastUsed = time.Now()
	slog.Debug("created snapshot", "offset", node.endOffset)
	c.enforceEvictionPolicy()
}

// advancePath 沿 trie 匹配 token、split 部分边并追加新节点。
// advancePath advances the active path from the current frontier by matching
// tokens against existing trie children, splitting partial matches, and
// appending any remaining tokens as new nodes. Returns the new frontier.
func (c *prefixCache) advancePath(frontier *trieNode, tokens []trieKey, endOffset int) *trieNode {
	// Check if existing children already cover some or all of tokens.
	// tokens may span multiple trie nodes when extending a previous run's
	// leaf and this snapshot now overlaps that same range.
	matchPath, matched := findBestMatch(frontier, tokens)
	// matchPath[0] is frontier itself; the rest are newly traversed nodes.
	remaining := tokens[matched:]

	// Check for a partial match within the last node's edge — if so, split it.
	if len(matchPath) > 1 {
		lastNode := matchPath[len(matchPath)-1]
		matchedInEdge := frontier.endOffset + matched - lastNode.startOffset()
		if matchedInEdge > 0 && matchedInEdge < len(lastNode.tokens) {
			matchPath[len(matchPath)-1] = splitNode(lastNode, matchedInEdge, c.caches, &c.pagedOutBytes)
		}
	}

	// Append traversed nodes (excluding frontier) to the active path.
	c.activePath = append(c.activePath, matchPath[1:]...)
	dest := matchPath[len(matchPath)-1]

	if len(remaining) > 0 {
		// Drop non-user snapshots so appendTokens can extend in-place
		// rather than creating a new child node.
		if len(dest.children) == 0 && !dest.user {
			dest.setSnapshots(nil, &c.pagedOutBytes)
		}
		newDest := dest.appendTokens(c.root, remaining, endOffset)
		if newDest != dest {
			c.activePath = append(c.activePath, newDest)
		}
		dest = newDest
	}
	return dest
}

// freeAll 释放全部 cache 层。
// freeAll releases all cache layers.
func (c *prefixCache) freeAll() {
	for _, kv := range c.caches {
		if kv != nil {
			kv.Free()
		}
	}
}

func (c *prefixCache) minCacheOffset() int {
	offset := 0
	found := false
	for _, kv := range c.caches {
		if kv == nil {
			continue
		}
		if off := kv.Offset(); !found || off < offset {
			offset = off
			found = true
		}
	}
	return offset
}

// close 若已 forward 则将生成 token 写入 trie 并 eval 状态。
// close saves the token state if the forward pass ran.
func (s *cacheSession) close() {
	// Release any prefill snapshots the session scheduled but never attached to
	// the trie. A successful prefill drains them in attachPrefillSnapshots (so
	// this is a no-op then); an abandoned one (e.g. cancellation between
	// schedule and attach) leaves them in the caches, where the next request's
	// PrepareSnapshots would overwrite the schedule without closing them,
	// leaking the pinned/lazy snapshots and their VRAM.
	s.discardPrefillSnapshots()

	offset := s.cache.minCacheOffset()
	if offset <= 0 {
		return
	}

	arrays := make([]*mlx.Array, 0, 2*len(s.caches))
	for _, kv := range s.caches {
		if kv == nil {
			continue
		}
		arrays = append(arrays, kv.State()...)
	}

	// 确保 forward 后 metadata 与数据一致。
	// Ensure that if we have run the forward pass and set the metadata
	// that we also actually have the data.
	mlx.AsyncEval(arrays...)

	// The caches never advance past the stored keys; anything more
	// means positions desynced.
	c := s.cache
	stored := s.storedKeys()
	if offset > len(stored) {
		panic(fmt.Sprintf("cache: offset %d exceeds %d stored keys", offset, len(stored)))
	}

	// Advance the trie frontier with any newly generated tokens.
	if len(c.activePath) > 0 {
		frontier := c.activePath[len(c.activePath)-1]
		if offset > frontier.endOffset {
			newTokens := stored[frontier.endOffset:offset]
			c.advancePath(frontier, newTokens, offset)
		}
		c.activePath[len(c.activePath)-1].lastUsed = time.Now()
	}
}

// enforceEvictionPolicy LRU 驱逐直至换出内存低于阈值。
// enforceEvictionPolicy evicts eligible nodes until paged-out memory is within limits.
func (c *prefixCache) enforceEvictionPolicy() {
	if c.pagedOutBytes <= maxPagedOutBytes {
		return
	}

	activeSet := make(map[*trieNode]bool, len(c.activePath))
	for _, n := range c.activePath {
		activeSet[n] = true
	}

	for c.pagedOutBytes > maxPagedOutBytes {
		var best *trieNode
		walkNodes(c.root, func(n *trieNode) bool {
			if n == c.root || activeSet[n] || len(n.children) > 1 {
				return true
			}
			// 驱逐优先级：最旧、最深、最大。
			// Evict: oldest, then deepest, then largest.
			if best == nil || cmp.Or(
				n.lastUsed.Compare(best.lastUsed),
				cmp.Compare(best.endOffset, n.endOffset),
				cmp.Compare(best.snapshotBytes(), n.snapshotBytes()),
			) < 0 {
				best = n
			}
			return true
		})
		if best == nil {
			break
		}
		c.evictNode(best)
	}
}

// evictNode 驱逐单节点：叶删除或单子 interior 合并。
// evictNode evicts a single node from the trie, freeing its snapshot memory.
func (c *prefixCache) evictNode(node *trieNode) {
	if len(node.children) == 0 {
		// Leaf: remove entirely.
		slog.Debug("evicting leaf", "offset", node.startOffset(), "tokens", len(node.tokens), "freed", mlx.PrettyBytes(int(node.snapshotBytes())))
		removeNode(node, &c.pagedOutBytes)
	} else if len(node.children) == 1 {
		// Interior node with one child: merge with child.
		before := c.pagedOutBytes
		tokens := len(node.tokens)
		mergeWithChild(node, c.caches, &c.pagedOutBytes)
		slog.Debug("evicting interior node", "offset", node.startOffset(), "tokens", tokens, "freed", mlx.PrettyBytes(int(before-c.pagedOutBytes)))
	} else {
		panic("evictNode called on multi-child branch point")
	}
}

// dumpTree 输出 trie 结构与 active/paged 内存统计（trace 级）。
func (c *prefixCache) dumpTree() {
	// Summary stats
	var cacheBytes int
	for _, kv := range c.caches {
		if kv == nil {
			continue
		}
		for _, a := range kv.State() {
			if a != nil {
				cacheBytes += a.NumBytes()
			}
		}
	}

	// Build active path set for marking.
	active := make(map[*trieNode]bool, len(c.activePath))
	for _, n := range c.activePath {
		active[n] = true
	}

	var nodeCount, snapshotCount int
	var pagedBytes int64
	var lines []string
	var dump func(n *trieNode, prefix string, isLast bool)
	dump = func(n *trieNode, prefix string, isLast bool) {
		if n == nil {
			return
		}
		nodeCount++

		// Build connector
		var connector string
		if n.parent == nil {
			connector = ""
		} else if isLast {
			connector = prefix + "`-- "
		} else {
			connector = prefix + "|-- "
		}

		// Node label
		nodeBytes := n.snapshotBytes()
		pagedBytes += nodeBytes

		label := fmt.Sprintf("[%d,%d) %dt", n.startOffset(), n.endOffset, len(n.tokens))
		if nodeBytes > 0 {
			label += " " + mlx.PrettyBytes(int(nodeBytes)).String()
		}
		if !n.lastUsed.IsZero() {
			label += fmt.Sprintf(" %s ago", time.Since(n.lastUsed).Truncate(time.Millisecond))
		}
		var flags []string
		if n.user {
			flags = append(flags, "user")
		}
		if n.hasAllSnapshots() {
			snapshotCount++
			flags = append(flags, "snap")
		}
		if active[n] {
			flags = append(flags, "active")
		}
		if len(flags) > 0 {
			label += " (" + flags[0]
			for _, f := range flags[1:] {
				label += ", " + f
			}
			label += ")"
		}
		lines = append(lines, connector+label)

		// Recurse children
		childPrefix := prefix
		if n.parent != nil {
			if isLast {
				childPrefix += "    "
			} else {
				childPrefix += "|   "
			}
		}
		for i, child := range n.children {
			dump(child, childPrefix, i == len(n.children)-1)
		}
	}
	dump(c.root, "", true)

	offset := c.minCacheOffset()
	logutil.Trace(fmt.Sprintf("prefix cache active_tokens: %d, active_size: %s, paged_out: %s, trie: nodes=%d, snapshots=%d",
		offset, mlx.PrettyBytes(cacheBytes), mlx.PrettyBytes(int(pagedBytes)), nodeCount, snapshotCount))
	for i, l := range lines {
		if i == 0 {
			logutil.Trace("cache trie: " + l)
		} else {
			logutil.Trace("  " + l)
		}
	}
}
