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
"""PIL image processor class for VideoMAE."""

import numpy as np

from ...image_processing_backends import PilBackend
from ...image_processing_utils import BatchFeature
from ...image_utils import (
    IMAGENET_STANDARD_MEAN,
    IMAGENET_STANDARD_STD,
    ImageInput,
    PILImageResampling,
    SizeDict,
    make_nested_list_of_images,
)
from ...processing_utils import ImagesKwargs, Unpack
from ...utils import TensorType, auto_docstring


# VideoMAE PIL 图像处理器：224 中心裁剪、ImageNet 归一化与视频帧批处理

@auto_docstring
# VideoMAEImageProcessorPil：VideoMAE PIL 处理器：224 缩放裁剪、归一化与 nested 视频帧结构
class VideoMAEImageProcessorPil(PilBackend):
    resample = PILImageResampling.BILINEAR
    image_mean = IMAGENET_STANDARD_MEAN
    image_std = IMAGENET_STANDARD_STD
    size = {"shortest_edge": 224}
    default_to_square = False
    crop_size = {"height": 224, "width": 224}
    do_resize = True
    do_center_crop = True
    do_rescale = True
    do_normalize = True

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, **kwargs: Unpack[ImagesKwargs]):
        super().__init__(**kwargs)

    # _prepare_images_structure：图像结构整理：嵌套列表规范化以支持单帧/批量/视频帧
    def _prepare_images_structure(self, images: ImageInput, expected_ndims: int = 3) -> ImageInput:
        return make_nested_list_of_images(images, expected_ndims=expected_ndims)

    @auto_docstring
    # preprocess：预处理入口：resize/crop/归一化并打包 pixel_values
    def preprocess(self, videos: ImageInput, **kwargs: Unpack[ImagesKwargs]) -> BatchFeature:
        r"""
        videos (`ImageInput`):
            Video or batch of videos to preprocess. Expects a single video (list of frames) or a batch of videos
            (list of list of frames). Each frame can be a PIL image, numpy array, or torch tensor with pixel values
            ranging from 0 to 255. If passing in frames with pixel values between 0 and 1, set `do_rescale=False`.
        """
        return super().preprocess(videos, **kwargs)

    def _preprocess(
        self,
        images: list[list[np.ndarray]],
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
        return_tensors: str | TensorType | None,
        **kwargs,
    ) -> BatchFeature:
        pixel_values = []
        for video_frames in images:
            processed_frames = []
            for image in video_frames:
                if do_resize:
                    image = self.resize(image, size, resample)
                if do_center_crop:
                    image = self.center_crop(image, crop_size)
                if do_rescale:
                    image = self.rescale(image, rescale_factor)
                if do_normalize:
                    image = self.normalize(image, image_mean, image_std)
                processed_frames.append(image)
            pixel_values.append(processed_frames)
        return BatchFeature(data={"pixel_values": pixel_values}, tensor_type=return_tensors)


__all__ = ["VideoMAEImageProcessorPil"]
