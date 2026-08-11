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
Speech processor class for Speech2Text
"""

from ...processing_utils import ProcessorMixin
from ...utils import auto_docstring
# Speech2Text 处理器：特征提取器与分词器联合封装 ASR 音频/文本输入



@auto_docstring
# Speech2TextProcessor：Speech2Text 处理器：联合特征提取与分词，支持 audio/text 单模态或 ASR 训练标签
class Speech2TextProcessor(ProcessorMixin):
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, feature_extractor, tokenizer):
        super().__init__(feature_extractor, tokenizer)

    @auto_docstring
    # __call__：调用入口：路由 audio/text 源与目标并组装 BatchFeature
    def __call__(self, *args, **kwargs):
        audio = kwargs.pop("audio", None)
        sampling_rate = kwargs.pop("sampling_rate", None)
        text = kwargs.pop("text", None)
        if len(args) > 0:
            audio = args[0]
            args = args[1:]

        if audio is None and text is None:
            raise ValueError("You need to specify either an `audio` or `text` input to process.")

        if audio is not None:
            inputs = self.feature_extractor(audio, *args, sampling_rate=sampling_rate, **kwargs)
        if text is not None:
            encodings = self.tokenizer(text, **kwargs)

        if text is None:
            return inputs
        elif audio is None:
            return encodings
        else:
            inputs["labels"] = encodings["input_ids"]
            return inputs


__all__ = ["Speech2TextProcessor"]
