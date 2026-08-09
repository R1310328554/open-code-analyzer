"""教程 004（Annotated）：Query 的 pattern 约束查询字符串格式。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: Annotated[
        str | None, Query(min_length=3, max_length=50, pattern="^fixedquery$")
    ] = None,
):
    """Annotated 形式集中声明长度与正则校验。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
