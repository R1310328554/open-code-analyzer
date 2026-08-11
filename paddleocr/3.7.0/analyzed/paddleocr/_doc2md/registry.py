# Copyright (c) 2025 PaddlePaddle Authors. All Rights Reserved.
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
import mimetypes
from pathlib import Path
from typing import Type

# doc2md 转换器注册表：扩展名/MIME 映射到 BaseConverter 子类
from .base import BaseConverter


    # 维护 ext→类 与 mime→类 双索引，支持装饰器 register 注册
class ConverterRegistry:
    """Registry mapping file extensions and MIME types to converter classes."""

    def __init__(self):
        self._ext_map: dict[str, Type[BaseConverter]] = {}
        self._mime_map: dict[str, Type[BaseConverter]] = {}

        # 注册转换器类并返回原类，便于 @registry.register 装饰
    def register(self, converter_cls: Type[BaseConverter]) -> Type[BaseConverter]:
        """Register a converter class; can be used as a decorator."""
        for ext in converter_cls.supported_extensions:
            self._ext_map[ext.lower().lstrip(".")] = converter_cls
        for mime in converter_cls.supported_mimetypes:
            self._mime_map[mime] = converter_cls
        return converter_cls

        # 按后缀或 mimetypes 猜测选取转换器实例
    def get_converter(self, file_path: Path) -> BaseConverter:
        """Return an appropriate converter instance for the given file path."""
        ext = file_path.suffix.lower().lstrip(".")
        if ext in self._ext_map:
            return self._ext_map[ext]()

        mime_type, _ = mimetypes.guess_type(str(file_path))
        if mime_type and mime_type in self._mime_map:
            return self._mime_map[mime_type]()

        supported = ", ".join(f".{e}" for e in sorted(self._ext_map.keys()))
        raise ValueError(f"Unsupported format: .{ext}\nSupported formats: {supported}")

        # 返回已注册扩展名排序列表
    def supported_extensions(self) -> list[str]:
        return sorted(self._ext_map.keys())


# 模块级默认注册表单例，各 converter 模块 import 时自动注册
# Global singleton registry
default_registry = ConverterRegistry()
