"""教程 003（Annotated）：Header(convert_underscores=False) 禁用下划线与连字符自动转换。"""

from typing import Annotated

from fastapi import FastAPI, Header
from pydantic import BaseModel

app = FastAPI()


class CommonHeaders(BaseModel):
    """Header 字段模型；convert_underscores=False 时字段名须与头名一致。"""
    host: str  # Host 头
    save_data: bool  # 须与 Save-Data 等标准头名按规则匹配
    if_modified_since: str | None = None  # If-Modified-Since
    traceparent: str | None = None  # traceparent
    x_tag: list[str] = []  # X-Tag 列表


@app.get("/items/")
async def read_items(
    headers: Annotated[CommonHeaders, Header(convert_underscores=False)],  # 不将字段名下划线转为连字符
):
    """按原始头名解析；适用于含下划线的自定义 Header 名。"""
    return headers
