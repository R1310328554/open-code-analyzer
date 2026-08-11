# Copyright 2026 HuggingFace Inc. team. All rights reserved.
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

import re
from dataclasses import dataclass

import torch
import torch.nn as nn
from huggingface_hub.dataclasses import strict

from ...configuration_utils import PreTrainedConfig
from ...image_utils import ImageInput
from ...modeling_outputs import BaseModelOutputWithPooling
from ...processing_utils import ProcessorMixin, Unpack
from ...tokenization_utils_base import PreTokenizedInput, TextInput
from ...utils import TransformersKwargs, auto_docstring
from ...utils.generic import can_return_tuple, merge_with_config_defaults
from ...utils.output_capturing import capture_outputs
from ..auto import CONFIG_MAPPING, AutoConfig
from ..beit.modeling_beit import BeitDropPath
from ..internvl.configuration_internvl import InternVLConfig, InternVLVisionConfig
from ..internvl.modeling_internvl import (
# 千帆 OCR modular 源：复用 InternVL 组件并定制 OCR 视觉与处理器

    InternVLCausalLMOutputWithPast,
    InternVLForConditionalGeneration,
    InternVLModel,
    InternVLModelOutputWithPast,
    InternVLMultiModalProjector,
    InternVLPreTrainedModel,
    InternVLVisionAttention,
    InternVLVisionEmbeddings,
    InternVLVisionLayer,
    InternVLVisionMLP,
    InternVLVisionModel,
    InternVLVisionPreTrainedModel,
)
from ..internvl.processing_internvl import InternVLProcessor, InternVLProcessorKwargs


@auto_docstring(checkpoint="baidu/Qianfan-OCR")
@strict
# QianfanOCRVisionConfig：千帆 OCR 视觉塔配置：ViT 层数、隐藏维与 patch 尺寸
class QianfanOCRVisionConfig(InternVLVisionConfig):
    r"""
    projection_dropout (`float`, *optional*, defaults to 0.0):
        Dropout probability for the projection layer.
    norm_type (`str`, *optional*, defaults to `"layer_norm"`):
        The type of normalization to use in the encoder. Can be `"layer_norm"` or `"rms_norm"`.
    use_mask_token (`bool`, *optional*, defaults to `False`):
        Whether to use a mask token for masked image modeling.
    use_mean_pooling (`bool`, *optional*, defaults to `True`):
        Whether to mean pool the final hidden states of the patches instead of using the final hidden state of the
        CLS token, before applying the classification head.
    drop_path_rate (`float`, *optional*, defaults to 0.1):
        Dropout rate for stochastic depth.

    Example:

    ```python
    >>> # Initializing a QianfanOCR vision style configuration
    >>> configuration = QianfanOCRVisionConfig()

    >>> # Initializing a model from the configuration
    >>> model = QianfanOCRVisionModel(configuration)

    >>> # Accessing the model configuration
    >>> configuration = model.config
    ```"""

    model_type = "qianfan_ocr_vision"
    base_config_key = "vision_config"

    attention_bias: bool = True
    drop_path_rate: float = 0.1


@auto_docstring(checkpoint="baidu/Qianfan-OCR")
@strict
# QianfanOCRConfig：千帆 OCR 联合配置：视觉塔 + 文本 LLM 与投影维度
class QianfanOCRConfig(InternVLConfig):
    r"""
    downsample_ratio (`float`, *optional*, defaults to 0.5):
        Factor by which to downsample the image.

    Example:

    ```python
    >>> # Initializing a QianfanOCR style configuration
    >>> configuration = QianfanOCRConfig()

    >>> # Initializing a model from the configuration
    >>> model = QianfanOCRForConditionalGeneration(configuration)

    >>> # Accessing the model configuration
    >>> configuration = model.config
    ```"""

    model_type = "qianfan_ocr"
    sub_configs = {"text_config": AutoConfig, "vision_config": QianfanOCRVisionConfig}

    tie_word_embeddings: bool = False

    # __post_init__：校验并补全配置默认值与骨干合并
    def __post_init__(self, **kwargs):
        if isinstance(self.vision_config, dict):
            self.vision_config = QianfanOCRVisionConfig(**self.vision_config)
        elif self.vision_config is None:
            self.vision_config = QianfanOCRVisionConfig()

        if isinstance(self.text_config, dict):
            self.text_config["model_type"] = self.text_config.get("model_type", "qwen3")
            self.text_config = CONFIG_MAPPING[self.text_config["model_type"]](**self.text_config)
        elif self.text_config is None:
            self.text_config = CONFIG_MAPPING["qwen3"]()

        PreTrainedConfig.__post_init__(self, **kwargs)


# QianfanOCRDropPath：视觉塔随机深度：Stochastic Depth 正则化
class QianfanOCRDropPath(BeitDropPath):
    pass


