"""教程 011（Annotated）：Annotated[list[str] | None, Query()] 声明多值查询参数。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[list[str] | None, Query()] = None):
    """Query() 无额外约束时仅声明参数来源；多值行为与 tutorial011 一致。"""
    query_items = {"q": q}
    return query_items
