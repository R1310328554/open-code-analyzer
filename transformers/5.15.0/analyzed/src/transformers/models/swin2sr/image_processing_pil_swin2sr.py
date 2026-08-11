# Copyright 2022 The HuggingFace Inc. team. All rights reserved.
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
"""Image processor class for Swin2SR."""

import numpy as np

from ...image_processing_backends import PilBackend
from ...image_processing_utils import BatchFeature
from ...image_transforms import pad as np_pad
from ...image_utils import (
# Swin2SR PIL 图像处理器：归一化缩放与对称 padding 至 size_divisor 整除

    ChannelDimension,
    ImageInput,
    PILImageResampling,
    SizeDict,
)
from ...processing_utils import ImagesKwargs, Unpack
from ...utils import TensorType, auto_docstring


# Adapted from transformers.models.swin2sr.image_processing_swin2sr.Swin2SRImageProcessorKwargs
# Swin2SRImageProcessorKwargs：Swin2SR 预处理参数：padding 对齐的 size_divisor
class Swin2SRImageProcessorKwargs(ImagesKwargs, total=False):
    """
    size_divisor (`int`, *optional*, defaults to `self.size_divisor`):
        The size to make the height and width divisible by when padding.
    """

    size_divisor: int


@auto_docstring
# Swin2SRImageProcessorPil：Swin2SR PIL 后端：归一化 + 对称 padding 至整除 size_divisor
class Swin2SRImageProcessorPil(PilBackend):
    """PIL backend for Swin2SR with custom pad."""

    valid_kwargs = Swin2SRImageProcessorKwargs

    do_rescale = True
    rescale_factor = 1 / 255
    do_pad = True
    size_divisor = 8

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, **kwargs: Unpack[Swin2SRImageProcessorKwargs]):
        # Handle legacy pad_size parameter
        pad_size = kwargs.pop("pad_size", None)
        if pad_size is not None:
            kwargs.setdefault("size_divisor", pad_size)
        super().__init__(**kwargs)

    @auto_docstring
    # preprocess：预处理入口：解析 kwargs 并调用后端 _preprocess
    def preprocess(
        self,
        images: ImageInput,
        **kwargs: Unpack[Swin2SRImageProcessorKwargs],
    ) -> BatchFeature:
        return super().preprocess(images, **kwargs)

    # pad：对称 padding：扩展 H/W 至 size_divisor 的最近倍数
    def pad(
        self,
        image: np.ndarray,
        pad_size: SizeDict | None,
        size_divisor: int = 8,
        **kwargs,
    ) -> np.ndarray:
        """Pad image to make height and width divisible by size_divisor using symmetric padding."""
        height, width = image.shape[-2:]
        pad_height = (height // size_divisor + 1) * size_divisor - height
        pad_width = (width // size_divisor + 1) * size_divisor - width
        return np_pad(
            image,
            padding=((0, pad_height), (0, pad_width)),
            mode="symmetric",
            data_format=ChannelDimension.FIRST,
            input_data_format=ChannelDimension.FIRST,
        )

    # _preprocess：内部预处理：缩放/归一化/灰度或 padding 流水线
    def _preprocess(
        self,
        images: list[np.ndarray],
        do_resize: bool,
        size: SizeDict,
        resample: "PILImageResampling | None",
        do_center_crop: bool,
        crop_size: SizeDict,
        do_rescale: bool,
        rescale_factor: float,
        do_normalize: bool,
        image_mean: float | list[float] | None,
        image_std: float | list[float] | None,
        do_pad: bool | None,
        pad_size: SizeDict | None,
        return_tensors: str | TensorType | None,
        size_divisor: int = 8,
        **kwargs,
    ) -> BatchFeature:
        """Custom preprocessing for Swin2SR."""
        processed_images = []
        for image in images:
            if do_rescale:
                image = self.rescale(image, rescale_factor)
            if do_pad:
                image = self.pad(image, pad_size=pad_size, size_divisor=size_divisor)
            processed_images.append(image)
        return BatchFeature(data={"pixel_values": processed_images}, tensor_type=return_tensors)


__all__ = ["Swin2SRImageProcessorPil"]
