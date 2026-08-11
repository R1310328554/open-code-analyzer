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

from dataclasses import dataclass

import torch
from huggingface_hub.dataclasses import strict
from torch import nn
from typing_extensions import Unpack

from ... import initialization as init
from ...activations import ACT2FN
from ...backbone_utils import consolidate_backbone_kwargs_to_config, load_backbone
from ...configuration_utils import PreTrainedConfig
from ...image_processing_outputs import SemanticSegmentationPostProcessorOutput
from ...modeling_outputs import DepthEstimatorOutput, SemanticSegmenterOutput
from ...modeling_utils import PreTrainedModel
from ...utils import ModelOutput, TensorType, TransformersKwargs, auto_docstring, can_return_tuple
from ..auto import AutoConfig
from ..depth_anything.modeling_depth_anything import DepthAnythingNeck, DepthAnythingPreActResidualLayer
from ..dpt.modeling_dpt import DPTReassembleLayer
from ..sapiens2.modeling_sapiens2 import Sapiens2NormalEstimatorOutput, Sapiens2PointmapFinalLayerBlock
from ..tipsv2.image_processing_tipsv2 import Tipsv2ImageProcessor
from ..zoedepth.modeling_zoedepth import (
    ZoeDepthFeatureFusionLayer,
    ZoeDepthFeatureFusionStage,
    ZoeDepthReassembleStage,
)


# Tipsv2-DPT 模块化实现：复用 DPT/ZoeDepth/DepthAnything 组件，供 modeling 代码生成

@dataclass
# Tipsv2DptDensePredictorOutput：Tipsv2-DPT 密集预测输出：深度图、法线图与分割 logits 联合封装
class Tipsv2DptDensePredictorOutput(ModelOutput):
    r"""
    predicted_depth (`torch.FloatTensor` of shape `(batch_size, height, width)`):
        Predicted depth for each pixel.
    normals (`torch.FloatTensor` of shape `(batch_size, 3, height, width)`):
        Raw normal map predictions (unnormalized).
    segmentation_logits (`torch.FloatTensor` of shape `(batch_size, config.num_labels, height, width)`):
        Classification scores for each pixel.
        <Tip warning={true}>
        The logits returned do not necessarily have the same size as the `pixel_values` passed as inputs. This is
        to avoid doing two interpolations and lose some quality when a user needs to resize the logits to the
        original image size as post-processing. You should always check your logits shape and resize as needed.
        </Tip>
    """

    predicted_depth: torch.FloatTensor | None = None
    normals: torch.FloatTensor | None = None
    segmentation_logits: torch.FloatTensor | None = None
    hidden_states: tuple[torch.FloatTensor, ...] | None = None
    attentions: tuple[torch.FloatTensor, ...] | None = None


# Tipsv2DptNormalEstimatorOutput：Tipsv2-DPT 法线估计输出：未归一化法线图与可选损失
class Tipsv2DptNormalEstimatorOutput(Sapiens2NormalEstimatorOutput):
    pass


