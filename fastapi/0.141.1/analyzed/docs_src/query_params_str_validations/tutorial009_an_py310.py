"""教程 009（Annotated）：Annotated + Query(alias=...) 指定外部查询参数名。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str | None, Query(alias="item-query")] = None):
    """alias 仅影响请求解析与 OpenAPI 展示；Python 变量名保持 q。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
