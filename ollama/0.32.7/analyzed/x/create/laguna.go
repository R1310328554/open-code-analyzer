// Laguna 导入量化策略：FP 量化下嵌入/lm_head 8 位、dense 层与专家 down 分层 cadence。
package create

import (
	"encoding/json"
	"fmt"
	"strings"
)

// lagunaImportTransform 记录 dense MLP 层集合与总层数。
type lagunaImportTransform struct {
	denseMLPLayers map[int]bool
	numLayers      int
}

// lagunaConfig 解析 mlp_only_layers 与 mlp_layer_types。
type lagunaConfig struct {
	NumHiddenLayers int      `json:"num_hidden_layers"`
	MLPOnlyLayers   []int    `json:"mlp_only_layers"`
	MLPLayerTypes   []string `json:"mlp_layer_types"`
}

// newLagunaImportTransform 构建 dense 层映射与默认层数。
func newLagunaImportTransform(rawConfig json.RawMessage) (quantizePolicy, error) {
	var cfg lagunaConfig
	if len(rawConfig) > 0 {
		if err := json.Unmarshal(rawConfig, &cfg); err != nil {
			return nil, fmt.Errorf("laguna: parse config.json: %w", err)
		}
	}

	denseLayers := make(map[int]bool)
	for i, typ := range cfg.MLPLayerTypes {
		if typ == "dense" {
			denseLayers[i] = true
		}
	}
	for _, layer := range cfg.MLPOnlyLayers {
		denseLayers[layer] = true
	}
	if len(denseLayers) == 0 {
		denseLayers[0] = true
	}

	numLayers := cfg.NumHiddenLayers
	if numLayers == 0 {
		numLayers = len(cfg.MLPLayerTypes)
	}
	if numLayers == 0 {
		numLayers = 40
	}

	return lagunaImportTransform{
		denseMLPLayers: denseLayers,
		numLayers:      numLayers,
	}, nil
}

// quantizationType 对 nvfp4/mxfp4/mxfp8 应用 Laguna 专用 tensor 分类规则。
func (t lagunaImportTransform) quantizationType(name string, shape []int32, quantize string) string {
	base := normalizeQuantType(quantize)
	if !lagunaFPQuant(base) {
		return GetTensorQuantization(name, shape, quantize)
	}

	switch {
	case isEmbedTokensWeight(name) || strings.HasSuffix(name, "lm_head.weight"):
		// Laguna 嵌入与 lm_head 分离且体积大，FP 量化下均提升至 8-bit。
		// quality-sensitive tensors, so keep them at 8-bit for FP quants.
		return promoteEmbedding(shape, base)
	case strings.HasSuffix(name, ".mlp.gate.weight"):
		return ""
	case base == "mxfp8" && lagunaAttentionProjection(name):
		return ""
	case lagunaAttentionProjection(name):
		return lagunaQuantizationType(name, shape, base)
	case lagunaDenseMLPProjection(name) && t.denseMLPLayers[layerIndex(name)]:
		return lagunaQuantizationType(name, shape, base)
	case base == "mxfp8" && lagunaRoutedExpertDownProjection(name):
		if lagunaPromoteExpertDown(layerIndex(name), t.numLayers) {
			return ""
		}
		return lagunaQuantizationType(name, shape, base)
	case lagunaSharedExpertDownProjection(name):
		return lagunaSensitiveType(lagunaPromoteExpertDown(layerIndex(name), t.numLayers), name, shape, base)
	case lagunaSharedExpertProjection(name):
		return lagunaQuantizationType(name, shape, base)
	case lagunaRoutedExpertProjection(name):
		return lagunaQuantizationType(name, shape, base)
	default:
		return ""
	}
}

// lagunaFPQuant 判断是否为 Laguna 架构策略覆盖的 FP 量化族。
func lagunaFPQuant(quantize string) bool {
	return quantize == "nvfp4" || quantize == "mxfp4" || quantize == "mxfp8"
}

