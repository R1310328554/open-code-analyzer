# Copyright 2026 The PaddlePaddle Team and The HuggingFace Inc. team. All rights reserved.
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


from collections.abc import Sequence

import torch
import torch.nn as nn
from huggingface_hub.dataclasses import strict

from ...activations import ACT2FN
from ...configuration_utils import PreTrainedConfig
from ...utils import

# PP-LCNetV4 modular 源：配置与骨干模块定义
 (
    auto_docstring,
)
from ..hgnet_v2.modeling_hgnet_v2 import HGNetV2ConvLayer, HGNetV2Embeddings
from ..pp_lcnet.configuration_pp_lcnet import PPLCNetConfig
from ..pp_lcnet.modeling_pp_lcnet import (
    PPLCNetBackbone,
    PPLCNetEncoder,
    PPLCNetPreTrainedModel,
    PPLCNetSqueezeExcitationModule,
)


@auto_docstring(checkpoint="PaddlePaddle/Not_yet_released")
# PPLCNetV4Config：PP-LCNetV4 配置：通道缩放、Stem 类型与块结构
@strict
class PPLCNetV4Config(PPLCNetConfig):
    r"""
    scale (`float`, *optional*, defaults to 1.0):
        The scaling factor for the model's channel dimensions, used to adjust the model size and computational cost
        without changing the overall architecture (e.g., 0.25, 0.5, 1.0, 1.5).
    block_configs (`list[list[tuple]]`, *optional*, defaults to `None`):
        Configuration for each block in each stage. Each tuple contains:
        (kernel_size, in_channels, out_channels, stride, use_squeeze_excitation).
        If `None`, uses the default PP-LCNet configuration.
    stem_channels (`list[int]`, *optional*, defaults to `[3, 48, 96]`):
        Channel dimensions for the stem layers:
        - First number (3) is input image channels
        - Second number (48) is intermediate stem channels
        - Third number (96) is output stem channels
    reduction (`int`, *optional*, defaults to 4):
        The reduction factor for feature channel dimensions in the squeeze-and-excitation (SE) blocks, used to
        reduce the number of model parameters and computational complexity while maintaining feature representability.
    stem_strides (`Sequence[int | list[int] | tuple[int, ...]]`, *optional*, defaults to `(2, 1, 1, 2, 1)`):
        Stride patterns for the stem layers.
    stem_type (`str`, *optional*, defaults to `large`):
        The type of stem layer to use. Can be one of:
        - `"large"`: Standard PP-LCNetV4 stem.
        - `"small"`: Variant with smaller channel dimensions.
    use_learnable_affine_block (`bool`, *optional*, defaults to `False`):
        Whether to use Learnable Affine Blocks (LAB) in the network.
        LAB adds learnable scale and bias parameters after certain operations.
    """

    model_type = "pp_lcnet_v4"

    num_channels: int = 3
    stem_channels: list[int] | tuple[int, ...] = (3, 48, 96)
    stem_strides: Sequence[int | list[int] | tuple[int, ...]] = (2, 1, 1, 2, 1)
    stem_type: str = "large"
    hidden_act: str = "relu"
    use_learnable_affine_block: bool = False

    divisor = AttributeError()
    stem_stride = AttributeError()
    hidden_dropout_prob = AttributeError()
    class_expand = AttributeError()

    # __post_init__：校验并规范化配置字段
    def __post_init__(self, **kwargs):
        # Default block configs for PP-LCNetV3
        # Each tuple: (kernel_size, in_channels, out_channels, stride, use_squeeze_excitation)
        self.block_configs = (
            [
                # Stage 1 (blocks2)
                [[3, 96, 96, 1, True]],
                # Stage 2 (blocks3)
                [[3, 96, 96, 1, False], [3, 96, 96, 1, False]],
                # Stage 3 (blocks4)
                [
                    [3, 96, 192, [2, 1], False],
                    [3, 192, 192, 1, True],
                    [3, 192, 192, 1, False],
                    [3, 192, 192, 1, True],
                    [3, 192, 192, 1, False],
                    [3, 192, 192, 1, True],
                    [3, 192, 192, 1, False],
                ],
                # Stage 4 (blocks5)
                [
                    [3, 192, 384, [2, 1], False],
                    [3, 384, 384, 1, True],
                    [3, 384, 384, 1, False],
                ],
            ]
            if self.block_configs is None
            else self.block_configs
        )

        self.depths = [len(blocks) for blocks in self.block_configs]
        self.stage_names = ["stem"] + [f"stage{idx}" for idx in range(1, len(self.block_configs) + 1)]
        self.set_output_features_output_indices(
            out_indices=kwargs.pop("out_indices", None), out_features=kwargs.pop("out_features", None)
        )
        self.stage_out_channels = [blocks[-1][2] for blocks in self.block_configs]
        PreTrainedConfig.__post_init__(**kwargs)

    # validate_architecture：校验模型架构配置合法性
    def validate_architecture(self):
        """Part of `@strict`-powered validation. Validates the architecture of the config."""
        if len(self.block_configs) != 4:
            raise ValueError(f"block_configs must have 5 stages, but got {len(self.block_configs)}")
        if self.stem_type not in ["large", "small"]:
            raise ValueError(f"stem_type must be either 'large' or 'small', but got {self.stem_type}")


# PPLCNetV4ConvLayer：卷积层别名：继承 HGNetV2ConvLayer
class PPLCNetV4ConvLayer(HGNetV2ConvLayer):
    pass


