"""教程 005（Annotated）：Body(embed=True) 将单模型嵌套在 JSON 键下。"""

from typing import Annotated

from fastapi import Body, FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 请求体模型。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Annotated[Item, Body(embed=True)]):
    """期望 body 形如 {"item": {...}}，而非顶层字段平铺。"""
    results = {"item_id": item_id, "item": item}
    return results
