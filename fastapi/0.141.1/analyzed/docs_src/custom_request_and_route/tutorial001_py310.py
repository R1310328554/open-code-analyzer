"""教程 001：GzipRequest/GzipRoute 自动解压 gzip body（Body() 经典写法）。"""

import gzip
from collections.abc import Callable

from fastapi import Body, FastAPI, Request, Response
from fastapi.routing import APIRoute


class GzipRequest(Request):
    """扩展 Request：Content-Encoding 含 gzip 时先解压再返回 body。"""

    async def body(self) -> bytes:
        if not hasattr(self, "_body"):
            body = await super().body()
            if "gzip" in self.headers.getlist("Content-Encoding"):
                body = gzip.decompress(body)
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
app.router.route_class = GzipRoute


@app.post("/sum")
async def sum_numbers(numbers: list[int] = Body()):
    """接收整数列表 body 并返回求和。"""
    return {"sum": sum(numbers)}