@auto_docstring
# Tipsv2DptImageProcessor：Tipsv2-DPT 图像处理器：448 固定尺寸 + 深度/法线/分割后处理
class Tipsv2DptImageProcessor(Tipsv2ImageProcessor):
    # post_process_depth_estimation：深度后处理：可选双线性插值至目标分辨率
    def post_process_depth_estimation(
        self,
        outputs,
        target_sizes: TensorType | list[tuple[int, int]] | None = None,
    ) -> list[dict[str, torch.Tensor]]:
        """
        Converts the output of [`Tipsv2DptForDepthEstimation`] or [`Tipsv2DptForDensePrediction`] into final depth predictions.

        Args:
            outputs ([`DepthEstimatorOutput`] or `Tipsv2DptDensePredictorOutput`):
                Raw outputs of the model.
            target_sizes ([`TensorType`] or `list[tuple[int, int]]`, *optional*):
                Tensor of shape `(batch_size, 2)` or list of tuples (`tuple[int, int]`) containing the target size
                (height, width) of each image in the batch. If left to None, predictions will not be resized.

        Returns:
            `list[dict[str, torch.Tensor]]`: A list of dictionaries of tensors representing the processed depth
            predictions.
        """
        predicted_depth = outputs.predicted_depth

        if target_sizes is not None and len(predicted_depth) != len(target_sizes):
            raise ValueError(
                "Make sure that you pass in as many target sizes as the batch dimension of the predicted depth"
            )

        target_sizes = [None] * len(predicted_depth) if target_sizes is None else target_sizes
        results = []
        for depth, target_size in zip(predicted_depth, target_sizes):
            if target_size is not None:
                depth = nn.functional.interpolate(
                    depth[None, None], size=target_size, mode="bilinear", align_corners=False
                ).squeeze()
            results.append({"predicted_depth": depth})
        return results

    # post_process_normal_estimation：法线后处理：L2 归一化 + 可选插值至目标尺寸
    def post_process_normal_estimation(
        self,
        outputs,
        target_sizes: TensorType | list[tuple[int, int]] | None = None,
    ) -> list[dict[str, torch.Tensor]]:
        """
        Converts the output of [`Tipsv2DptForNormalEstimation`] or [`Tipsv2DptForDensePrediction`] into L2-normalized surface normal maps.

        Args:
            outputs (`Tipsv2DptNormalEstimatorOutput` or `Tipsv2DptDensePredictorOutput`):
                Raw outputs of the model.
            target_sizes ([`TensorType`] or `list[tuple[int, int]]`, *optional*):
                Tensor of shape `(batch_size, 2)` or list of tuples (`tuple[int, int]`) containing the target size
                (height, width) of each image in the batch. If left to None, predictions will not be resized.

        Returns:
            `list[dict[str, torch.Tensor]]` of length `batch_size`. Each dict has a `"normals"` key
            mapping to a tensor of shape `(3, height, width)` with L2-normalized unit vectors in
            `[-1, 1]` per channel (XYZ surface normals).
        """
        normals = nn.functional.normalize(outputs.normals, p=2, dim=1)

        if target_sizes is not None and len(normals) != len(target_sizes):
            raise ValueError(
                "Make sure that you pass in as many target sizes as the batch dimension of the normals output"
            )

        target_sizes = [None] * len(normals) if target_sizes is None else target_sizes
        results = []
        for normal, target_size in zip(normals, target_sizes):
            if target_size is not None:
                normal = nn.functional.interpolate(
                    normal.unsqueeze(0), size=target_size, mode="bilinear", align_corners=False
                ).squeeze(0)
            results.append({"normals": normal})
        return results

    # post_process_semantic_segmentation：分割后处理：argmax 类别图 + 可选插值与分数输出
    def post_process_semantic_segmentation(
        self,
        outputs,
        target_sizes: TensorType | list[tuple[int, int]] | None = None,
        return_segmentation_scores: bool = False,
    ) -> "list[torch.Tensor] | list[SemanticSegmentationPostProcessorOutput]":
        """
        Converts the output of [`Tipsv2DptForSemanticSegmentation`] or [`Tipsv2DptForDensePrediction`] into semantic segmentation maps.

        Args:
            outputs ([`SemanticSegmenterOutput`] or `Tipsv2DptDensePredictorOutput`):
                Raw outputs of the model.
            target_sizes [([`TensorType`] or `list[tuple[int, int]]`, *optional*):
                Tensor of shape `(batch_size, 2)` or list of tuples (`tuple[int, int]`) containing the target size
                (height, width) of each image in the batch. If left to None, predictions will not be resized.
            return_segmentation_scores (`bool`, *optional*, defaults to `False`):
                Whether to return segmentation scores alongside the segmentation map. When `True`, each element of
                the returned list is a [`SemanticSegmentationPostProcessorOutput`] with fields `segmentation`
                (class IDs, shape `(height, width)`) and `segmentation_scores` (shape `(num_classes, height, width)`).

        Returns:
            `list[torch.Tensor]` or `list[SemanticSegmentationPostProcessorOutput]`: When
            `return_segmentation_scores=False` (default), a list of length `batch_size` where each item is a
            segmentation map of shape `(height, width)` with class IDs. When `return_segmentation_scores=True`,
            a list of [`SemanticSegmentationPostProcessorOutput`] with fields `segmentation` (class IDs, shape
            `(height, width)`) and `segmentation_scores` (shape `(num_classes, height, width)`). In both cases,
            `(height, width)` corresponds to the target size (if `target_sizes` is specified).
        """
        logits = getattr(outputs, "segmentation_logits", None)  # DptOutput
        if logits is None:
            logits = outputs.logits  # SemanticSegmentorOutput

        if target_sizes is not None and len(logits) != len(target_sizes):
            raise ValueError("Make sure that you pass in as many target sizes as the batch dimension of the logits")

        semantic_segmentation = []
        for idx in range(len(logits)):
            logit = logits[idx].unsqueeze(0)
            if target_sizes is not None:
                logit = nn.functional.interpolate(logit, size=target_sizes[idx], mode="bilinear", align_corners=False)
            semantic_segmentation.append(
                SemanticSegmentationPostProcessorOutput(
                    data={"segmentation": logit[0].argmax(dim=0), "segmentation_scores": logit[0]}
                )
            )

        if not return_segmentation_scores:
            semantic_segmentation = [item.segmentation for item in semantic_segmentation]

        return semantic_segmentation


