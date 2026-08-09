"""教程 002：同一请求中接收多个 Pydantic body 模型（Item 与 User）。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """物品 body 模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


class User(BaseModel):
    """用户 body 模型。"""
    username: str
    full_name: str | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item, user: User):
    """FastAPI 将 JSON 中 item、user 两个键分别解析为对应模型。"""
    results = {"item_id": item_id, "item": item, "user": user}
    return results
