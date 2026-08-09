"""教程 013：`Annotated[T, metadata]` 附加元数据，不影响运行时类型或校验行为。"""

from typing import Annotated


def say_hello(name: Annotated[str, "this is just metadata"]) -> str:
    """第二个参数仅为文档/工具元数据，运行时仍按 str 处理。"""
    return f"Hello {name}"
