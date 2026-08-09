"""教程 005（Annotated）：带默认值的必填 Query 与 min_length。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: Annotated[str, Query(min_length=3)] = "fixedquery"):
    """默认写在参数侧；Query 仅承载校验元数据。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
