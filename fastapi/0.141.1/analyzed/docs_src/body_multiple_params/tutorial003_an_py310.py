"""教程 003（Annotated）：多 body 模型外加 Body() 声明的标量 importance。"""

from typing import Annotated

from fastapi import Body, FastAPI
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
async def update_item(
    item_id: int, item: Item, user: User, importance: Annotated[int, Body()]
):
    """importance 为单独 body 字段，与 item、user 模型键并列出现在 JSON 中。"""
    results = {"item_id": item_id, "item": item, "user": user, "importance": importance}
    return results
