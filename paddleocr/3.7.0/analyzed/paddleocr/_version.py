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

# 包版本号读取：从已安装 wheel 元数据解析 PaddleOCR 发行版本
import importlib.metadata

# 未安装时回退 0.0.0，供 __init__ 与 CLI --version 展示
try:
    version = importlib.metadata.version(__package__)
except importlib.metadata.PackageNotFoundError:
    version = "0.0.0"
