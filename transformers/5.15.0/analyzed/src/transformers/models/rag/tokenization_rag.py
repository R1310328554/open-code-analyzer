# Copyright 2020, The RAG Authors and The HuggingFace Inc. team.
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
"""Tokenization classes for RAG."""

import os

from ...utils import logging
from .configuration_rag import RagConfig
# RAG 分词器：问题编码器与生成器双 AutoTokenizer 联合封装



logger = logging.get_logger(__name__)


# RagTokenizer：双分词器：问题编码器/生成器切换与联合保存加载
class RagTokenizer:
    # __init__：初始化子模块、默认超参与可训练参数
    def __init__(self, question_encoder, generator):
        self.question_encoder = question_encoder
        self.generator = generator
        self.current_tokenizer = self.question_encoder

    # save_pretrained：保存双分词器到 question_encoder/generator 子目录
    def save_pretrained(self, save_directory):
        if os.path.isfile(save_directory):
            raise ValueError(f"Provided path ({save_directory}) should be a directory, not a file")
        os.makedirs(save_directory, exist_ok=True)
        question_encoder_path = os.path.join(save_directory, "question_encoder_tokenizer")
        generator_path = os.path.join(save_directory, "generator_tokenizer")
        self.question_encoder.save_pretrained(question_encoder_path)
        self.generator.save_pretrained(generator_path)

    @classmethod
    # from_pretrained：从 Hub 或本地路径加载 RAG 双分词器
    def from_pretrained(cls, pretrained_model_name_or_path, **kwargs):
        # dynamically import AutoTokenizer
        from ..auto.tokenization_auto import AutoTokenizer

        config = kwargs.pop("config", None)

        if config is None:
            config = RagConfig.from_pretrained(pretrained_model_name_or_path)

        question_encoder = AutoTokenizer.from_pretrained(
            pretrained_model_name_or_path, config=config.question_encoder, subfolder="question_encoder_tokenizer"
        )
        generator = AutoTokenizer.from_pretrained(
            pretrained_model_name_or_path, config=config.generator, subfolder="generator_tokenizer"
        )
        return cls(question_encoder=question_encoder, generator=generator)

    def __call__(self, *args, **kwargs):
        return self.current_tokenizer(*args, **kwargs)

    # batch_decode：批量 token id 解码为文本（使用生成器分词器）
    def batch_decode(self, *args, **kwargs):
        return self.generator.batch_decode(*args, **kwargs)

    # decode：单条 token 序列解码为文本
    def decode(self, *args, **kwargs):
        return self.generator.decode(*args, **kwargs)

    # _switch_to_input_mode：切换当前分词器为问题编码器模式
    def _switch_to_input_mode(self):
        self.current_tokenizer = self.question_encoder

    # _switch_to_target_mode：切换当前分词器为生成器目标模式
    def _switch_to_target_mode(self):
        self.current_tokenizer = self.generator


__all__ = ["RagTokenizer"]
