// 共享量化策略辅助：默认策略、层索引解析、useMoreBits 启发式与嵌入/敏感投影提精。
package create

import (
	"regexp"
	"strconv"
	"strings"
)

// defaultQuantPolicy 为无注册覆盖的架构提供 GetTensorQuantization 默认决策。
// defaultQuantPolicy is the quantize policy for any architecture without a
// registered override: the shared GetTensorQuantization decision with no
// architecture-specific adjustments.
// defaultQuantPolicy 零大小值类型。
type defaultQuantPolicy struct{}

// quantizationType 直接委托 GetTensorQuantization。
func (defaultQuantPolicy) quantizationType(name string, shape []int32, quantize string) string {
	return GetTensorQuantization(name, shape, quantize)
}

// layerIndexRe 从 tensor 名中提取 .layers.N. 的层索引。
// layerIndexRe extracts the layer index from tensor names like
// "model.language_model.layers.5.self_attn.v_proj.weight" or
// "model.language_model.layers.5.moe.experts.42.down_proj.weight"
var layerIndexRe = regexp.MustCompile(`\.layers\.(\d+)\.`)

// layerIndex 返回 name 中的 transformer 层号，无法解析则 -1。
// layerIndex returns the transformer layer index encoded in name, or -1.
// layerIndex 使用 layerIndexRe 正则捕获组。
func layerIndex(name string) int {
	m := layerIndexRe.FindStringSubmatch(name)
	if m == nil {
		return -1
	}
	idx, err := strconv.Atoi(m[1])
	if err != nil {
		return -1
	}
	return idx
}

// useMoreBits 对首尾 1/8 层及中间每第 3 层返回 true，限制残差流误差累积。
// useMoreBits returns true for layers where quantization-sensitive tensors
// should use higher precision: the first and last 1/8 of layers (which handle
// input grounding and final output refinement), plus every 3rd layer in between
// to limit error accumulation through the residual stream.
// useMoreBits 委托 useMoreBitsWithMiddleEnd，middleEnd 为 7/8 总层数。
func useMoreBits(layerIdx, numLayers int) bool {
	return useMoreBitsWithMiddleEnd(layerIdx, numLayers, 7*numLayers/8)
}

// useMoreBitsWithMiddleEnd 标准 early/late 提升，并将每 3 层 cadence 限制在 middleEnd 之前。
// useMoreBitsWithMiddleEnd applies the standard early/late promotion and
// limits the every-third-layer cadence to layers before middleEnd.
// useMoreBitsWithMiddleEnd 层索引非法或 numLayers<=0 时返回 false。
func useMoreBitsWithMiddleEnd(layerIdx, numLayers, middleEnd int) bool {
	if layerIdx < 0 || numLayers <= 0 {
		return false
	}
	first := numLayers / 8
	last := 7 * numLayers / 8
	return layerIdx < first ||
		layerIdx >= last ||
		(layerIdx >= first && layerIdx < middleEnd && (layerIdx-first)%3 == 2)
}

// eightBit 返回 base 量化族内的 8-bit 类型：affine 为 int8，fp4 族为 mxfp8。
// eightBit returns the 8-bit quantization type in base's family: int8 for the
// affine family, mxfp8 for the fp4 family.
// eightBit 将 int4/int8 映射为 int8，其余映射为 mxfp8。
func eightBit(base string) string {
	if base == "int4" || base == "int8" {
		return "int8"
	}
	return "mxfp8"
}

// promoteEmbedding 当嵌入 shape 对齐时返回同族 8-bit，否则 ""。
// promoteEmbedding returns the 8-bit type in base's family when the embedding
// shape fits it, or "" when it does not. Token embeddings often double as the
// lm_head projection, where an 8-bit type keeps quality close to bf16 while
// saving decode bandwidth; the caller decides the fallback when 8-bit does not
// fit (the base type, or source precision).
// promoteEmbedding 用于 embed 兼 lm_head 时的带宽/质量折中。
func promoteEmbedding(shape []int32, base string) string {
	if e := eightBit(base); isAligned(shape, e) {
		return e
	}
	return ""
}

// sensitiveType 解析敏感投影：promote 且对齐时用 8-bit，否则 base 或源精度。
// sensitiveType resolves a quantization-sensitive projection (v/k/down): the
// 8-bit type in base's family when promote is set and fits the shape,
// otherwise the base type when it fits, otherwise source precision.
// sensitiveType 由架构策略在 useMoreBits 等启发式下调用。
func sensitiveType(promote bool, shape []int32, base string) string {
	if promote {
		if e := eightBit(base); isAligned(shape, e) {
			return e
		}
	}
	if isAligned(shape, base) {
		return base
	}
	return ""
}

// isEmbedTokensWeight 识别主 token 嵌入 embed_tokens.weight（排除 per_layer）。
// isEmbedTokensWeight returns true for the main token embedding weight.
// isEmbedTokensWeight 后缀匹配且不含 per_layer。
func isEmbedTokensWeight(name string) bool {
	return strings.HasSuffix(name, "embed_tokens.weight") &&
		!strings.Contains(name, "per_layer")
}

// isVision 识别 vision/visual 路径下的视觉 tower 与投影 tensor。
// isVision reports tensors under a model's vision components: towers,
// encoder-free embedders, and vision-to-text projections alike.
// isVision 子串匹配 vision 或 visual。
func isVision(name string) bool {
	return strings.Contains(name, "vision") || strings.Contains(name, "visual")
}

// isAudioTower 识别 audio_tower 或 embed_audio 路径。
// isAudioTower reports tensors under a model's audio tower or audio embedding.
// isAudioTower 子串匹配音频 tower 相关名称。
func isAudioTower(name string) bool {
	return strings.Contains(name, "audio_tower") || strings.Contains(name, "embed_audio")
}
