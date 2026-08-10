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
# distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

"""
沙箱 Provider 包：统一导出各后端实现与 ProviderManager。

本包包含：
- base.py：所有沙箱后端的抽象接口
- manager.py：全局单例 Provider 管理器
- self_managed.py：自托管实现（封装 executor_manager HTTP API）
- aliyun_codeinterpreter.py: Aliyun Code Interpreter provider implementation
  Official Documentation: https://help.aliyun.com/zh/functioncompute/fc/sandbox-sandbox-code-interepreter
- e2b.py: E2B provider implementation
- local.py: Local process provider implementation
- ssh.py: Remote SSH provider implementation
"""

from .base import SandboxProvider, SandboxInstance, ExecutionResult, SandboxProviderConfigError
from .manager import ProviderManager
from .self_managed import SelfManagedProvider
from .aliyun_codeinterpreter import AliyunCodeInterpreterProvider
from .e2b import E2BProvider
from .local import LocalProvider
from .ssh import SSHProvider

__all__ = [
    "SandboxProvider",
    "SandboxInstance",
    "ExecutionResult",
    "SandboxProviderConfigError",
    "ProviderManager",
    "SelfManagedProvider",
    "AliyunCodeInterpreterProvider",
    "E2BProvider",
    "LocalProvider",
    "SSHProvider",
]