# QianfanOCRVisionAttention：视觉自注意力：多头缩放点积与 QK 归一化
class QianfanOCRVisionAttention(InternVLVisionAttention):
    pass


# QianfanOCRVisionMLP：视觉 MLP：SwiGLU 风格前馈网络
class QianfanOCRVisionMLP(InternVLVisionMLP):
    pass


# QianfanOCRVisionLayer：视觉 Transformer 层：注意力 + MLP 残差
class QianfanOCRVisionLayer(InternVLVisionLayer):
    """Vision transformer layer with stochastic depth (DropPath) support."""

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: QianfanOCRVisionConfig, drop_path_rate: float = 0.0) -> None:
        super().__init__(config)
        del self.seq_len_dim
        del self.chunk_size_feed_forward
        self.drop_path1 = nn.Identity() if drop_path_rate <= 0.0 else QianfanOCRDropPath(drop_path_rate)
        self.drop_path2 = nn.Identity() if drop_path_rate <= 0.0 else QianfanOCRDropPath(drop_path_rate)

    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        hidden_states: torch.Tensor,
        **kwargs: Unpack[TransformersKwargs],
    ) -> torch.Tensor:
        residual = hidden_states
        hidden_states = self.layernorm_before(hidden_states)
        # Self Attention
        hidden_states, _ = self.attention(hidden_states, **kwargs)
        hidden_states = self.lambda_1 * hidden_states
        hidden_states = self.drop_path1(hidden_states)
        hidden_states = hidden_states + residual

        residual = hidden_states
        hidden_states = self.layernorm_after(hidden_states)
        # Fully Connected
        hidden_states = self.mlp(hidden_states)
        hidden_states = self.dropout(hidden_states)
        hidden_states = self.lambda_2 * hidden_states
        hidden_states = self.drop_path2(hidden_states) + residual

        return hidden_states


# QianfanOCRVisionEmbeddings：视觉嵌入：patch + 位置编码 + 可选 CLS
class QianfanOCRVisionEmbeddings(InternVLVisionEmbeddings):
    pass


# QianfanOCRVisionModelOutputWithPooling：视觉输出：序列隐状态与池化向量
class QianfanOCRVisionModelOutputWithPooling(BaseModelOutputWithPooling):
    r"""
    pooler_output (`torch.FloatTensor` of shape `(batch_size, hidden_size)`):
        Average of the last layer hidden states of the patch tokens (excluding the *[CLS]* token) if
        *config.use_mean_pooling* is set to True. If set to False, then the final hidden state of the *[CLS]* token
        will be returned.
    """

    pass


@auto_docstring
# QianfanOCRVisionPreTrainedModel：视觉塔预训练基类：权重初始化策略
class QianfanOCRVisionPreTrainedModel(InternVLVisionPreTrainedModel):
    config_class = QianfanOCRVisionConfig
    base_model_prefix = "vision_model"
    _no_split_modules = ["QianfanOCRVisionLayer"]
    _can_record_outputs = {
        "hidden_states": QianfanOCRVisionLayer,
        "attentions": QianfanOCRVisionAttention,
    }


@auto_docstring
# QianfanOCRVisionModel：视觉编码器：多层 ViT 提取图像表征
class QianfanOCRVisionModel(InternVLVisionModel):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: QianfanOCRVisionConfig) -> None:
        super().__init__(config)
        del self.encoder
        dpr = [x.item() for x in torch.linspace(0, config.drop_path_rate, config.num_hidden_layers, device="cpu")]
        self.layers = nn.ModuleList(
            [QianfanOCRVisionLayer(config, drop_path_rate=dpr[i]) for i in range(config.num_hidden_layers)]
        )

    @merge_with_config_defaults
    @capture_outputs
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        pixel_values: torch.Tensor,
        bool_masked_pos: torch.BoolTensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> tuple | QianfanOCRVisionModelOutputWithPooling:
        r"""
        bool_masked_pos (`torch.BoolTensor` of shape `(batch_size, num_patches)`, *optional*):
            Boolean masked positions. Indicates which patches are masked (1) and which aren't (0).
        """
        hidden_states = self.embeddings(pixel_values, bool_masked_pos=bool_masked_pos)
        for layer_module in self.layers:
            hidden_states = layer_module(hidden_states, **kwargs)
        hidden_states = self.layernorm(hidden_states)

        return QianfanOCRVisionModelOutputWithPooling(
            last_hidden_state=hidden_states,
        )


# QianfanOCRMultiModalProjector：多模态投影：视觉特征映射到 LLM 嵌入空间
class QianfanOCRMultiModalProjector(InternVLMultiModalProjector):
    pass


