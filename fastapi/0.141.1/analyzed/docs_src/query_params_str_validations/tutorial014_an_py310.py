"""教程 014（Annotated）：Annotated + Query(include_in_schema=False) 隐藏文档中的查询参数。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    hidden_query: Annotated[str | None, Query(include_in_schema=False)] = None,
):
    """Annotated 形式集中声明 include_in_schema；运行时校验与路由行为不变。"""
    if hidden_query:
        return {"hidden_query": hidden_query}
    else:
        return {"hidden_query": "Not found"}