@auto_docstring(checkpoint="google/tipsv2-b14-dpt")
@strict
# Tipsv2DptConfig：Tipsv2-DPT 主配置：颈部 hidden 尺寸、融合通道、深度分箱与骨干参数
class Tipsv2DptConfig(PreTrainedConfig):
    r"""
    neck_hidden_sizes (`list[int]`, *optional*, defaults to `[96, 192, 384, 768]`):
        The hidden sizes to project to for the feature maps of the backbone.
    fusion_hidden_size (`int`, *optional*, defaults to 256):
        The number of channels before fusion.
    reassemble_factors (`list[float]`, *optional*, defaults to `[4, 2, 1, 0.5]`):
        The up/downsampling factors of the reassemble layers.
    readout_activation (`str`, *optional*, defaults to `"gelu_pytorch_tanh"`):
        Activation applied after the readout projection layer.
    num_depth_bins (`int`, *optional*, defaults to 256):
        The number of depth bins used by the depth-estimation head.
    min_depth (`float`, *optional*, defaults to 0.001):
        The minimum depth value (meters) for depth bin calculation.
    max_depth (`float`, *optional*, defaults to 10.0):
        The maximum depth value (meters) for depth bin calculation.
    depth_decoder_activation (`str`, *optional*, defaults to `"relu"`):
        Activation applied after the depth decoder projection layer.
    semantic_loss_ignore_index (`int`, *optional*, defaults to 255):
        Label index to ignore in the cross-entropy loss for semantic segmentation.

    Example:

    ```python
    >>> from transformers import Tipsv2DptConfig, Tipsv2DptForDensePrediction

    >>> configuration = Tipsv2DptConfig()
    >>> model = Tipsv2DptForDensePrediction(configuration)
    >>> configuration = model.config
    ```
    """

    model_type = "tipsv2_dpt"
    sub_configs = {"backbone_config": AutoConfig}

    backbone_config: dict | PreTrainedConfig | None = None
    neck_hidden_sizes: list[int] | tuple[int, ...] | None = None
    fusion_hidden_size: int = 256
    reassemble_factors: list[int | float] | tuple[int | float, ...] | None = None
    readout_activation: str = "gelu_pytorch_tanh"
    num_depth_bins: int = 256
    min_depth: float = 0.001
    max_depth: float = 10.0
    depth_decoder_activation: str = "relu"
    semantic_loss_ignore_index: int = 255

    # __post_init__：后初始化：默认 neck 尺寸、重组因子与骨干配置合并
    def __post_init__(self, **kwargs):
        if self.neck_hidden_sizes is None:
            self.neck_hidden_sizes = [96, 192, 384, 768]
        if self.reassemble_factors is None:
            self.reassemble_factors = [4, 2, 1, 0.5]

        self.backbone_config, kwargs = consolidate_backbone_kwargs_to_config(
            backbone_config=self.backbone_config,
            default_config_type="tipsv2_vision_model",
            default_config_kwargs={
                "out_indices": [3, 6, 9, 12],
                "apply_layernorm": True,
                "reshape_hidden_states": False,
            },
            **kwargs,
        )
        super().__post_init__(**kwargs)


# Tipsv2DptReadoutProjectLayer：Tipsv2-DPT readout 投影：CLS token 与 patch token 拼接后线性投影
class Tipsv2DptReadoutProjectLayer(Sapiens2PointmapFinalLayerBlock):
    pass


# Tipsv2DptReassembleLayer：Tipsv2-DPT 重组层：1×1 投影 + 按 factor 上/下采样恢复空间分辨率
class Tipsv2DptReassembleLayer(DPTReassembleLayer):
    pass


