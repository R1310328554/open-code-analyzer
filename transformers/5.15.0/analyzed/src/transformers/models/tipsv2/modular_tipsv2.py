# Copyright 2026 Google LLC and the HuggingFace Inc. team. All rights reserved.
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

import math
from collections.abc import Callable

import torch
from huggingface_hub.dataclasses import strict
from tokenizers import Tokenizer, decoders, normalizers, pre_tokenizers
from tokenizers.models import BPE
from torch import nn

from ... import initialization as init
from ...backbone_utils import filter_output_hidden_states
from ...configuration_utils import PreTrainedConfig
from ...image_processing_backends import TorchvisionBackend
from ...image_utils import PILImageResampling
from ...masking_utils import create_bidirectional_mask
from ...modeling_outputs import BackboneOutput, BaseModelOutput, BaseModelOutputWithPooling
from ...modeling_utils import ALL_ATTENTION_FUNCTIONS, PreTrainedModel
from ...processing_utils import ProcessingKwargs, ProcessorMixin, Unpack
from ...tokenization_utils_tokenizers import TokenizersBackend
from ...utils import TransformersKwargs, auto_docstring, logging, torch_int
from ...utils.generic import can_return_tuple, merge_with_config_defaults
from ...utils.output_capturing import capture_outputs
from ..clip.modeling_clip import (
    CLIPAttention,
    CLIPEncoder,
    CLIPEncoderLayer,
    CLIPOutput,
    CLIPTextEmbeddings,
    _get_vector_norm,
    image_text_contrastive_loss,
)
from ..dinov2_with_registers.configuration_dinov2_with_registers import Dinov2WithRegistersConfig
from ..dinov2_with_registers.modeling_dinov2_with_registers import (
    Dinov2WithRegistersBackbone,
    Dinov2WithRegistersEmbeddings,
    Dinov2WithRegistersEncoder,
    Dinov2WithRegistersModel,
    Dinov2WithRegistersPreTrainedModel,
)
from ..siglip2.configuration_siglip2 import Siglip2TextConfig
from ..speech_to_text.modeling_speech_to_text import Speech2TextSinusoidalPositionalEmbedding


# Tipsv2 模块化实现：复用 DINOv2/CLIP/SigLIP 组件，供 modeling 代码生成

logger = logging.get_logger(__name__)


VOCAB_FILES_NAMES = {"vocab_file": "tokenizer.model"}


# Tipsv2Tokenizer：Tipsv2 分词器：SentencePiece 风格 BPE + Metaspace 预分词
class Tipsv2Tokenizer(TokenizersBackend):
    """Tipsv2 tokenizer backed by HuggingFace's *tokenizers* library, based on a BPE (SentencePiece) model."""

    vocab_files_names = VOCAB_FILES_NAMES
    model_input_names = ["input_ids", "attention_mask"]
    model = BPE
    padding_side = "right"

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(
        self,
        vocab: dict[str, int] | None = None,
        merges: list[tuple[str, str]] | None = None,
        unk_token: str | None = "<unk>",
        pad_token: str | None = "<pad>",
        bos_token: str | None = None,
        eos_token: str | None = None,
        model_max_length: int = 64,
        do_lower_case: bool = True,
        token_type_ids_pattern: str = "all_zeros",
        _spm_precompiled_charsmap=None,
        **kwargs,
    ) -> None:
        if vocab is None:
            vocab = {
                str(pad_token): 0,
                str(unk_token): 1,
            }
        self._vocab = vocab
        self._merges = merges or []

        self._tokenizer = Tokenizer(
            BPE(
                vocab=self._vocab,
                merges=self._merges,
                fuse_unk=True,
                byte_fallback=True,
                dropout=None,
            )
        )

        list_normalizers = []
        if do_lower_case:
            list_normalizers.append(normalizers.Lowercase())
        if _spm_precompiled_charsmap:
            list_normalizers.append(normalizers.Precompiled(_spm_precompiled_charsmap))
        self._tokenizer.normalizer = normalizers.Sequence(list_normalizers)

        self._tokenizer.pre_tokenizer = pre_tokenizers.Sequence(
            [
                pre_tokenizers.WhitespaceSplit(),
                pre_tokenizers.Metaspace(replacement="▁", prepend_scheme="always"),
            ]
        )
        self._tokenizer.decoder = decoders.Sequence(
            [
                decoders.Metaspace(replacement="▁", prepend_scheme="always"),
                decoders.ByteFallback(),
                decoders.Fuse(),
            ]
        )

        super().__init__(
            unk_token=unk_token,
            pad_token=pad_token,
            bos_token=bos_token,
            eos_token=eos_token,
            model_max_length=model_max_length,
            do_lower_case=do_lower_case,
            token_type_ids_pattern=token_type_ids_pattern,
            **kwargs,
        )


