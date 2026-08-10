// Phi-3 转换：Microsoft Phi-3 小模型（含 LongRoPE/YaRN）到 GGUF。
package convert

import (
	"cmp"
	"encoding/binary"
	"io"
	"math"
	"strings"
	"sync"

	"github.com/ollama/ollama/fs/ggml"
)

// phi3Model 解析 Phi-3 配置（兼容 n_layers/n_head 等别名）。
type phi3Model struct {
	ModelParameters
	NumHiddenLayers   uint32  `json:"num_hidden_layers"`
	NLayers           uint32  `json:"n_layers"`
	HiddenSize        uint32  `json:"hidden_size"`
	NEmbd             uint32  `json:"n_embd"`
	IntermediateSize  uint32  `json:"intermediate_size"`
	NumAttentionHeads uint32  `json:"num_attention_heads"`
	NHead             uint32  `json:"n_head"`
	NumKeyValueHeads  uint32  `json:"num_key_value_heads"`
	NHeadKV           uint32  `json:"n_head_kv"`
	RopeTheta         float32 `json:"rope_theta"`
	RopeScaling       struct {
		Type        string     `json:"type"`
		LongFactor  ropeFactor `json:"long_factor"`
		ShortFactor ropeFactor `json:"short_factor"`
	} `json:"rope_scaling"`
	RMSNormEPS                    float32 `json:"rms_norm_eps"`
	NPositions                    uint32  `json:"n_positions"`
	MaxPositionEmbeddings         uint32  `json:"max_position_embeddings"`
	OriginalMaxPositionEmbeddings uint32  `json:"original_max_position_embeddings"`
	SlidingWindow                 uint32  `json:"sliding_window"`
}

var _ ModelConverter = (*phi3Model)(nil)

// KV 写入 phi3 元数据并据 rope_scaling 类型计算 attn_factor。
func (p *phi3Model) KV(t *Tokenizer) KV {
	kv := p.ModelParameters.KV(t)
	kv["general.architecture"] = "phi3"
	kv["phi3.context_length"] = p.MaxPositionEmbeddings
	kv["phi3.embedding_length"] = cmp.Or(p.HiddenSize, p.NEmbd)
	kv["phi3.feed_forward_length"] = p.IntermediateSize
	kv["phi3.block_count"] = cmp.Or(p.NumHiddenLayers, p.NLayers)
	kv["phi3.attention.head_count"] = cmp.Or(p.NumAttentionHeads, p.NHead)
	kv["phi3.attention.head_count_kv"] = cmp.Or(p.NumKeyValueHeads, p.NHeadKV)
	kv["phi3.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS
	kv["phi3.rope.dimension_count"] = p.HiddenSize / cmp.Or(p.NumAttentionHeads, p.NHead)
	kv["phi3.rope.freq_base"] = p.RopeTheta
	kv["phi3.rope.scaling.original_context_length"] = p.OriginalMaxPositionEmbeddings
	kv["phi3.attention.sliding_window"] = p.SlidingWindow

	scale := float64(p.MaxPositionEmbeddings) / float64(p.OriginalMaxPositionEmbeddings)

	switch p.RopeScaling.Type {
	case "":
		// 无 RoPE 缩放。
		// no scaling
	case "su", "longrope":
		kv["phi3.rope.scaling.attn_factor"] = float32(max(math.Sqrt(1+math.Log(scale)/math.Log(float64(p.OriginalMaxPositionEmbeddings))), 1.0))
	case "yarn":
		kv["phi3.rope.scaling.attn_factor"] = float32(max(0.1*math.Log(scale)+1.0, 1.0))
	default:
		panic("unknown rope scaling type")
	}

	return kv
}

// Tensors 在首个 blk 张量前注入 long/short rope factor 权重。
func (p *phi3Model) Tensors(ts []Tensor) []*ggml.Tensor {
	var addRopeFactors sync.Once

	out := make([]*ggml.Tensor, 0, len(ts)+2)
	for _, t := range ts {
		if strings.HasPrefix(t.Name(), "blk.0.") {
			addRopeFactors.Do(func() {
				out = append(out, &ggml.Tensor{
					Name:     "rope_factors_long.weight",
					Kind:     0,
					Shape:    []uint64{uint64(len(p.RopeScaling.LongFactor))},
					WriterTo: p.RopeScaling.LongFactor,
				}, &ggml.Tensor{
					Name:     "rope_factors_short.weight",
					Kind:     0,
					Shape:    []uint64{uint64(len(p.RopeScaling.ShortFactor))},
					WriterTo: p.RopeScaling.ShortFactor,
				})
			})
		}

		out = append(out, &ggml.Tensor{
			Name:     t.Name(),
			Kind:     t.Kind(),
			Shape:    t.Shape(),
			WriterTo: t,
		})
	}

	return out
}

// Replacements 映射 Phi-3 HF 张量路径（含 QKV 合并）到 GGUF。
func (p *phi3Model) Replacements() []string {
	return []string{
		"lm_head", "output",
		"model.embed_tokens", "token_embd",
		"model.norm", "output_norm",
		"model.layers", "blk",
		"input_layernorm", "attn_norm",
		"self_attn.qkv_proj", "attn_qkv",
		"self_attn.o_proj", "attn_output",
		"mlp.down_proj", "ffn_down",
		"mlp.gate_up_proj", "ffn_up",
		"post_attention_layernorm", "ffn_norm",
	}
}

// ropeFactor 为 LongRoPE 因子序列，实现 io.WriterTo。
type ropeFactor []float32

// WriteTo 以小端 float32 序列写入 rope factor。
func (r ropeFactor) WriteTo(w io.Writer) (int64, error) {
	return 0, binary.Write(w, binary.LittleEndian, r)
}
