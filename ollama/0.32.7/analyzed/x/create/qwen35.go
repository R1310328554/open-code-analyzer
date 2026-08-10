// Qwen3.5/Qwen3Next 导入量化策略：低秩 linear-attention 投影保持源精度。
package create

import (
	"encoding/json"
	"strings"
)

// qwen35ImportTransform 无状态架构策略。
type qwen35ImportTransform struct{}

// newQwen35ImportTransform 构造空 Qwen3.5 策略（无需解析 config）。
func newQwen35ImportTransform(json.RawMessage) (quantizePolicy, error) {
	return qwen35ImportTransform{}, nil
}

// quantizationType 跳过低秩 linear-attention 投影，其余走 GetTensorQuantization。
func (qwen35ImportTransform) quantizationType(name string, shape []int32, quantize string) string {
	// 低秩 linear-attention 投影对量化敏感，保持源精度。
	// at source precision.
	if qwen35IsLowRankProjection(name) {
		return ""
	}
	return GetTensorQuantization(name, shape, quantize)
}

// qwen35IsLowRankProjection 识别 in_proj_a/b/ba 低秩注意力权重。
func qwen35IsLowRankProjection(name string) bool {
	return strings.HasSuffix(name, ".linear_attn.in_proj_a.weight") ||
		strings.HasSuffix(name, ".linear_attn.in_proj_b.weight") ||
		strings.HasSuffix(name, ".linear_attn.in_proj_ba.weight")
}