@auto_docstring
# Tipsv2ImageProcessor：Tipsv2 图像处理器：448 固定尺寸 Torchvision 后端预处理
class Tipsv2ImageProcessor(TorchvisionBackend):
    resample = PILImageResampling.BILINEAR
    size = {"height": 448, "width": 448}
    do_resize = True
    do_rescale = True
    do_normalize = False
    do_convert_rgb = True


# Tipsv2ProcessorKwargs：Tipsv2 处理器参数：文本默认 max_length=64 截断 padding
class Tipsv2ProcessorKwargs(ProcessingKwargs, total=False):
    _defaults = {
        "text_kwargs": {
            "padding": "max_length",
            "truncation": True,
            "max_length": 64,
        },
    }


@auto_docstring
# Tipsv2Processor：Tipsv2 多模态处理器：组合 ImageProcessor 与 Tokenizer 联合调用
class Tipsv2Processor(ProcessorMixin):
    valid_processor_kwargs = Tipsv2ProcessorKwargs

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, image_processor=None, tokenizer=None):
        super().__init__(image_processor, tokenizer)


@auto_docstring(checkpoint="google/tipsv2-b14")
@strict
# Tipsv2VisionConfig：Tipsv2 视觉配置：ViT 层数、register token、LayerScale 与 SwiGLU FFN 开关
class Tipsv2VisionConfig(Dinov2WithRegistersConfig):
    r"""
    layerscale_value (`float`, *optional*, defaults to `1.0`):
        Initial value to use for layer scale.
    use_swiglu_ffn (`bool`, *optional*, defaults to `False`):
        Whether to use the SwiGLU feedforward neural network. Otherwise a standard MLP
        with `hidden_act` as activation function is used.
    num_register_tokens (`int`, *optional*, defaults to `1`):
        Number of register tokens to use.
    apply_layernorm (`bool`, *optional*, defaults to `True`):
        Whether to apply layer normalization to the feature maps in case the model is used as backbone.
    reshape_hidden_states (`bool`, *optional*, defaults to `True`):
        Whether to reshape the feature maps to 4D tensors of shape `(batch_size, hidden_size, height, width)` in
        case the model is used as backbone. If `False`, the feature maps will be 3D tensors of shape `(batch_size,
        seq_len, hidden_size)`.

    Example:

    ```python
    >>> from transformers import Tipsv2VisionConfig, Tipsv2VisionModel

    >>> configuration = Tipsv2VisionConfig()
    >>> model = Tipsv2VisionModel(configuration)
    >>> configuration = model.config
    ```"""

    model_type = "tipsv2_vision_model"
    base_config_key = "vision_config"

    patch_size: int | list[int] | tuple[int, int] = 14
    image_size: int | list[int] | tuple[int, int] = 448
    mlp_ratio: int | float = 4  # float required for so400m14 checkpoint
    num_register_tokens: int = 1


@auto_docstring(checkpoint="google/tipsv2-b14")
@strict
# Tipsv2TextConfig：Tipsv2 文本配置：BPE 词表、正弦位置编码与 masked mean 池化 epsilon
class Tipsv2TextConfig(Siglip2TextConfig):
    r"""
    scale_sqrt_depth (`bool`, *optional*, defaults to `True`):
        Whether to scale token embeddings by `sqrt(hidden_size)` before adding sinusoidal position embeddings.
    pooling_epsilon (`float`, *optional*, defaults to `1e-8`):
        Epsilon added to the valid token count when computing masked mean pooling.

    Example:

    ```python
    >>> from transformers import Tipsv2TextConfig, Tipsv2TextModel

    >>> configuration = Tipsv2TextConfig()
    >>> model = Tipsv2TextModel(configuration)
    >>> configuration = model.config
    ```"""

    model_type = "tipsv2_text_model"
    base_config_key = "text_config"

    hidden_act: str = "relu"
    layer_norm_eps: float = 1e-5
    initializer_range: float = 0.02
    pad_token_id: int | None = 0
    bos_token_id: int | None = None
    eos_token_id: int | list[int] | None = None
    scale_sqrt_depth: bool = True
    pooling_epsilon: float = 1e-8

    projection_size = AttributeError()

    # __post_init__：后初始化：派生 stage_names/out_indices 等字段并校验架构一致性
    def __post_init__(self, **kwargs):
        raise AttributeError("Not used")

    # validate_architecture：架构校验：hidden_size 整除头数、双塔 hidden 一致、温度>0
    def validate_architecture(self):
        """Part of `@strict`-powered validation. Validates the architecture of the config."""
        PreTrainedConfig.validate_architecture(self)
        if self.hidden_size % self.num_attention_heads != 0:
            raise ValueError(
                f"The hidden size ({self.hidden_size}) is not a multiple of the number of attention "
                f"heads ({self.num_attention_heads})."
            )


