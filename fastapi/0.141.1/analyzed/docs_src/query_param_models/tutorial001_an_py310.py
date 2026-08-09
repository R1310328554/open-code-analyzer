"""教程 001（Annotated）：Pydantic 模型 + Query() 一次声明多个查询参数。"""

from typing import Annotated, Literal

from fastapi import FastAPI, Query
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class FilterParams(BaseModel):
    """列表过滤查询参数：limit/offset/order_by/tags 均从 URL 查询字符串解析。"""
    limit: int = Field(100, gt=0, le=100)  # 1–100
    offset: int = Field(0, ge=0)  # 分页偏移 ≥0
    order_by: Literal["created_at", "updated_at"] = "created_at"  # 排序字段枚举
    tags: list[str] = []  # 重复 query key 解析为列表


@app.get("/items/")
async def read_items(filter_query: Annotated[FilterParams, Query()]):
    """Query() 将模型各字段映射为查询参数并校验后注入。"""
    return filter_query
