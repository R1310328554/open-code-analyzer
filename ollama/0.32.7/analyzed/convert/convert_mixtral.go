// Mixtral 转换：在 LLaMA 基础上追加稀疏 MoE 专家合并。
package convert

import (
	"fmt"

	"github.com/ollama/ollama/fs/ggml"
)

// mixtralModel 嵌入 llamaModel 并追加 Mixtral MoE 字段。
type mixtralModel struct {
	llamaModel
	NumLocalExperts    uint32 `json:"num_local_experts"`
	NumExpertsPerToken uint32 `json:"num_experts_per_tok"`
}

// KV 继承 llama KV 并写入专家数与每 token 专家数。
func (p *mixtralModel) KV(t *Tokenizer) KV {
	kv := p.llamaModel.KV(t)

	if p.NumLocalExperts > 0 {
		kv["llama.expert_count"] = p.NumLocalExperts
	}

	if p.NumExpertsPerToken > 0 {
		kv["llama.expert_used_count"] = p.NumExpertsPerToken
	}

	return kv
}

// Tensors 合并每层 w1/w2/w3 专家权重后委托 llamaModel.Tensors。
func (p *mixtralModel) Tensors(ts []Tensor) []*ggml.Tensor {
	merges := make([]merge, 0, p.NumHiddenLayers*6)
	for i := range p.NumHiddenLayers {
		merges = append(merges, merge{
			fmt.Sprintf("blk.%d.*.w1.weight", i),
			fmt.Sprintf("blk.%d.ffn_gate_exps.weight", i),
		}, merge{
			fmt.Sprintf("blk.%d.*.w1.bias", i),
			fmt.Sprintf("blk.%d.ffn_gate_exps.bias", i),
		}, merge{
			fmt.Sprintf("blk.%d.*.w2.weight", i),
			fmt.Sprintf("blk.%d.ffn_up_exps.weight", i),
		}, merge{
			fmt.Sprintf("blk.%d.*.w2.bias", i),
			fmt.Sprintf("blk.%d.ffn_up_exps.bias", i),
		}, merge{
			fmt.Sprintf("blk.%d.*.w3.weight", i),
			fmt.Sprintf("blk.%d.ffn_down_exps.weight", i),
		}, merge{
			fmt.Sprintf("blk.%d.*.w3.bias", i),
			fmt.Sprintf("blk.%d.ffn_down_exps.bias", i),
		})
	}

	out, ts := mergeTensors(ts, merges...)
	return append(out, p.llamaModel.Tensors(ts)...)
}

// Replacements 扩展 LLaMA 规则以覆盖 block_sparse_moe 路径。
func (p *mixtralModel) Replacements() []string {
	return append(
		p.llamaModel.Replacements(),
		"model.layers", "blk",
		"block_sparse_moe.gate", "ffn_gate_inp",
		"block_sparse_moe.experts.", ".",
	)
}
