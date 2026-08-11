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

# 本地同步推理桥接器：在后台线程执行阻塞 PaddleOCR 调用，通过 asyncio Future 回传结果
import asyncio
from queue import Queue
from threading import Thread
from typing import Any, Callable


    # 线程池式 runner：主事件循环投递任务，worker 线程同步执行 func 并 threadsafe 设置 Future
class LocalSyncRunner:
        # 绑定 inference 实例，启动 daemon worker 线程与任务队列
    def __init__(self, inference: Any) -> None:
        self._inference = inference
        self._queue: Queue = Queue()
        self._closed = False
        self._loop = asyncio.get_running_loop()
        self._thread = Thread(target=self._worker)
        self._thread.start()

    @property
    def inference(self) -> Any:
        return self._inference

        # 异步包装：将 func 调用入队并 await 对应 Future
    async def call(self, func: Callable, *args: Any, **kwargs: Any) -> Any:
        if self._closed:
            raise RuntimeError("Local sync runner has already been closed")
        fut = self._loop.create_future()
        self._queue.put((func, args, kwargs, fut))
        return await fut

        # 发送哨兵 None 终止 worker，join 线程并标记 closed
    async def close(self) -> None:
        if not self._closed:
            self._queue.put(None)
            await self._loop.run_in_executor(None, self._thread.join)
            self._closed = True

        # 后台循环：取队列任务、执行 func、成功 set_result 或异常 set_exception
    def _worker(self) -> None:
        while not self._closed:
            item = self._queue.get()
            if item is None:
                break
            func, args, kwargs, fut = item
            try:
                result = func(*args, **kwargs)
                self._loop.call_soon_threadsafe(fut.set_result, result)
            except Exception as e:
                self._loop.call_soon_threadsafe(fut.set_exception, e)
            finally:
                self._queue.task_done()
