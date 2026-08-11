# Copyright (c) 2026 PaddlePaddle Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from typing import Any, Callable

from .base import Inference
from .ocr.aistudio import OCRAIStudioInference
from .ocr.local import OCRLocalInference
from .ocr.self_hosted import OCRSelfHostedInference
from .paddleocr_vl.aistudio import PaddleOCRVLAIStudioInference
from .paddleocr_vl.local import PaddleOCRVLLocalInference
from .paddleocr_vl.qianfan import PaddleOCRVLQianfanInference
from .paddleocr_vl.self_hosted import PaddleOCRVLSelfHostedInference
from .pp_structurev3.aistudio import PPStructureV3AIStudioInference
from .pp_structurev3.local import PPStructureV3LocalInference
from .pp_structurev3.qianfan import PPStructureV3QianfanInference
from .pp_structurev3.self_hosted import PPStructureV3SelfHostedInference
from ..selection import tool_for_model
# inference/factory.py — 推理后端工厂：按 (工具, provider) 注册并实例化 OCR/Structure/VL 适配器
from ..providers import InferenceProvider, normalize_provider


class InferenceFactory:
    # 推理工厂：维护 (tool, provider) → 构造函数的注册表，供 MCP 服务按 CLI 参数创建后端
class InferenceFactory:
    _registry: dict[tuple[str, InferenceProvider], Callable[..., Inference]] = {}

    @classmethod
    # register 将指定工具与 provider 的构造函数写入 _registry
    def register(
        cls,
        tool: str,
        provider: InferenceProvider | str,
        factory_fn: Callable[..., Inference],
    ) -> None:
        cls._registry[(tool, normalize_provider(provider))] = factory_fn

    @classmethod
    # create 根据 model 解析 tool 名，查找注册表并传入 kwargs 实例化 Inference
    def create(
        cls,
        model: str,
        provider: InferenceProvider | str,
        **kwargs,
    ) -> Inference:
        tool = tool_for_model(model)
        normalized_provider = normalize_provider(provider)
        key = (tool, normalized_provider)
        if key not in cls._registry:
            supported = [
                (tool, provider.value)
                for tool, provider in sorted(
                    cls._registry.keys(), key=lambda item: (item[0], item[1].value)
                )
            ]
            raise ValueError(
                f"Unsupported inference combination: model={model!r}, "
                f"provider={normalized_provider.value!r}. Supported combinations: "
                f"{supported}"
            )
        factory_fn = cls._registry[key]
        return factory_fn(model=model, **kwargs)

    @classmethod
    # list_supported 返回当前已注册的全部 (tool, provider) 组合
    def list_supported(cls) -> set[tuple[str, InferenceProvider]]:
        return set(cls._registry.keys())


# _create_ocr_local 构造本地 PaddleOCR 推理实例
def _create_ocr_local(model: str, **kwargs: Any) -> Inference:
    return OCRLocalInference(
        config=kwargs.get("config"),
        device=kwargs.get("device"),
        model=model,
    )


# _create_ocr_aistudio 构造 AI Studio 云端 OCR 客户端
def _create_ocr_aistudio(model: str, **kwargs: Any) -> Inference:
    return OCRAIStudioInference(
        token=kwargs["token"],
        base_url=kwargs.get("base_url"),
        request_timeout=kwargs.get("request_timeout", 120.0),
        poll_timeout=kwargs.get("poll_timeout", 600.0),
        model=model,
    )


# _create_ocr_self_hosted 构造指向自建 HTTP 服务的 OCR 客户端
def _create_ocr_self_hosted(model: str, **kwargs: Any) -> Inference:
    return OCRSelfHostedInference(
        base_url=kwargs["base_url"],
        http_timeout=kwargs.get("http_timeout", 600),
    )


# _create_pp_structurev3_local 构造本地 PP-StructureV3 文档解析 pipeline
def _create_pp_structurev3_local(model: str, **kwargs: Any) -> Inference:
    return PPStructureV3LocalInference(
        config=kwargs.get("config"),
        device=kwargs.get("device"),
    )


# _create_pp_structurev3_aistudio 构造 AI Studio PP-StructureV3 客户端
def _create_pp_structurev3_aistudio(model: str, **kwargs: Any) -> Inference:
    return PPStructureV3AIStudioInference(
        token=kwargs["token"],
        base_url=kwargs.get("base_url"),
        request_timeout=kwargs.get("request_timeout", 120.0),
        poll_timeout=kwargs.get("poll_timeout", 600.0),
        model=model,
    )


