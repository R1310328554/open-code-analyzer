"""教程 003：Header(convert_underscores=False) 禁用下划线转换（非 Annotated 写法）。"""

from fastapi import FastAPI, Header
from pydantic import BaseModel

app = FastAPI()


class CommonHeaders(BaseModel):
    """Header 模型；convert_underscores=False 保持字段名与头名对应关系。"""
    host: str  # Host 头
    save_data: bool  # Save-Data
    if_modified_since: str | None = None  # If-Modified-Since
    traceparent: str | None = None  # traceparent
    x_tag: list[str] = []  # X-Tag 列表


@app.get("/items/")
async def read_items(headers: CommonHeaders = Header(convert_underscores=False)):
    """`= Header(convert_underscores=False)` 按原始头名解析整个模型。"""
    return headers
