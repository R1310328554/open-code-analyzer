# Copyright 2025 the HuggingFace Inc. team. All rights reserved.
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
from ...processing_utils import ProcessorMixin


# PeAudioVideo 处理器：音频/视频/文本三模态联合预处理

# PeAudioVideoProcessor：PeAudioVideo 特征提取/视频/文本联合处理器
class PeAudioVideoProcessor(ProcessorMixin):
    # __init__：初始化模块/处理器默认参数与依赖组件
    def __init__(self, feature_extractor=None, video_processor=None, tokenizer=None, **kwargs):
        super().__init__(feature_extractor, video_processor, tokenizer, **kwargs)


__all__ = ["PeAudioVideoProcessor"]
