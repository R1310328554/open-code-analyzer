"""教程 001：HTTPException——资源不存在时返回 404 与 detail 消息。"""

from fastapi import FastAPI, HTTPException

app = FastAPI()

items = {"foo": "The Foo Wrestlers"}  # 模拟内存存储


@app.get("/items/{item_id}")
async def read_item(item_id: str):
    """按 ID 查询；不存在则抛出 HTTPException(404)。"""
    if item_id not in items:
        raise HTTPException(status_code=404, detail="Item not found")
    return {"item": items[item_id]}