# QianfanOCRPreTrainedModel：千帆 OCR 预训练基类：视觉-语言联合加载
class QianfanOCRPreTrainedModel(InternVLPreTrainedModel):
    config_class = QianfanOCRConfig
    input_modalities = ("image", "text")


@auto_docstring(
    custom_intro="""
    Base class for QianfanOCR outputs, with hidden states and attentions.
    """
)
@dataclass
# QianfanOCRModelOutputWithPast：多模态输出：logits、past KV 与视觉隐状态
class QianfanOCRModelOutputWithPast(InternVLModelOutputWithPast):
    r"""
    image_hidden_states (`torch.FloatTensor`, *optional*):
        A `torch.FloatTensor` of size `(batch_size, num_images, sequence_length, hidden_size)`.
        image_hidden_states of the model produced by the vision encoder and after projecting the last hidden state.
    """


# QianfanOCRModel：千帆 OCR 骨干：视觉塔 + 文本 LLM 融合前向
class QianfanOCRModel(InternVLModel):
    pass


# QianfanOCRCausalLMOutputWithPast：因果 LM 输出：交叉熵损失与生成缓存
class QianfanOCRCausalLMOutputWithPast(InternVLCausalLMOutputWithPast):
    pass


# QianfanOCRForConditionalGeneration：条件生成：OCR 图像理解与自然语言回复
class QianfanOCRForConditionalGeneration(InternVLForConditionalGeneration):
    @can_return_tuple
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(self, **super_kwargs) -> tuple | QianfanOCRCausalLMOutputWithPast:
        r"""
        Example:
        ```python
        >>> import torch
        >>> from transformers import AutoProcessor, AutoModelForImageTextToText
        >>> torch_device = "cuda"
        >>> processor = AutoProcessor.from_pretrained("baidu/Qianfan-OCR")
        >>> model = AutoModelForImageTextToText.from_pretrained(
        ...     "baidu/Qianfan-OCR", dtype=torch.bfloat16, device_map=torch_device
        ... )
        >>> messages = [
        ...     {
        ...         "role": "user",
        ...         "content": [
        ...             {"type": "image", "url": "https://example.com/image.jpg"},
        ...             {"type": "text", "text": "Describe this image."},
        ...         ],
        ...     },
        ... ]
        >>> inputs = processor.apply_chat_template(messages, add_generation_prompt=True, tokenize=True, return_dict=True, return_tensors="pt").to(torch_device)
        >>> generate_ids = model.generate(**inputs, max_new_tokens=200)
        >>> print(processor.decode(generate_ids[0, inputs["input_ids"].shape[1] :], skip_special_tokens=True))
        ```"""
        return super().forward(**super_kwargs)


# QianfanOCRProcessorKwargs：处理器关键字参数：图像尺寸与对话模板选项
class QianfanOCRProcessorKwargs(InternVLProcessorKwargs):
    pass


