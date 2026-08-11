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

# 异步 HTTP 客户端：封装 httpx.AsyncClient，供 MCP 推理层向远端 OCR 服务 POST JSON
from typing import Any, Optional

import httpx


    # 轻量异步 HTTP 封装：管理连接生命周期、超时与 Bearer 鉴权头
class AsyncHTTPClient:
        # 初始化 base_url、读写超时上限与默认请求头
    def __init__(
        self,
        base_url: str,
        http_timeout: int = 600,
        headers: Optional[dict[str, str]] = None,
    ):
        self._base_url = base_url
        self._http_timeout = http_timeout
        self._headers = headers or {}
        self._client: Optional[httpx.AsyncClient] = None

        # 创建 httpx.AsyncClient，connect/read/write/pool 分别配置超时
    async def start(self) -> None:
        write_timeout = min(float(self._http_timeout), 120.0)
        timeout = httpx.Timeout(
            connect=30.0,
            read=float(self._http_timeout),
            write=write_timeout,
            pool=30.0,
        )
        self._client = httpx.AsyncClient(timeout=timeout)

        # 关闭底层客户端并释放连接池资源
    async def stop(self) -> None:
        if self._client:
            await self._client.aclose()
            self._client = None

        # 向 endpoint 发送 JSON POST，raise_for_status 后返回解析后的 dict
    async def post(
        self,
        endpoint: str,
        payload: dict[str, Any],
        headers: Optional[dict[str, str]] = None,
    ) -> dict[str, Any]:
        if not self._client:
            raise RuntimeError("HTTP client not started")

        url = f"{self._base_url.rstrip('/')}/{endpoint.lstrip('/')}"
        merged_headers = {**self._headers, **(headers or {})}

        response = await self._client.post(url, json=payload, headers=merged_headers)
        response.raise_for_status()
        return response.json()
