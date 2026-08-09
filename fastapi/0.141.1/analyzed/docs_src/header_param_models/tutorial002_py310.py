"""教程 002：Header 模型 extra=forbid（非 Annotated 写法）。"""

from fastapi import FastAPI, Header
from pydantic import BaseModel

app = FastAPI()


class CommonHeaders(BaseModel):
    """声明允许的 Header 集合；额外请求头不被接受。"""
    model_config = {"extra": "forbid"}  # Pydantic 拒绝未声明字段

    host: str  # Host 头
    save_data: bool  # Save-Data
    if_modified_since: str | None = None  # If-Modified-Since
    traceparent: str | None = None  # traceparent
    x_tag: list[str] = []  # X-Tag 列表


@app.get("/items/")
async def read_items(headers: CommonHeaders = Header()):
    """解析 Header 为模型；未知头键会校验失败。"""
    return headers
