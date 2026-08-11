# Copyright 2024 The Qwen team, Alibaba Group and the HuggingFace Inc. team. All rights reserved.
#
# This code is based on EleutherAI's GPT-NeoX library and the GPT-NeoX
# and OPT implementations in this library. It has been modified from its
# original forms to accommodate minor architectural differences compared
# to GPT-NeoX and OPT used by the Meta AI team that trained the model.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""PyTorch Qwen2MoE model."""

import torch
import torch.nn.functional as F
from torch import nn

from ...activations import ACT2FN
from ...cache_utils import Cache, DynamicCache
from ...generation import GenerationMixin
from ...masking_utils import create_causal_mask, create_sliding_window_causal_mask
from ...modeling_layers import (
# Qwen2 MoE modular 源：复用 Llama/Gemma/Mixtral 组件并定制路由与共享专家

    GenericForQuestionAnswering,
    GenericForSequenceClassification,
    GenericForTokenClassification,
)
from ...modeling_outputs import MoeModelOutputWithPast
from ...processing_utils import Unpack
from ...utils import TransformersKwargs, auto_docstring
from ...utils.generic import merge_with_config_defaults
from ...utils.output_capturing import OutputRecorder, capture_outputs
from ..gemma.modeling_gemma import GemmaMLP
from ..gemma2.modeling_gemma2 import Gemma2RotaryEmbedding
from ..llama.modeling_llama import LlamaAttention, LlamaDecoderLayer, LlamaRMSNorm
from ..mixtral.modeling_mixtral import (
    MixtralExperts,
    MixtralForCausalLM,
    MixtralModel,
    MixtralPreTrainedModel,
)
from .configuration_qwen2_moe import Qwen2MoeConfig


# Qwen2MoeRMSNorm：RMS 层归一化：Root Mean Square 归一化替代 LayerNorm
class Qwen2MoeRMSNorm(LlamaRMSNorm):
    pass


# Qwen2MoeRotaryEmbedding：旋转位置编码：RoPE 频率计算与动态序列长度扩展
class Qwen2MoeRotaryEmbedding(Gemma2RotaryEmbedding):
    pass


# Qwen2MoeMLP：稠密前馈网络：SwiGLU 风格 gate/up/down 投影
class Qwen2MoeMLP(GemmaMLP):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config, intermediate_size=None):
        super().__init__()
        self.config = config
        self.hidden_size = config.hidden_size
        self.intermediate_size = config.intermediate_size if intermediate_size is None else intermediate_size
        self.gate_proj = nn.Linear(self.hidden_size, self.intermediate_size, bias=False)
        self.up_proj = nn.Linear(self.hidden_size, self.intermediate_size, bias=False)
        self.down_proj = nn.Linear(self.intermediate_size, self.hidden_size, bias=False)
        self.act_fn = ACT2FN[config.hidden_act]


# Qwen2MoeAttention：自注意力：GQA 分组查询与滑动窗口注意力支持
class Qwen2MoeAttention(LlamaAttention):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Qwen2MoeConfig, layer_idx: int):
        super().__init__(config, layer_idx)
        if self.config.layer_types[layer_idx] == "sliding_attention":
            self.sliding_window = config.sliding_window

        self.q_proj = nn.Linear(config.hidden_size, config.num_attention_heads * self.head_dim, bias=config.qkv_bias)
        self.k_proj = nn.Linear(config.hidden_size, config.num_key_value_heads * self.head_dim, bias=config.qkv_bias)
        self.v_proj = nn.Linear(config.hidden_size, config.num_key_value_heads * self.head_dim, bias=config.qkv_bias)
        self.o_proj = nn.Linear(config.num_attention_heads * self.head_dim, config.hidden_size, bias=False)


# Qwen2MoeExperts：MoE 专家组：并行专家 FFN 与路由权重加权求和
class Qwen2MoeExperts(MixtralExperts):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config):
        super().__init__(config)
        self.num_experts = config.num_experts
        self.intermediate_dim = config.moe_intermediate_size


