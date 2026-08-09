"""教程 006：无 default 的 Query 使 q 成为必填查询参数，min_length=3。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str = Query(min_length=3)):
    """缺少 q 时返回 422；提供的 q 须至少 3 个字符。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
