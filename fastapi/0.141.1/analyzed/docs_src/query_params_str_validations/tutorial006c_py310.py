"""教程 006c：str | None 且无 default——q 可选，但若提供须满足 min_length=3。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(min_length=3)):
    """与 tutorial006 不同：省略 q 合法；传入空串或过短会 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
