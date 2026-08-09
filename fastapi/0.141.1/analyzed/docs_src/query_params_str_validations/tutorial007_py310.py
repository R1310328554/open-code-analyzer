"""教程 007：Query 的 title 与 min_length——为 OpenAPI 文档标注字段名并限制最短 3 字符。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: str | None = Query(default=None, title="Query string", min_length=3),
):
    """title 出现在 Swagger UI；min_length=3 时过短 q 返回 422。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
