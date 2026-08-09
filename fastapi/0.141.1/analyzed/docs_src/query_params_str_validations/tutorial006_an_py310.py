"""教程 006（Annotated）：Annotated[str, Query(...)] 声明必填 q 与长度下限。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str, Query(min_length=3)]):
    """无默认值 ⇒ 客户端必须显式传入 q。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
