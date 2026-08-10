// LFM2 转换：ShortConv + 注意力混合层与可选 MoE。
package convert

import (
	"cmp"
	"fmt"
	"slices"
	"strings"

	"github.com/ollama/ollama/fs/ggml"
)

// lfm2Model 解析 Liquid Foundation Model 2 配置。
type lfm2Model struct {
	ModelParameters
	HiddenSize            uint32   `json:"hidden_size"`
	NumHiddenLayers       uint32   `json:"num_hidden_layers"`
	MaxPositionEmbeddings uint32   `json:"max_position_embeddings"`
	IntermediateSize      uint32   `json:"intermediate_size"`
	BlockFFDim            uint32   `json:"block_ff_dim"`
	BlockMultipleOf       uint32   `json:"block_multiple_of"`
	BlockAutoAdjustFFDim  bool     `json:"block_auto_adjust_ff_dim"`
	BlockFFNDimMultiplier float32  `json:"block_ffn_dim_multiplier"`
	NumAttentionHeads     uint32   `json:"num_attention_heads"`
	NumKeyValueHeads      uint32   `json:"num_key_value_heads"`
	RopeTheta             float32  `json:"rope_theta"`
	NormEps               float32  `json:"norm_eps"`
	ConvLCache            uint32   `json:"conv_L_cache"`
	MoEIntermediateSize   uint32   `json:"moe_intermediate_size"`
	NumExperts            uint32   `json:"num_experts"`
	NumLocalExperts       uint32   `json:"num_local_experts"`
	NumExpertsPerToken    uint32   `json:"num_experts_per_tok"`
	NumDenseLayers        uint32   `json:"num_dense_layers"`
	RoutedScalingFactor   float32  `json:"routed_scaling_factor"`
	LayerTypes            []string `json:"layer_types"`
	TieEmbedding          bool     `json:"tie_embedding"`
	RopeParameters        struct {
		RopeTheta float32 `json:"rope_theta"`
	} `json:"rope_parameters"`
}

// lfm2Model 实现 ModelConverter。
var _ ModelConverter = (*lfm2Model)(nil)

// 默认上下文长度与已知 MoE 变体的回退值。
const (
	defaultMaxPositionEmbeddings = uint32(128_000)
	fallbackContextLength        = uint32(32_768)
)

// isMoE 根据 model_type 或专家数判断是否为 MoE 变体。
func (p *lfm2Model) isMoE() bool {
	return p.ModelType == "lfm2_moe" || p.expertCount() > 0
}

// ropeFreqBase 读取 RoPE 基频（兼容 rope_parameters）。
func (p *lfm2Model) ropeFreqBase() float32 {
	if p.RopeTheta != 0 {
		return p.RopeTheta
	}

	return p.RopeParameters.RopeTheta
}

// expertCount 优先 num_local_experts，否则 num_experts。
func (p *lfm2Model) expertCount() uint32 {
	if p.NumLocalExperts > 0 {
		return p.NumLocalExperts
	}
	return p.NumExperts
}

// feedForwardLength 按 llama.cpp 规则计算 FFN 隐层宽度（含倍数对齐）。
func (p *lfm2Model) feedForwardLength() uint32 {
	ff := p.IntermediateSize
	if p.BlockFFDim != 0 {
		ff = p.BlockFFDim
	}

	if !p.BlockAutoAdjustFFDim || p.BlockMultipleOf == 0 {
		return ff
	}

	ff = (2 * ff) / 3

	// 与 llama.cpp 转换保持相同的 FFN 倍数默认值。
	// Keep default multiplier behavior consistent with llama.cpp conversion.
	if p.BlockFFNDimMultiplier != 0 {
		ff = uint32(float32(ff) * p.BlockFFNDimMultiplier)
	}

	m := p.BlockMultipleOf
	return m * ((ff + m - 1) / m)
}

// hasKnownContextLengthFallbackSignature 识别需 32K 上下文回退的已知 MoE 签名。
func (p *lfm2Model) hasKnownContextLengthFallbackSignature() bool {
	return p.isMoE() &&
		p.VocabSize == 65536 &&
		p.HiddenSize == 2048 &&
		p.NumHiddenLayers == 40 &&
		p.IntermediateSize == 11776 &&
		p.NumAttentionHeads == 32 &&
		p.NumKeyValueHeads == 8 &&
		p.NumDenseLayers == 2 &&
		p.expertCount() == 64 &&
		p.NumExpertsPerToken == 4 &&
		p.MoEIntermediateSize == 1536
}

// contextLength 在特定 MoE 签名下将 128K 回退为 32K。
func (p *lfm2Model) contextLength() uint32 {
	if p.MaxPositionEmbeddings == defaultMaxPositionEmbeddings && p.hasKnownContextLengthFallbackSignature() {
		return fallbackContextLength
	}

	return p.MaxPositionEmbeddings
}

