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
沙箱代码执行的 FastAPI 请求处理器。

负责 Base64 解码、Node.js 导出补丁、静态安全扫描与并发限流。
"""

#
import base64

from core.container import _CONTAINER_EXECUTION_SEMAPHORES
from core.logger import logger
from fastapi import Request
from models.enums import ResultStatus, SupportLanguage
from models.schemas import CodeExecutionRequest, CodeExecutionResult
from services.execution import execute_code
from services.limiter import limiter
from services.security import analyze_code_security


async def healthz_handler():
    """存活探针：返回固定 ok 状态。"""
    return {"status": "ok"}


@limiter.limit("5/second")
async def run_code_handler(req: CodeExecutionRequest, request: Request):
    """
    接收 Base64 编码代码，经安全分析后在容器池中执行。
    """
    logger.info("🟢 Received /run request")

    # 按语言共享信号量，避免同语言容器被过度并发占用
    async with _CONTAINER_EXECUTION_SEMAPHORES[req.language]:
        code = base64.b64decode(req.code_b64).decode("utf-8")
        # Node 用户代码需显式导出 main，便于 runner 加载
        if req.language == SupportLanguage.NODEJS:
            code += "\n\nmodule.exports = { main };"
            req.code_b64 = base64.b64encode(code.encode("utf-8")).decode("utf-8")
        # 静态规则扫描；不通过则直接返回 PROGRAM_RUNNER_ERROR
        is_safe, issues = analyze_code_security(code, language=req.language)
        if not is_safe:
            issue_details = "\n".join([f"Line {lineno}: {issue}" for issue, lineno in issues])
            return CodeExecutionResult(status=ResultStatus.PROGRAM_RUNNER_ERROR, stdout="", stderr=issue_details, exit_code=-999, detail="Code is unsafe")

        try:
            return await execute_code(req)
        except Exception as e:
            return CodeExecutionResult(status=ResultStatus.PROGRAM_RUNNER_ERROR, stdout="", stderr=str(e), exit_code=-999, detail="unhandled_exception")
