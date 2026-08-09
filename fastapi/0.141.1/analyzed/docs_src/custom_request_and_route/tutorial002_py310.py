"""教程 002：自定义 APIRoute 记录校验失败时的请求体（非 Annotated 写法）。"""

from collections.abc import Callable

from fastapi import Body, FastAPI, HTTPException, Request, Response
from fastapi.exceptions import RequestValidationError
from fastapi.routing import APIRoute


class ValidationErrorLoggingRoute(APIRoute):
    """包装默认 handler，422 时 detail 包含 errors 与 body。"""
    def get_route_handler(self) -> Callable:
        original_route_handler = super().get_route_handler()  # 委托给父类生成 handler

        async def custom_route_handler(request: Request) -> Response:
            try:
                return await original_route_handler(request)
            except RequestValidationError as exc:  # 请求体验证失败
                body = await request.body()  # 再次读取 body 字符串
                detail = {"errors": exc.errors(), "body": body.decode()}  # 便于排查错误输入
                raise HTTPException(status_code=422, detail=detail)  # 统一 422 响应格式

        return custom_route_handler


app = FastAPI()
# 在 app 级别设置 route_class
app.router.route_class = ValidationErrorLoggingRoute


@app.post("/")
async def sum_numbers(numbers: list[int] = Body()):
    """Body() 声明 JSON 数组；失败时由自定义 Route 附加 body。"""
    return sum(numbers)
