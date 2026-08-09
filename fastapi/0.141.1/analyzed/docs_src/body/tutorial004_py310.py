"""教程 004：同时接收路径参数、请求体与可选查询参数 q。"""

from fastapi import FastAPI
from pydantic import BaseModel


class Item(BaseModel):
    """Item 请求体模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


app = FastAPI()


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item, q: str | None = None):
    """更新物品；若提供查询参数 q 则一并放入响应。"""
    result = {"item_id": item_id, **item.model_dump()}
    if q:
        result.update({"q": q})
    return result
