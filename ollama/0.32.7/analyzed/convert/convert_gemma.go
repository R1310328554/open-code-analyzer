package convert

import (
	"strings"

	"github.com/pdevine/tensor"
	"github.com/pdevine/tensor/native"

	"github.com/ollama/ollama/fs/ggml"
)

// convert_gemma 实现 Google Gemma v1 因果语言模型的 GGUF 转换。
// gemmaModel 表示 Gemma v1 架构的 ModelConverter。
type gemmaModel struct {
	ModelParameters
	MaxPositionEmbeddings uint32  `json:"max_position_embeddings"`
	HiddenSize            uint32  `json:"hidden_size"`
	HiddenLayers          uint32  `json:"num_hidden_layers"`
	IntermediateSize      uint32  `json:"intermediate_size"`
	NumAttentionHeads     uint32  `json:"num_attention_heads"`
	NumKeyValueHeads      uint32  `json:"num_key_value_heads"`
	RMSNormEPS            float32 `json:"rms_norm_eps"`
	HeadDim               uint32  `json:"head_dim"`
}

var _ ModelConverter = (*gemmaModel)(nil)

// KV 写入 gemma 架构超参与特殊 token ID。
func (p *gemmaModel) KV(t *Tokenizer) KV {
	kv := p.ModelParameters.KV(t)
	kv["general.architecture"] = "gemma"
	kv["gemma.context_length"] = p.MaxPositionEmbeddings
	kv["gemma.embedding_length"] = p.HiddenSize
	kv["gemma.block_count"] = p.HiddenLayers
	kv["gemma.feed_forward_length"] = p.IntermediateSize
	kv["gemma.attention.head_count"] = p.NumAttentionHeads
	kv["gemma.attention.head_count_kv"] = p.NumKeyValueHeads
	kv["gemma.attention.layer_norm_rms_epsilon"] = p.RMSNormEPS
	kv["gemma.attention.key_length"] = p.HeadDim
	kv["gemma.attention.value_length"] = p.HeadDim
	kv["tokenizer.ggml.eot_token_id"] = uint32(107)
	kv["tokenizer.ggml.middle_token_id"] = uint32(68)
	kv["tokenizer.ggml.prefix_token_id"] = uint32(67)
	kv["tokenizer.ggml.suffix_token_id"] = uint32(69)
	return kv
}

// Tensors 为非视觉 norm 权重注册 addOne repacker。
func (p *gemmaModel) Tensors(ts []Tensor) []*ggml.Tensor {
	var out []*ggml.Tensor
	for _, t := range ts {
		if !strings.HasPrefix(t.Name(), "v.") && strings.HasSuffix(t.Name(), "_norm.weight") {
			t.SetRepacker(p.addOne)
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

// Replacements 映射 Gemma v1 层张量命名。
func (p *gemmaModel) Replacements() []string {
	return []string{
		"model.embed_tokens", "token_embd",
		"model.norm", "output_norm",
		"model.layers", "blk",
		"input_layernorm", "attn_norm",
		"self_attn.q_proj", "attn_q",
		"self_attn.k_proj", "attn_k",
		"self_attn.v_proj", "attn_v",
		"self_attn.o_proj", "attn_output",
		"mlp.gate_proj", "ffn_gate",
		"mlp.down_proj", "ffn_down",
		"mlp.up_proj", "ffn_up",
		"post_attention_layernorm", "ffn_norm",
	}
}

// addOne 对 RMS norm 权重加 1，符合 Gemma 推理约定。
func (*gemmaModel) addOne(_ string, data []float32, shape []uint64) ([]float32, error) {
	n := tensor.New(tensor.WithShape(int(shape[0])), tensor.WithBacking(data))
	ones := tensor.Ones(tensor.Float32, int(shape[0]))

	n, err := n.Add(ones)
	if err != nil {
		return nil, err
	}

	ts, err := native.SelectF32(n, 0)
	if err != nil {
		return nil, err
	}

	var f32s []float32
	for _, t := range ts {
		f32s = append(f32s, t...)
	}

	return f32s, nil
}