# PPLCNetV4SqueezeExcitationModule：SE 模块别名：继承 PPLCNet SE
class PPLCNetV4SqueezeExcitationModule(PPLCNetSqueezeExcitationModule):
    pass


# PPLCNetV4LargeStem：大型 Stem 别名：继承 HGNetV2Embeddings
class PPLCNetV4LargeStem(HGNetV2Embeddings):
    pass


# PPLCNetV4SmallStem：小型 Stem：两层 GELU 卷积下采样
class PPLCNetV4SmallStem(nn.Module):
    # __init__：初始化模块/处理器默认参数与依赖组件
    def __init__(self, config):
        super().__init__()
        self.conv1 = PPLCNetV4ConvLayer(
            in_channels=config.stem_channels[0],
            out_channels=config.stem_channels[1],
            kernel_size=3,
            stride=2,
            activation=None,
        )
        self.act_fn = ACT2FN["gelu"]
        self.conv2 = PPLCNetV4ConvLayer(
            in_channels=config.stem_channels[1],
            out_channels=config.stem_channels[2],
            kernel_size=3,
            stride=2,
            activation=None,
        )

    # forward：前向计算主逻辑
    def forward(self, hidden_states: torch.Tensor) -> torch.Tensor:
        hidden_states = self.conv1(hidden_states)
        hidden_states = self.act_fn(hidden_states)
        hidden_states = self.conv2(hidden_states)

        return hidden_states


# PPLCNetV4DepthwiseSeparableConvLayer：深度可分离卷积块实现
class PPLCNetV4DepthwiseSeparableConvLayer(nn.Module):
    # __init__：初始化模块/处理器默认参数与依赖组件
    def __init__(
        self,
        in_channels,
        out_channels,
        stride,
        kernel_size,
        use_squeeze_excitation,
        config,
    ):
        super().__init__()

        self.has_residual = in_channels == out_channels and stride == 1
        self.use_rep_dw = stride == 1 and in_channels == out_channels

        self.token_conv = (
            nn.Conv2d(
                in_channels=in_channels,
                out_channels=out_channels,
                kernel_size=kernel_size,
                stride=stride,
                padding=kernel_size // 2,
                groups=in_channels,
            )
            if self.use_rep_dw
            else PPLCNetV4ConvLayer(
                in_channels=in_channels,
                out_channels=in_channels,
                kernel_size=kernel_size,
                stride=stride,
                groups=in_channels,
                activation=None,
            )
        )
        self.token_squeeze_excitation = (
            PPLCNetV4SqueezeExcitationModule(in_channels, config.reduction)
            if use_squeeze_excitation
            else nn.Identity()
        )
        self.channel_conv1 = PPLCNetV4ConvLayer(
            in_channels=in_channels, out_channels=in_channels * 2, kernel_size=1, stride=1, activation=None
        )
        self.channel_act_fn = ACT2FN["gelu"]
        self.channel_conv2 = PPLCNetV4ConvLayer(
            in_channels=in_channels * 2, out_channels=out_channels, kernel_size=1, stride=1, activation=None
        )

    # forward：前向计算主逻辑
    def forward(self, hidden_states: torch.Tensor) -> torch.Tensor:
        hidden_states = self.token_conv(hidden_states)
        hidden_states = self.token_squeeze_excitation(hidden_states)
        residual = hidden_states

        hidden_states = self.channel_conv1(hidden_states)
        hidden_states = self.channel_act_fn(hidden_states)
        hidden_states = self.channel_conv2(hidden_states)

        hidden_states = residual + hidden_states if self.has_residual else hidden_states

        return hidden_states


# PPLCNetV4Block：阶段块：按配置堆叠深度可分离层
class PPLCNetV4Block(nn.Module):
    # __init__：初始化模块/处理器默认参数与依赖组件
    def __init__(self, config, stage_index):
        super().__init__()
        self.config = config

        blocks = config.block_configs[stage_index]

        self.blocks = nn.ModuleList()
        for kernel_size, in_channels, out_channels, stride, use_squeeze_excitation in blocks:
            self.blocks.append(
                PPLCNetV4DepthwiseSeparableConvLayer(
                    in_channels=in_channels,
                    out_channels=out_channels,
                    kernel_size=kernel_size,
                    stride=stride,
                    use_squeeze_excitation=use_squeeze_excitation,
                    config=config,
                )
            )

    # forward：前向计算主逻辑
    def forward(self, hidden_states: torch.Tensor) -> torch.Tensor:
        for block in self.blocks:
            hidden_states = block(hidden_states)
        return hidden_states


# PPLCNetV4PreTrainedModel：预训练基类别名
class PPLCNetV4PreTrainedModel(PPLCNetPreTrainedModel):
    pass


# PPLCNetV4Encoder：编码器：按 stem_type 选择 Stem
class PPLCNetV4Encoder(PPLCNetEncoder):
    # __init__：初始化模块/处理器默认参数与依赖组件
    def __init__(self, config: PPLCNetV4Config):
        super().__init__(config)
        self.config = config

        # stem
        self.convolution = PPLCNetV4LargeStem(config) if config.stem_type == "large" else PPLCNetV4SmallStem(config)


# PPLCNetV4Backbone：骨干网络别名
class PPLCNetV4Backbone(PPLCNetBackbone):
    pass


__all__ = [
    "PPLCNetV4Backbone",
    "PPLCNetV4Config",
    "PPLCNetV4PreTrainedModel",
]
