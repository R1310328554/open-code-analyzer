// 前向批次输入：token、序列偏移、媒体与 memo 缓存。
package batch

import "github.com/ollama/ollama/x/mlxrunner/mlx"

// Batch 为每次前向传给模型的输入。
// Batch is the per-forward-pass input handed to a model.
type Batch struct {
	// InputIDs 为本前向的输入 token ID，形状 (B, L)。
	// InputIDs is the input token IDs for this forward pass, shape (B, L).
	InputIDs *mlx.Array

	// SeqOffsets 为各行在序列中的当前位置（本 chunk 起点）。
	// SeqOffsets gives each row's current position within its sequence —
	// where the chunk in InputIDs starts. Length equals the batch dimension
	// of InputIDs.
	SeqOffsets []int32

	// SeqQueryLens 为各行本前向的真实 query 长度；小于 L 时尾部为 padding。
	// SeqQueryLens is each row's real query length in this forward. Values
	// less than L mean the row's tail is padding that must be masked out.
	// Length equals the batch dimension of InputIDs.
	SeqQueryLens []int32

	// Hidden 为 draft 模型前向的条件隐状态；普通前向为 nil。
	// Hidden is the draft-conditioning state for a draft model's forward.
	// It is nil for ordinary forward passes.
	Hidden *mlx.Array

	// Media 列出行的媒体项（prefill/draft）；decode 与纯文本为 nil。
	// Media lists a row's media items, on prefill forwards and draft
	// forwards that embed prompt tokens; items outside the query range
	// ride featureless. Nil at decode and for text-only requests.
	Media []MediaItem

	// Layout 携带 PrepareMedia 返回的不透明布局状态；runner 不读取。
	// Layout carries each row's opaque layout state from PrepareMedia,
	// identical on every forward of the request; the runner never reads
	// it. Nil entries derive nothing from layout.
	Layout []any

	// Memo 为前向级 memo，缓存 mask 等跨层复用结果。
	// Memo is per-forward memoization used to cache results, such as masks,
	// which are often the same across layers.
	Memo Memo
}

// MediaItem 表示序列中一处媒体展开；runner 仅知位置，特征由模型从 Pos/Opaque 推导。
// MediaItem is one media occurrence in a row's sequence. The runner
// knows only where the expansion was spliced; which positions bear
// features is the model's, derived from Pos and Opaque.
type MediaItem struct {
	// Seq 为媒体项所属 batch 行。
	// Seq is the batch row the item belongs to.
	Seq int

	// Pos 为展开首 token 的绝对序列位置。
	// Pos is the absolute sequence position of the expansion's first token.
	Pos int

	// Features 为整行特征数组，仅在与 query 范围重叠的前向中附加。
	// Features is the item's whole feature-row array, attached only while
	// the item's token range overlaps this forward's query range.
	Features *mlx.Array

	// Opaque 为 PreparedMedia.Opaque，原样往返。
	// Opaque is the item's PreparedMedia.Opaque, round-tripped untouched.
	Opaque any
}

// Memo 为前向内键值 memo 表。
type Memo struct {
	entries map[any]any
}

// Get 返回 key 的 memo 值及是否存在。
// Get returns the memoized value for key and true if present, or nil
// and false otherwise.
func (m *Memo) Get(key any) (any, bool) {
	v, ok := m.entries[key]
	return v, ok
}

// Put 存储 key 对应值，首次使用时分配 map。
// Put stores value under key, allocating on first use.
func (m *Memo) Put(key, value any) {
	if m.entries == nil {
		m.entries = map[any]any{}
	}
	m.entries[key] = value
}
