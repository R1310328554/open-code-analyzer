// Cohere2 MoE 导入量化策略：嵌入 8 位提升、路由门保持源精度、敏感层按位置启发式提精。
package create

import (
	"encoding/json"
	"fmt"
	"strings"
)

// cohere2MoeImportTransform 为 Cohere2 MoE（Command A / North）导入调整逐 tensor 量化类型。
// cohere2MoeImportTransform adjusts quantization for Cohere2 MoE imports
// (Command A family / North models).
// cohere2MoeImportTransform 持有层数，用于敏感投影的层位置启发式。
type cohere2MoeImportTransform struct {
	numLayers int
}

// newCohere2MoeImportTransform 从 config.json 解析 num_hidden_layers 并构造策略。
func newCohere2MoeImportTransform(rawConfig json.RawMessage) (quantizePolicy, error) {
	var cfg struct {
		NumHiddenLayers int `json:"num_hidden_layers"`
	}
	if err := json.Unmarshal(rawConfig, &cfg); err != nil {
		return nil, fmt.Errorf("cohere2moe: parse config.json: %w", err)
	}
	return cohere2MoeImportTransform{numLayers: cfg.NumHiddenLayers}, nil
}

// quantizationType 按 Cohere2 MoE 规则覆盖默认 GetTensorQuantization 决策。
func (t cohere2MoeImportTransform) quantizationType(name string, shape []int32, quantize string) string {
	base := normalizeQuantType(quantize)

	// embed_tokens 兼作 tied lm_head；大词表下 8 位可显著降低 decode 带宽。
	// The embedding serves double duty: lookup (via QuantizedEmbedding) and the
	// tied lm_head projection (via AsLinear). With a 262k vocab the bf16
	// embedding dominates decode bandwidth through the lm_head matmul, so
	// quantize it to the 8-bit variant of the requested mode, or keep source
	// precision when that does not fit.
	if isEmbedTokensWeight(name) && len(shape) == 2 {
		return promoteEmbedding(shape, base)
	}

	// MoE 路由门对量化噪声极敏感，保持源精度；此处显式跳过以防默认策略回归。
	// The MoE router picks the top-k expert set; quantization noise there can
	// flip expert selection and compound downstream. It is tiny, so keep it in
	// source precision. (GetTensorQuantization already skips "mlp.gate.weight";
	// kept explicit here so renames in the default policy cannot regress this.)
	if strings.HasSuffix(name, ".mlp.gate.weight") {
		return ""
	}

	// v/k/down 仅在 useMoreBits 层位提精，避免 blanket int8 down 占用 ~25% decode 带宽。
	// Sensitive tensors (v_proj, k_proj, down_proj) get higher precision only
	// at quantization-sensitive layer positions (useMoreBits) instead of the
	// default policy's blanket promotion. The blanket int8 down_proj costs
	// ~25% of decode bandwidth on a top-8 MoE; the layer-position heuristic
	// keeps the early/late layers (and every third in between) at 8 bits where
	// residual-stream error matters most.
	isSensitive := strings.Contains(name, ".v_proj") || strings.Contains(name, ".k_proj") || strings.Contains(name, "down_proj")
	if isSensitive && eightBit(base) != base && t.numLayers > 0 {
		if idx := layerIndex(name); idx >= 0 {
			// 绕过默认 blanket 提升，以层位置启发式为准。
			// Bypass GetTensorQuantization's blanket promotion — the
			// layer-position heuristic is authoritative here.
			return sensitiveType(useMoreBits(idx, t.numLayers), shape, base)
		}
	}

	return GetTensorQuantization(name, shape, quantize)
}
