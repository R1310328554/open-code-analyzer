"""教程 005：RequestValidationError 返回 JSON——detail 与原始 body 一并序列化。"""

from fastapi import FastAPI, Request
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from pydantic import BaseModel

app = FastAPI()


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """422 JSON：包含 Pydantic 错误列表与无法解析的原始请求体。"""
    return JSONResponse(
        status_code=422,
        content=jsonable_encoder({"detail": exc.errors(), "body": exc.body}),
    )


class Item(BaseModel):
    """POST /items/ 的请求体模型。"""
    title: str
    size: int


@app.post("/items/")
async def create_item(item: Item):
    """校验通过则原样返回 Item；失败由 validation_exception_handler 处理。"""
    return item
