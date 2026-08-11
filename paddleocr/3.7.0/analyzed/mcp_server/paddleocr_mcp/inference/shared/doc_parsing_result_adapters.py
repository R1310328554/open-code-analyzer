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

# 文档解析结果适配器：将 local pipeline 与 AI Studio API 的原始输出统一为 DocParsingResult
import base64
import io
from typing import Any

from ..types import DocParsingResult


    # 解析本地 PP-Structure / VL pipeline 返回的逐页 markdown 与 PIL 图片
def parse_local_doc_parsing_result(result: Any) -> DocParsingResult:
    markdown_parts: list[str] = []
    all_images_mapping: dict[str, str] = {}

    for res in result:
        # 每页提取 markdown_texts 并将 markdown_images 中的 PIL 对象 JPEG+Base64 编码
        markdown = res.markdown
        markdown_parts.append(markdown["markdown_texts"])
        processed_images = {}
        for img_key, img_data in markdown["markdown_images"].items():
            with io.BytesIO() as buffer:
                img_data.save(buffer, format="JPEG")
                processed_images[img_key] = base64.b64encode(buffer.getvalue()).decode(
                    "ascii"
                )
        all_images_mapping.update(processed_images)

    return DocParsingResult(
        markdown="\n".join(markdown_parts),
        pages=len(result),
        images_mapping=all_images_mapping,
    )


    # 解析 AI Studio 异步 API 返回的 pages 列表（已是 Base64 图片映射）
def parse_aistudio_doc_parsing_result(result: Any) -> DocParsingResult:
    markdown_parts: list[str] = []
    all_images_mapping: dict[str, str] = {}

    for page in result.pages:
        markdown_parts.append(page.markdown_text)
        all_images_mapping.update(page.markdown_images)

    return DocParsingResult(
        markdown="\n".join(markdown_parts),
        pages=len(result.pages),
        images_mapping=all_images_mapping,
    )
