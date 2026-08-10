// Muse Glimmer 导入量化策略：视觉保持源精度，注意力/MLP 敏感投影分层提精。
package create

import (
	"encoding/json"
	"fmt"
	"strings"
)

// glimmerImportTransform 持有隐藏层数，用于 useMoreBits 启发式。
type glimmerImportTransform struct {
	numLayers int
}

// glimmerConfig 从 config.json 读取 num_hidden_layers。
type glimmerConfig struct {
	NumHiddenLayers int `json:"num_hidden_layers"`
	TextConfig      struct {
		NumHiddenLayers int `json:"num_hidden_layers"`
	} `json:"text_config"`
}

// newGlimmerImportTransform 解析 Glimmer config 并构造策略。
func newGlimmerImportTransform(rawConfig json.RawMessage) (quantizePolicy, error) {
	var cfg glimmerConfig
	if err := json.Unmarshal(rawConfig, &cfg); err != nil {
		return nil, fmt.Errorf("glimmer: parse config.json: %w", err)
	}
	numLayers := cfg.NumHiddenLayers
	if numLayers == 0 {
		numLayers = cfg.TextConfig.NumHiddenLayers
	}
	return glimmerImportTransform{numLayers: numLayers}, nil
}

// quantizationType 跳过视觉 tensor，对嵌入与敏感注意力/MLP 投影应用 Glimmer 规则。
func (t glimmerImportTransform) quantizationType(name string, shape []int32, quantize string) string {
	// 视觉 tensor 保持源精度，避免语言模型量化策略损害图像细节。
	// are not degraded by the language model's quantization policy.
	if isGlimmerVisionTensor(name) {
		return ""
	}

	base := normalizeQuantType(quantize)
	if isEmbedTokensWeight(name) {
		if e := promoteEmbedding(shape, base); e != "" {
			return e
		}
		if isAligned(shape, base) {
			return base
		}
		return ""
	}

	if isGlimmerSensitiveProjection(name) && eightBit(base) != base {
		return sensitiveType(t.promoteSensitive(name), shape, base)
	}

	return GetTensorQuantization(name, shape, quantize)
}

// isGlimmerVisionTensor 委托 isVision 识别视觉相关 tensor。
func isGlimmerVisionTensor(name string) bool {
	return isVision(name)
}

// isGlimmerSensitiveProjection 识别 Glimmer self_attn 与 mlp.down 敏感投影。
func isGlimmerSensitiveProjection(name string) bool {
	return strings.Contains(name, ".self_attn.q_proj") ||
		strings.Contains(name, ".self_attn.o_proj") ||
		strings.Contains(name, ".self_attn.k_proj") ||
		strings.Contains(name, ".self_attn.v_proj") ||
		strings.Contains(name, ".self_attn.gate_proj") ||
		strings.Contains(name, ".self_attn.output_gate_proj") ||
		strings.Contains(name, ".mlp.down_proj")
}

// promoteSensitive：q/o/k/v 恒提精；down 等按 useMoreBits 层位决定。
func (t glimmerImportTransform) promoteSensitive(name string) bool {
	if strings.Contains(name, ".self_attn.q_proj") ||
		strings.Contains(name, ".self_attn.o_proj") ||
		strings.Contains(name, ".self_attn.k_proj") ||
		strings.Contains(name, ".self_attn.v_proj") {
		return true
	}
	layer := layerIndex(name)
	return t.numLayers > 0 && layer >= 0 && useMoreBits(layer, t.numLayers)
}
