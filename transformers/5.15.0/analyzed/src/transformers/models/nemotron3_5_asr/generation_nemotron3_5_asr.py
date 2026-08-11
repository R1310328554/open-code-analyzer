# Copyright 2026 The HuggingFace Inc. team. All rights reserved.
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

from ..nemotron_asr_streaming.generation_nemotron_asr_streaming import (
    NemotronAsrStreamingGenerationMixin,
    NemotronAsrStreamingRNNTDecoderCache,
)


# Nemotron 3.5 ASR 生成：RNN-T 解码 mixin（注入 language prompt_ids）

# Nemotron3_5AsrRNNTDecoderCache：3.5 ASR RNN-T 解码器 LSTM 缓存
class Nemotron3_5AsrRNNTDecoderCache(NemotronAsrStreamingRNNTDecoderCache): ...


# Nemotron3_5AsrGenerationMixin：3.5 ASR 生成 mixin（注入 prompt_ids 到音频编码）
class Nemotron3_5AsrGenerationMixin(NemotronAsrStreamingGenerationMixin):
    # generate：RNN-T 贪心/流式转写生成（支持 mel 分块 generator 输入）
    def generate(self, inputs=None, generation_config=None, **kwargs):
        self._prompt_ids = kwargs.pop("prompt_ids", None)
        get_audio_features = self.get_audio_features

        def get_audio_features_with_prompt(*args, prompt_ids=None, **features_kwargs):
            prompt_ids = self._prompt_ids if prompt_ids is None else prompt_ids
            return get_audio_features(*args, prompt_ids=prompt_ids, **features_kwargs)

        self.get_audio_features = get_audio_features_with_prompt
        try:
            return super().generate(inputs=inputs, generation_config=generation_config, **kwargs)
        finally:
            del self.get_audio_features
            del self._prompt_ids