@auto_docstring(checkpoint="google/tipsv2-b14")
@strict
# Tipsv2Config：Tipsv2 主配置：聚合 text/vision 子配置与对比学习温度初值
class Tipsv2Config(PreTrainedConfig):
    r"""
    text_config (`dict`, *optional*):
        Dictionary of configuration options used to initialize [`Tipsv2TextConfig`].
    vision_config (`dict`, *optional*):
        Dictionary of configuration options used to initialize [`Tipsv2VisionConfig`].
    temperature_init_value (`float`, *optional*, defaults to `0.005065968260169029`):
        Initial value for the learnable temperature parameter used to scale cosine-similarity logits in [`Tipsv2Model`].

    Example:

    ```python
    >>> from transformers import Tipsv2Config, Tipsv2Model

    >>> configuration = Tipsv2Config()
    >>> model = Tipsv2Model(configuration)
    >>> configuration = model.config

    >>> from transformers import Tipsv2TextConfig, Tipsv2VisionConfig

    >>> text_config = Tipsv2TextConfig()
    >>> vision_config = Tipsv2VisionConfig()
    >>> config = Tipsv2Config(text_config=text_config, vision_config=vision_config)
    ```"""

    model_type = "tipsv2"
    sub_configs = {"text_config": Tipsv2TextConfig, "vision_config": Tipsv2VisionConfig}

    text_config: dict | Tipsv2TextConfig | None = None
    vision_config: dict | Tipsv2VisionConfig | None = None
    temperature_init_value: float = 0.005065968260169029

    # __post_init__：后初始化：派生 stage_names/out_indices 等字段并校验架构一致性
    def __post_init__(self, **kwargs):
        if isinstance(self.text_config, dict):
            self.text_config = self.sub_configs["text_config"](**self.text_config)
        elif self.text_config is None:
            self.text_config = self.sub_configs["text_config"]()

        if isinstance(self.vision_config, dict):
            self.vision_config = self.sub_configs["vision_config"](**self.vision_config)
        elif self.vision_config is None:
            self.vision_config = self.sub_configs["vision_config"]()

        super().__post_init__(**kwargs)

    # validate_architecture：架构校验：hidden_size 整除头数、双塔 hidden 一致、温度>0
    def validate_architecture(self):
        super().validate_architecture()
        if (
            isinstance(self.text_config, Tipsv2TextConfig)
            and isinstance(self.vision_config, Tipsv2VisionConfig)
            and self.text_config.hidden_size != self.vision_config.hidden_size
        ):
            raise ValueError(
                f"`text_config.hidden_size` ({self.text_config.hidden_size}) and "
                f"`vision_config.hidden_size` ({self.vision_config.hidden_size}) must be equal."
            )
        if self.temperature_init_value <= 0:
            raise ValueError(f"`temperature_init_value` ({self.temperature_init_value}) must be strictly positive.")


# Tipsv2Output：Tipsv2 对比输出：图文嵌入、logits_per_image/text 与可选对比损失
class Tipsv2Output(CLIPOutput):
    pass


