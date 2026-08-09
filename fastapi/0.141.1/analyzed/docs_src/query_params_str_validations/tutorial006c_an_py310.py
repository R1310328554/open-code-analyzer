"""教程 006c（Annotated）：可选 Union 类型 + Query min_length 组合示例。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str | None, Query(min_length=3)]):
    """Annotated 可选写法；行为与 tutorial006c 非 Annotated 版一致。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
