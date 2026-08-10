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
Executor Manager 应用配置与 FastAPI 生命周期。

启动时预热容器池，关闭时清理 Docker 容器。
"""

#
import os
from contextlib import asynccontextmanager

from fastapi import FastAPI
from util import format_timeout_duration, parse_timeout_duration

from core.container import init_containers, teardown_containers
from core.logger import logger

# 单次代码执行的全局超时（可被 docker timeout 使用）
TIMEOUT = parse_timeout_duration(os.getenv("SANDBOX_TIMEOUT", "10s"))


@asynccontextmanager
async def _lifespan(app: FastAPI):
    """应用启动/关闭时的异步生命周期钩子。"""
    # 每种语言各预热 size 个容器
    size = int(os.getenv("SANDBOX_EXECUTOR_MANAGER_POOL_SIZE", 1))

    success_count, total_task_count = await init_containers(size)
    logger.info(f"\n📊 Container pool initialization complete: {success_count}/{total_task_count} available")

    yield

    await teardown_containers()


def init():
    """返回 FastAPI lifespan 工厂，并记录全局超时配置。"""
    logger.info(f"Global timeout: {format_timeout_duration(TIMEOUT)}")
    return _lifespan
