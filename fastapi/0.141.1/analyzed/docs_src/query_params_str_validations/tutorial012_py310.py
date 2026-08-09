"""教程 012：Query 声明 list[str] 查询参数，default 为字符串列表默认值。"""

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(q: list[str] = Query(default=["foo", "bar"])):
    """省略 q 时返回默认 ["foo","bar"]；传 ?q=a&q=b 可覆盖为多个值。"""
    query_items = {"q": q}
    return query_items
