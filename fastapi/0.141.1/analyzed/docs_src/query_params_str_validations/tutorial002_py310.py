"""教程 002：Query(default=None, max_length=50) 为 q 添加最大长度校验。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(default=None, max_length=50)):
    """显式 Query() 声明查询参数校验；default=None 保持可选。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
