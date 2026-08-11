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

# HTTP 推理基类：千帆/自建 HTTP 等 provider 共用的 predict 流程与 payload 组装
from abc import abstractmethod
from typing import Any, Optional

import httpx

from ..base import Inference
from ..errors import InferenceError
from ..types import InferenceRequest, InferenceResult
from .async_http_client import AsyncHTTPClient
from .input_adapters import HTTPInputAdapter, InputAdapter
from .param_mapping import convert_params_to_camel
from ...providers import InferenceProvider


    # 抽象 HTTP 推理：start 预热 AsyncHTTPClient，predict 提交 JSON 并解析结果
class HTTPInferenceBase(Inference):
        # 保存 base_url、超时、API key，并绑定 HTTPInputAdapter
    def __init__(
        self,
        base_url: str,
        http_timeout: int = 600,
        api_key: Optional[str] = None,
        provider: InferenceProvider | str = InferenceProvider.SELF_HOSTED,
    ):
        self._base_url = base_url
        self._http_timeout = http_timeout
        self._api_key = api_key
        self._input_adapter = HTTPInputAdapter(provider)
        self._client: Optional[AsyncHTTPClient] = None

    @property
    def input_adapter(self) -> InputAdapter:
        return self._input_adapter

        # 若有 api_key 则注入 Authorization Bearer 头后启动 HTTP 客户端
    async def start(self) -> None:
        headers = {}
        if self._api_key:
            headers["Authorization"] = f"Bearer {self._api_key}"
        self._client = AsyncHTTPClient(self._base_url, self._http_timeout, headers)
        await self._client.start()

        # 关闭 HTTP 客户端并清空引用
    async def stop(self) -> None:
        if self._client:
            await self._client.stop()
            self._client = None

        # 组装 payload → POST endpoint → 将 httpx 异常映射为 InferenceError
    async def predict(self, request: InferenceRequest) -> InferenceResult:
        if not self._client:
            raise RuntimeError("Inference not started")

        payload = self._prepare_payload(
            request.input_data,
            request.file_type,
            **request.runtime_params,
        )

        endpoint = self._get_endpoint()
        provider = self._input_adapter.provider.value
        try:
            response = await self._client.post(endpoint, payload)
            return self._parse_result(response)
        except httpx.ReadTimeout as e:
            raise InferenceError(
                f"HTTP read timeout after {self._http_timeout}s "
                f"({provider}/{endpoint}): {e}"
            ) from e
        except httpx.HTTPStatusError as e:
            raise InferenceError(
                f"HTTP {e.response.status_code} ({provider}/{endpoint}): {e}"
            ) from e
        except httpx.HTTPError as e:
            raise InferenceError(f"HTTP error ({provider}/{endpoint}): {e}") from e
        except Exception as e:
            raise InferenceError(
                f"HTTP request failed ({provider}/{endpoint}): {e}"
            ) from e

        # 将 input_data 转为 file 字段，设置 fileType 并 snake→camel 转换 runtime 参数
    def _prepare_payload(
        self, input_data: str, file_type: Optional[str], **params
    ) -> dict[str, Any]:
        http_adapter = self._input_adapter
        payload: dict[str, Any] = {
            "file": http_adapter.prepare_http_file_field(input_data)
        }
        if file_type == "image":
            payload["fileType"] = 1
        elif file_type == "pdf":
            payload["fileType"] = 0

        payload.update(convert_params_to_camel({**params, "visualize": False}))
        return payload

    @abstractmethod
        # 子类实现：返回 OCR 或文档解析的具体 REST 路径
    def _get_endpoint(self) -> str:
        pass

    @abstractmethod
    def _get_params(self) -> tuple[set[str], dict[str, Any]]:
        pass

    @abstractmethod
    def _parse_result(self, response: dict[str, Any]) -> InferenceResult:
        pass

        # 暴露子类声明的可选 runtime 参数字段名
    def get_valid_params(self) -> set[str]:
        valid_params, _ = self._get_params()
        return valid_params

    def get_default_params(self) -> dict[str, Any]:
        _, defaults = self._get_params()
        return defaults
