"""教程 002（Annotated）：自定义 APIRoute，在校验失败时附带原始请求体。"""

from collections.abc import Callable
from typing import Annotated

from fastapi import Body, FastAPI, HTTPException, Request, Response
from fastapi.exceptions import RequestValidationError
from fastapi.routing import APIRoute


class ValidationErrorLoggingRoute(APIRoute):
    """捕获 RequestValidationError 并将 body 写入 422 详情。"""
    def get_route_handler(self) -> Callable:
        original_route_handler = super().get_route_handler()  # 保留 FastAPI 默认处理链

        async def custom_route_handler(request: Request) -> Response:
            try:
                return await original_route_handler(request)
            except RequestValidationError as exc:  # Pydantic/参数校验失败
                body = await request.body()  # 读取原始 body 便于调试
                detail = {"errors": exc.errors(), "body": body.decode()}  # 合并错误与 body
                raise HTTPException(status_code=422, detail=detail)  # 仍返回 422

        return custom_route_handler


app = FastAPI()
# 全局替换路由类，使所有端点使用自定义校验错误处理
app.router.route_class = ValidationErrorLoggingRoute


@app.post("/")
async def sum_numbers(numbers: Annotated[list[int], Body()]):
    """接收 JSON 数组并求和；校验失败时响应含原始 body。"""
    return sum(numbers)
