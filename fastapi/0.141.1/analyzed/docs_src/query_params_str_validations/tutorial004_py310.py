"""教程 004：Query 增加 pattern 正则，q 须完全匹配 ^fixedquery$。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: str | None = Query(
        default=None, min_length=3, max_length=50, pattern="^fixedquery$"
    ),
):
    """pattern 与 min/max_length 同时生效；不匹配时返回 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
