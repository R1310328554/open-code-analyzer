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
"""Tokenization classes for SqueezeBERT model."""

from ..bert.tokenization_bert import BertTokenizer
# SqueezeBERT 分词器：复用 BertTokenizer 的别名封装，词表与 BERT 兼容



# SqueezeBertTokenizer is an alias for BertTokenizer
# SqueezeBertTokenizer 为 BertTokenizer 别名，共享 BERT 词表与分词逻辑
SqueezeBertTokenizer = BertTokenizer

# SqueezeBertTokenizerFast is an alias for SqueezeBertTokenizer (since BertTokenizer is already a fast tokenizer)
# Fast 分词器别名：BertTokenizer 已基于 Rust 后端，无需单独 Fast 类
SqueezeBertTokenizerFast = SqueezeBertTokenizer

__all__ = ["SqueezeBertTokenizer", "SqueezeBertTokenizerFast"]
