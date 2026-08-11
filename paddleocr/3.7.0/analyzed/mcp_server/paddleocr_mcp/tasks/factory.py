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

# MCP 任务工厂：按 model 名称解析 tool 类型，从注册表实例化对应 Task
from typing import Dict

from ..inference.base import Inference
from ..selection import tool_for_model
from .base import Task


    # 任务注册表：tool 字符串 → Task 子类，create 时注入 Inference 后端
class TaskFactory:
    _registry: Dict[str, type[Task]] = {}

    @classmethod
        # 将 tool 标识（ocr / pp_structurev3 / paddleocr_vl）绑定到 Task 实现类
    def register(cls, tool: str, task_class: type[Task]) -> None:
        cls._registry[tool] = task_class

    @classmethod
        # 根据 model 查 tool_for_model，构造 Task 实例；未知 tool 抛出 ValueError
    def create(cls, model: str, inference: Inference) -> Task:
        tool = tool_for_model(model)
        if tool not in cls._registry:
            raise ValueError(
                f"Unknown tool for model {model!r}: {tool}. "
                f"Supported: {sorted(cls._registry.keys())}"
            )
        task_class = cls._registry[tool]
        return task_class(inference)

    @classmethod
        # 返回当前已注册的全部 tool 名称集合
    def list_supported(cls) -> set[str]:
        return set(cls._registry.keys())


from .ocr import OCRTask
from .doc_parsing import PPStructureV3Task, PaddleOCRVLTask

TaskFactory.register("ocr", OCRTask)
TaskFactory.register("pp_structurev3", PPStructureV3Task)
TaskFactory.register("paddleocr_vl", PaddleOCRVLTask)


    # 模块级便捷入口：委托 TaskFactory.create 创建任务
def create_task(model: str, inference: Inference) -> Task:
    return TaskFactory.create(model, inference)