# Tipsv2VisionEmbeddings：Tipsv2 视觉嵌入：CLS/mask/register token + 可插值位置编码
class Tipsv2VisionEmbeddings(Dinov2WithRegistersEmbeddings):
    # interpolate_pos_encoding：位置编码插值：双线性缩放预训练 sin 位置以适配更高分辨率
    def interpolate_pos_encoding(self, embeddings: torch.Tensor, height: int, width: int) -> torch.Tensor:
        """
        This method allows to interpolate the pre-trained position encodings, to be able to use the model on higher
        resolution images. This implementation supports torch.jit tracing while maintaining backwards compatibility
        with the original implementation.

        Adapted from:
        - https://github.com/facebookresearch/dino/blob/main/vision_transformer.py
        - https://github.com/facebookresearch/dinov2/blob/main/dinov2/models/vision_transformer.py
        """
        num_patches = embeddings.shape[1] - 1
        num_positions = self.position_embeddings.shape[1] - 1

        # Skip interpolation for matching dimensions (unless tracing)
        if not torch.jit.is_tracing() and num_patches == num_positions and height == width:
            return self.position_embeddings

        # Handle class token and patch embeddings separately
        class_pos_embed = self.position_embeddings[:, 0]
        patch_pos_embed = self.position_embeddings[:, 1:]
        dim = embeddings.shape[-1]

        # Calculate new dimensions
        height = height // self.config.patch_size
        width = width // self.config.patch_size

        # Reshape for interpolation
        sqrt_num_positions = torch_int(num_positions**0.5)
        patch_pos_embed = patch_pos_embed.reshape(1, sqrt_num_positions, sqrt_num_positions, dim)
        patch_pos_embed = patch_pos_embed.permute(0, 3, 1, 2)

        # Store original dtype for restoration after interpolation
        target_dtype = patch_pos_embed.dtype

        # Interpolate at float32 precision
        patch_pos_embed = nn.functional.interpolate(
            patch_pos_embed.to(dtype=torch.float32),
            size=(torch_int(height), torch_int(width)),
            mode="bilinear",  # Different from Dinov2 which uses bicubic interpolation
            align_corners=False,
            antialias=True,
        ).to(dtype=target_dtype)

        # Validate output dimensions if not tracing
        if not torch.jit.is_tracing():
            if int(height) != patch_pos_embed.shape[-2] or int(width) != patch_pos_embed.shape[-1]:
                raise ValueError("Width or height does not match with the interpolated position embeddings")

        # Reshape back to original format
        patch_pos_embed = patch_pos_embed.permute(0, 2, 3, 1).view(1, -1, dim)

        # Combine class and patch embeddings
        return torch.cat((class_pos_embed.unsqueeze(0), patch_pos_embed), dim=1)


# Tipsv2VisionPreTrainedModel：Tipsv2 视觉预训练基类：ViT 权重初始化与梯度检查点支持
class Tipsv2VisionPreTrainedModel(Dinov2WithRegistersPreTrainedModel):
    config: Tipsv2VisionConfig
    base_model_prefix = "vision_model"
    _no_split_modules = ["Tipsv2VisionEmbeddings", "Tipsv2VisionLayer"]
    _keys_to_ignore_on_load_unexpected = {"text_encoder"}


# Tipsv2VisionEncoder：Tipsv2 视觉编码器：多层 VisionLayer 堆叠输出 patch 序列
class Tipsv2VisionEncoder(Dinov2WithRegistersEncoder):
    pass


# Tipsv2VisionModel：Tipsv2 视觉塔：patch 嵌入 + 编码器 + CLS 池化投影
class Tipsv2VisionModel(Dinov2WithRegistersModel):
    # forward：前向传播：组装特征并返回模型输出
    def forward(self, **super_kwargs) -> BaseModelOutputWithPooling:
        r"""
        bool_masked_pos (`torch.BoolTensor` of shape `(batch_size, sequence_length)`):
            Boolean masked positions. Indicates which patches are masked (1) and which aren't (0). Only relevant for
            pre-training.

        Example:

        ```python
        >>> import torch
        >>> from transformers import AutoConfig, AutoImageProcessor, AutoModel
        >>> from transformers.image_utils import load_image

        >>> model_id = "google/tipsv2-b14"
        >>> config = AutoConfig.from_pretrained(model_id)
        >>> model = AutoModel.from_pretrained(model_id, config=config.vision_config, device_map="auto")
        >>> image_processor = AutoImageProcessor.from_pretrained(model_id)

        >>> image = load_image("https://huggingface.co/datasets/huggingface/documentation-images/resolve/main/pipeline-cat-chonk.jpeg")
        >>> inputs = image_processor(images=image, return_tensors="pt").to(model.device)

        >>> with torch.no_grad():
        ...     outputs = model(**inputs)

        >>> # Tipsv2 repurposes the register token from DINOv2-with-registers as a secondary class token
        >>> sequence = outputs.last_hidden_state  # (batch_size, 1 + num_register_tokens + num_patches, hidden_size)
        >>> cls_token_1 = sequence[:, 0]  # supervised by web alt-text captions
        >>> cls_token_2 = sequence[:, 1 : 1 + model.config.num_register_tokens]  # supervised by synthetic captions
        ```"""
        return super().forward(**super_kwargs)


