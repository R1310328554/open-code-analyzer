"""教程 003：body 模型中的 set[str] 去重标签集合。"""

from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()


class Item(BaseModel):
    """Item 模型，tags 为字符串集合（自动去重）。"""
    name: str
    description: str | None = None
    price: float
    tax: float | None = None
    tags: set[str] = set()  # JSON 数组解析为 set，重复项会被丢弃


@app.put("/items/{item_id}")
async def update_item(item_id: int, item: Item):
    """接收含 set 类型 tags 的 body 并返回。"""
    results = {"item_id": item_id, "item": item}
    return results
