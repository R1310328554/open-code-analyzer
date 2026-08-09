"""教程 013：未参数化的 list + Query(default=[])——接收重复查询键组成列表。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: list = Query(default=[])):
    """?q=foo&q=bar 解析为 ["foo","bar"]；未传 q 时为空列表。"""
    query_items = {"q": q}
    return query_items