# Tipsv2DptReassembleStage：Tipsv2-DPT 重组阶段：多尺度 backbone 隐状态重组为图像特征图
class Tipsv2DptReassembleStage(ZoeDepthReassembleStage):
    """
    This class reassembles the hidden states of the backbone into image-like feature representations at various
    resolutions.

    This happens in 3 stages:
    1. Map the N + class + register tokens to a set of N tokens.
    2. Project the channel dimension of the hidden states according to `config.neck_hidden_sizes`.
    3. Resizing the spatial dimensions (height, width).

    Args:
        config ([`Tipsv2DptConfig`]):
            Model configuration class defining the model architecture.
    """

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        nn.Module.__init__(self)
        self.num_register_tokens = config.backbone_config.num_register_tokens
        self.readout_projects = nn.ModuleList(
            [
                Tipsv2DptReadoutProjectLayer(
                    in_dim=2 * config.backbone_config.hidden_size,
                    out_dim=config.backbone_config.hidden_size,
                    activation=ACT2FN[config.readout_activation],
                )
                for _ in config.neck_hidden_sizes
            ]
        )
        self.layers = nn.ModuleList(
            [
                Tipsv2DptReassembleLayer(config, channels=channels, factor=factor)
                for channels, factor in zip(config.neck_hidden_sizes, config.reassemble_factors)
            ]
        )

    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        hidden_states: list[torch.Tensor],
        patch_height: int,
        patch_width: int,
    ) -> list[torch.Tensor]:
        out = []
        for stage_idx, hidden_state in enumerate(hidden_states):
            cls_token = hidden_state[:, 0]
            patch_tokens = hidden_state[:, 1 + self.num_register_tokens :]
            batch_size, num_patches, hidden_size = patch_tokens.shape

            readout = cls_token.unsqueeze(1).expand(-1, num_patches, -1)
            patch_tokens = self.readout_projects[stage_idx](torch.cat([patch_tokens, readout], dim=-1))

            patch_tokens = patch_tokens.reshape(batch_size, patch_height, patch_width, hidden_size)
            patch_tokens = patch_tokens.permute(0, 3, 1, 2).contiguous()

            patch_tokens = self.layers[stage_idx](patch_tokens)
            out.append(patch_tokens)

        return out


# Tipsv2DptPreActResidualLayer：Tipsv2-DPT 预激活残差卷积：PreAct + 双 3×3 Conv 残差单元
class Tipsv2DptPreActResidualLayer(DepthAnythingPreActResidualLayer):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        super().__init__(config)
        self.convolution1 = nn.Conv2d(
            config.fusion_hidden_size, config.fusion_hidden_size, kernel_size=3, stride=1, padding=1, bias=False
        )
        self.convolution2 = nn.Conv2d(
            config.fusion_hidden_size, config.fusion_hidden_size, kernel_size=3, stride=1, padding=1, bias=False
        )


# Tipsv2DptFeatureFusionLayer：Tipsv2-DPT 特征融合层：跨尺度残差融合 + 2× 双线性上采样
class Tipsv2DptFeatureFusionLayer(ZoeDepthFeatureFusionLayer):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig, align_corners: bool = True, has_residual: bool = True):
        super().__init__(config, align_corners=align_corners)
        self.residual_layer1 = Tipsv2DptPreActResidualLayer(config) if has_residual else nn.Identity()


# Tipsv2DptFeatureFusionStage：Tipsv2-DPT 特征融合阶段：自顶向下逐层融合多尺度特征
class Tipsv2DptFeatureFusionStage(ZoeDepthFeatureFusionStage):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        nn.Module.__init__(self)
        self.layers = nn.ModuleList(
            [
                Tipsv2DptFeatureFusionLayer(config, has_residual=(idx > 0))
                for idx in range(len(config.neck_hidden_sizes))
            ]
        )


# Tipsv2DptNeck：Tipsv2-DPT 颈部：重组 + 通道对齐 + 多尺度特征融合
class Tipsv2DptNeck(DepthAnythingNeck):
    pass


