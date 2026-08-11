# Copyright 2026 the HuggingFace Team. All rights reserved.
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

from dataclasses import dataclass

import torch
from huggingface_hub.dataclasses import strict
from torch import nn

from ...file_utils import ModelOutput
from ...processing_utils import Unpack
from ...utils import TransformersKwargs, auto_docstring
from ..eomt.configuration_eomt import EomtConfig
from ..eomt.modeling_eomt import (
    EomtEmbeddings,
    EomtForUniversalSegmentation,
    EomtLayer,
    EomtLayerNorm2d,
    EomtLayerScale,
    EomtMLP,
    EomtPatchEmbeddings,
    EomtPreTrainedModel,
    EomtScaleBlock,
    EomtScaleLayer,
    EomtSwiGLUFFN,
)


# Videomt 模块化实现：复用 EoMT 组件并扩展视频 patch 嵌入与分割输出

@auto_docstring(checkpoint="tue-mps/videomt-dinov2-small-ytvis2019")
@strict
# VideomtConfig：Videomt 主配置：LayerScale、SwiGLU FFN、分割损失权重与 Hungarian 匹配参数
class VideomtConfig(EomtConfig):
    model_type = "videomt"


# VideomtPatchEmbeddings：Videomt patch 嵌入：Conv2d 将每帧切为 patch 并展平为 token
class VideomtPatchEmbeddings(EomtPatchEmbeddings):
    # forward：前向传播：组装特征并返回模型输出
    def forward(self, pixel_values: torch.Tensor) -> torch.Tensor:
        num_channels = pixel_values.shape[1]
        if num_channels != self.num_channels:
            raise ValueError(
                "Make sure that the channel dimension of the pixel values match with the one set in the configuration."
                f" Expected {self.num_channels} but got {num_channels}."
            )

        pixel_values = pixel_values.to(dtype=self.projection.weight.dtype)
        embeddings = self.projection(pixel_values).flatten(2).transpose(1, 2)
        return embeddings


# VideomtEmbeddings：Videomt 嵌入层：patch 嵌入 + 可学习位置编码与 class token
class VideomtEmbeddings(EomtEmbeddings):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: VideomtConfig):
        super().__init__(config)
        self.patch_embeddings = VideomtPatchEmbeddings(config)
        self.mask_token = nn.Parameter(torch.zeros(1, 1, config.hidden_size))

    # forward：前向传播：组装特征并返回模型输出
    def forward(self, pixel_values: torch.Tensor, bool_masked_pos: torch.Tensor | None = None) -> torch.Tensor:
        if pixel_values.ndim == 5:
            batch_size, num_frames, num_channels, height, width = pixel_values.shape
            pixel_values = pixel_values.reshape(batch_size * num_frames, num_channels, height, width)

            if bool_masked_pos is not None:
                bool_masked_pos = bool_masked_pos.reshape(batch_size * num_frames, -1)
        elif bool_masked_pos is not None and bool_masked_pos.ndim > 2:
            bool_masked_pos = bool_masked_pos.reshape(bool_masked_pos.shape[0], -1)

        batch_size = pixel_values.shape[0]
        embeddings = self.patch_embeddings(pixel_values)

        if bool_masked_pos is not None:
            mask = bool_masked_pos.to(device=embeddings.device, dtype=torch.bool).unsqueeze(-1)
            embeddings = torch.where(mask, self.mask_token, embeddings)

        cls_tokens = self.cls_token.expand(batch_size, -1, -1)
        register_tokens = self.register_tokens.expand(batch_size, -1, -1)

        embeddings = embeddings + self.position_embeddings(self.position_ids)
        embeddings = torch.cat([cls_tokens, register_tokens, embeddings], dim=1)
        embeddings = self.dropout(embeddings)
        return embeddings


# VideomtMLP：Videomt 标准 FFN：两层 Linear + GELU 激活
class VideomtMLP(EomtMLP):
    pass


# VideomtGatedMLP：Videomt 门控 FFN：gate/up 双线性 + 激活乘积（SwiGLU 前置）
class VideomtGatedMLP(EomtSwiGLUFFN):
    pass


