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

"""MCP 推理层异常层次：鉴权、服务不可用与超时等 typed 错误。"""
"""Inference-related exceptions."""


    # 推理错误基类，所有 MCP inference 异常均继承此类
class InferenceError(RuntimeError):
    """Base class for inference errors."""


    # 鉴权失败：token/API key 无效或过期
class AuthenticationError(InferenceError):
    """Authentication failed."""


    # 远端服务不可用或本地模型资源加载失败
class ResourceUnavailableError(InferenceError):
    """Service unavailable."""


    # 请求或轮询超时：超过 http_timeout / poll_timeout 配置
class ExecutionTimeoutError(InferenceError):
    """Request timeout."""
