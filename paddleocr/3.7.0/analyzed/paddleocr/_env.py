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

# 包级环境开关：PADDLEOCR_DISABLE_AUTO_LOGGING_CONFIG=1 时跳过自动 logging 配置
import os

    # 读取环境变量，默认 0 表示允许 PaddleOCR 自动配置日志
DISABLE_AUTO_LOGGING_CONFIG = (
    os.getenv("PADDLEOCR_DISABLE_AUTO_LOGGING_CONFIG", "0") == "1"
)