// lagunaAttentionProjection 识别 self_attn 各投影 weight。
func lagunaAttentionProjection(name string) bool {
	return strings.Contains(name, ".self_attn.q_proj.weight") ||
		strings.Contains(name, ".self_attn.k_proj.weight") ||
		strings.Contains(name, ".self_attn.v_proj.weight") ||
		strings.Contains(name, ".self_attn.o_proj.weight") ||
		strings.Contains(name, ".self_attn.g_proj.weight")
}

// lagunaDenseMLPProjection 识别 dense MLP 的 gate/up/down 投影。
func lagunaDenseMLPProjection(name string) bool {
	return strings.Contains(name, ".mlp.gate_proj.weight") ||
		strings.Contains(name, ".mlp.up_proj.weight") ||
		strings.Contains(name, ".mlp.down_proj.weight")
}

// lagunaRoutedExpertProjection 识别 routed expert 的 MLP 投影。
func lagunaRoutedExpertProjection(name string) bool {
	if !lagunaMLPProjectionWeight(name) {
		return false
	}
	return strings.Contains(name, ".mlp.experts.")
}

// lagunaRoutedExpertDownProjection 识别 routed expert 的 down_proj。
func lagunaRoutedExpertDownProjection(name string) bool {
	return strings.Contains(name, ".mlp.experts.") && strings.HasSuffix(name, ".down_proj.weight")
}

// lagunaSharedExpertProjection 识别 shared expert 的 MLP 投影。
func lagunaSharedExpertProjection(name string) bool {
	if !lagunaMLPProjectionWeight(name) {
		return false
	}
	return strings.Contains(name, ".mlp.shared_expert.")
}

// lagunaSharedExpertDownProjection 识别 shared expert down_proj。
func lagunaSharedExpertDownProjection(name string) bool {
	return strings.Contains(name, ".mlp.shared_expert.down_proj.weight")
}

// lagunaPromoteExpertDown 对 expert down 在输入/输出层与稀疏 cadence 使用更高精度。
// Laguna XS 2 and 2.1 are sensitive to fully quantizing expert down
// projections. Keep the same cadence for both: use higher precision on the
// input-side layers, final layers, and a sparse cadence early in the residual
// stream. For 4-bit fp quants that higher precision is mxfp8. For mxfp8, the
// shared expert down projections stay at source precision because the tensor
// class is small; routed expert down projections use the cadence to avoid
// pushing the model too close to bf16 size.
// lagunaPromoteExpertDown 委托 useMoreBitsWithMiddleEnd 决定 down 是否提精。
func lagunaPromoteExpertDown(layerIdx, numLayers int) bool {
	return useMoreBitsWithMiddleEnd(layerIdx, numLayers, numLayers/2-4)
}

// lagunaMLPProjectionWeight 判断是否为 gate/up/down_proj weight 后缀。
func lagunaMLPProjectionWeight(name string) bool {
	return strings.HasSuffix(name, ".gate_proj.weight") ||
		strings.HasSuffix(name, ".up_proj.weight") ||
		strings.HasSuffix(name, ".down_proj.weight")
}

// lagunaQuantizationType 调用通用策略并撤销 blanket 4→8 提升。
func lagunaQuantizationType(name string, shape []int32, quantize string) string {
	q := GetTensorQuantization(name, shape, quantize)
	// Laguna 架构策略自行决定提精对象，此处撤销通用 blanket 提升。
	// promote. Undo the generic policy's blanket 4-to-8-bit promotion here.
	if q != quantize && q == eightBit(quantize) {
		return quantize
	}
	return q
}

// lagunaSensitiveType 在 mxfp8 与 promote 标志下解析 shared expert down 精度。
func lagunaSensitiveType(promote bool, name string, shape []int32, quantize string) string {
	if quantize == "mxfp8" {
		return ""
	}
	if promote {
		return GetTensorQuantization(name, shape, quantize)
	}
	return lagunaQuantizationType(name, shape, quantize)
}
