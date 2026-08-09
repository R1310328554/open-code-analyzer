"""教程 001（Annotated）：用 Pydantic 模型一次性声明多个 Header 字段。"""

from typing import Annotated

from fastapi import FastAPI, Header
from pydantic import BaseModel

app = FastAPI()


class CommonHeaders(BaseModel):
    """从请求 Header 解析出的字段集合；连字符名自动映射为下划线字段。"""
    host: str  # Host 头
    save_data: bool  # Save-Data 头（save-data -> save_data）
    if_modified_since: str | None = None  # If-Modified-Since
    traceparent: str | None = None  # traceparent（分布式追踪）
    x_tag: list[str] = []  # X-Tag，可重复出现，解析为列表


@app.get("/items/")
async def read_items(headers: Annotated[CommonHeaders, Header()]):
    """Header() 将模型各字段映射为 HTTP 头并注入。"""
    return headers
