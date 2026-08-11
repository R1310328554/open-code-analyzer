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

"""各推理 provider 的输入适配器：将统一 MCP input_data 转为 local/API/HTTP 原生形式。"""
"""Provider-specific input adapters for the unified MCP input contract."""

from __future__ import annotations

import abc
import base64
import io
from contextlib import contextmanager
from typing import Any, Generator, Union

from .input_contract import (
    COMMON_INPUT_DESCRIPTION,
    InputKind,
    classify_input,
    decode_input_bytes,
    extract_base64_payload,
    infer_file_type_from_bytes,
    materialize_bytes_as_temp_file,
    normalize,
    resolve_absolute_path,
    validate_external_contract,
)
from ...providers import (
    InferenceProvider,
    get_provider_spec,
    is_http_provider,
    normalize_provider,
)


    # 输入适配器 ABC：normalize/validate 校验契约，prepare 上下文管理器产出 provider 形态
class InputAdapter(abc.ABC):
    @property
    @abc.abstractmethod
    def provider(self) -> InferenceProvider:
        pass

    @property
        # 生成工具描述：provider 显示名 + 通用 input_data 格式说明
    def description(self) -> str:
        spec = get_provider_spec(self.provider)
        return (
            f"Inference provider: {spec.provider.value} ({spec.display_name}). "
            f"{COMMON_INPUT_DESCRIPTION}"
        )

    def normalize(self, input_data: str) -> str:
        return normalize(input_data)

    def validate(self, input_data: str) -> None:
        validate_external_contract(input_data)

    @abc.abstractmethod
    @contextmanager
        # 子类实现：将 URL/路径/Base64 转为推理后端可消费的值
    def prepare(self, input_data: str) -> Generator[Any, None, None]:
        """Convert user input into the inference provider's native form."""
        raise NotImplementedError


    # 本地推理适配器：URL 直传、绝对路径、Base64 图片转 BGR numpy 或 PDF 临时文件
class LocalInputAdapter(InputAdapter):
    @property
    def provider(self) -> InferenceProvider:
        return InferenceProvider.LOCAL

    @contextmanager
    def prepare(self, input_data: str) -> Generator[Union[str, Any], None, None]:
        value = normalize(input_data)
        kind = classify_input(value)

        if kind is InputKind.URL:
            yield value
            return

        if kind is InputKind.ABSOLUTE_PATH:
            yield str(resolve_absolute_path(value))
            return

        data = decode_input_bytes(value, kind)
        file_type = infer_file_type_from_bytes(data)
        if file_type == "image":
            import numpy as np
            from PIL import Image as PILImage

            try:
                image_pil = PILImage.open(io.BytesIO(data))
                image_arr = np.array(image_pil.convert("RGB"))
                yield np.ascontiguousarray(image_arr[..., ::-1])
            except Exception as e:
                raise ValueError(f"Failed to decode Base64 image: {e}") from e
            return

        if file_type == "pdf":
            with materialize_bytes_as_temp_file(data, file_type="pdf") as temp_path:
                yield str(temp_path)
            return

        raise ValueError(
            "Unsupported Base64 content type for local inference. "
            "Only image and PDF inputs are supported."
        )


    # AI Studio 适配器：产出 {file_url} 或 {file_path} 字典供 SDK 调用
class AIStudioInputAdapter(InputAdapter):
    @property
    def provider(self) -> InferenceProvider:
        return InferenceProvider.AISTUDIO

    @contextmanager
    def prepare(self, input_data: str) -> Generator[dict[str, str], None, None]:
        value = normalize(input_data)
        kind = classify_input(value)

        if kind is InputKind.URL:
            yield {"file_url": value}
            return

        if kind is InputKind.ABSOLUTE_PATH:
            yield {"file_path": str(resolve_absolute_path(value))}
            return

        data = decode_input_bytes(value, kind)
        file_type = infer_file_type_from_bytes(data)
        if file_type not in ("image", "pdf"):
            raise ValueError(
                "Unsupported Base64 content type for AI Studio. "
                "Only image and PDF inputs are supported."
            )

        with materialize_bytes_as_temp_file(data, file_type=file_type) as temp_path:
            yield {"file_path": str(temp_path)}


    # HTTP 适配器：URL 直传、本地文件读字节后 Base64、或透传 Base64 payload
class HTTPInputAdapter(InputAdapter):
    def __init__(self, provider: InferenceProvider | str) -> None:
        normalized_provider = normalize_provider(provider)
        if not is_http_provider(normalized_provider):
            raise ValueError(
                f"HTTPInputAdapter requires an HTTP transport provider, "
                f"got {normalized_provider.value!r}."
            )
        self._provider = normalized_provider

    @property
    def provider(self) -> InferenceProvider:
        return self._provider

    @contextmanager
    def prepare(self, input_data: str) -> Generator[str, None, None]:
        yield self.prepare_http_file_field(input_data)

        # 专供 HTTP payload 的 file 字段：返回 URL 字符串或 Base64 编码内容
    def prepare_http_file_field(self, input_data: str) -> str:
        value = normalize(input_data)
        kind = classify_input(value)

        if kind is InputKind.URL:
            return value

        if kind is InputKind.ABSOLUTE_PATH:
            path = resolve_absolute_path(value)
            return base64.b64encode(path.read_bytes()).decode("ascii")

        payload = extract_base64_payload(value)
        decode_input_bytes(value, kind)
        return payload


# 各 provider 单例适配器，供工厂与 HTTP 基类直接引用
LOCAL_INPUT_ADAPTER = LocalInputAdapter()
AISTUDIO_INPUT_ADAPTER = AIStudioInputAdapter()
SELF_HOSTED_INPUT_ADAPTER = HTTPInputAdapter(InferenceProvider.SELF_HOSTED)
QIANFAN_INPUT_ADAPTER = HTTPInputAdapter(InferenceProvider.QIANFAN)