# VideomtLayer：Videomt Transformer 层：LayerScale 自注意力 + SwiGLU/MLP 残差堆叠
class VideomtLayer(EomtLayer):
    pass


# VideomtLayerScale：Videomt LayerScale：可学习标量缩放残差分支输出
class VideomtLayerScale(EomtLayerScale):
    pass


@auto_docstring(
    custom_intro="""
    Class for outputs of [`VideomtForUniversalSegmentationOutput`].

    This output can be directly passed to [`~VideomtVideoProcessor.post_process_semantic_segmentation`] or
    [`~VideomtVideoProcessor.post_process_instance_segmentation`] or
    [`~VideomtVideoProcessor.post_process_panoptic_segmentation`] to compute final segmentation maps. Please, see
    [`~VideomtVideoProcessor`] for details regarding usage.
    """
)
@dataclass
# VideomtForUniversalSegmentationOutput：Videomt 通用分割输出：class/mask logits、辅助损失与中间特征
class VideomtForUniversalSegmentationOutput(ModelOutput):
    r"""
    loss (`torch.Tensor`, *optional*):
        The computed loss, returned when labels are present.
    class_queries_logits (`torch.FloatTensor`):
        A tensor of shape `(batch_size, num_queries, num_labels + 1)` representing the proposed classes for each
        query. Note the `+ 1` is needed because we incorporate the null class.
    masks_queries_logits (`torch.FloatTensor`):
        A tensor of shape `(batch_size, num_queries, height, width)` representing the proposed masks for each
        query.
    last_hidden_state (`torch.FloatTensor` of shape `(batch_size, num_channels, height, width)`):
        Last hidden states (final feature map) of the last layer.
    hidden_states (`tuple(torch.FloatTensor)`, *optional*, returned when `output_hidden_states=True` is passed or when `config.output_hidden_states=True`):
        Tuple of `torch.FloatTensor` (one for the output of the embeddings + one for the output of each stage) of
        shape `(batch_size, sequence_length, hidden_size)`. Hidden-states all layers of the model.
    attentions (`tuple(tuple(torch.FloatTensor))`, *optional*, returned when `output_attentions=True` is passed or when `config.output_attentions=True`):
        Tuple of `tuple(torch.FloatTensor)` (one for each layer) of shape `(batch_size, num_heads, sequence_length,
        sequence_length)`. Self and Cross Attentions weights from transformer decoder.
    """

    loss: torch.FloatTensor | None = None
    class_queries_logits: torch.FloatTensor | None = None
    masks_queries_logits: torch.FloatTensor | None = None
    last_hidden_state: torch.FloatTensor | None = None
    hidden_states: tuple[torch.FloatTensor] | None = None
    attentions: tuple[torch.FloatTensor] | None = None


# VideomtPreTrainedModel：Videomt 预训练基类：Conv/Linear 初始化与分割头权重绑定
class VideomtPreTrainedModel(EomtPreTrainedModel):
    main_input_name = "pixel_values_videos"
    input_modalities = ("video",)

    @torch.no_grad()
    # _init_weights：权重初始化：Linear/Conv 截断正态、LayerNorm 偏置置零
    def _init_weights(self, module: nn.Module) -> None:
        super()._init_weights(module)
        if isinstance(module, VideomtEmbeddings):
            nn.init.zeros_(module.mask_token)


# VideomtLayerNorm2d：Videomt 2D LayerNorm：在通道维做 LayerNorm（ConvNeXt 风格）
class VideomtLayerNorm2d(EomtLayerNorm2d):
    pass


# VideomtScaleLayer：Videomt 尺度层：上采样 + Conv 调整特征图分辨率
class VideomtScaleLayer(EomtScaleLayer):
    pass


# VideomtScaleBlock：Videomt 尺度块：堆叠多个 ScaleLayer 构建多尺度特征
class VideomtScaleBlock(EomtScaleBlock):
    pass


