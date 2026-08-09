"""教程 002：extra=forbid 的 Query 模型（非 Annotated 写法）。"""

from typing import Literal

from fastapi import FastAPI, Query
from pydantic import BaseModel, Field

app = FastAPI()  # 创建 FastAPI 应用实例


class FilterParams(BaseModel):
    """过滤参数模型；extra=forbid 时未知 query key 会触发 422。"""
    model_config = {"extra": "forbid"}

    limit: int = Field(100, gt=0, le=100)  # 1–100
    offset: int = Field(0, ge=0)  # 分页偏移 ≥0
    order_by: Literal["created_at", "updated_at"] = "created_at"  # 排序字段枚举
    tags: list[str] = []  # 重复 query key 解析为列表


@app.get("/items/")
async def read_items(filter_query: FilterParams = Query()):
    """仅允许模型已声明字段；额外 query 参数会被 Pydantic 拒绝。"""
    return filter_query
