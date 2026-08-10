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
自管沙箱的 Docker 容器池管理。

维护 Python/Node.js 双队列、并发信号量，以及容器的创建、分配与回收。
"""

#
import asyncio
import contextlib
import os
from queue import Empty, Queue

from models.enums import SupportLanguage
from util import env_setting_enabled, is_valid_memory_limit
from utils.common import async_run_command

from core.logger import logger

# 各语言可用容器名队列；锁与信号量协调并发分配
_CONTAINER_QUEUES: dict[SupportLanguage, Queue] = {}
_CONTAINER_LOCK: asyncio.Lock = asyncio.Lock()
_CONTAINER_EXECUTION_SEMAPHORES: dict[SupportLanguage, asyncio.Semaphore] = {}


async def init_containers(size: int) -> tuple[int, int]:
    """
    预热容器池：每种语言创建 size 个 gVisor 沙箱容器。

    返回 (成功数, 总任务数)。
    """
    global _CONTAINER_QUEUES
    _CONTAINER_QUEUES = {SupportLanguage.PYTHON: Queue(), SupportLanguage.NODEJS: Queue()}

    async with _CONTAINER_LOCK:
        while not _CONTAINER_QUEUES[SupportLanguage.PYTHON].empty():
            _CONTAINER_QUEUES[SupportLanguage.PYTHON].get_nowait()
        while not _CONTAINER_QUEUES[SupportLanguage.NODEJS].empty():
            _CONTAINER_QUEUES[SupportLanguage.NODEJS].get_nowait()

    for language in SupportLanguage:
        _CONTAINER_EXECUTION_SEMAPHORES[language] = asyncio.Semaphore(size)

    create_tasks = []
    for i in range(size):
        name = f"sandbox_python_{i}"
        logger.info(f"🛠️ Creating Python container {i + 1}/{size}")
        create_tasks.append(_prepare_container(name, SupportLanguage.PYTHON))

        name = f"sandbox_nodejs_{i}"
        logger.info(f"🛠️ Creating Node.js container {i + 1}/{size}")
        create_tasks.append(_prepare_container(name, SupportLanguage.NODEJS))

    results = await asyncio.gather(*create_tasks, return_exceptions=True)
    success_count = sum(1 for r in results if r is True)
    total_task_count = len(create_tasks)
    return success_count, total_task_count


async def teardown_containers():
    """应用关闭时强制删除池中全部容器。"""
    async with _CONTAINER_LOCK:
        while not _CONTAINER_QUEUES[SupportLanguage.PYTHON].empty():
            name = _CONTAINER_QUEUES[SupportLanguage.PYTHON].get_nowait()
            await async_run_command("docker", "rm", "-f", name, timeout=5)
        while not _CONTAINER_QUEUES[SupportLanguage.NODEJS].empty():
            name = _CONTAINER_QUEUES[SupportLanguage.NODEJS].get_nowait()
            await async_run_command("docker", "rm", "-f", name, timeout=5)


async def _prepare_container(name: str, language: SupportLanguage) -> bool:
    """清理同名残留容器后创建并加入队列。"""
    with contextlib.suppress(Exception):
        await async_run_command("docker", "rm", "-f", name, timeout=5)

    if await create_container(name, language):
        _CONTAINER_QUEUES[language].put(name)
        return True
    return False


async def create_container(name: str, language: SupportLanguage) -> bool:
    """
    以只读根文件系统、gVisor runtime 与内存上限创建沙箱容器。
    """
    create_args = [
        "docker",
        "run",
        "-d",
        "--runtime=runsc",
        "--name",
        name,
        "--read-only",  # 根文件系统只读，工作目录使用 tmpfs
        "--tmpfs",
        "/workspace:rw,exec,size=100M,uid=65534,gid=65534",
        "--tmpfs",
        "/tmp:rw,exec,size=50M",
        "--user",
        "nobody",
        "--workdir",
        "/workspace",
    ]
    if os.getenv("SANDBOX_MAX_MEMORY"):
        memory_limit = os.getenv("SANDBOX_MAX_MEMORY") or "256m"
        if is_valid_memory_limit(memory_limit):
            logger.info(f"SANDBOX_MAX_MEMORY: {os.getenv('SANDBOX_MAX_MEMORY')}")
        else:
            logger.info("Invalid SANDBOX_MAX_MEMORY, using default value: 256m")
            memory_limit = "256m"
        create_args.extend(["--memory", memory_limit])
    else:
        logger.info("Set default SANDBOX_MAX_MEMORY: 256m")
        create_args.extend(["--memory", "256m"])

    # 可选挂载 seccomp 配置文件进一步限制系统调用
    if env_setting_enabled("SANDBOX_ENABLE_SECCOMP", "false"):
        logger.info(f"SANDBOX_ENABLE_SECCOMP: {os.getenv('SANDBOX_ENABLE_SECCOMP')}")
        create_args.extend(["--security-opt", "seccomp=/app/seccomp-profile-default.json"])

    if language == SupportLanguage.PYTHON:
        create_args.append(os.getenv("SANDBOX_BASE_PYTHON_IMAGE", "sandbox-base-python:latest"))
    elif language == SupportLanguage.NODEJS:
        create_args.append(os.getenv("SANDBOX_BASE_NODEJS_IMAGE", "sandbox-base-nodejs:latest"))

    logger.info(f"Sandbox config:\n\t {create_args}")

    try:
        return_code, _, stderr = await async_run_command(*create_args, timeout=10)
        if return_code != 0:
            logger.error(f"❌ Container creation failed {name}: {stderr}")
            return False

        # Node 镜像将 node_modules 复制到可写 workspace
        if language == SupportLanguage.NODEJS:
            copy_cmd = ["docker", "exec", name, "bash", "-c", "cp -a /app/node_modules /workspace/"]
            return_code, _, stderr = await async_run_command(*copy_cmd, timeout=10)
            if return_code != 0:
                logger.error(f"❌ Failed to prepare dependencies for {name}: {stderr}")
                return False

        return await container_is_running(name)
    except Exception as e:
        logger.error(f"❌ Container creation exception {name}: {str(e)}")
        return False


async def recreate_container(name: str, language: SupportLanguage) -> bool:
    """删除崩溃容器并按原配置重建。"""
    logger.info(f"🛠️ Recreating container: {name}")
    try:
        await async_run_command("docker", "rm", "-f", name, timeout=5)

        return await create_container(name, language)
    except Exception as e:
        logger.error(f"❌ Container {name} recreation failed: {str(e)}")
        return False


async def release_container(name: str, language: SupportLanguage):
    """执行完毕后归还容器；若已退出则尝试重建。"""
    async with _CONTAINER_LOCK:
        if await container_is_running(name):
            _CONTAINER_QUEUES[language].put(name)
            logger.info(f"🟢 Released container: {name} (remaining available: {_CONTAINER_QUEUES[language].qsize()})")
        else:
            logger.warning(f"⚠️ Container {name} has crashed, attempting to recreate...")
            if await recreate_container(name, language):
                _CONTAINER_QUEUES[language].put(name)
                logger.info(f"✅ Container {name} successfully recreated and returned to queue")


async def allocate_container_blocking(language: SupportLanguage, timeout=10) -> str:
    """
    在超时窗口内阻塞式分配可用容器；失败返回空字符串。
    """
    start_time = asyncio.get_running_loop().time()
    while asyncio.get_running_loop().time() - start_time < timeout:
        try:
            name = _CONTAINER_QUEUES[language].get_nowait()
            async with _CONTAINER_LOCK:
                if not await container_is_running(name) and not await recreate_container(name, language):
                    continue

                return name
        except Empty:
            await asyncio.sleep(0.1)

    return ""


async def container_is_running(name: str) -> bool:
    """通过 docker inspect 判断容器是否仍在运行。"""
    try:
        return_code, stdout, _ = await async_run_command("docker", "inspect", "-f", "{{.State.Running}}", name, timeout=2)
        return return_code == 0 and stdout.strip() == "true"
    except Exception:
        return False