# QianfanOCRProcessor：OCR 处理器：图像预处理 + 分词器联合调用
class QianfanOCRProcessor(InternVLProcessor):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(
        self,
        image_processor=None,
        tokenizer=None,
        image_seq_length: int = 256,
        chat_template=None,
        image_placeholder_token: str = "<image>",
        **kwargs,
    ):
        r"""
        image_placeholder_token (`str`, *optional*, defaults to `"<image>"`):
            The token emitted by the chat template to mark image positions.
            It is replaced by the full ``<img><IMG_CONTEXT>...<IMG_CONTEXT></img>``
            sequence during processing.
        """
        ProcessorMixin.__init__(self, image_processor, tokenizer, chat_template=chat_template, **kwargs)
        self.image_seq_length = image_seq_length
        self.start_image_token = tokenizer.start_image_token
        self.end_image_token = tokenizer.end_image_token
        self.start_image_token_id = tokenizer.start_image_token_id
        self.end_image_token_id = tokenizer.end_image_token_id
        self.image_token = tokenizer.context_image_token
        self.image_token_id = tokenizer.context_image_token_id
        self.image_ids = [self.image_token_id, self.start_image_token_id, self.end_image_token_id]
        self.image_placeholder_token = image_placeholder_token
        self.video_token = None
        self.video_processor = None

    @auto_docstring
    # __call__：处理器调用：联合处理图像与文本输入
    def __call__(
        self,
        images: ImageInput | None = None,
        text: TextInput | PreTokenizedInput | list[TextInput] | list[PreTokenizedInput] | None = None,
        **kwargs: Unpack[QianfanOCRProcessorKwargs],
    ):
        # remove video from signature as well because the modality isn't supported
        # some tests pass all modalities from signature, and stumble upon `ValueError`
        return ProcessorMixin.__call__(images=images, text=text, **kwargs)

    def get_text_with_replacements(
        self,
        text: list[str],
        images_replacements: list[str] = [],
        videos_replacements: list[str] = [],
        audio_replacements: list[str] = [],
    ):
        """
        Replace multimodal placeholder tokens in a batch of text strings with their
        expanded representations, and return the modified texts alongside offset metadata.

        This method is the core text-side preprocessing step for multimodal inputs. It
        scans each text in the batch for special tokens (image, video, audio) and replaces
        them in-order with the pre-computed replacement strings produced by
        `self.replace_image_token` / `self.replace_video_token` / `self.replace_audio_token`.
        Replacements are consumed from each modality's list sequentially, so the i-th
        occurrence of e.g. ``self.image_token`` is replaced by ``images_replacements[i]``.

        To add a new multimodal processor with placeholder tokens, you need to define a correct
        `self.image_token` which is the same token that is embedded in input text and also used as
        placeholder and repeated many times. Then you need to override `self.replace_image_token`
        to return the correct replacement string for a given image at index `i`. Same goes for all
        other supported modalities.

        Args:
            text (`list[str]`):
                Batch of raw text strings, each potentially containing multimodal
                placeholder tokens. Note that it will be modified in-place and returned.
            images_replacements (`list[str]`, *optional*, defaults to `[]`):
                Expanded replacement strings for each image, in the order they appear
                across the batch. Produced by `self._process_images`.
            videos_replacements (`list[str]`, *optional*, defaults to `[]`):
                Expanded replacement strings for each video. Produced by
                `self._process_videos`.
            audio_replacements (`list[str]`, *optional*, defaults to `[]`):
                Expanded replacement strings for each audio input. Produced by
                `self._process_audio`.

        Returns:
            `tuple[list[str], list[dict[str, Any]]]`: A tuple of:
                - The modified `text` batch with all placeholder tokens expanded.
                - `batch_replacement_offsets`: one entry per batch item, each being a
                list of dicts with keys:
                    - `"type"` (`str`): modality name — `"image"`, `"video"`, or `"audio"`
                    - `"span"` (`tuple[int, int]`): original `(start, end)` char offsets of the placeholder token
                    - `"new_span"` (`tuple[int, int]`): `(start, end)` offsets of placeholder in the expanded string
                    - `"text"` (`str`): the original placeholder token string that was matched
                    - `"replacement"` (`str`): the string it was replaced with
        """
        # Override: model uses `image_placeholder_token` and `image_token` instead of a single token for both purposes
        token_groups = []
        if len(images_replacements) > 0:
            token_groups.append(f"(?P<image>{re.escape(self.image_placeholder_token)})")

        regex_special_mm_tokens = "|".join(token_groups) or r"(?!)"
        replacements_iters = {
            "image": iter(images_replacements),
        }
        batch_replacement_offsets = []
        for batch_idx in range(len(text)):
            last = 0
            offset = 0
            replacement_offsets = []
            expanded_sample = []
            for m in re.finditer(regex_special_mm_tokens, text[batch_idx]):
                start, end = m.span()
                expanded_sample.append(text[batch_idx][last:start])

                # adjust spans using running offset if one sample has several MM data associated
                start_with_offset = start + offset

                mm_type = m.lastgroup
                replacement_text = next(replacements_iters[mm_type])
                replacement_offsets.append(
                    {
                        "type": mm_type,
                        "span": (start, end),
                        "new_span": (start_with_offset, start_with_offset + len(replacement_text)),
                        "text": m.group(),
                        "replacement": replacement_text,
                    }
                )
                expanded_sample.append(replacement_text)
                # update the offsets and the last position
                offset += len(replacement_text) - (end - start)
                last = end

            expanded_sample.append(text[batch_idx][last:])
            text[batch_idx] = "".join(expanded_sample)
            batch_replacement_offsets.append(replacement_offsets)
        return text, batch_replacement_offsets

    def validate_inputs(
        self,
        images: ImageInput | None = None,
        text: TextInput | PreTokenizedInput | list[TextInput] | list[PreTokenizedInput] | None = None,
        videos=None,
        **kwargs: Unpack[QianfanOCRProcessorKwargs],
    ):
        super().validate_inputs(images=images, text=text, videos=videos, **kwargs)
        if text is None:
            raise ValueError("You have to specify text.")

        if videos is not None:
            raise ValueError("QianfanOCR does not support video input.")

    def replace_video_token(self, video_inputs: dict, video_idx: int, **kwargs) -> str:
        raise NotImplementedError("QianfanOCR does not support video input")

    @property
    def unused_input_names(self) -> list[str]:
        return ["num_patches"]


__all__ = [
    "QianfanOCRVisionConfig",
    "QianfanOCRConfig",
    "QianfanOCRVisionPreTrainedModel",
    "QianfanOCRVisionModel",
    "QianfanOCRPreTrainedModel",
    "QianfanOCRModel",
    "QianfanOCRForConditionalGeneration",
    "QianfanOCRProcessor",
]
