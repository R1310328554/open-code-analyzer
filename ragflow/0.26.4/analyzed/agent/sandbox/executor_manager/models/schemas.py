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
代码执行 API 的 Pydantic 请求与响应模型。

定义 Base64 代码载荷、结构化返回值与产物附件字段。
"""

#
import base64
from typing import Any, Optional

from pydantic import BaseModel, Field, field_validator

from models.enums import ResourceLimitType, ResultStatus, RuntimeErrorType, SupportLanguage, UnauthorizedAccessType


class ArtifactItem(BaseModel):
    """用户代码写入 artifacts/ 目录的单文件产物。"""
    name: str
    mime_type: str
    size: int
    content_b64: str


class ExecutionStructuredResult(BaseModel):
    """main() 返回值经 runner 编码后的结构化信封。"""
    present: bool
    value: Any = None
    type: str = "json"


class CodeExecutionResult(BaseModel):
    """/run 接口统一响应体，含 stdout/stderr 与错误分类。"""
    status: ResultStatus
    stdout: str
    stderr: str
    exit_code: int
    detail: Optional[str] = None

    # 资源用量（毫秒/千字节）
    time_used_ms: Optional[float] = None
    memory_used_kb: Optional[float] = None

    # 失败时的细分错误类型
    resource_limit_type: Optional[ResourceLimitType] = None
    unauthorized_access_type: Optional[UnauthorizedAccessType] = None
    runtime_error_type: Optional[RuntimeErrorType] = None

    # 用户代码生成的附件（图片、PDF、CSV 等）
    artifacts: list[ArtifactItem] = []

    # main() 的结构化返回值
    result: Optional[ExecutionStructuredResult] = None


class CodeExecutionRequest(BaseModel):
    """客户端提交的代码执行请求。"""
    code_b64: str = Field(..., description="Base64 encoded code string")
    language: SupportLanguage = Field(default=SupportLanguage.PYTHON, description="Programming language")
    arguments: Optional[dict] = Field(default={}, description="Arguments")

    @field_validator("code_b64")
    @classmethod
    def validate_base64(cls, v: str) -> str:
        # 校验 code_b64 为合法 Base64 字符串
        try:
            base64.b64decode(v, validate=True)
            return v
        except Exception as e:
            raise ValueError(f"Invalid base64 encoding: {str(e)}")