# Tipsv2VisionBackbone：Tipsv2 视觉骨干：多 stage 特征图输出，支持 reshape 为 4D 张量
class Tipsv2VisionBackbone(Dinov2WithRegistersBackbone):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: "Tipsv2Config | Tipsv2VisionConfig"):
        # reassign config to vision_config for compatibility with AutoBackbone
        if isinstance(config, Tipsv2Config):
            config = config.vision_config
            super().__init__(config)
        else:
            super().__init__(config)
        self.num_features = [config.hidden_size for _ in range(config.num_hidden_layers + 1)]
        self.embeddings = Tipsv2VisionEmbeddings(config)
        self.encoder = Tipsv2VisionEncoder(config)

        self.layernorm = nn.LayerNorm(config.hidden_size, eps=config.layer_norm_eps)

        self.num_register_tokens = config.num_register_tokens

        # Initialize weights and apply final processing
        self.post_init()

    @can_return_tuple
    @filter_output_hidden_states
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(self, **super_kwargs) -> BackboneOutput:
        r"""
        Examples:

        ```python
        >>> import torch
        >>> from transformers import AutoBackbone, Tipsv2VisionConfig

        >>> config = Tipsv2VisionConfig(out_features=["stage3", "stage6", "stage9", "stage12"])
        >>> model = AutoBackbone.from_config(config)

        >>> pixel_values = torch.randn(1, 3, 448, 448)

        >>> outputs = model(pixel_values)
        >>> feature_maps = outputs.feature_maps
        >>> list(feature_maps[-1].shape)
        [1, 768, 32, 32]
        ```"""
        return super().forward(**super_kwargs)


# Tipsv2SinusoidalPositionalEmbedding：Tipsv2 正弦位置编码：固定 sin/cos 位置嵌入表
class Tipsv2SinusoidalPositionalEmbedding(Speech2TextSinusoidalPositionalEmbedding):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2TextConfig):
        nn.Module.__init__(self)
        self.make_weights(
            num_embeddings=config.max_position_embeddings, embedding_dim=config.hidden_size, padding_idx=None
        )

    # forward：前向传播：组装特征并返回模型输出
    def forward(self, position_ids: torch.LongTensor) -> torch.Tensor:
        return self.weights[position_ids]

    def create_position_ids_from_input_ids(self):
        raise AttributeError("Not needed")


# Tipsv2TextEmbeddings：Tipsv2 文本嵌入：词嵌入 + sqrt(hidden) 缩放 + 正弦位置编码
class Tipsv2TextEmbeddings(CLIPTextEmbeddings):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2TextConfig):
        super().__init__(config)
        self.position_embedding = Tipsv2SinusoidalPositionalEmbedding(config)
        self.embed_scale = math.sqrt(config.hidden_size) if config.scale_sqrt_depth else 1.0

    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        input_ids: torch.LongTensor | None = None,
        position_ids: torch.LongTensor | None = None,
        inputs_embeds: torch.FloatTensor | None = None,
    ) -> torch.Tensor:
        seq_length = input_ids.shape[-1] if input_ids is not None else inputs_embeds.shape[-2]
        max_position_embedding = self.position_ids.shape[-1]

        if seq_length > max_position_embedding:
            raise ValueError(
                f"Sequence length must be less than max_position_embeddings (got `sequence length`: "
                f"{seq_length} and max_position_embeddings: {max_position_embedding}"
            )

        if position_ids is None:
            position_ids = self.position_ids[:, :seq_length]

        if inputs_embeds is None:
            inputs_embeds = self.token_embedding(input_ids)

        # Additional scale compared to CLIP embeddings
        inputs_embeds = inputs_embeds * self.embed_scale
        position_embeddings = self.position_embedding(position_ids).to(dtype=inputs_embeds.dtype)
        return inputs_embeds + position_embeddings


# Identical to CLIP's implementation but couldn't import it because the vision model
# already requires eager_attention_forward from DINOv2 which results in modular
# conflicts.
# text_eager_attention_forward：Tipsv2 文本 eager 注意力：双向 mask + 缩放点积 softmax
def text_eager_attention_forward(
    module: nn.Module,
    query: torch.Tensor,
    key: torch.Tensor,
    value: torch.Tensor,
    attention_mask: torch.Tensor | None,
    scaling: float,
    dropout: float = 0.0,
    **kwargs: Unpack[TransformersKwargs],
):
    attn_weights = torch.matmul(query, key.transpose(-1, -2)) * scaling
    if attention_mask is not None:
        attn_weights = attn_weights + attention_mask
    attn_weights = nn.functional.softmax(attn_weights, dim=-1, dtype=torch.float32).to(query.dtype)
    attn_weights = nn.functional.dropout(attn_weights, p=dropout, training=module.training)

    attn_output = torch.matmul(attn_weights, value)
    attn_output = attn_output.transpose(1, 2).contiguous()
    return attn_output, attn_weights


