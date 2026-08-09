"""教程 003：可选查询参数 q 用 Query 的 min_length/max_length 约束长度（3–50）。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(default=None, min_length=3, max_length=50)):
    """q 省略时为 None；提供时须满足长度约束，否则 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
