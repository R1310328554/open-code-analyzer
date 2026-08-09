"""教程 011：list[str] 查询参数——同一键可重复出现，FastAPI 解析为字符串列表。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: list[str] | None = Query(default=None)):
    """例如 ?q=a&q=b 得到 ["a","b"]；省略 q 时返回 {"q": null}。"""
    query_items = {"q": q}
    return query_items