# VideomtForUniversalSegmentation：Videomt 通用分割：视频 Transformer + 多尺度 mask 头，支持全景/实例
class VideomtForUniversalSegmentation(EomtForUniversalSegmentation):
    main_input_name = "pixel_values_videos"

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: VideomtConfig):
        super().__init__(config)
        self.query_updater = nn.Linear(config.hidden_size, config.hidden_size)

    def _disable_attention_mask(attn_mask, prob, num_query_tokens, encoder_start_tokens, device):
        raise AttributeError("Not needed for Videomt")

    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        pixel_values_videos: torch.Tensor | None = None,
        mask_labels: list[torch.Tensor] | None = None,
        class_labels: list[torch.Tensor] | None = None,
        patch_offsets: list[torch.Tensor] | None = None,  # Unused, kept for modular compatibility.
        **kwargs: Unpack[TransformersKwargs],
    ) -> VideomtForUniversalSegmentationOutput:
        r"""
        pixel_values_videos (`torch.Tensor`, *optional*):
            Video inputs of shape `(batch_size, num_frames, num_channels, height, width)`.
        mask_labels (`list[torch.Tensor]`, *optional*):
            Not supported for 5D video inputs.
        class_labels (`list[torch.LongTensor]`, *optional*):
            Not supported for 5D video inputs.
        patch_offsets (`list[torch.Tensor]`, *optional*):
            Unused for video inputs and only kept for modular compatibility.
        """
        if "pixel_values" in kwargs:
            raise ValueError("Use `pixel_values_videos` with `VideomtForUniversalSegmentation`.")

        if pixel_values_videos is None:
            raise ValueError("You have to specify pixel_values_videos")

        if pixel_values_videos.ndim != 5:
            raise ValueError(
                "VideomtForUniversalSegmentation only supports 5D video inputs of shape "
                "(batch_size, num_frames, channels, height, width)."
            )

        if mask_labels is not None or class_labels is not None:
            raise ValueError(
                "Training with 5D video inputs is not supported in `VideomtForUniversalSegmentation`. "
                "Flatten frames and use `EomtForUniversalSegmentation` instead."
            )

        batch_size, num_frames, num_channels, height, width = pixel_values_videos.shape
        flat_pixel_values = pixel_values_videos.reshape(batch_size * num_frames, num_channels, height, width)

        hidden_states = self.embeddings(flat_pixel_values)
        query_start_idx = self.num_hidden_layers - self.config.num_blocks

        for layer_module in self.layers[:query_start_idx]:
            hidden_states = layer_module(hidden_states)

        hidden_states = hidden_states.view(batch_size, num_frames, hidden_states.shape[1], hidden_states.shape[2])

        all_masks_queries_logits = []
        all_class_queries_logits = []
        all_last_hidden_states = []
        propagated_query = None

        for frame_idx in range(num_frames):
            frame_hidden_states = hidden_states[:, frame_idx]

            if propagated_query is None:
                query_tokens = self.query.weight[None, :, :].expand(batch_size, -1, -1).to(frame_hidden_states.device)
            else:
                query_tokens = self.query_updater(propagated_query).to(frame_hidden_states.device) + self.query.weight[
                    None, :, :
                ].to(frame_hidden_states.device)
            frame_hidden_states = torch.cat((query_tokens, frame_hidden_states), dim=1)

            for layer_module in self.layers[query_start_idx:]:
                frame_hidden_states = layer_module(frame_hidden_states)

            sequence_output = self.layernorm(frame_hidden_states)
            masks_queries_logits, class_queries_logits = self.predict(sequence_output)

            all_masks_queries_logits.append(masks_queries_logits)
            all_class_queries_logits.append(class_queries_logits)
            all_last_hidden_states.append(sequence_output)
            propagated_query = frame_hidden_states[:, : self.config.num_queries, :]

        return VideomtForUniversalSegmentationOutput(
            loss=None,  # Training not supported yet
            masks_queries_logits=torch.cat(all_masks_queries_logits, dim=0),
            class_queries_logits=torch.cat(all_class_queries_logits, dim=0),
            last_hidden_state=torch.cat(all_last_hidden_states, dim=0),
        )


__all__ = [
    "VideomtConfig",
    "VideomtPreTrainedModel",
    "VideomtForUniversalSegmentation",
]