# Tipsv2DptDecoder：Tipsv2-DPT 解码头：Conv 投影 + Linear 逐像素分类/回归
class Tipsv2DptDecoder(nn.Module):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig, out_channels: int, activation: str | None = None):
        super().__init__()
        self.project = nn.Conv2d(config.fusion_hidden_size, config.fusion_hidden_size, kernel_size=3, padding=1)
        self.activation = ACT2FN[activation] if activation is not None else nn.Identity()
        self.head = nn.Linear(config.fusion_hidden_size, out_channels)

    # forward：前向传播：组装特征并返回模型输出
    def forward(self, hidden_state: torch.Tensor) -> torch.Tensor:
        hidden_state = self.project(hidden_state)
        hidden_state = self.activation(hidden_state)
        hidden_state = hidden_state.permute(0, 2, 3, 1)
        hidden_state = self.head(hidden_state)
        hidden_state = hidden_state.permute(0, 3, 1, 2).contiguous()
        return hidden_state


# Tipsv2DptFeaturesToDepth：Tipsv2-DPT 深度分箱回归：softmax 加权深度 bin 中心得到深度图
class Tipsv2DptFeaturesToDepth(nn.Module):
    """Converts raw logits from the depth head into a depth map using depth bins."""

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        super().__init__()
        self.min_depth = config.min_depth
        self.max_depth = config.max_depth
        self.activation = nn.ReLU()
        bin_centers = torch.linspace(config.min_depth, config.max_depth, config.num_depth_bins)
        self.bin_centers = nn.Buffer(bin_centers, persistent=False)

    # forward：前向传播：组装特征并返回模型输出
    def forward(self, depth_logits: torch.Tensor) -> torch.Tensor:
        probs = self.activation(depth_logits) + self.min_depth
        probs = probs / probs.sum(dim=1, keepdim=True)
        bin_centers = self.bin_centers.to(dtype=depth_logits.dtype)
        return probs.permute(0, 2, 3, 1) @ bin_centers


@auto_docstring
# Tipsv2DptPreTrainedModel：Tipsv2-DPT 预训练基类：Kaiming 初始化与深度 bin 中心注册
class Tipsv2DptPreTrainedModel(PreTrainedModel):
    config: Tipsv2DptConfig
    base_model_prefix = "backbone"
    main_input_name = "pixel_values"
    input_modalities = ["image"]
    supports_gradient_checkpointing = True

    # _init_weights：权重初始化：Linear/Conv Kaiming 正态、深度 bin 中心拷贝
    def _init_weights(self, module) -> None:
        super()._init_weights(module)
        if isinstance(module, (nn.Linear, nn.Conv2d, nn.ConvTranspose2d)):
            init.kaiming_normal_(module.weight, mode="fan_out", nonlinearity="relu")
        elif isinstance(module, Tipsv2DptFeaturesToDepth):
            bin_centers = torch.linspace(module.min_depth, module.max_depth, module.bin_centers.shape[0])
            init.copy_(module.bin_centers, bin_centers)


