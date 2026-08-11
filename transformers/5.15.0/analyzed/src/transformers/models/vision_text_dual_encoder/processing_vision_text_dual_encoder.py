# Copyright 2021 The HuggingFace Inc. team.
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
Processor class for VisionTextDualEncoder
"""

from ...processing_utils import ProcessingKwargs, ProcessorMixin
from ...utils import auto_docstring


# VisionTextDualEncoder 处理器：image_processor + tokenizer 联合封装，服务 CLIP 风格双塔

# VisionTextDualEncoderProcessorKwargs：双塔处理器参数：继承 ProcessingKwargs，预留默认 kwargs 扩展点
class VisionTextDualEncoderProcessorKwargs(ProcessingKwargs, total=False):
    _defaults = {}


@auto_docstring
# VisionTextDualEncoderProcessor：双塔多模态处理器：image_processor 与 tokenizer 联合调用
class VisionTextDualEncoderProcessor(ProcessorMixin):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, image_processor=None, tokenizer=None, **kwargs):
        super().__init__(image_processor, tokenizer)


__all__ = ["VisionTextDualEncoderProcessor"]
