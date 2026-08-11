# Copyright 2026 Upstage and HuggingFace Inc. team.
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
"""PyTorch SolarOpen model."""

from huggingface_hub.dataclasses import strict
from torch import nn

from ...utils import auto_docstring, logging
from ..glm4_moe.configuration_glm4_moe import Glm4MoeConfig
from ..glm4_moe.modeling_glm4_moe import (
# SolarOpen modular 源：继承 Glm4Moe/Llama 组件并覆盖 Solar 特有配置默认值

    Glm4MoeForCausalLM,
    Glm4MoeModel,
    Glm4MoeMoE,
    Glm4MoePreTrainedModel,
    Glm4MoeRMSNorm,
)
from ..llama.modeling_llama import LlamaAttention, LlamaDecoderLayer


logger = logging.get_logger(__name__)


@auto_docstring(checkpoint="upstage/Solar-Open-100B")
@strict
# SolarOpenConfig：SolarOpen 配置：196K 词表、128 路由专家、8 top-k 与 RoPE 超参数
class SolarOpenConfig(Glm4MoeConfig):
    r"""
    n_group (`int`, *optional*, defaults to 1):
        Number of groups for routed experts.
    """

    model_type = "solar_open"
    default_theta = 1_000_000.0

    # Default tensor parallel plan for base model `SolarOpenModel`
    base_model_tp_plan = {
        "layers.*.self_attn.q_proj": "colwise",
        "layers.*.self_attn.k_proj": "colwise",
        "layers.*.self_attn.v_proj": "colwise",
        "layers.*.self_attn.o_proj": "rowwise",
        "layers.*.mlp.experts.gate_up_proj": "packed_colwise",
        "layers.*.mlp.experts.down_proj": "rowwise",
        "layers.*.mlp.experts": "moe_tp_experts",
    }
    attribute_map = {
        "num_local_experts": "n_routed_experts",
    }

    vocab_size: int = 196608
    moe_intermediate_size: int = 1280
    num_hidden_layers: int = 48
    num_attention_heads: int = 64
    head_dim: int = 128
    num_experts_per_tok: int = 8
    intermediate_size = AttributeError()
    first_k_dense_replace = AttributeError()
    use_qk_norm = AttributeError()
    num_mtp_layers = AttributeError()

    # __post_init__：后初始化：解析子配置、RoPE 参数与派生字段
    def __post_init__(self, **kwargs):
        kwargs.setdefault("partial_rotary_factor", 1.0)
        super().__post_init__(**kwargs)


# SolarOpenDecoderLayer：SolarOpen 解码层：Pre-LN 自注意力 + MoE 前馈残差堆叠
class SolarOpenDecoderLayer(LlamaDecoderLayer):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: SolarOpenConfig, layer_idx: int):
        super().__init__(config, layer_idx)
        self.mlp = SolarOpenMoE(config)


# SolarOpenMoE：SolarOpen MoE 模块：Top-K 路由专家 + 共享专家残差融合
class SolarOpenMoE(Glm4MoeMoE):
    pass


# SolarOpenAttention：SolarOpen 注意力：GQA 分组查询 + RoPE 因果自注意力
class SolarOpenAttention(LlamaAttention):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: SolarOpenConfig, layer_idx: int):
        super().__init__(config, layer_idx)
        self.o_proj = nn.Linear(config.num_attention_heads * self.head_dim, config.hidden_size, bias=False)


# SolarOpenRMSNorm：SolarOpen RMSNorm：均方根归一化层（等价 T5LayerNorm）
class SolarOpenRMSNorm(Glm4MoeRMSNorm):
    pass


# SolarOpenPreTrainedModel：SolarOpen 预训练基类：MoE/路由权重初始化与输出录制
class SolarOpenPreTrainedModel(Glm4MoePreTrainedModel):
    _keys_to_ignore_on_load_unexpected = None


# SolarOpenModel：SolarOpen 基模型：词嵌入 + 多层 MoE 解码器 + RMSNorm
class SolarOpenModel(Glm4MoeModel):
    pass


# SolarOpenForCausalLM：SolarOpen 因果 LM：基模型 + lm_head，支持生成与 logits_to_keep
class SolarOpenForCausalLM(Glm4MoeForCausalLM):
    pass


__all__ = [
    "SolarOpenConfig",
    "SolarOpenPreTrainedModel",
    "SolarOpenModel",
    "SolarOpenForCausalLM",
]
