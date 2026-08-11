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
"""Tokenization classes for MobileBERT model."""

# MobileBERT 分词：复用 BertTokenizer（fast tokenizer 别名）

from ..bert.tokenization_bert import BertTokenizer


# MobileBertTokenizer 为 BertTokenizer 别名
# MobileBertTokenizer is an alias for BertTokenizer
MobileBertTokenizer = BertTokenizer

# MobileBertTokenizerFast 为 MobileBertTokenizer 别名（BertTokenizer 已是 fast）
# MobileBertTokenizerFast is an alias for MobileBertTokenizer (since BertTokenizer is already a fast tokenizer)
MobileBertTokenizerFast = MobileBertTokenizer

__all__ = ["MobileBertTokenizer", "MobileBertTokenizerFast"]
