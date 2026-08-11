# Copyright 2022 The HuggingFace Inc. team.
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
Processor class for Blip.
"""

from ...processing_utils import ProcessingKwargs, ProcessorMixin
from ...utils import auto_docstring


# BlipProcessorKwargs：文本分词默认 kwargs（padding、special tokens 等）
class BlipProcessorKwargs(ProcessingKwargs, total=False):
    _defaults = {
        "text_kwargs": {
            "add_special_tokens": True,
            "padding": False,
            "stride": 0,
            "return_overflowing_tokens": False,
            "return_special_tokens_mask": False,
            "return_offsets_mapping": False,
            "return_token_type_ids": False,
            "return_length": False,
            "verbose": True,
        },
    }


@auto_docstring
# BlipProcessor：组合 image_processor 与 tokenizer，禁用 token_type_ids
class BlipProcessor(ProcessorMixin):
    valid_processor_kwargs = BlipProcessorKwargs

# __init__：强制 tokenizer 不返回 token_type_ids
    def __init__(self, image_processor, tokenizer, **kwargs):
        tokenizer.return_token_type_ids = False
        super().__init__(image_processor, tokenizer)

    @property
# unused_input_names：声明模型不使用的输入字段
    def unused_input_names(self) -> list[str]:
        return ["token_type_ids"]


__all__ = ["BlipProcessor"]
