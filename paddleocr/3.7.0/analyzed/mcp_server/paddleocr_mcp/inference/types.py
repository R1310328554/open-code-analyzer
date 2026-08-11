# Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
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

# MCP 推理层核心数据类型：请求载荷、OCR/文档解析结果与 TextLine 行级结构
from dataclasses import asdict, dataclass, field
from typing import Any, Mapping, Optional, Union


@dataclass(frozen=True)
    # 推理请求：input_data、可选 file_type（HTTP 需 image/pdf）与 runtime_params 映射
class InferenceRequest:
    """推理请求体：统一封装用户输入与运行时参数。"""
    """Inference request payload."""

    input_data: str
    file_type: Optional[str] = None
    runtime_params: Mapping[str, Any] = field(default_factory=dict)


@dataclass(frozen=True)
    # 单行 OCR 结果：文本、置信度与检测框 bbox（四点多边形或矩形）
class TextLine:
    """A single text line with its bounding box and confidence."""

    text: str
    confidence: float
    bbox: Any


@dataclass(frozen=True)
    # OCR 聚合结果：全文拼接、平均置信度与 TextLine 列表，支持 to_dict 序列化
class OCRResult:
    """OCR result."""

    text: str
    confidence: float
    text_lines: list[TextLine]

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


@dataclass(frozen=True)
    # 文档解析结果：Markdown 全文、页数与 img src→Base64 的图片映射表
class DocParsingResult:
    """Document parsing result."""

    markdown: str
    pages: int
    images_mapping: dict[str, str]

    def to_dict(self) -> dict[str, Any]:
        return asdict(self)


# predict 返回类型的联合别名：OCR 与文档解析共用 Inference 接口
InferenceResult = Union[OCRResult, DocParsingResult]
