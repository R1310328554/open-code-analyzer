# Copyright 2025 the HuggingFace Team. All rights reserved.
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


import torch
from huggingface_hub.dataclasses import strict

from ...cache_utils import Cache
from ...utils import auto_docstring
from ..gemma2.configuration_gemma2 import Gemma2Config
from ..gemma2.modeling_gemma2 import Gemma2Attention, Gemma2DecoderLayer, Gemma2ForCausalLM, Gemma2MLP, Gemma2RMSNorm


# VaultGemma 模块化实现：复用 Gemma2 组件并裁剪 post-attention/FFN LayerNorm

@auto_docstring(checkpoint="google/vaultgemma-1b")
@strict
# VaultGemmaConfig：VaultGemma 主配置：词表/层数、注意力 logit softcapping 与 RoPE 滑动窗口
class VaultGemmaConfig(Gemma2Config):
    r"""
    query_pre_attn_scalar (`float`, *optional*, defaults to 256):
        scaling factor used on the attention scores
    final_logit_softcapping (`float`, *optional*, defaults to 30.0):
        scaling factor when applying tanh softcapping on the logits.
    attn_logit_softcapping (`float`, *optional*, defaults to 50.0):
        scaling factor when applying tanh softcapping on the attention scores.

    ```python
    >>> from transformers import VaultGemmaModel, VaultGemmaConfig
    >>> # Initializing a VaultGemma vaultgemma-7b style configuration
    >>> configuration = VaultGemmaConfig()
    >>> # Initializing a model from the vaultgemma-7b style configuration
    >>> model = VaultGemmaModel(configuration)
    >>> # Accessing the model configuration
    >>> configuration = model.config
    ```"""

    use_bidirectional_attention = AttributeError()


# VaultGemmaRMSNorm：VaultGemma RMSNorm：hidden 维 RMS 归一化，权重以 (1+w) 缩放
class VaultGemmaRMSNorm(Gemma2RMSNorm):
    pass


# VaultGemmaMLP：VaultGemma 门控 FFN：gate/up/down 线性层 + gelu_pytorch_tanh 激活
class VaultGemmaMLP(Gemma2MLP):
    pass


# VaultGemmaAttention：VaultGemma 注意力：GQA 多头因果注意力，支持 softcapped 注意力分数
class VaultGemmaAttention(Gemma2Attention):
    """Multi-headed attention from 'Attention Is All You Need' paper"""

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: VaultGemmaConfig, layer_idx: int):
        super().__init__()
        self.is_causal = True


# VaultGemmaDecoderLayer：VaultGemma 解码层：自注意力 + FFN 双残差，可选梯度检查点
class VaultGemmaDecoderLayer(Gemma2DecoderLayer):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, **super_kwargs):
        super().__init__(**super_kwargs)
        del self.post_attention_layernorm
        del self.post_feedforward_layernorm

    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        hidden_states: torch.Tensor,
        position_embeddings: tuple[torch.Tensor, torch.Tensor],
        attention_mask: torch.Tensor | None = None,
        position_ids: torch.LongTensor | None = None,
        past_key_values: Cache | None = None,
        **kwargs,
    ) -> tuple[torch.FloatTensor, tuple[torch.FloatTensor, torch.FloatTensor] | None]:
        residual = hidden_states
        hidden_states = self.input_layernorm(hidden_states)
        # Self Attention
        hidden_states, _ = self.self_attn(
            hidden_states=hidden_states,
            position_embeddings=position_embeddings,
            attention_mask=attention_mask,
            position_ids=position_ids,
            past_key_values=past_key_values,
            **kwargs,
        )
        hidden_states = residual + hidden_states

        residual = hidden_states
        hidden_states = self.pre_feedforward_layernorm(hidden_states)
        hidden_states = self.mlp(hidden_states)
        hidden_states = residual + hidden_states

        return hidden_states


# VaultGemmaForCausalLM：VaultGemma 因果 LM：Model + LM 头，支持 logit softcapping 生成
class VaultGemmaForCausalLM(Gemma2ForCausalLM):
    pass


__all__ = [
    "VaultGemmaConfig",
    "VaultGemmaForCausalLM",
    "VaultGemmaModel",  # noqa: F822
    "VaultGemmaPreTrainedModel",  # noqa: F822
]
