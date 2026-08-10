// Gemma4 导入量化策略：嵌入 8 位、敏感投影按层/专家数启发式提精。
package create

import (
	"encoding/json"
	"fmt"
	"strings"
)

// gemma4ImportTransform 持有层数与专家数，驱动敏感投影提升逻辑。
type gemma4ImportTransform struct {
	numLayers  int
	numExperts int
}

// gemma4Config 为量化决策所需的 Gemma4 config.json 最小字段子集。
// gemma4Config is a minimal subset of the Gemma 4 config.json used for quant decisions.
type gemma4Config struct {
	NumHiddenLayers int `json:"num_hidden_layers"`
	NumExperts      int `json:"num_experts"`
	TextConfig      struct {
		NumHiddenLayers int `json:"num_hidden_layers"`
		NumExperts      int `json:"num_experts"`
	} `json:"text_config"`
}

// newGemma4ImportTransform 解析层数/专家数并构造 Gemma4 策略。
func newGemma4ImportTransform(rawConfig json.RawMessage) (quantizePolicy, error) {
	var cfg gemma4Config
	if err := json.Unmarshal(rawConfig, &cfg); err != nil {
		return nil, fmt.Errorf("gemma4: parse config.json: %w", err)
	}

	numLayers := cfg.NumHiddenLayers
	if numLayers == 0 {
		numLayers = cfg.TextConfig.NumHiddenLayers
	}
	numExperts := cfg.NumExperts
	if numExperts == 0 {
		numExperts = cfg.TextConfig.NumExperts
	}

	return gemma4ImportTransform{numLayers: numLayers, numExperts: numExperts}, nil
}

// quantizationType 对嵌入与敏感投影应用 Gemma4 专用规则，其余走通用策略。
func (t gemma4ImportTransform) quantizationType(name string, shape []int32, quantize string) string {
	base := normalizeQuantType(quantize)
	switch {
	case isEmbedTokensWeight(name):
		// 嵌入兼 lm_head；8-bit 接近 bf16 质量并节省带宽，形状不符时回退 base。
		// quality close to bf16 (matching GGUF Q6_K) while saving bandwidth.
		// Fall back to the base type when 8-bit does not fit the vocab shape.
		if e := promoteEmbedding(shape, base); e != "" {
			return e
		}
		if isAligned(shape, base) {
			return base
		}
		return ""
	case t.isSensitiveProjection(name) && eightBit(base) != base:
		return sensitiveType(t.promoteSensitive(name), shape, base)
	default:
		// 路由门、norm、嵌入等由通用 GetTensorQuantization 处理。
		// policy; everything else quantizes at the requested type.
		return GetTensorQuantization(name, shape, quantize)
	}
}

// isSensitiveProjection 识别 v/k/down 等最影响质量的敏感投影（排除视听 tower）。
// isSensitiveProjection reports the value/key/down projections whose precision
// most affects quality — attention output (v/k) and the residual stream
// (down). Audio and vision tensors are excluded and follow the generic
// policy.
func (t gemma4ImportTransform) isSensitiveProjection(name string) bool {
	if isVision(name) || isAudioTower(name) {
		return false
	}
	return strings.Contains(name, ".v_proj") ||
		strings.Contains(name, ".k_proj") ||
		strings.Contains(name, "down_proj")
}

// promoteSensitive 决定是否将敏感投影提升至 8-bit：8 专家模型恒提升 k/v，其余按 useMoreBits。
// promoteSensitive decides whether a sensitive projection uses 8-bit precision.
// 8-expert models share very few KV heads, so their k/v projections are always
// promoted; otherwise v/down projections are promoted at the input and output
// layers and periodically between (useMoreBits), where residual-stream error
// accumulates most.
func (t gemma4ImportTransform) promoteSensitive(name string) bool {
	if t.numLayers == 0 {
		return false
	}
	if t.numExperts == 8 && (strings.Contains(name, ".v_proj") || strings.Contains(name, ".k_proj")) {
		return true
	}
	if strings.Contains(name, ".k_proj") {
		return false // k_proj 仅经 8 专家路径提升
	}
	layer := layerIndex(name)
	return layer >= 0 && useMoreBits(layer, t.numLayers)
}