@auto_docstring(
    custom_intro="""
    TIPSv2-DPT Model with three independent heads for depth estimation, surface normal estimation,
    and semantic segmentation — running a single shared backbone forward pass.
    """
)
# Tipsv2DptForDensePrediction：Tipsv2-DPT 三头密集预测：单次前向输出深度/法线/分割
class Tipsv2DptForDensePrediction(Tipsv2DptPreTrainedModel):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        super().__init__(config)
        self.backbone = load_backbone(config)
        self.depth_neck = Tipsv2DptNeck(config)
        self.depth_decoder = Tipsv2DptDecoder(
            config, out_channels=config.num_depth_bins, activation=config.depth_decoder_activation
        )
        self.depth_bin_regressor = Tipsv2DptFeaturesToDepth(config)
        self.normals_neck = Tipsv2DptNeck(config)
        self.normals_decoder = Tipsv2DptDecoder(config, out_channels=3)
        self.segmentation_neck = Tipsv2DptNeck(config)
        self.segmentation_decoder = Tipsv2DptDecoder(config, out_channels=config.num_labels)
        self.post_init()

    # get_input_embeddings：获取输入嵌入：返回 backbone 词/patch 嵌入层
    def get_input_embeddings(self):
        return self.backbone.get_input_embeddings()

    @can_return_tuple
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        pixel_values: torch.FloatTensor,
        **kwargs: Unpack[TransformersKwargs],
    ) -> Tipsv2DptDensePredictorOutput:
        r"""
        Example:

        ```python
        >>> import torch
        >>> from transformers import Tipsv2DptForDensePrediction, AutoImageProcessor
        >>> from transformers.image_utils import load_image

        >>> model_id = "google/tipsv2-b14-dpt"
        >>> model = Tipsv2DptForDensePrediction.from_pretrained(model_id, device_map="auto")
        >>> image_processor = AutoImageProcessor.from_pretrained(model_id)

        >>> image = load_image("https://huggingface.co/datasets/huggingface/documentation-images/resolve/main/transformers/model_doc/room.jpg")
        >>> inputs = image_processor(images=image, return_tensors="pt").to(model.device)

        >>> with torch.no_grad():
        ...     outputs = model(**inputs)

        >>> # outputs.predicted_depth: (batch_size, height, width) tensor with predicted depth in meters
        >>> # outputs.normals: (batch_size, 3, height, width) tensor with normals in XYZ format (unnormalized)
        >>> # outputs.segmentation_logits: (batch_size, config.num_labels, height, width) tensor with segmentation logits
        >>> depth_results = image_processor.post_process_depth_estimation(outputs, target_sizes=[(image.height, image.width)])
        >>> normal_results = image_processor.post_process_normal_estimation(outputs, target_sizes=[(image.height, image.width)])
        >>> segmentation_results = image_processor.post_process_semantic_segmentation(outputs, target_sizes=[(image.height, image.width)])

        >>> predicted_depth = depth_results[0]["predicted_depth"]  # (height, width) tensor with predicted depth in meters
        >>> normals = normal_results[0]["normals"]  # (3, height, width) tensor with normals in XYZ format (L2-normalized)
        >>> segmentation = segmentation_results[0]  # (height, width) tensor with class ids
        ```"""
        outputs = self.backbone.forward_with_filtered_kwargs(pixel_values, **kwargs)
        feature_maps = outputs.feature_maps

        _, _, height, width = pixel_values.shape
        patch_size = self.config.backbone_config.patch_size
        patch_size_height = patch_size if isinstance(patch_size, int) else patch_size[0]
        patch_size_width = patch_size if isinstance(patch_size, int) else patch_size[1]
        patch_height = height // patch_size_height
        patch_width = width // patch_size_width

        depth_fused = self.depth_neck(feature_maps, patch_height=patch_height, patch_width=patch_width)
        depth_logits = self.depth_decoder(depth_fused[-1])
        predicted_depth = self.depth_bin_regressor(depth_logits)

        normals_fused = self.normals_neck(feature_maps, patch_height=patch_height, patch_width=patch_width)
        normals = self.normals_decoder(normals_fused[-1])

        segmentation_feature_maps_fused = self.segmentation_neck(
            feature_maps, patch_height=patch_height, patch_width=patch_width
        )
        segmentation_logits = self.segmentation_decoder(segmentation_feature_maps_fused[-1])

        return Tipsv2DptDensePredictorOutput(
            predicted_depth=predicted_depth,
            normals=normals,
            segmentation_logits=segmentation_logits,
            hidden_states=outputs.hidden_states,
            attentions=outputs.attentions,
        )


