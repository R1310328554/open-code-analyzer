// Package qwen3_5_moe 注册 Qwen3.5 MoE 架构别名到 qwen3_5.NewModel。
// Package qwen3_5_moe registers Qwen 3.5 MoE architecture aliases.
package qwen3_5_moe

import (
	"github.com/ollama/ollama/x/mlxrunner/model/base"
	"github.com/ollama/ollama/x/models/qwen3_5"
)

// init 注册 Qwen3.5 / Qwen3Next MoE 条件生成与 CausalLM 类型。
func init() {
	base.Register("Qwen3_5MoeForConditionalGeneration", qwen3_5.NewModel)
	base.Register("Qwen3_5MoeForCausalLM", qwen3_5.NewModel)
	base.Register("Qwen3NextMoeForConditionalGeneration", qwen3_5.NewModel)
	base.Register("Qwen3NextMoeForCausalLM", qwen3_5.NewModel)
}
