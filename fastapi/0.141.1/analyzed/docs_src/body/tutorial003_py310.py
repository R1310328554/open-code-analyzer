"""教程 003：PUT 路径参数与 Pydantic 请求体组合更新资源。"""

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
async def update_item(item_id: int, item: Item):
    """根据 item_id 更新物品，并将 path 与 body 字段合并返回。"""
    return {"item_id": item_id, **item.model_dump()}
