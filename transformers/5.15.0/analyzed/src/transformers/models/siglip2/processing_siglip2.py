# Copyright 2025 The HuggingFace Inc. team.
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
"""
Image/Text processor class for SigLIP2.
"""

from ...processing_utils import ProcessingKwargs, ProcessorMixin
from ...utils import auto_docstring
# SigLIP2 处理器：图像 patch 预处理与 BPE 分词默认参数组合



# Siglip2ProcessorKwargs：SigLIP2 处理器参数：文本 max_length 与图像 patch 默认选项
class Siglip2ProcessorKwargs(ProcessingKwargs, total=False):
    _defaults = {
        "text_kwargs": {
            "padding": "max_length",
            "truncation": True,
            "max_length": 64,
        },
        "images_kwargs": {
            "max_num_patches": 256,
            "patch_size": 16,
        },
    }


@auto_docstring
# Siglip2Processor：SigLIP2 处理器：封装可变 patch 图像处理与 BPE 分词器
class Siglip2Processor(ProcessorMixin):
    valid_processor_kwargs = Siglip2ProcessorKwargs

    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, image_processor, tokenizer):
        super().__init__(image_processor, tokenizer)


__all__ = ["Siglip2Processor"]
