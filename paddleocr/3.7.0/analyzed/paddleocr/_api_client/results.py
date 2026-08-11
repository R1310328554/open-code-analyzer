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

# API 结果与任务状态 dataclass：OCR/文档解析页、Job 与 Batch 聚合
from dataclasses import dataclass, field
from typing import Any, Dict, List, Literal, Optional


@dataclass
    # 单页 OCR 结果：prunedResult 与可视化图片 URL
class OCRPage:
    pruned_result: Any
    ocr_image_url: Optional[str] = None
    doc_preprocessing_image_url: Optional[str] = None
    input_image_url: Optional[str] = None
    raw: Dict[str, Any] = field(default_factory=dict)


@dataclass
    # 单页文档解析：Markdown 文本、图片映射与 exports
class DocParsingPage:
    markdown_text: str
    markdown_images: Dict[str, str] = field(default_factory=dict)
    output_images: Dict[str, str] = field(default_factory=dict)
    pruned_result: Any = None
    input_image_url: Optional[str] = None
    exports: Dict[str, Any] = field(default_factory=dict)
    markdown: Dict[str, Any] = field(default_factory=dict)
    raw: Dict[str, Any] = field(default_factory=dict)


@dataclass
    # OCR 任务完整结果：job_id、pages 与 data_info
class OCRResult:
    job_id: str
    pages: List[OCRPage] = field(default_factory=list)
    data_info: Dict[str, Any] = field(default_factory=dict)


@dataclass
    # 文档解析完整结果：多页 Markdown 与元数据
class DocParsingResult:
    job_id: str
    pages: List[DocParsingPage] = field(default_factory=list)
    data_info: Dict[str, Any] = field(default_factory=dict)


@dataclass
    # 任务提取进度：总页数、已提取页数与时间戳
class Progress:
    total_pages: int = 0
    extracted_pages: int = 0
    start_time: Optional[str] = None
    end_time: Optional[str] = None


@dataclass
    # 已提交任务句柄：job_id、model 与 task 类型
class Job:
    job_id: str
    model: str
    task: Literal["ocr", "document_parsing"]


@dataclass
    # 单 job 状态：state、progress、result 与 error_msg
class JobStatus:
    job_id: str
    state: str
    progress: Optional[Progress] = None
    result: Any = None
    error_msg: Optional[str] = None


@dataclass
    # batch 下全部 job 的状态列表
class BatchStatus:
    batch_id: str
    jobs: List[JobStatus] = field(default_factory=list)
