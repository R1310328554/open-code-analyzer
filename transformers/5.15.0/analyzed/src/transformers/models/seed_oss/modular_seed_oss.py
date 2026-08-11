# Copyright 2025 Bytedance-Seed Ltd and the HuggingFace Inc. team. All rights reserved.
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
"""PyTorch SeedOss model."""

from collections.abc import Callable

import torch
import torch.nn as nn

from ...activations import ACT2FN
from ...cache_utils import Cache
from ...modeling_outputs import CausalLMOutputWithPast
from ...modeling_utils import ALL_ATTENTION_FUNCTIONS
from ...processing_utils import Unpack
from ...utils import TransformersKwargs, logging
from ..llama.modeling_llama import (
# Seed-OSS modular 源：复用 Llama 组件并实现 Seed-OSS 专用逻辑

    LlamaDecoderLayer,
    LlamaForCausalLM,
    LlamaForQuestionAnswering,
    LlamaForSequenceClassification,
    LlamaForTokenClassification,
    LlamaModel,
    LlamaPreTrainedModel,
    LlamaRMSNorm,
    apply_rotary_pos_emb,
    eager_attention_forward,
)
from .configuration_seed_oss import SeedOssConfig


logger = logging.get_logger(__name__)

_CHECKPOINT_FOR_DOC = "ByteDance-Seed/Seed-OSS-36B-Instruct"


# SeedOssRMSNorm：Seed-OSS RMSNorm：根均方归一化稳定训练
class SeedOssRMSNorm(LlamaRMSNorm):
    pass


# SeedOssMLP：Seed-OSS MLP：门控 SwiGLU 风格前馈子层
class SeedOssMLP(nn.Module):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config):
        super().__init__()
        self.config = config
        self.hidden_size = config.hidden_size
        self.intermediate_size = config.intermediate_size
        self.gate_proj = nn.Linear(self.hidden_size, self.intermediate_size, bias=config.mlp_bias)
        self.up_proj = nn.Linear(self.hidden_size, self.intermediate_size, bias=config.mlp_bias)
        self.down_proj = nn.Linear(self.intermediate_size, self.hidden_size, bias=config.mlp_bias)
        self.act_fn = ACT2FN[config.hidden_act]
        self.residual_dropout = config.residual_dropout

    # forward：前向传播：组装特征并返回模型输出
    def forward(self, x):
        down_proj = self.down_proj(self.act_fn(self.gate_proj(x)) * self.up_proj(x))
        down_proj = nn.functional.dropout(down_proj, p=self.residual_dropout, training=self.training)
        return down_proj


# SeedOssAttention：Seed-OSS 注意力：RoPE 多头缩放点积自注意力
class SeedOssAttention(nn.Module):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: SeedOssConfig, layer_idx: int):
        super().__init__()
        self.config = config
        self.layer_idx = layer_idx
        self.head_dim = config.head_dim
        self.num_key_value_heads = config.num_key_value_heads
        self.num_attention_heads = config.num_attention_heads
        self.num_key_value_groups = self.num_attention_heads // self.num_key_value_heads
        self.scaling = self.head_dim**-0.5
        self.attention_dropout = config.attention_dropout
        self.is_causal = True

        self.q_proj = nn.Linear(
            config.hidden_size, self.num_attention_heads * self.head_dim, bias=config.attention_bias
        )
        self.k_proj = nn.Linear(
            config.hidden_size, config.num_key_value_heads * self.head_dim, bias=config.attention_bias
        )
        self.v_proj = nn.Linear(
            config.hidden_size, config.num_key_value_heads * self.head_dim, bias=config.attention_bias
        )
        self.o_proj = nn.Linear(
            self.num_attention_heads * self.head_dim, config.hidden_size, bias=config.attention_out_bias
        )

        self.residual_dropout = config.residual_dropout

    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        hidden_states: torch.Tensor,
        position_embeddings: tuple[torch.Tensor, torch.Tensor],
        attention_mask: torch.Tensor | None,
        past_key_values: Cache | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> tuple[torch.Tensor, torch.Tensor]:
        input_shape = hidden_states.shape[:-1]
        hidden_shape = (*input_shape, -1, self.head_dim)

        query_states = self.q_proj(hidden_states).view(hidden_shape).transpose(1, 2)
        key_states = self.k_proj(hidden_states).view(hidden_shape).transpose(1, 2)
        value_states = self.v_proj(hidden_states).view(hidden_shape).transpose(1, 2)

        cos, sin = position_embeddings
        query_states, key_states = apply_rotary_pos_emb(query_states, key_states, cos, sin)

        if past_key_values is not None:
            key_states, value_states = past_key_values.update(key_states, value_states, self.layer_idx)

        attention_interface: Callable = ALL_ATTENTION_FUNCTIONS.get_interface(
            self.config._attn_implementation, eager_attention_forward
        )

        attn_output, attn_weights = attention_interface(
            self,
            query_states,
            key_states,
            value_states,
            attention_mask,
            dropout=0.0 if not self.training else self.attention_dropout,
            scaling=self.scaling,
            **kwargs,
        )

        attn_output = attn_output.reshape(*input_shape, -1).contiguous()
        attn_output = self.o_proj(attn_output)
        attn_output = nn.functional.dropout(attn_output, p=self.residual_dropout, training=self.training)

        return attn_output, attn_weights