# Tipsv2TextAttention：Tipsv2 文本自注意力：双向多头注意力 + 可选 eager 实现
class Tipsv2TextAttention(CLIPAttention):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2TextConfig):
        super().__init__(config)

    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        hidden_states: torch.Tensor,
        attention_mask: torch.Tensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> tuple[torch.Tensor, torch.Tensor | None]:
        input_shape = hidden_states.shape[:-1]
        hidden_shape = (*input_shape, -1, self.head_dim)

        queries = self.q_proj(hidden_states).view(hidden_shape).transpose(1, 2)
        keys = self.k_proj(hidden_states).view(hidden_shape).transpose(1, 2)
        values = self.v_proj(hidden_states).view(hidden_shape).transpose(1, 2)

        attention_interface: Callable = ALL_ATTENTION_FUNCTIONS.get_interface(
            self.config._attn_implementation, text_eager_attention_forward
        )

        attn_output, attn_weights = attention_interface(
            self,
            queries,
            keys,
            values,
            attention_mask,
            scaling=self.scale,
            dropout=0.0 if not self.training else self.dropout,
            is_causal=self.is_causal,
            **kwargs,
        )

        attn_output = attn_output.reshape(*input_shape, -1).contiguous()
        attn_output = self.out_proj(attn_output)
        return attn_output, attn_weights


# Tipsv2TextEncoderLayer：Tipsv2 文本编码层：自注意力 + MLP 双残差 + LayerNorm
class Tipsv2TextEncoderLayer(CLIPEncoderLayer):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2TextConfig):
        super().__init__(config)
        self.self_attn = Tipsv2TextAttention(config)


# Tipsv2TextEncoder：Tipsv2 文本编码器：多层 TextEncoderLayer 堆叠
class Tipsv2TextEncoder(CLIPEncoder):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2TextConfig):
        super().__init__(config)
        self.layers = nn.ModuleList([Tipsv2TextEncoderLayer(config) for _ in range(config.num_hidden_layers)])


@auto_docstring
# Tipsv2TextPreTrainedModel：Tipsv2 文本预训练基类：Linear/LayerNorm 权重初始化
class Tipsv2TextPreTrainedModel(PreTrainedModel):
    config: Tipsv2TextConfig
    base_model_prefix = "text_model"
    main_input_name = "input_ids"
    input_modalities = ["text"]
    supports_gradient_checkpointing = True
    _keys_to_ignore_on_load_unexpected = {"vision_encoder"}
    _no_split_modules = ["Tipsv2TextEmbeddings", "Tipsv2TextEncoderLayer"]
    _supports_sdpa = True
    _supports_flash_attn = True
    _supports_flex_attn = True
    _supports_attention_backend = True
    _can_record_outputs = {
        "hidden_states": Tipsv2TextEncoderLayer,
        "attentions": Tipsv2TextAttention,
    }

    @torch.no_grad()
    # _init_weights：权重初始化：Linear/Conv 截断正态、LayerNorm 偏置置零
    def _init_weights(self, module):
        super()._init_weights(module)
        if isinstance(module, Tipsv2TextEmbeddings):
            init.copy_(module.position_ids, torch.arange(module.position_ids.shape[-1]).expand((1, -1)))
        elif isinstance(module, Tipsv2SinusoidalPositionalEmbedding):
            num_embeddings, embedding_dim = module.weights.shape
            embedding_weights = module.get_embedding(
                num_embeddings=num_embeddings,
                embedding_dim=embedding_dim,
                padding_idx=None,
            )
            init.copy_(module.weights, embedding_weights)