# Qwen2MoeTopKRouter：Top-K 路由器：线性门控 softmax 选取活跃专家
class Qwen2MoeTopKRouter(nn.Module):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config):
        super().__init__()
        self.top_k = config.num_experts_per_tok
        self.num_experts = config.num_experts
        self.norm_topk_prob = config.norm_topk_prob
        self.hidden_dim = config.hidden_size
        self.weight = nn.Parameter(torch.zeros(self.num_experts, self.hidden_dim))

    # forward：前向传播：组装特征并返回模型输出
    def forward(self, hidden_states):
        hidden_states = hidden_states.reshape(-1, self.hidden_dim)
        router_logits = F.linear(hidden_states, self.weight)  # (seq_len, num_experts)
        router_probs = torch.nn.functional.softmax(router_logits, dtype=torch.float, dim=-1)
        router_top_value, router_indices = torch.topk(router_probs, self.top_k, dim=-1)  # (seq_len, top_k)
        if self.norm_topk_prob:
            router_top_value /= router_top_value.sum(dim=-1, keepdim=True)
        router_top_value = router_top_value.to(router_logits.dtype)
        router_scores = router_top_value
        return router_logits, router_scores, router_indices


# Qwen2MoeSparseMoeBlock：稀疏 MoE 块：路由 + 专家 + 共享专家门控融合
class Qwen2MoeSparseMoeBlock(nn.Module):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config):
        super().__init__()
        self.gate = Qwen2MoeTopKRouter(config)
        self.experts = Qwen2MoeExperts(config)
        self.shared_expert = Qwen2MoeMLP(config, intermediate_size=config.shared_expert_intermediate_size)
        self.shared_expert_gate = torch.nn.Linear(config.hidden_size, 1, bias=False)

    # forward：前向传播：组装特征并返回模型输出
    def forward(self, hidden_states: torch.Tensor) -> tuple[torch.Tensor, torch.Tensor]:
        batch_size, sequence_length, hidden_dim = hidden_states.shape
        hidden_states_reshaped = hidden_states.view(-1, hidden_dim)
        shared_expert_output = self.shared_expert(hidden_states_reshaped)
        _, routing_weights, selected_experts = self.gate(hidden_states_reshaped)
        expert_output = self.experts(hidden_states_reshaped, selected_experts, routing_weights)

        shared_expert_output = F.sigmoid(self.shared_expert_gate(hidden_states_reshaped)) * shared_expert_output

        expert_output = expert_output + shared_expert_output
        expert_output = expert_output.reshape(batch_size, sequence_length, hidden_dim)
        return expert_output


# Qwen2MoeDecoderLayer：解码器层：自注意力 + 稀疏/稠密 FFN 残差结构
class Qwen2MoeDecoderLayer(LlamaDecoderLayer):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Qwen2MoeConfig, layer_idx: int):
        nn.Module.__init__(self)
        self.self_attn = Qwen2MoeAttention(config, layer_idx)
        if (layer_idx not in config.mlp_only_layers) and (
            config.num_experts > 0 and (layer_idx + 1) % config.decoder_sparse_step == 0
        ):
            self.mlp = Qwen2MoeSparseMoeBlock(config)
        else:
            self.mlp = Qwen2MoeMLP(config, intermediate_size=config.intermediate_size)
        self.input_layernorm = Qwen2MoeRMSNorm(config.hidden_size, eps=config.rms_norm_eps)
        self.post_attention_layernorm = Qwen2MoeRMSNorm(config.hidden_size, eps=config.rms_norm_eps)
        self.hidden_size = config.hidden_size


@auto_docstring
# Qwen2MoePreTrainedModel：Qwen2 MoE 预训练基类：通用初始化与输出录制
class Qwen2MoePreTrainedModel(MixtralPreTrainedModel):
    _can_record_outputs = {
        "router_logits": OutputRecorder(Qwen2MoeTopKRouter, index=0),
        "hidden_states": Qwen2MoeDecoderLayer,
        "attentions": Qwen2MoeAttention,
    }