# _create_pp_structurev3_self_hosted 构造自建 PP-StructureV3 HTTP 客户端
def _create_pp_structurev3_self_hosted(model: str, **kwargs: Any) -> Inference:
    return PPStructureV3SelfHostedInference(
        base_url=kwargs["base_url"],
        http_timeout=kwargs.get("http_timeout", 600),
    )


# _create_paddleocr_vl_local 构造本地 PaddleOCR-VL 视觉语言文档解析
def _create_paddleocr_vl_local(model: str, **kwargs: Any) -> Inference:
    return PaddleOCRVLLocalInference(
        config=kwargs.get("config"),
        device=kwargs.get("device"),
        model=model,
    )


# _create_paddleocr_vl_aistudio 构造 AI Studio PaddleOCR-VL 客户端
def _create_paddleocr_vl_aistudio(model: str, **kwargs: Any) -> Inference:
    return PaddleOCRVLAIStudioInference(
        token=kwargs["token"],
        base_url=kwargs.get("base_url"),
        request_timeout=kwargs.get("request_timeout", 120.0),
        poll_timeout=kwargs.get("poll_timeout", 600.0),
        model=model,
    )


# _create_paddleocr_vl_self_hosted 构造自建 PaddleOCR-VL HTTP 客户端
def _create_paddleocr_vl_self_hosted(model: str, **kwargs: Any) -> Inference:
    return PaddleOCRVLSelfHostedInference(
        base_url=kwargs["base_url"],
        http_timeout=kwargs.get("http_timeout", 600),
    )


# _create_pp_structurev3_qianfan 构造千帆平台 PP-StructureV3 客户端
def _create_pp_structurev3_qianfan(model: str, **kwargs: Any) -> Inference:
    return PPStructureV3QianfanInference(
        base_url=kwargs["base_url"],
        api_key=kwargs["api_key"],
        http_timeout=kwargs.get("http_timeout", 600),
    )


# _create_paddleocr_vl_qianfan 构造千帆平台 PaddleOCR-VL 客户端
def _create_paddleocr_vl_qianfan(model: str, **kwargs: Any) -> Inference:
    return PaddleOCRVLQianfanInference(
        base_url=kwargs["base_url"],
        api_key=kwargs["api_key"],
        http_timeout=kwargs.get("http_timeout", 600),
    )


# 模块加载时注册 OCR / PP-StructureV3 / PaddleOCR-VL 各 provider 的工厂函数
InferenceFactory.register("ocr", InferenceProvider.LOCAL, _create_ocr_local)
InferenceFactory.register("ocr", InferenceProvider.AISTUDIO, _create_ocr_aistudio)
InferenceFactory.register("ocr", InferenceProvider.SELF_HOSTED, _create_ocr_self_hosted)

InferenceFactory.register(
    "pp_structurev3", InferenceProvider.LOCAL, _create_pp_structurev3_local
)
InferenceFactory.register(
    "pp_structurev3", InferenceProvider.AISTUDIO, _create_pp_structurev3_aistudio
)
InferenceFactory.register(
    "pp_structurev3", InferenceProvider.QIANFAN, _create_pp_structurev3_qianfan
)
InferenceFactory.register(
    "pp_structurev3", InferenceProvider.SELF_HOSTED, _create_pp_structurev3_self_hosted
)

InferenceFactory.register(
    "paddleocr_vl", InferenceProvider.LOCAL, _create_paddleocr_vl_local
)
InferenceFactory.register(
    "paddleocr_vl", InferenceProvider.AISTUDIO, _create_paddleocr_vl_aistudio
)
InferenceFactory.register(
    "paddleocr_vl", InferenceProvider.QIANFAN, _create_paddleocr_vl_qianfan
)
InferenceFactory.register(
    "paddleocr_vl", InferenceProvider.SELF_HOSTED, _create_paddleocr_vl_self_hosted
)


# create_inference 对外便捷入口，委托 InferenceFactory.create
def create_inference(
    model: str,
    provider: InferenceProvider | str,
    **kwargs,
) -> Inference:
    return InferenceFactory.create(model, provider, **kwargs)
