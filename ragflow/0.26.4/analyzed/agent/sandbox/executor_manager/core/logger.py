#
#  Copyright 2025 The InfiniFlow Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""
Executor Manager 统一日志配置。

使用名为 sandbox 的 logger，默认 INFO 级别。
"""

#
import logging

logging.basicConfig(level=logging.INFO)  # 全局基础日志级别
logger = logging.getLogger("sandbox")  # 沙箱服务专用 logger
