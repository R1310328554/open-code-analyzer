"""教程 009：Query(alias="item-query")——URL 查询键与 Python 参数名可不同。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: str | None = Query(default=None, alias="item-query")):
    """客户端须用 ?item-query= 传值；函数内仍通过 q 访问。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
