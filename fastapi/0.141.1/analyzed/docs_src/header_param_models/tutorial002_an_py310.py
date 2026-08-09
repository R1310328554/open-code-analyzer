"""教程 002（Annotated）：Header 模型禁止额外字段（extra=forbid）。"""

from typing import Annotated

from fastapi import FastAPI, Header
from pydantic import BaseModel

app = FastAPI()


class CommonHeaders(BaseModel):
    """仅允许声明过的 Header；未知头将导致校验失败。"""
    model_config = {"extra": "forbid"}  # 拒绝模型未定义的 Header

    host: str  # Host 头
    save_data: bool  # Save-Data
    if_modified_since: str | None = None  # If-Modified-Since
    traceparent: str | None = None  # traceparent
    x_tag: list[str] = []  # X-Tag 列表


@app.get("/items/")
async def read_items(headers: Annotated[CommonHeaders, Header()]):
    """返回校验通过的 Header 模型；多余请求头会触发 422。"""
    return headers
