"""教程 001（Annotated）：自定义 GzipRequest/GzipRoute 自动解压 gzip 编码 body。"""

import gzip
from collections.abc import Callable
from typing import Annotated

from fastapi import Body, FastAPI, Request, Response
from fastapi.routing import APIRoute


class GzipRequest(Request):
    """扩展 Request：Content-Encoding 含 gzip 时先解压再返回 body。"""

    async def body(self) -> bytes:
        if not hasattr(self, "_body"):
            body = await super().body()
            if "gzip" in self.headers.getlist("Content-Encoding"):
                body = gzip.decompress(body)  # 解压后再交给路由处理
            self._body = body
        return self._body


class GzipRoute(APIRoute):
    """自定义路由类：将每个请求的 Request 替换为 GzipRequest。"""

    def get_route_handler(self) -> Callable:
        original_route_handler = super().get_route_handler()

        async def custom_route_handler(request: Request) -> Response:
            request = GzipRequest(request.scope, request.receive)
            return await original_route_handler(request)

        return custom_route_handler


app = FastAPI()
app.router.route_class = GzipRoute  # 全局启用 gzip 感知路由


@app.post("/sum")
async def sum_numbers(numbers: Annotated[list[int], Body()]):
    """接收整数列表 body 并返回求和；客户端可 gzip 压缩 payload。"""
    return {"sum": sum(numbers)}
