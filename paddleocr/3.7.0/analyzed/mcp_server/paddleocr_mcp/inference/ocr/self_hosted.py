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

from typing import Any
from typing_extensions import override

from ...providers import InferenceProvider
from ..shared.http_base import HTTPInferenceBase
from ..types import InferenceResult
from ..shared.http_result_parsers import parse_ocr_result
# ocr/self_hosted.py — 自建 HTTP OCR 服务客户端，复用 HTTPInferenceBase 通用逻辑
from .params import OCR_DEFAULT_PARAMS, OCR_RUNTIME_PARAMS


class OCRSelfHostedInference(HTTPInferenceBase):
    # 自建 OCR 推理：POST 至 {base_url}/ocr，无需 API key
class OCRSelfHostedInference(HTTPInferenceBase):
        # 指定服务根 URL 与 HTTP 超时（秒）
    def __init__(self, base_url: str, http_timeout: int = 600):
        super().__init__(
            base_url,
            http_timeout,
            api_key=None,
            provider=InferenceProvider.SELF_HOSTED,
        )

    @override
        # REST 路径后缀为 ocr
    def _get_endpoint(self) -> str:
        return "ocr"

    @override
        # 暴露 OCR runtime 白名单与默认参数
    def _get_params(self) -> tuple[set[str], dict[str, Any]]:
        return set(OCR_RUNTIME_PARAMS.keys()), OCR_DEFAULT_PARAMS.copy()

    @override
        # 从 JSON 响应提取 result 字段并调用 parse_ocr_result
    def _parse_result(self, response: dict[str, Any]) -> InferenceResult:
        result_data = response.get("result", response)
        return parse_ocr_result(result_data)
