# Copyright 2023 The Intel Labs Team Authors, The Microsoft Research Team Authors and HuggingFace Inc. team. All rights reserved.
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
Processor class for BridgeTower.
"""

from ...processing_utils import ProcessingKwargs, ProcessorMixin
from ...utils import auto_docstring


# BridgeTowerProcessorKwargs：文本与图像默认 kwargs
class BridgeTowerProcessorKwargs(ProcessingKwargs, total=False):
    _defaults = {
        "text_kwargs": {
            "add_special_tokens": True,
            "padding": False,
            "stride": 0,
            "return_overflowing_tokens": False,
            "return_special_tokens_mask": False,
            "return_offsets_mapping": False,
            "return_length": False,
            "verbose": True,
        },
        "images_kwargs": {
            "do_normalize": True,
            "do_center_crop": True,
        },
    }


@auto_docstring
# BridgeTowerProcessor：组合 image_processor 与 tokenizer
class BridgeTowerProcessor(ProcessorMixin):
    valid_processor_kwargs = BridgeTowerProcessorKwargs

# __init__：注册图像处理器与 RoBERTa 分词器
    def __init__(self, image_processor, tokenizer):
        super().__init__(image_processor, tokenizer)


__all__ = ["BridgeTowerProcessor"]