@auto_docstring(
    custom_intro="""
    TIPSv2-DPT Model with a monocular depth estimation head.
    """
)
# Tipsv2DptForDepthEstimation：Tipsv2-DPT 单目深度估计：DPT 颈部 + 分箱深度回归头
class Tipsv2DptForDepthEstimation(Tipsv2DptPreTrainedModel):
    _keys_to_ignore_on_load_unexpected = {"normals_head", "segmentation_head"}

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        super().__init__(config)
        self.backbone = load_backbone(config)
        self.neck = Tipsv2DptNeck(config)
        self.decoder = Tipsv2DptDecoder(
            config, out_channels=config.num_depth_bins, activation=config.depth_decoder_activation
        )
        self.bin_regressor = Tipsv2DptFeaturesToDepth(config)
        self.post_init()

    # get_input_embeddings：获取输入嵌入：返回 backbone 词/patch 嵌入层
    def get_input_embeddings(self):
        return self.backbone.get_input_embeddings()

    @can_return_tuple
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        pixel_values: torch.FloatTensor,
        labels: torch.FloatTensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> DepthEstimatorOutput:
        r"""
        Example:

        ```python
        >>> import torch
        >>> from transformers import AutoModelForDepthEstimation, AutoImageProcessor
        >>> from transformers.image_utils import load_image

        >>> model_id = "google/tipsv2-b14-dpt"
        >>> model = AutoModelForDepthEstimation.from_pretrained(model_id, device_map="auto")
        >>> image_processor = AutoImageProcessor.from_pretrained(model_id)

        >>> image = load_image("https://huggingface.co/datasets/huggingface/documentation-images/resolve/main/transformers/model_doc/room.jpg")
        >>> inputs = image_processor(images=image, return_tensors="pt").to(model.device)

        >>> with torch.no_grad():
        ...     outputs = model(**inputs)

        >>> results = image_processor.post_process_depth_estimation(outputs, target_sizes=[(image.height, image.width)])
        >>> predicted_depth = results[0]["predicted_depth"]  # (height, width) tensor with predicted depth in meters
        ```"""
        outputs = self.backbone.forward_with_filtered_kwargs(pixel_values, **kwargs)
        feature_maps = outputs.feature_maps

        _, _, height, width = pixel_values.shape
        patch_size = self.config.backbone_config.patch_size
        patch_size_height = patch_size if isinstance(patch_size, int) else patch_size[0]
        patch_size_width = patch_size if isinstance(patch_size, int) else patch_size[1]
        patch_height = height // patch_size_height
        patch_width = width // patch_size_width

        fused = self.neck(feature_maps, patch_height=patch_height, patch_width=patch_width)
        logits = self.decoder(fused[-1])
        predicted_depth = self.bin_regressor(logits)

        loss = None
        if labels is not None:
            raise NotImplementedError("Training is not yet supported")

        return DepthEstimatorOutput(
            loss=loss,
            predicted_depth=predicted_depth,
            hidden_states=outputs.hidden_states,
            attentions=outputs.attentions,
        )


@auto_docstring(
    custom_intro="""
    TIPSv2-DPT Model with a surface normal estimation head.
    """
)
# Tipsv2DptForNormalEstimation：Tipsv2-DPT 表面法线估计：三通道未归一化法线输出
class Tipsv2DptForNormalEstimation(Tipsv2DptPreTrainedModel):
    _keys_to_ignore_on_load_unexpected = {"depth_head", "segmentation_head"}

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        super().__init__(config)
        self.backbone = load_backbone(config)
        self.neck = Tipsv2DptNeck(config)
        self.decoder = Tipsv2DptDecoder(config, out_channels=3)
        self.post_init()

    # get_input_embeddings：获取输入嵌入：返回 backbone 词/patch 嵌入层
    def get_input_embeddings(self):
        return self.backbone.get_input_embeddings()

    @can_return_tuple
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        pixel_values: torch.FloatTensor,
        labels: torch.FloatTensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> Tipsv2DptNormalEstimatorOutput:
        r"""
        Example:

        ```python
        >>> import torch
        >>> from transformers import Tipsv2DptForNormalEstimation, AutoImageProcessor
        >>> from transformers.image_utils import load_image

        >>> model_id = "google/tipsv2-b14-dpt"
        >>> model = Tipsv2DptForNormalEstimation.from_pretrained(model_id, device_map="auto")
        >>> image_processor = AutoImageProcessor.from_pretrained(model_id)

        >>> image = load_image("https://huggingface.co/datasets/huggingface/documentation-images/resolve/main/transformers/model_doc/room.jpg")
        >>> inputs = image_processor(images=image, return_tensors="pt").to(model.device)

        >>> with torch.no_grad():
        ...     outputs = model(**inputs)

        >>> results = image_processor.post_process_normal_estimation(outputs, target_sizes=[(image.height, image.width)])
        >>> normals = results[0]["normals"]  # (3, height, width) tensor with normals in XYZ format (L2-normalized)
        ```"""
        outputs = self.backbone.forward_with_filtered_kwargs(pixel_values, **kwargs)
        feature_maps = outputs.feature_maps

        _, _, height, width = pixel_values.shape
        patch_size = self.config.backbone_config.patch_size
        patch_size_height = patch_size if isinstance(patch_size, int) else patch_size[0]
        patch_size_width = patch_size if isinstance(patch_size, int) else patch_size[1]
        patch_height = height // patch_size_height
        patch_width = width // patch_size_width

        fused = self.neck(feature_maps, patch_height=patch_height, patch_width=patch_width)
        normals = self.decoder(fused[-1])  # (B, 3, H', W') — unnormalized

        loss = None
        if labels is not None:
            raise NotImplementedError("Training is not yet supported")

        return Tipsv2DptNormalEstimatorOutput(
            loss=loss,
            normals=normals,
            hidden_states=outputs.hidden_states,
            attentions=outputs.attentions,
        )


