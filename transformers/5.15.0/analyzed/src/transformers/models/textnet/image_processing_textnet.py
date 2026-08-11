# Copyright 2024 the Fast authors and The HuggingFace Inc. team. All rights reserved.
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
"""Image processor class for TextNet."""

import torch
from torchvision.transforms.v2 import functional as tvF

from ...image_processing_backends import TorchvisionBackend
from ...image_processing_utils import BatchFeature
from ...image_transforms import get_resize_output_image_size, group_images_by_shape, reorder_images
from ...image_utils import (
# TextNet Torchvision 图像处理器：最短边缩放并对齐 size_divisor 整除约束

    IMAGENET_DEFAULT_MEAN,
    IMAGENET_DEFAULT_STD,
    ChannelDimension,
    ImageInput,
    PILImageResampling,
    SizeDict,
)
from ...processing_utils import ImagesKwargs, Unpack
from ...utils import TensorType, auto_docstring


# TextNetImageProcessorKwargs：TextNet 图像处理器 kwargs：size_divisor 缩放对齐参数
class TextNetImageProcessorKwargs(ImagesKwargs, total=False):
    """
    size_divisor (`int`, *optional*, defaults to `self.size_divisor`):
        Ensures height and width are rounded to a multiple of this value after resizing.
    """

    size_divisor: int


@auto_docstring
# TextNetImageProcessor：TextNet 图像处理器：Torchvision 后端，最短边缩放后向上取整至 divisor 倍数
class TextNetImageProcessor(TorchvisionBackend):
    """Torchvision backend for TextNet with size_divisor resize."""

    valid_kwargs = TextNetImageProcessorKwargs

    resample = PILImageResampling.BILINEAR
    image_mean = IMAGENET_DEFAULT_MEAN
    image_std = IMAGENET_DEFAULT_STD
    size = {"shortest_edge": 640}
    default_to_square = False
    crop_size = {"height": 224, "width": 224}
    do_resize = True
    do_center_crop = False
    do_rescale = True
    do_normalize = True
    do_convert_rgb = True
    size_divisor = 32

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, **kwargs: Unpack[TextNetImageProcessorKwargs]):
        super().__init__(**kwargs)

    @auto_docstring
    # preprocess：预处理入口：解析 kwargs 并调用后端 _preprocess
    def preprocess(self, images: ImageInput, **kwargs: Unpack[TextNetImageProcessorKwargs]) -> BatchFeature:
        return super().preprocess(images, **kwargs)

    # resize：缩放图像：最短边缩放后向上取整至 size_divisor 整除
    def resize(
        self,
        image: "torch.Tensor",
        size: SizeDict,
        resample: "PILImageResampling | tvF.InterpolationMode | int | None",
        size_divisor: int = 32,
        **kwargs,
    ) -> "torch.Tensor":
        """Resize to shortest_edge then round up to be divisible by size_divisor."""
        if not size.shortest_edge:
            raise ValueError(f"Size must contain 'shortest_edge' key. Got {size.keys()}")
        new_size = get_resize_output_image_size(
            image,
            size=size.shortest_edge,
            default_to_square=False,
            input_data_format=ChannelDimension.FIRST,
        )
        height, width = new_size
        # 将高宽向上取整至 size_divisor 的倍数，满足骨干下采样对齐
        # Round up to be divisible by size_divisor
        if height % size_divisor != 0:
            height += size_divisor - (height % size_divisor)
        if width % size_divisor != 0:
            width += size_divisor - (width % size_divisor)
        return super().resize(
            image,
            SizeDict(height=height, width=width),
            resample=resample,
            **kwargs,
        )

    # _preprocess：内部预处理：按形状分组 resize/归一化后重组 batch
    def _preprocess(
        self,
        images: list["torch.Tensor"],
        do_resize: bool,
        size: SizeDict,
        resample: "PILImageResampling | tvF.InterpolationMode | int | None",
        do_center_crop: bool,
        crop_size: SizeDict,
        do_rescale: bool,
        rescale_factor: float,
        do_normalize: bool,
        image_mean: float | list[float] | None,
        image_std: float | list[float] | None,
        do_pad: bool | None,
        pad_size: SizeDict | None,
        disable_grouping: bool | None,
        return_tensors: str | TensorType | None,
        size_divisor: int = 32,
        **kwargs,
    ) -> BatchFeature:
        """Custom preprocessing for TextNet."""
        # 按输入形状分组 batch，同形状样本堆叠后批量 resize 提升效率
        grouped_images, grouped_images_index = group_images_by_shape(images, disable_grouping=disable_grouping)
        resized_images_grouped = {}
        for shape, stacked_images in grouped_images.items():
            if do_resize:
                stacked_images = self.resize(stacked_images, size, resample, size_divisor=size_divisor)
            resized_images_grouped[shape] = stacked_images
        resized_images = reorder_images(resized_images_grouped, grouped_images_index)

        # 按输入形状分组 batch，同形状样本堆叠后批量 resize 提升效率
        grouped_images, grouped_images_index = group_images_by_shape(resized_images, disable_grouping=disable_grouping)
        processed_images_grouped = {}
        for shape, stacked_images in grouped_images.items():
            if do_center_crop:
                stacked_images = self.center_crop(stacked_images, crop_size)
            stacked_images = self.rescale_and_normalize(
                stacked_images, do_rescale, rescale_factor, do_normalize, image_mean, image_std
            )
            processed_images_grouped[shape] = stacked_images
        processed_images = reorder_images(processed_images_grouped, grouped_images_index)
        return BatchFeature(data={"pixel_values": processed_images}, tensor_type=return_tensors)


__all__ = ["TextNetImageProcessor"]
