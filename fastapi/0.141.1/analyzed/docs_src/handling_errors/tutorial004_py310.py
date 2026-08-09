"""教程 004：覆盖默认异常处理器——StarletteHTTPException 与 RequestValidationError 返回纯文本。"""

from fastapi import FastAPI, HTTPException
from fastapi.exceptions import RequestValidationError
from fastapi.responses import PlainTextResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

app = FastAPI()


@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request, exc):
    """HTTP 异常（含 HTTPException）统一返回 PlainTextResponse，内容为 detail 字符串。"""
    return PlainTextResponse(str(exc.detail), status_code=exc.status_code)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc: RequestValidationError):
    """请求体验证失败时汇总各字段 loc/msg，返回 400 纯文本。"""
    message = "Validation errors:"
    for error in exc.errors():
        message += f"\nField: {error['loc']}, Error: {error['msg']}"
    return PlainTextResponse(message, status_code=400)


@app.get("/items/{item_id}")
async def read_item(item_id: int):
    """item_id=3 时主动抛出 HTTPException(418)，由 http_exception_handler 处理。"""
    if item_id == 3:
        raise HTTPException(status_code=418, detail="Nope! I don't like 3.")
    return {"item_id": item_id}
