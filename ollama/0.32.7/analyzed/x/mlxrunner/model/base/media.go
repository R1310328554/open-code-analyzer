// 多模态输入：prompt 分段、PrepareMedia 与 EncodeMedia 契约。
package base

import (
	// 各模型 PrepareMedia 经 image.Decode；此处一次性注册通用图像格式。
	// Every model's PrepareMedia decodes through image.Decode; the decoder
	// set is registered once here so all models accept the same formats.
	_ "image/gif"
	_ "image/jpeg"
	_ "image/png"

	_ "golang.org/x/image/webp"

	"github.com/ollama/ollama/x/mlxrunner/mlx"
)

// Segment 为 prompt 流中一段：文本 token 或单条媒体。
// Segment is one run of the prompt in stream order: either a tokenized text
// run (Tokens set) or a single media item (Kind and Data set).
type Segment struct {
	Tokens []int32
	Kind   string
	Data   []byte
}

// PreparedItem 描述 prepared 流中一处媒体；粒度由模型决定（整段或分 tile）。
// PreparedItem describes one media occurrence in the prepared stream. A
// model chooses item granularity: one per media segment, or several when
// parts encode and evaluate independently (e.g. per tile).
type PreparedItem struct {
	// Range 为该展开在 Tokens 中的 [start,end)；前缀缓存身份依赖此区间。
	// Range is the expansion's token range [start, end) in Tokens;
	// non-empty, since cache identity enters through these positions.
	Range [2]int

	// Source 为源 Segment 索引；前缀缓存按 segment 字节键控。
	// Source is the index of the segment this item was prepared from; the
	// item's prefix-cache identity is keyed on that segment's bytes.
	Source int

	// MediaData 为预处理后 encoder 输入；Dims 也进入 cache key。
	// MediaData is the preprocessed encoder input with shape Dims. Dims
	// enters the cache keys too: geometry changes features under
	// identical bytes.
	MediaData []float32
	Dims      []int

	// Opaque 携带模型私有状态至 EncodeMedia/Forward。
	// Opaque carries model-private preprocessing state to EncodeMedia
	// and Forward.
	Opaque any

	// Causal 表示因果注意力展开，可在 chunk 内切分；否则需整段一次前向。
	// Causal marks an expansion whose tokens attend causally, so chunks
	// may split it. Unset, the first evaluation covers the whole
	// expansion in one forward, as bidirectional runs require.
	Causal bool
}

// PreparedRequest 为 splice 媒体后的完整 token 流与 Items 列表。
// PreparedRequest is the expanded input stream, every media segment's
// expansion spliced in place, with the items in stream order.
type PreparedRequest struct {
	Tokens []int32
	Items  []PreparedItem

	// Layout 为请求级不透明布局，runner 原样传给各 forward。
	// Layout is an opaque request-scoped value computed in the one pass
	// that sees every splice position; immutable, carried unread by the
	// runner to every forward. Delivered only when Items is non-empty.
	// Nil when the model derives nothing from it.
	Layout any
}

// MediaModel 由支持图像/媒体输入的模型实现。
// MediaModel is implemented by models that accept media inputs.
type MediaModel interface {
	// PrepareMedia 每请求在 CPU 上运行一次，须对相同 segments 确定性。
	// PrepareMedia runs once per request on the request goroutine, CPU
	// only, and returns the expanded stream. It must be deterministic for
	// given segments: prefix-cache restores splice cached state with
	// recomputed state.
	PrepareMedia(segments []Segment) (*PreparedRequest, error)

	// EncodeMedia 在 MLX 线程构建 lazy 特征图，不 eval；eval 由消费 forward 触发。
	// EncodeMedia builds one item's lazy feature graph on the MLX thread;
	// it must not evaluate — the consuming forward's evaluation pulls it.
	// Read the pixels from data: the runner frees the item's MediaData
	// once its expansion is evaluated.
	EncodeMedia(item *PreparedItem, data *mlx.Array) *mlx.Array
}
