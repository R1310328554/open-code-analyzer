# Copyright 2024 The HuggingFace Inc. team.
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
Image/Text processor class for SigLIP.
"""

from ...processing_utils import ProcessorMixin
from ...utils import auto_docstring
# SigLIP 处理器：组合图像处理器与 SentencePiece 分词器



@auto_docstring
# SiglipProcessor：SigLIP 处理器：封装图像处理器与分词器的联合调用
class SiglipProcessor(ProcessorMixin):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, image_processor, tokenizer):
        super().__init__(image_processor, tokenizer)


__all__ = ["SiglipProcessor"]
