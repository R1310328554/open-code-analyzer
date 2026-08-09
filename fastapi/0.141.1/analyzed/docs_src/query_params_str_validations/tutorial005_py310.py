"""教程 005：Query 设置 default="fixedquery"，省略 q 时使用默认值并仍校验 min_length。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str = Query(default="fixedquery", min_length=3)):
    """q 非 Optional；未传参时默认 fixedquery（长度已满足 min_length=3）。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
