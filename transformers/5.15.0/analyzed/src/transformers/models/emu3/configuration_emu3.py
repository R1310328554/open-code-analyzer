# Copyright 2024 HuggingFace Inc. team. All rights reserved.
#
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


from huggingface_hub.dataclasses import strict

from ...configuration_utils import PreTrainedConfig
from ...modeling_rope_utils import RopeParameters
from ...utils import auto_docstring


# Emu3 系列配置：Emu3-Chat-hf checkpoint 默认超参
@auto_docstring(checkpoint="Emu3-community/Emu3-Chat-hf")
@strict
# Emu3VQVAEConfig：codebook_size/embed_dim/temporal_downsample 等 VQ 超参
class Emu3VQVAEConfig(PreTrainedConfig):
    r"""
    embed_dim (`int`, *optional*, defaults to 4):
        Dimension of the quantized vector in codebook.
    out_channels (`int`, *optional*, defaults to 3):
        Output channel of decoder.
    temporal_downsample_factor (`int`, *optional*, defaults to 4):
        Temporal downsample factor.
    base_channels (`int`, *optional*, defaults to 256):
        Basic channel number of the intermediate blocks.
    channel_multiplier (`list[int]`, *optional*, defaults to `[1, 2, 2, 4]`):
        Channel scaling factor of the intermediate blocks.
    num_res_blocks (`int`, *optional*, defaults to 2):
        Residual block number in each stage.
    attn_resolutions (`list[int]`, *optional*, defaults to `[3]`):
        Stage indices to apply attention.

    ```python
    >>> from transformers import Emu3VQVAE, Emu3VQVAEConfig

    >>> # Initializing a video VQ model of Emu3 configuration
    >>> configuration = Emu3VQVAEConfig()

    >>> # Initializing a model from the Emu3 VQ model style configuration
    >>> model = Emu3VQVAE(configuration)

    >>> # Accessing the model configuration
    >>> configuration = model.config
    ```
    """

    model_type = "emu3_vqgan"
    base_config_key = "vq_config"

    codebook_size: int = 32768
    embed_dim: int = 4
    latent_channels: int = 4
    double_latent: bool = False
    in_channels: int = 3
    out_channels: int = 3
    temporal_downsample_factor: int = 4
    base_channels: int = 256
    channel_multiplier: list[int] | tuple[int, ...] = (1, 2, 2, 4)
    num_res_blocks: int = 2
    attn_resolutions: list[int] | tuple[int, ...] = (3,)
    hidden_size: int = 1024
    num_attention_heads: int = 1
    attention_dropout: float | int = 0.0


@auto_docstring(checkpoint="Emu3-community/Emu3-Chat-hf")
@strict
# Emu3TextConfig：4096 维 32 层 GQA LLM，RoPE 与 184622 词表
class Emu3TextConfig(PreTrainedConfig):
    r"""
    Example:

    ```python
    >>> from transformers import Emu3Model, Emu3Config

    >>> # Initializing a Emu3-community/Emu3-Chat-hf style configuration
    >>> configuration = Emu3Config()

    >>> # Initializing a model from the Emu3-community/Emu3-Chat-hf style configuration
    >>> model = Emu3Model(configuration)

    >>> # Accessing the model configuration
    >>> configuration = model.config
    ```"""

    model_type = "emu3_text_model"
    base_config_key = "text_config"
    keys_to_ignore_at_inference = ["past_key_values"]
    default_theta = 1000000.0

    vocab_size: int = 184622
    hidden_size: int = 4096
    intermediate_size: int = 14336
    num_hidden_layers: int = 32
    num_attention_heads: int = 32
    num_key_value_heads: int | None = 8
    hidden_act: str = "silu"
    max_position_embeddings: int = 9216
    rms_norm_eps: float = 1e-5
    use_cache: bool = True
    pad_token_id: int = 151643
    bos_token_id: int = 151849
    eos_token_id: int | list[int] | None = 151850
    rope_parameters: RopeParameters | dict | None = None
    mlp_bias = False
    attention_bias = False
    attention_dropout: float | int = 0.1
    initializer_range: float = 0.02
    tie_word_embeddings: bool = False


@auto_docstring(checkpoint="Emu3-community/Emu3-Chat-hf")
@strict
# Emu3Config：sub_configs 聚合 vq_config + text_config + vocabulary_map
class Emu3Config(PreTrainedConfig):
    r"""
    vocabulary_map (`dict`, *optional*):
        A dictionary containing the vocabulary map from the tokenizer. Used to obtain tokens from the image inputs.
    """

    model_type = "emu3"
    keys_to_ignore_at_inference = ["past_key_values"]
    sub_configs = {"text_config": Emu3TextConfig, "vq_config": Emu3VQVAEConfig}

    vq_config: dict | Emu3VQVAEConfig | None = None
    text_config: dict | Emu3TextConfig | None = None
    vocabulary_map: dict[str, int] | None = None
    tie_word_embeddings: bool = False

# __post_init__：实例化子配置并从 vocabulary_map 解析 image_token_id
    def __post_init__(self, **kwargs):
        if self.vq_config is None:
            self.vq_config = Emu3VQVAEConfig()
        elif isinstance(self.vq_config, dict):
            self.vq_config = Emu3VQVAEConfig(**self.vq_config)

        if self.text_config is None:
            self.text_config = Emu3TextConfig()
        elif isinstance(self.text_config, dict):
            self.text_config = Emu3TextConfig(**self.text_config)

        self.image_token_id = self.vocabulary_map.get("<image>") if self.vocabulary_map is not None else None
        super().__post_init__(**kwargs)


__all__ = ["Emu3Config", "Emu3TextConfig", "Emu3VQVAEConfig"]
