#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
"""
NER 抽取数据类型：Entity/Relation/ExtractionResult 及 spaCy 标签映射。
Data types for entity and relation extraction.
"""

from dataclasses import dataclass, field
from typing import Any, Dict, List


@dataclass
class Entity:
    """抽取实体：文本、NER 标签、字符区间与置信度。"""
    """Extracted entity."""

    text: str
    label: str  # spaCy NER label: PERSON, ORG, GPE, ...
    start_char: int
    end_char: int
    confidence: float = 1.0
    metadata: Dict[str, Any] = field(default_factory=dict)


@dataclass
class Relation:
    """实体间 typed 关系：主语、谓词、宾语与上下文。"""
    """Extracted relation between two entities."""

    subject: Entity
    predicate: str  # relation type: "founded_by", "works_for", ...
    obj: Entity
    confidence: float = 1.0
    context: str = ""  # surrounding text
    metadata: Dict[str, Any] = field(default_factory=dict)


@dataclass
class ExtractionResult:
    """单次抽取结果：entities、relations 与 metadata。"""
    """Result of a full extraction pass."""

    entities: List[Entity] = field(default_factory=list)
    relations: List[Relation] = field(default_factory=list)
    language: str = "en"
    metadata: Dict[str, Any] = field(default_factory=dict)


# spaCy NER 标签 → GraphRAG 应用 entity_types
SPACY_TO_APP_ENTITY_TYPE: Dict[str, str] = {
    "PERSON": "person",
    "ORG": "organization",
    "GPE": "geo",
    "LOC": "geo",
    "FAC": "geo",
    "EVENT": "event",
    "PRODUCT": "category",
    "WORK_OF_ART": "category",
    "LAW": "category",
    "LANGUAGE": "category",
    "NORP": "category",
    "MONEY": "category",
    "QUANTITY": "category",
    "TIME": "event",
    "DATE": "event",
    "PERCENT": "category",
    "CARDINAL": "category",
    "ORDINAL": "category",
}

SKIP_SPACY_LABELS = {"ORDINAL", "CARDINAL"}
