"""教程 006：注册自定义异常处理器，记录日志后委托默认 handler 返回标准响应。"""

from fastapi import FastAPI, HTTPException
from fastapi.exception_handlers import (
    http_exception_handler,
    request_validation_exception_handler,
)
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException

app = FastAPI()  # 创建 FastAPI 应用实例


@app.exception_handler(StarletteHTTPException)
async def custom_http_exception_handler(request, exc):
    """捕获 HTTP 异常：打印调试信息后调用内置 http_exception_handler。"""
    print(f"OMG! An HTTP error!: {repr(exc)}")  # 自定义日志（保留英文示例输出）
    return await http_exception_handler(request, exc)  # 委托默认处理，保持标准 JSON 响应


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc):
    """捕获请求校验错误：打印无效数据后调用默认 validation handler。"""
    print(f"OMG! The client sent invalid data!: {exc}")  # 自定义日志
    return await request_validation_exception_handler(request, exc)  # 返回标准 422 响应


@app.get("/items/{item_id}")
async def read_item(item_id: int):
    """读取 item；item_id 为 3 时主动抛出 HTTPException 演示自定义 handler。"""
    if item_id == 3:
        raise HTTPException(status_code=418, detail="Nope! I don't like 3.")  # 418 触发自定义 HTTP handler
    return {"item_id": item_id}