// KV 写入 lfm2/lfm2moe 元数据与逐层 KV 头数组。
func (p *lfm2Model) KV(t *Tokenizer) KV {
	architecture := "lfm2"
	if p.isMoE() {
		architecture = "lfm2moe"
	}

	kv := p.ModelParameters.KV(t)
	kv["general.architecture"] = architecture
	kv["tokenizer.ggml.pre"] = "lfm2"
	kv["vocab_size"] = p.VocabSize
	kv["block_count"] = p.NumHiddenLayers
	kv["embedding_length"] = p.HiddenSize
	kv["feed_forward_length"] = p.feedForwardLength()
	kv["context_length"] = p.contextLength()

	// 按 layer_types 构建逐层 KV 头数（0 表示 ShortConv 层）。
	// Build per-layer KV head count array based on layer_types
	// (0 = shortconv layer, non-zero = attention layer with that many KV heads).
	//
	// Dense LFM2 in HF defaults to all attention layers when layer_types is absent.
	// Preserve that behavior to avoid accidentally emitting all-conv metadata.
	kvHeadCounts := make([]uint32, p.NumHiddenLayers)
	if len(p.LayerTypes) == 0 {
		for i := range p.NumHiddenLayers {
			kvHeadCounts[i] = p.NumKeyValueHeads
		}
	} else {
		for i := range p.NumHiddenLayers {
			if int(i) < len(p.LayerTypes) && p.LayerTypes[i] == "full_attention" {
				kvHeadCounts[i] = p.NumKeyValueHeads
			}
		}
	}

	kv["attention.head_count"] = p.NumAttentionHeads
	kv["attention.head_count_kv"] = kvHeadCounts
	kv["attention.key_length"] = p.HiddenSize / p.NumAttentionHeads
	kv["attention.value_length"] = p.HiddenSize / p.NumAttentionHeads
	kv["attention.layer_norm_rms_epsilon"] = p.NormEps
	kv["shortconv.l_cache"] = p.ConvLCache

	if ropeFreqBase := p.ropeFreqBase(); ropeFreqBase != 0 {
		kv["rope.freq_base"] = ropeFreqBase
	}

	if p.isMoE() {
		kv["expert_count"] = p.expertCount()
		kv["expert_used_count"] = p.NumExpertsPerToken
		kv["expert_feed_forward_length"] = p.MoEIntermediateSize
		kv["leading_dense_block_count"] = p.NumDenseLayers
		kv["expert_gating_func"] = uint32(2) // sigmoid
		kv["expert_weights_scale"] = cmp.Or(p.RoutedScalingFactor, float32(1.0))
	}

	return kv
}

// Tensors 合并 MoE 专家并压缩 shortconv 卷积权重维度。
func (p *lfm2Model) Tensors(ts []Tensor) []*ggml.Tensor {
	var out []*ggml.Tensor

	if p.isMoE() {
		merges := make([]merge, 0, p.NumHiddenLayers*3)
		for i := range p.NumHiddenLayers {
			if i < p.NumDenseLayers {
				continue
			}

			merges = append(merges, merge{
				fmt.Sprintf("blk.%d.feed_forward.experts.*.w1.weight", i),
				fmt.Sprintf("blk.%d.ffn_gate_exps.weight", i),
			}, merge{
				fmt.Sprintf("blk.%d.feed_forward.experts.*.w2.weight", i),
				fmt.Sprintf("blk.%d.ffn_down_exps.weight", i),
			}, merge{
				fmt.Sprintf("blk.%d.feed_forward.experts.*.w3.weight", i),
				fmt.Sprintf("blk.%d.ffn_up_exps.weight", i),
			})
		}

		merged, remaining := mergeTensors(ts, merges...)
		out = append(out, merged...)
		ts = remaining
	}

	for _, t := range ts {
		shape := t.Shape()

		// ShortConv 权重 [D,1,K] 压成 [D,K]。
		// Squeeze conv weights: [D, 1, K] -> [D, K]
		if strings.HasSuffix(t.Name(), "shortconv.conv.weight") {
			if len(shape) == 3 && shape[1] == 1 {
				shape = []uint64{shape[0], shape[2]}
			}
		}

		out = append(out, &ggml.Tensor{
			Name:     t.Name(),
			Kind:     t.Kind(),
			Shape:    slices.Clone(shape),
			WriterTo: t,
		})
	}

	return out
}

// Replacements 映射 LFM2 注意力、ShortConv 与 MoE 张量名。
func (p *lfm2Model) Replacements() []string {
	return []string{
		"model.embed_tokens", "token_embd",
		"model.embedding_norm", "token_embd_norm",
		"model.layers", "blk",
		"operator_norm", "attn_norm",
		"self_attn.q_proj", "attn_q",
		"self_attn.k_proj", "attn_k",
		"self_attn.v_proj", "attn_v",
		"self_attn.out_proj", "attn_output",
		"self_attn.q_layernorm", "attn_q_norm",
		"self_attn.k_layernorm", "attn_k_norm",
		"conv.conv", "shortconv.conv",
		"conv.in_proj", "shortconv.in_proj",
		"conv.out_proj", "shortconv.out_proj",
		"feed_forward.gate", "ffn_gate_inp",
		"feed_forward.expert_bias", "exp_probs_b.bias",
		"feed_forward.w1", "ffn_gate",
		"feed_forward.w2", "ffn_down",
		"feed_forward.w3", "ffn_up",
		"ffn_norm", "ffn_norm",
	}
}
