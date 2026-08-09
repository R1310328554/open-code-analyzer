"""教程 001：可选字符串查询参数 q——后续示例将为其添加长度等校验。"""

from fastapi import FastAPI

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = None):
    """q 为可选查询参数；传入时合并进响应 JSON。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