@auto_docstring
# Qwen2MoeModel：MoE 骨干：多层解码器堆栈与 RoPE 位置编码
class Qwen2MoeModel(MixtralModel):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Qwen2MoeConfig):
        super().__init__(config)
        self.layers = nn.ModuleList(
            [Qwen2MoeDecoderLayer(config, layer_idx) for layer_idx in range(config.num_hidden_layers)]
        )
        self.norm = Qwen2MoeRMSNorm(config.hidden_size, eps=config.rms_norm_eps)
        self.rotary_emb = Qwen2MoeRotaryEmbedding(config=config)

    @merge_with_config_defaults
    @capture_outputs
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        input_ids: torch.LongTensor | None = None,
        attention_mask: torch.Tensor | None = None,
        position_ids: torch.LongTensor | None = None,
        past_key_values: Cache | None = None,
        inputs_embeds: torch.FloatTensor | None = None,
        use_cache: bool | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> MoeModelOutputWithPast:
        if (input_ids is None) ^ (inputs_embeds is not None):
            raise ValueError("You must specify exactly one of input_ids or inputs_embeds")

        if inputs_embeds is None:
            inputs_embeds = self.embed_tokens(input_ids)

        if use_cache and past_key_values is None:
            past_key_values = DynamicCache(config=self.config)

        if position_ids is None:
            past_seen_tokens = past_key_values.get_seq_length() if past_key_values is not None else 0
            position_ids = torch.arange(inputs_embeds.shape[1], device=inputs_embeds.device) + past_seen_tokens
            position_ids = position_ids.unsqueeze(0)

        # It may already have been prepared by e.g. `generate`
        if not isinstance(causal_mask_mapping := attention_mask, dict):
            # Prepare mask arguments
            mask_kwargs = {
                "config": self.config,
                "inputs_embeds": inputs_embeds,
                "attention_mask": attention_mask,
                "past_key_values": past_key_values,
                "position_ids": position_ids,
            }
            # Create the masks
            causal_mask_mapping = {
                "full_attention": create_causal_mask(**mask_kwargs),
                "sliding_attention": create_sliding_window_causal_mask(**mask_kwargs),
            }

        hidden_states = inputs_embeds
        position_embeddings = self.rotary_emb(hidden_states, position_ids)

        for i, decoder_layer in enumerate(self.layers[: self.config.num_hidden_layers]):
            hidden_states = decoder_layer(
                hidden_states,
                attention_mask=causal_mask_mapping[self.config.layer_types[i]],
                position_ids=position_ids,
                past_key_values=past_key_values,
                use_cache=use_cache,
                position_embeddings=position_embeddings,
                **kwargs,
            )

        hidden_states = self.norm(hidden_states)
        return MoeModelOutputWithPast(
            last_hidden_state=hidden_states,
            past_key_values=past_key_values if use_cache else None,
        )


# Qwen2MoeForCausalLM：因果语言模型：MoE 自回归文本生成与 lm_head
class Qwen2MoeForCausalLM(MixtralForCausalLM, GenerationMixin):
    _tied_weights_keys = {"lm_head.weight": "model.embed_tokens.weight"}
    _tp_plan = {"lm_head": "colwise_gather_output"}
    _pp_plan = {"lm_head": (["hidden_states"], ["logits"])}

    _fsdp_plan = {"lm_head": "keep_full_weight"}

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config):
        super().__init__(config)
        self.num_experts = config.num_experts
        self.model = Qwen2MoeModel(config)


# Qwen2MoeForSequenceClassification：序列分类头：池化隐状态 + 线性分类器
class Qwen2MoeForSequenceClassification(GenericForSequenceClassification, Qwen2MoePreTrainedModel): ...


# Qwen2MoeForTokenClassification：Token 分类头：逐 token 线性分类（NER 等）
class Qwen2MoeForTokenClassification(GenericForTokenClassification, Qwen2MoePreTrainedModel): ...


# Qwen2MoeForQuestionAnswering：问答头：span 起始/结束位置预测
class Qwen2MoeForQuestionAnswering(GenericForQuestionAnswering, Qwen2MoePreTrainedModel): ...


__all__ = [
    "Qwen2MoeForCausalLM",
    "Qwen2MoeForQuestionAnswering",
    "Qwen2MoeModel",
    "Qwen2MoePreTrainedModel",
    "Qwen2MoeForSequenceClassification",
    "Qwen2MoeForTokenClassification",
]