@auto_docstring(
    custom_intro="""
    The TIPSv2 text tower without any projection head on top.
    """
)
# Tipsv2TextModel：Tipsv2 文本塔：token 嵌入 + 编码器 + masked mean 池化
class Tipsv2TextModel(Tipsv2TextPreTrainedModel):
    _input_embed_layer = "token_embedding"

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2TextConfig):
        super().__init__(config)
        self.config = config
        self.embeddings = Tipsv2TextEmbeddings(config)
        self.encoder = Tipsv2TextEncoder(config)
        self.final_layer_norm = nn.LayerNorm(config.hidden_size, eps=config.layer_norm_eps)
        self.post_init()

    @merge_with_config_defaults
    @capture_outputs(tie_last_hidden_states=False)
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        input_ids: torch.LongTensor | None = None,
        attention_mask: torch.Tensor | None = None,
        position_ids: torch.LongTensor | None = None,
        inputs_embeds: torch.FloatTensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> BaseModelOutputWithPooling:
        r"""
        Example:

        ```python
        >>> import torch
        >>> from transformers import AutoConfig, Tipsv2TextModel, AutoProcessor

        >>> model_id = "google/tipsv2-b14"
        >>> config = AutoConfig.from_pretrained(model_id)
        >>> model = Tipsv2TextModel.from_pretrained(model_id, config=config.text_config, device_map="auto")
        >>> processor = AutoProcessor.from_pretrained(model_id)

        >>> candidate_labels = ["a photo of a cat", "a photo of a dog", "a photo of a car"]
        >>> inputs = processor(text=candidate_labels, return_tensors="pt").to(model.device)

        >>> with torch.no_grad():
        ...     outputs = model(**inputs)

        >>> text_embeds = outputs.pooler_output  # (batch_size, hidden_size)
        ```"""
        if (input_ids is None) ^ (inputs_embeds is not None):
            raise ValueError("You must specify exactly one of input_ids or inputs_embeds")

        hidden_states = self.embeddings(input_ids=input_ids, position_ids=position_ids, inputs_embeds=inputs_embeds)

        pooling_mask = attention_mask
        attention_mask = create_bidirectional_mask(
            config=self.config,
            inputs_embeds=hidden_states,
            attention_mask=attention_mask,
        )

        encoder_outputs: BaseModelOutput = self.encoder(
            inputs_embeds=hidden_states,
            attention_mask=attention_mask,
            **kwargs,
        )

        last_hidden_state = encoder_outputs.last_hidden_state
        last_hidden_state = self.final_layer_norm(last_hidden_state)
        if pooling_mask is not None:
            pooling_mask = pooling_mask.to(last_hidden_state.device)
            masked_hidden_state = last_hidden_state * pooling_mask[..., None]
            pooled_output = masked_hidden_state.sum(dim=1) / (
                pooling_mask.sum(dim=1, keepdim=True) + self.config.pooling_epsilon
            )
        else:
            pooled_output = last_hidden_state.mean(dim=1)

        return BaseModelOutputWithPooling(
            last_hidden_state=last_hidden_state,
            pooler_output=pooled_output,
        )


@auto_docstring
# Tipsv2PreTrainedModel：Tipsv2 联合预训练基类：双塔共享初始化与输出录制
class Tipsv2PreTrainedModel(PreTrainedModel):
    config: Tipsv2Config
    base_model_prefix = "model"
    input_modalities = ["image", "text"]
    supports_gradient_checkpointing = True
    _no_split_modules = [
        "Tipsv2TextEmbeddings",
        "Tipsv2TextEncoderLayer",
        "Tipsv2VisionEmbeddings",
        "Tipsv2VisionLayer",
    ]
    _supports_sdpa = True
    _supports_flash_attn = True
    _supports_flex_attn = True
    _supports_attention_backend = True

    # _init_weights：权重初始化：Linear/Conv 截断正态、LayerNorm 偏置置零
    def _init_weights(self, module):
        super()._init_weights(module)
        if isinstance(module, Tipsv2Model):
            init.constant_(module.temperature, module.config.temperature_init_value)


