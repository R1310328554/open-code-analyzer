"""教程 013（Annotated）：Annotated[list, Query()] 声明可重复的多值查询参数。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[list, Query()] = []):
    """Annotated 写法；行为与 tutorial013 非 Annotated 版一致。"""
    query_items = {"q": q}
    return query_items