@auto_docstring(
    custom_intro="""
    TIPSv2-DPT Model with a semantic segmentation head.
    """
)
# Tipsv2DptForSemanticSegmentation：Tipsv2-DPT 语义分割：逐像素交叉熵损失与 logits 输出
class Tipsv2DptForSemanticSegmentation(Tipsv2DptPreTrainedModel):
    _keys_to_ignore_on_load_unexpected = {"depth_head", "normals_head"}

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, config: Tipsv2DptConfig):
        super().__init__(config)
        self.backbone = load_backbone(config)
        self.neck = Tipsv2DptNeck(config)
        self.decoder = Tipsv2DptDecoder(config, out_channels=config.num_labels)
        self.post_init()

    # get_input_embeddings：获取输入嵌入：返回 backbone 词/patch 嵌入层
    def get_input_embeddings(self):
        return self.backbone.get_input_embeddings()

    @can_return_tuple
    @auto_docstring
    # forward：前向传播：组装特征并返回模型输出
    def forward(
        self,
        pixel_values: torch.FloatTensor,
        labels: torch.LongTensor | None = None,
        **kwargs: Unpack[TransformersKwargs],
    ) -> SemanticSegmenterOutput:
        r"""
        labels (`torch.LongTensor` of shape `(batch_size, height, width)`, *optional*):
            Ground truth semantic segmentation maps for computing the loss. Indices should be in `[0, ...,
            config.num_labels - 1]`. If `config.num_labels > 1`, a classification loss is computed (Cross-Entropy).

        Example:

        ```python
        >>> import torch
        >>> from transformers import AutoModelForSemanticSegmentation, AutoImageProcessor
        >>> from transformers.image_utils import load_image

        >>> model_id = "google/tipsv2-b14-dpt"
        >>> model = AutoModelForSemanticSegmentation.from_pretrained(model_id, device_map="auto")
        >>> image_processor = AutoImageProcessor.from_pretrained(model_id)

        >>> image = load_image("https://huggingface.co/datasets/huggingface/documentation-images/resolve/main/transformers/model_doc/room.jpg")
        >>> inputs = image_processor(images=image, return_tensors="pt").to(model.device)

        >>> with torch.no_grad():
        ...     outputs = model(**inputs)

        >>> results = image_processor.post_process_semantic_segmentation(outputs, target_sizes=[(image.height, image.width)])
        >>> segmentation_map = results[0]  # (height, width) tensor with class ids
        ```
        """
        outputs = self.backbone.forward_with_filtered_kwargs(pixel_values, **kwargs)
        feature_maps = outputs.feature_maps

        _, _, height, width = pixel_values.shape
        patch_size = self.config.backbone_config.patch_size
        patch_size_height = patch_size if isinstance(patch_size, int) else patch_size[0]
        patch_size_width = patch_size if isinstance(patch_size, int) else patch_size[1]
        patch_height = height // patch_size_height
        patch_width = width // patch_size_width

        feature_maps_fused = self.neck(feature_maps, patch_height=patch_height, patch_width=patch_width)
        logits = self.decoder(feature_maps_fused[-1])

        loss = None
        if labels is not None:
            upsampled_logits = nn.functional.interpolate(
                logits, size=labels.shape[-2:], mode="bilinear", align_corners=False
            )
            loss = self.loss_function(
                upsampled_logits,
                labels,
                ignore_index=self.config.semantic_loss_ignore_index,
            )

        return SemanticSegmenterOutput(
            loss=loss,
            logits=logits,
            hidden_states=outputs.hidden_states,
            attentions=outputs.attentions,
        )


__all__ = [
    "Tipsv2DptConfig",
    "Tipsv2DptImageProcessor",
    "Tipsv2DptPreTrainedModel",
    "Tipsv2DptForDensePrediction",
    "Tipsv2DptForDepthEstimation",
    "Tipsv2DptForNormalEstimation",
    "Tipsv2DptForSemanticSegmentation",
]