@auto_docstring
# Tipsv2Model：Tipsv2 对比模型：视觉/文本塔 + L2 归一化投影 + 温度缩放余弦相似度
class Tipsv2Model(Tipsv2PreTrainedModel):
    _keys_to_ignore_on_load_missing = ["temperature"]  # learnable but not in published checkpoints

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2Config):
        super().__init__(config)
        self.text_model = Tipsv2TextModel._from_config(config.text_config)
        self.vision_model = Tipsv2VisionModel._from_config(config.vision_config)
        self.temperature = nn.Parameter(torch.tensor(config.temperature_init_value))
        self.post_init()

    @can_return_tuple
    @auto_docstring
    def get_text_features(
        self,
        input_ids: torch.LongTensor | None = None,
        attention_mask: torch.Tensor | None = None,
        position_ids: torch.LongTensor | None = None,
        inputs_embeds: torch.FloatTensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> tuple | BaseModelOutputWithPooling:
        return self.text_model(
            input_ids=input_ids,
            attention_mask=attention_mask,
            position_ids=position_ids,
            inputs_embeds=inputs_embeds,
            **kwargs,
        )

    @can_return_tuple
    @auto_docstring
    def get_image_features(
        self,
        pixel_values: torch.FloatTensor,
        bool_masked_pos: torch.Tensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> tuple | BaseModelOutputWithPooling:
        r"""
        bool_masked_pos (`torch.BoolTensor` of shape `(batch_size, sequence_length)`, *optional*):
            Boolean masked positions. Indicates which patches are masked (1) and which aren't (0). Only relevant for
            pre-training.
        """
        return self.vision_model(
            pixel_values=pixel_values,
            bool_masked_pos=bool_masked_pos,
            **kwargs,
        )

    # get_input_embeddings：获取输入嵌入：TimeSformer 返回 patch 投影；TimmWrapper 无 token 嵌入
    def get_input_embeddings(self) -> nn.Module:
        return self.text_model.get_input_embeddings()

    # set_input_embeddings：设置 token 嵌入：TimmWrapper 不支持，直接 NotImplementedError
    def set_input_embeddings(self, value: nn.Module):
        self.text_model.set_input_embeddings(value)

    @can_return_tuple
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        input_ids: torch.LongTensor | None = None,
        pixel_values: torch.FloatTensor | None = None,
        attention_mask: torch.Tensor | None = None,
        position_ids: torch.LongTensor | None = None,
        inputs_embeds: torch.FloatTensor | None = None,
        bool_masked_pos: torch.Tensor | None = None,
        return_loss: bool | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> Tipsv2Output:
        r"""
        bool_masked_pos (`torch.BoolTensor` of shape `(batch_size, sequence_length)`, *optional*):
            Boolean masked positions. Indicates which patches are masked (1) and which aren't (0). Only relevant for
            pre-training.
        return_loss (`bool`, *optional*):
            Whether or not to return the contrastive loss when both image and text inputs are provided.

        Example:

        ```python
        >>> import torch
        >>> from transformers import AutoModel, AutoProcessor
        >>> from transformers.image_utils import load_image

        >>> model_id = "google/tipsv2-b14"
        >>> model = AutoModel.from_pretrained(model_id, device_map="auto")
        >>> processor = AutoProcessor.from_pretrained(model_id)

        >>> image = load_image("https://huggingface.co/datasets/huggingface/documentation-images/resolve/main/pipeline-cat-chonk.jpeg")
        >>> candidate_labels = ["a photo of a cat", "a photo of a dog", "a photo of a car"]

        >>> inputs = processor(text=candidate_labels, images=image, return_tensors="pt").to(model.device)

        >>> with torch.no_grad():
        ...     outputs = model(**inputs)

        >>> probs = outputs.logits_per_image.softmax(dim=1)
        >>> most_likely_idx = probs.argmax(dim=1).item()
        >>> most_likely_label = candidate_labels[most_likely_idx]
        >>> print(f"Most likely label: '{most_likely_label}' with probability: {probs[0][most_likely_idx].item():.3f}")
        Most likely label: 'a photo of a cat' with probability: 0.975
        ```
        """
        if pixel_values is None and input_ids is None and inputs_embeds is None:
            raise ValueError("You have to specify pixel_values, input_ids, or inputs_embeds")

        vision_outputs = None
        image_embeds = None
        if pixel_values is not None:
            vision_outputs = self.vision_model(
                pixel_values=pixel_values,
                bool_masked_pos=bool_masked_pos,
                return_dict=True,
                **kwargs,
            )
            image_embeds = vision_outputs.pooler_output
            image_embeds = image_embeds / _get_vector_norm(image_embeds)

        text_outputs = None
        text_embeds = None
        if input_ids is not None or inputs_embeds is not None:
            text_outputs = self.text_model(
                input_ids=input_ids,
                attention_mask=attention_mask,
                position_ids=position_ids,
                inputs_embeds=inputs_embeds,
                return_dict=True,
                **kwargs,
            )
            text_embeds = text_outputs.pooler_output
            text_embeds = text_embeds / _get_vector_norm(text_embeds)

        logits_per_text = None
        logits_per_image = None
        loss = None
        if image_embeds is not None and text_embeds is not None:
            logits_per_text = torch.matmul(text_embeds, image_embeds.t().to(text_embeds.device))
            logits_per_text = logits_per_text / self.temperature.to(logits_per_text.device)
            logits_per_image = logits_per_text.t()
            if return_loss:
                loss = image_text_contrastive_loss(logits_per_text)

        return Tipsv2Output(
            loss=loss,
            logits_per_image=logits_per_image,
            logits_per_text=logits_per_text,
            text_embeds=text_embeds,
            image_embeds=image_embeds,
            text_model_output=text_outputs,
            vision_model_output=vision_outputs,
        )


__all__ = [
    "Tipsv2ImageProcessor",
    "Tipsv2Processor",
    "Tipsv2Tokenizer",
    "Tipsv2Config",
    "Tipsv2Model",
    "Tipsv2PreTrainedModel",
    "Tipsv2TextConfig",
    "Tipsv2TextModel",
    "Tipsv2TextPreTrainedModel",
    "Tipsv2VisionBackbone",
    "Tipsv2VisionConfig",
    "Tipsv2VisionModel",
    "Tipsv2VisionPreTrainedModel",
]
