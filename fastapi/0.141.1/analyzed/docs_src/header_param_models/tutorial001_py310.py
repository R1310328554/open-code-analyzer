"""教程 001：Pydantic Header 模型（非 Annotated 默认参数写法）。"""

from fastapi import FastAPI, Header
from pydantic import BaseModel

app = FastAPI()


class CommonHeaders(BaseModel):
    """Header 字段模型；字段名对应 HTTP 头（连字符转下划线）。"""
    host: str  # Host 头
    save_data: bool  # Save-Data
    if_modified_since: str | None = None  # If-Modified-Since
    traceparent: str | None = None  # traceparent
    x_tag: list[str] = []  # 重复 X-Tag 头合并为列表


@app.get("/items/")
async def read_items(headers: CommonHeaders = Header()):
    """`= Header()` 声明整个模型来自请求 Header。"""
    return headers