# SeedOssDecoderLayer：Seed-OSS 解码层：自注意力 + MLP 残差堆叠
class SeedOssDecoderLayer(LlamaDecoderLayer):
    pass


# SeedOssPreTrainedModel：Seed-OSS 预训练基类：权重初始化与配置绑定
class SeedOssPreTrainedModel(LlamaPreTrainedModel):
    pass


# SeedOssModel：Seed-OSS 骨干：多层解码器提取序列隐状态
class SeedOssModel(LlamaModel):
    pass


# SeedOssForCausalLM：Seed-OSS 因果 LM：自回归 next-token 预测与生成
class SeedOssForCausalLM(LlamaForCausalLM):
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        **super_kwargs: Unpack[TransformersKwargs],
    ) -> CausalLMOutputWithPast:
        r"""
        labels (`torch.LongTensor` of shape `(batch_size, sequence_length)`, *optional*):
            Labels for computing the masked language modeling loss. Indices should either be in `[0, ...,
            config.vocab_size]` or -100 (see `input_ids` docstring). Tokens with indices set to `-100` are ignored
            (masked), the loss is only computed for the tokens with labels in `[0, ..., config.vocab_size]`.

        Example:

        ```python
        >>> from transformers import AutoTokenizer, SeedOssForCausalLM

        >>> model = SeedOssForCausalLM.from_pretrained("ByteDance-Seed/Seed-OSS-36B-Instruct")
        >>> tokenizer = AutoTokenizer.from_pretrained("ByteDance-Seed/Seed-OSS-36B-Instruct")

        >>> prompt = "Hey, are you conscious? Can you talk to me?"
        >>> inputs = tokenizer(prompt, return_tensors="pt")

        >>> # Generate
        >>> generate_ids = model.generate(inputs.input_ids, max_length=30)
        >>> tokenizer.batch_decode(generate_ids, skip_special_tokens=True, clean_up_tokenization_spaces=False)[0]
        "Hey, are you conscious? Can you talk to me?\nI'm not conscious, but I can talk to you."
        ```"""
        return super().forward(**super_kwargs)


# SeedOssForSequenceClassification：Seed-OSS 序列分类：池化隐状态 + 分类头
class SeedOssForSequenceClassification(LlamaForSequenceClassification):
    pass


# SeedOssForTokenClassification：Seed-OSS 词元分类：逐 token 标签预测头
class SeedOssForTokenClassification(LlamaForTokenClassification):
    pass


# SeedOssForQuestionAnswering：Seed-OSS 问答：span 起止位置预测头
class SeedOssForQuestionAnswering(LlamaForQuestionAnswering):
    pass


__all__ = [
    "SeedOssForCausalLM",
    "SeedOssForQuestionAnswering",
    "SeedOssPreTrainedModel",
    "SeedOssModel",
    "SeedOssForSequenceClassification",
    "SeedOssForTokenClassification",
]
